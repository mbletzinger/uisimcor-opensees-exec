package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.listener.ClientIdWithConnection;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class StateActionsProcessor {

	protected Archiving archive;
	protected ConnectionManager cm;
	protected final Logger log = Logger
			.getLogger(StateActionsProcessorWithLcf.class);
	protected TcpParameters params;
	private ClientIdWithConnection remoteClient;
	protected TransactionFactory tf;
	private TcpError savedError = new TcpError();

	public StateActionsProcessor() {
		super();
		tf = new TransactionFactory();
		cm = new ConnectionManager();
		archive = new Archiving();
	}

	public StateActionsProcessor(TransactionFactory tf, ConnectionManager cm,
			Archiving archive) {
		super();
		this.archive = archive;
		this.cm = cm;
		this.tf = tf;
	}

	public void assembleSessionMessage(SimpleTransaction transaction,
			boolean isOpen, boolean isCommand, TransactionStateNames next) {
		SimCorMsg cnt;
		if (isCommand) {
			cnt = tf.createSessionCommand(isOpen);
			transaction.setCommand(cnt);
		} else {
			cnt = tf.createSessionResponse(transaction.getCommand());
			transaction.setResponse(cnt);
		}
		log.debug("Assembled " + transaction);
		setUpWrite(transaction, isCommand, next);
	}

	public void checkOpenConnection(SimpleTransaction transaction,
			TransactionStateNames next) {
		TcpError er = new TcpError();
		Connection connection = null;
		connection = cm.getConnection();
		if (connection.isBusy()) {
			return;
		}
		er = cm.checkForErrors();
		saveError(er);
		setStatus(transaction, er, next);
		log.debug("Check Open Connection  " + transaction);
	}

	public void closingConnection(SimpleTransaction transaction,
			TransactionStateNames next) {
		boolean closed = cm.closeConnection();
		if (closed == false) {
			return;
		}
		TcpError er = cm.checkForErrors();
		if (er.getType().equals(TcpErrorTypes.NONE)) {
			er = getSavedError();
		}
		setStatus(transaction, er, next,next);
		log.debug("Closing Connection  " + transaction);
	}

	public Archiving getArchive() {
		return archive;
	}

	public ConnectionManager getCm() {
		return cm;
	}

	public TcpParameters getParams() {
		return params;
	}

	public ClientIdWithConnection getRemoteClient() {
		return remoteClient;
	}

	public TransactionFactory getTf() {
		return tf;
	}

	public void openConnection(SimpleTransaction transaction) {
		log.debug("Starting connection");
		cm.setParams(params);
		cm.openConnection();
		setStatus(transaction, new TcpError(),
				TransactionStateNames.CHECK_OPEN_CONNECTION);
		log.debug(" Open Connection  " + transaction);

	}

	public void recordTransaction(Transaction transaction,
			TransactionStateNames next) {
		if (archive.isArchivingEnabled()) {
			log.debug("Handling: " + transaction);
			archive.logTransaction(transaction);
		}
		if (next.equals(TransactionStateNames.READY)) {
			clearError();
		}
		setStatus(transaction, new TcpError(), next);
		log.debug("Record Transaction  " + transaction);

	}

	public void setArchive(Archiving archive) {
		this.archive = archive;
	}

	public void setCm(ConnectionManager cm) {
		this.cm = cm;
	}

	public void setIdentity(String mdl, String systemDescription) {
		tf.setMdl(mdl);
		tf.setSystemDescription(systemDescription);
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}

	protected void setStatus(Transaction transaction, TcpError error,
			TransactionStateNames state) {
		setStatus(transaction, error, state,
				TransactionStateNames.CLOSING_CONNECTION);
	}

	protected void setStatus(Transaction transaction, TcpError error,
			TransactionStateNames state, TransactionStateNames errstate) {
		transaction.setError(error);
		if (error.getType() != TcpErrorTypes.NONE) {
			transaction.setState(errstate);
		} else {
			transaction.setState(state);
		}
	}

	public void setTf(TransactionFactory tf) {
		this.tf = tf;
	}

	public void setUpRead(Transaction transaction, boolean isCommand,
			TransactionStateNames next) {
		log.debug("Set Up Read  " + transaction);
		Connection c = cm.getConnection();
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		c.setMsgTimeout(transaction.getTimeout());
		c.setToRemoteMsg(action);
		transaction.setState(next);

	}

	public void setUpWrite(SimpleTransaction transaction, boolean isCommand,
			TransactionStateNames next) {
		Connection connection = cm.getConnection();
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.WRITE);
		Msg2Tcp msg = action.getMsg();
		msg.setId(transaction.getId());
		if (isCommand) {
			msg.setMsg(transaction.getCommand());
		} else {
			msg.setMsg(transaction.getResponse());
		}
		connection.setToRemoteMsg(action);
		transaction.setPosted(false);
		transaction.setState(next);
		log.debug("Set Up Write  " + transaction);
	}

	public void waitForPickUp(SimpleTransaction transaction,
			TransactionStateNames next) {
		if (transaction.isPickedUp() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), next);
		transaction.setPickedUp(false);
		log.debug("Wait For Pickup " + transaction);

	}

	public void waitForPosted(SimpleTransaction transaction,
			TransactionStateNames next) {
		if (transaction.isPosted() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), next);
		transaction.setPosted(false);
		log.debug("Wait For Posted " + transaction);
	}

	public void waitForRead(SimpleTransaction transaction, boolean isCommand,
			TransactionStateNames next) {
		Connection connection = cm.getConnection();
		if (connection.isBusy()) {
			log.debug("Connection still busy");
			return;
		}
		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg msg = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Received msg:" + result);
		if (isCommand) {
			transaction.setCommand(msg);
		} else {
			transaction.setResponse(msg);
		}
		transaction.setId(id);
		saveError(result.getError());
		setStatus(transaction, result.getError(), next);
		transaction.setPickedUp(false);
		log.debug("Wait For Read" + transaction);
	}

	public void waitForSend(SimpleTransaction transaction,
			TransactionStateNames next) {
		Connection connection = cm.getConnection();
		// Check if command has been sent
		if (connection.isBusy()) {
			return;
		}
		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg msg = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Sent msg:" + msg + " id: " + id);
		saveError(result.getError());
		setStatus(transaction, result.getError(), next);
		log.debug("Wait For Send " + transaction);


	}

	public void waitForSessionMsgRead(SimpleTransaction transaction,
			boolean isCommand, TransactionStateNames next) {
		waitForRead(transaction, isCommand, next);
		if (transaction.getState().equals(next)) {
			log.debug("Open session transaction: " + transaction);
			Connection connection = cm.getConnection();
			String system;
			if(isCommand) {
				system = transaction.getCommand().getContent();
			} else {
				String rstrng = transaction.getResponse().getContent();
				int bidx = rstrng.indexOf("[");
				int eudx = rstrng.indexOf("]");
				if(bidx < 0 || eudx < 0) {
					log.error("system description not found in \"" + rstrng + "\"");
					system = "NOT RECOGNIZED";
				} else {
					system = rstrng.substring(bidx + 1, eudx);
				}
			}
			remoteClient = new ClientIdWithConnection(connection, system,
					connection.getRemoteHost());
			log.debug("Wait For Session Msg Read " + transaction);
		}
	}

	private TcpError getSavedError() {
		return savedError;
	}

	protected void saveError(TcpError error) {
		savedError = error;
		log.debug("Saved Error " + savedError);
	}

	private void clearError() {
		savedError = new TcpError();
	}

}