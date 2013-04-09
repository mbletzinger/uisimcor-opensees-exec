package org.nees.uiuc.simcor.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.states.common.AssembleResponse;
import org.nees.uiuc.simcor.states.common.CloseConnection;
import org.nees.uiuc.simcor.states.common.SendingCommand;
import org.nees.uiuc.simcor.states.common.SendingResponse;
import org.nees.uiuc.simcor.states.common.SetupReadMessage;
import org.nees.uiuc.simcor.states.common.TransactionDone;
import org.nees.uiuc.simcor.states.common.WaitForOpenCommand;
import org.nees.uiuc.simcor.states.common.WaitForOpenResponse;
import org.nees.uiuc.simcor.states.listener.ListenForConnections;
import org.nees.uiuc.simcor.states.p2p.AssembleCommand;
import org.nees.uiuc.simcor.states.p2p.AssembleCommand.AssembleCommandType;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class ListenerStateMachine extends Thread {
	private final ClientConnections cc;
	private TransactionStateNames currentState = TransactionStateNames.START_LISTENING;
	private TcpError error = new TcpError();
	private final boolean isP2P;
	private boolean isRunning;
	private boolean clientAvailable;
	public synchronized boolean isClientAvailable() {
		return clientAvailable;
	}

	public synchronized void setClientAvailable(boolean clientAvailable) {
		this.clientAvailable = clientAvailable;
	}

	private final Logger log = Logger.getLogger(ListenerStateMachine.class);
	protected Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();
	private ClientIdWithConnection oneClient = null;
	private StateActionsProcessorWithLcf sap = new StateActionsProcessorWithLcf();

	public ListenerStateMachine(ClientConnections cc, boolean isP2P, String mdl, String system) {
		super();
		this.cc = cc;
		this.isP2P = isP2P;
		setRunning(false);
		sap.setIdentity(mdl, system);
	}

	public synchronized ClientConnections getCc() {
		return cc;
	}

	public synchronized TransactionStateNames getCurrentState() {
		return currentState;
	}

	public synchronized TcpError getError() {
		return error;
	}

	public synchronized ClientIdWithConnection getOneClient() {
		return oneClient;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	public SimpleTransaction initialize() {
		if (isP2P) {
			machine.put(TransactionStateNames.LISTEN_FOR_CONNECTIONS,
					new ListenForConnections(sap,
							TransactionStateNames.SETUP_READ_COMMAND));
			machine.put(TransactionStateNames.SETUP_READ_COMMAND,
					new SetupReadMessage(
							TransactionStateNames.SETUP_READ_COMMAND, sap,
							true, TransactionStateNames.WAIT_FOR_OPEN_COMMAND));
			machine.put(TransactionStateNames.WAIT_FOR_OPEN_COMMAND,
					new WaitForOpenCommand(sap));
			machine.put(TransactionStateNames.ASSEMBLE_OPEN_RESPONSE,
					new AssembleResponse(
							TransactionStateNames.ASSEMBLE_OPEN_RESPONSE, sap,
							true));
			machine.put(TransactionStateNames.SENDING_RESPONSE,
					new SendingResponse(sap));
		} else {
			machine.put(TransactionStateNames.LISTEN_FOR_CONNECTIONS,
					new ListenForConnections(sap,
							TransactionStateNames.ASSEMBLE_OPEN_COMMAND));
			machine.put(TransactionStateNames.ASSEMBLE_OPEN_COMMAND,
					new AssembleCommand(
							TransactionStateNames.ASSEMBLE_OPEN_COMMAND, sap,
							AssembleCommandType.OPEN));
			machine.put(TransactionStateNames.SENDING_COMMAND,
					new SendingCommand(sap,
							TransactionStateNames.SETUP_READ_RESPONSE));
			machine
					.put(
							TransactionStateNames.SETUP_READ_RESPONSE,
							new SetupReadMessage(
									TransactionStateNames.SETUP_READ_RESPONSE,
									sap,
									false,
									TransactionStateNames.WAIT_FOR_OPEN_RESPONSE));
			machine.put(TransactionStateNames.WAIT_FOR_OPEN_RESPONSE,
					new WaitForOpenResponse(sap));
		}
		machine.put(TransactionStateNames.TRANSACTION_DONE,
				new TransactionDone(sap,
						TransactionStateNames.LISTEN_FOR_CONNECTIONS));
		machine.put(TransactionStateNames.CLOSING_CONNECTION,
				new CloseConnection(sap));
		SimpleTransaction transaction = sap.getTf()
				.createSendCommandTransaction(null,
						sap.getTf().getTransactionTimeout());
		sap.startListening(transaction);
		return transaction;
	}

	public synchronized boolean isRunning() {
		return isRunning;
	}

	public synchronized ClientIdWithConnection pickupOneClient() {
		ClientIdWithConnection result = oneClient;
		oneClient = null;
		setClientAvailable(false);
		return result;
	}

	@Override
	public void run() {
		SimpleTransaction transaction = initialize();
		log.debug("LSM initialized " + transaction);
		setError(transaction.getError());
		transaction.setState(TransactionStateNames.LISTEN_FOR_CONNECTIONS);
		setCurrentState(transaction.getState());
		if (transaction.getError().errorsExist()) {
			return;
		}
		setRunning(true);
		TransactionStateNames prevState = TransactionStateNames.READY;
		while (isRunning()) {
			if (transaction.getState().equals(prevState) == false) {
				log.debug("LSM state:" + transaction.getState() + " client "
						+ sap.getRemoteClient() + "  transaction "
						+ transaction);
				prevState = transaction.getState();
			}
			machine.get(getCurrentState()).execute(transaction);
			if (transaction.getState().equals(
					TransactionStateNames.TRANSACTION_DONE)) {
				updateClient(transaction);
				SimpleTransaction t = sap.getTf()
				.createSendCommandTransaction(null,
						sap.getTf().getTransactionTimeout());
				t.setState(transaction.getState());
				transaction = t;
			}
			setCurrentState(transaction.getState());
			try {
				sleep(100);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Stopped LSM state:" + transaction.getState() + " client "
				+ sap.getRemoteClient() + "  error " + transaction.getError());
		sap.stopListening(transaction);
		while ( transaction.getState().equals(TransactionStateNames.TRANSACTION_DONE) == false) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
			}
			sap.stopListening(transaction);
			log.info("Stopping LSM listener:" + transaction);
			}
		log.debug("LSM is done:" + transaction);
	}

	public synchronized void setCurrentState(TransactionStateNames currentState) {
		this.currentState = currentState;
	}

	public synchronized void setError(TcpError error) {
		this.error = error;
	}

	public synchronized void setOneClient(ClientIdWithConnection oneClient) {
		this.oneClient = oneClient;
	}

	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	private void updateClient(SimpleTransaction transaction) {
		setError(transaction.getError());
		ClientIdWithConnection client = sap.getRemoteClient();
		if(client == null) {
			log.error("Null client received");
		}
		if (isP2P) {
			oneClient = client;
		} else {
			cc.addClient(client);
		}
		setClientAvailable(true);
		log.debug("Added Client " + oneClient);
	}
}
