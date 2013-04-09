package org.nees.uiuc.simcor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.ListenerConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.logging.ExitTransaction;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLsm;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.states.common.AssembleResponse;
import org.nees.uiuc.simcor.states.common.CheckOpenConnection;
import org.nees.uiuc.simcor.states.common.CloseConnection;
import org.nees.uiuc.simcor.states.common.CommandAvailable;
import org.nees.uiuc.simcor.states.common.OpenConnection;
import org.nees.uiuc.simcor.states.common.Ready;
import org.nees.uiuc.simcor.states.common.SendingCommand;
import org.nees.uiuc.simcor.states.common.SendingResponse;
import org.nees.uiuc.simcor.states.common.SetupReadMessage;
import org.nees.uiuc.simcor.states.common.StartListener;
import org.nees.uiuc.simcor.states.common.StopListener;
import org.nees.uiuc.simcor.states.common.TransactionDone;
import org.nees.uiuc.simcor.states.common.WaitForCommand;
import org.nees.uiuc.simcor.states.common.WaitForOpenCommand;
import org.nees.uiuc.simcor.states.common.WaitForOpenResponse;
import org.nees.uiuc.simcor.states.common.WaitForResponsePosting;
import org.nees.uiuc.simcor.states.p2p.AssembleCommand;
import org.nees.uiuc.simcor.states.p2p.CheckListenerOpenConnection;
import org.nees.uiuc.simcor.states.p2p.ResponseAvailable;
import org.nees.uiuc.simcor.states.p2p.ShutdownConnection;
import org.nees.uiuc.simcor.states.p2p.WaitForResponse;
import org.nees.uiuc.simcor.states.p2p.AssembleCommand.AssembleCommandType;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.TransactionIdentity.StepTypes;

public class UiSimCorTcp {

	public enum ConnectType {
		P2P_RECEIVE_COMMAND, P2P_SEND_COMMAND, TRIGGER_CLIENT
	}

	private ConnectType connectType;

	private Archiving archive;

	private final Logger log = Logger.getLogger(UiSimCorTcp.class);

	private Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();

	private StateActionsProcessor sap;

	private Transaction transaction;

	private boolean connectionActive = false;

	public UiSimCorTcp(String type, String mdl, String system) {
		initialize(ConnectType.valueOf(type), mdl, system);
	}

	public UiSimCorTcp(ConnectType type, String mdl, String system) {
		initialize(type, mdl, system);
	}

	private void execute() {
		TransactionState state = machine.get(transaction.getState());
		log.debug("Executing state: " + transaction.getState());
		if (state == null) {
			log.error("State not recognized " + transaction.getState());
		}
		state.execute(transaction);
	}

	public TransactionFactory getTf() {
		return sap.getTf();
	}

	public Archiving getArchive() {
		return archive;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	/**
	 * 
	 * @return - returns the current transaction as a reference
	 */
	public Transaction getTransaction() {
		return transaction;
	}

	/**
	 * Executes the next state of the state machine
	 * 
	 * @return the resulting state
	 */
	public TransactionStateNames isReady() {
		execute();
		log.debug("Current transaction: " + transaction);
		return transaction.getState();
	}

	public void setArchiveFilename(String filename) {
		archive.setFilename(filename);
		archive.setArchivingEnabled(true);
	}

	/**
	 * Set the current step or substep
	 * 
	 * @param s
	 * @param type
	 */
	public void setStep(int s, StepTypes type) {
		sap.getTf().setStep(s, type);
	}

	public void setTransaction(SimpleTransaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * For the receive command direction. This is called when a response is
	 * ready to be sent.
	 * 
	 * @param response
	 */
	public void continueTransaction(SimCorMsg response) {
		SimpleTransaction tr = (SimpleTransaction) transaction;
		tr.setResponse(response);
		tr.setPosted(true);
		tr.setState(TransactionStateNames.ASSEMBLE_RESPONSE);
		execute();

	}

	public void initialize(ConnectType type, String mdl, String system) {
		this.connectType = type;
		if (connectType.equals(ConnectType.P2P_RECEIVE_COMMAND)) {
			ListenerStateMachine lsm = new ListenerStateMachine(
					new ClientConnections(), true, mdl, system);
			this.sap = new StateActionsProcessorWithLsm(lsm);
		} else {
			this.sap = new StateActionsProcessor();
		}
		this.sap.setIdentity(mdl, system);

		if (connectType.equals(ConnectType.P2P_RECEIVE_COMMAND)) {
			machine
					.put(TransactionStateNames.ASSEMBLE_RESPONSE,
							new AssembleResponse(
									TransactionStateNames.ASSEMBLE_RESPONSE,
									sap, false));
			machine.put(TransactionStateNames.CHECK_LISTENER_OPEN_CONNECTION,
					new CheckListenerOpenConnection(sap));
			machine.put(TransactionStateNames.CLOSING_CONNECTION,
					new CloseConnection(sap));
			machine.put(TransactionStateNames.SHUTDOWN_CONNECTION,
					new ShutdownConnection(sap));
			machine.put(TransactionStateNames.COMMAND_AVAILABLE,
					new CommandAvailable(sap));
			machine.put(TransactionStateNames.READY, new Ready(sap));
			machine.put(TransactionStateNames.SENDING_RESPONSE,
					new SendingResponse(sap));
			machine.put(TransactionStateNames.SETUP_READ_COMMAND,
					new SetupReadMessage(
							TransactionStateNames.SETUP_READ_COMMAND, sap,
							true, TransactionStateNames.WAIT_FOR_COMMAND));
			machine
					.put(
							TransactionStateNames.START_LISTENER,
							new StartListener(
									sap,
									TransactionStateNames.CHECK_LISTENER_OPEN_CONNECTION));
			machine.put(TransactionStateNames.STOP_LISTENER, new StopListener(
					sap,TransactionStateNames.TRANSACTION_DONE));
			machine.put(TransactionStateNames.WAIT_FOR_COMMAND,
					new WaitForCommand(sap));
			machine.put(TransactionStateNames.WAIT_FOR_RESPONSE_POSTING,
					new WaitForResponsePosting(sap));
		}
		if (connectType.equals(ConnectType.P2P_SEND_COMMAND)) {
			machine.put(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND,
					new AssembleCommand(
							TransactionStateNames.ASSEMBLE_CLOSE_COMMAND, sap,
							AssembleCommandType.CLOSE));
			machine.put(TransactionStateNames.ASSEMBLE_COMMAND,
					new AssembleCommand(TransactionStateNames.ASSEMBLE_COMMAND,
							sap, AssembleCommandType.OTHER));
			machine.put(TransactionStateNames.ASSEMBLE_OPEN_COMMAND,
					new AssembleCommand(
							TransactionStateNames.ASSEMBLE_OPEN_COMMAND, sap,
							AssembleCommandType.OPEN));
			machine.put(TransactionStateNames.CHECK_OPEN_CONNECTION,
					new CheckOpenConnection(sap,
							TransactionStateNames.ASSEMBLE_OPEN_COMMAND));
			machine.put(TransactionStateNames.CLOSING_CONNECTION,
					new CloseConnection(sap));
			machine.put(TransactionStateNames.OPENING_CONNECTION,
					new OpenConnection(sap));
			machine.put(TransactionStateNames.RESPONSE_AVAILABLE,
					new ResponseAvailable(sap));
			machine.put(TransactionStateNames.READY, new Ready(sap));
			machine.put(TransactionStateNames.SENDING_COMMAND,
					new SendingCommand(sap,
							TransactionStateNames.SETUP_READ_RESPONSE));
			machine.put(TransactionStateNames.SENDING_CLOSE_COMMAND,
					new SendingCommand(sap,
							TransactionStateNames.CLOSING_CONNECTION));
			machine.put(TransactionStateNames.SETUP_READ_RESPONSE,
					new SetupReadMessage(
							TransactionStateNames.SETUP_READ_COMMAND, sap,
							false, TransactionStateNames.WAIT_FOR_RESPONSE));
			machine.put(TransactionStateNames.WAIT_FOR_OPEN_RESPONSE,
					new WaitForOpenResponse(sap));
			machine.put(TransactionStateNames.WAIT_FOR_RESPONSE,
					new WaitForResponse(sap));
		}
		if (connectType.equals(ConnectType.TRIGGER_CLIENT)) {
			machine
					.put(TransactionStateNames.ASSEMBLE_OPEN_RESPONSE,
							new AssembleResponse(
									TransactionStateNames.ASSEMBLE_RESPONSE,
									sap, true));
			machine
					.put(TransactionStateNames.ASSEMBLE_RESPONSE,
							new AssembleResponse(
									TransactionStateNames.ASSEMBLE_RESPONSE,
									sap, false));
			machine.put(TransactionStateNames.CHECK_OPEN_CONNECTION,
					new CheckOpenConnection(sap,
							TransactionStateNames.SETUP_READ_OPEN_COMMAND));
			machine.put(TransactionStateNames.CLOSING_CONNECTION,
					new CloseConnection(sap));
			machine.put(TransactionStateNames.COMMAND_AVAILABLE,
					new CommandAvailable(sap));
			machine.put(TransactionStateNames.OPENING_CONNECTION,
					new OpenConnection(sap));
			machine.put(TransactionStateNames.SENDING_RESPONSE,
					new SendingResponse(sap));
			machine.put(TransactionStateNames.SETUP_READ_OPEN_COMMAND,
					new SetupReadMessage(
							TransactionStateNames.SETUP_READ_OPEN_COMMAND, sap,
							true, TransactionStateNames.WAIT_FOR_OPEN_COMMAND));
			machine.put(TransactionStateNames.WAIT_FOR_OPEN_COMMAND,
					new WaitForOpenCommand(sap));
			machine.put(TransactionStateNames.SETUP_READ_COMMAND,
					new SetupReadMessage(
							TransactionStateNames.SETUP_READ_COMMAND, sap,
							true, TransactionStateNames.WAIT_FOR_COMMAND));
			machine.put(TransactionStateNames.WAIT_FOR_COMMAND,
					new WaitForCommand(sap));
			machine.put(TransactionStateNames.WAIT_FOR_RESPONSE_POSTING,
					new WaitForResponsePosting(sap));
		}
		machine.put(TransactionStateNames.READY, new Ready(sap));

		this.archive = sap.getArchive();
		transaction = sap.getTf().createSendCommandTransaction(null,
				sap.getTf().getTransactionTimeout());
		machine.put(TransactionStateNames.TRANSACTION_DONE,
				new TransactionDone(sap, TransactionStateNames.READY));
		transaction.setState(TransactionStateNames.READY);
		sap.getTf().setMdl(mdl);

	}

	/**
	 * 
	 * @return returns a copy of the transaction and tells the state machine
	 *         that the message has been offloaded
	 */
	public SimpleTransaction pickupTransaction() {
		((SimpleTransaction) transaction).setPickedUp(true);
		return new SimpleTransaction((SimpleTransaction) transaction);
	}

	public void shutdown() {
		if (connectionActive == false) {
			return;
		}

		transaction = getTf().createSendCommandTransaction(
				getTf().createSessionCommand(false),
				sap.getTf().getTransactionTimeout());
		if (connectType.equals(ConnectType.P2P_RECEIVE_COMMAND)) {
			transaction.setState(TransactionStateNames.SHUTDOWN_CONNECTION);
		}
		if (connectType.equals(ConnectType.P2P_SEND_COMMAND)) {
			transaction.setState(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND);
		}
		if (connectType.equals(ConnectType.TRIGGER_CLIENT)) {
			transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		}
		log.info("Closing connection");
		log.info("Shutting down network logger");
		archive.logTransaction(new ExitTransaction());
		connectionActive = false;
	}

	/**
	 * Starts a Receive command transaction.
	 */
	public void startTransaction(int msgTimeout) {
		transaction = getTf().createReceiveCommandTransaction(msgTimeout);
		transaction.setState(TransactionStateNames.SETUP_READ_COMMAND);
		transaction.setDirection(DirectionType.RECEIVE_COMMAND);
		execute();
	}

	/**
	 * Start a Transmit command transaction
	 * 
	 * @param command
	 *            - transaction containing the command. This should be created
	 *            with the transactionFactory.
	 */

	public void startTransaction(SimCorMsg msg, TransactionIdentity id,
			int msgTimeout) {

		transaction = sap.getTf().createSendCommandTransaction(msg, msgTimeout);
		transaction.setId(id);
		transaction.setState(TransactionStateNames.ASSEMBLE_COMMAND);
		transaction.setDirection(DirectionType.SEND_COMMAND);
		execute();
	}

	/**
	 * Sets up the connection used for transactions. For connections that
	 * connect to a remote host. This function will connected to the host. Use
	 * {@link Connection#isBusy()} to determine when the connection
	 * is ready. For connections that listen on a local port, This function will
	 * start the listener. However the listener will need to be monitored to get
	 * an incoming connection request.
	 * 
	 * @see {@link ListenerConnectionFactory#getConnection()}
	 */

	public void startup(TcpParameters params) {
		sap.setParams(params);
		TransactionFactory transactionFactory = sap.getTf();
		transaction = transactionFactory.createSendCommandTransaction(null,
				transactionFactory.getTransactionTimeout());
		if (connectType.equals(ConnectType.P2P_RECEIVE_COMMAND)) {
			transaction.setState(TransactionStateNames.START_LISTENER);
			transaction.setDirection(DirectionType.RECEIVE_COMMAND);
		} else {
			transaction.setState(TransactionStateNames.OPENING_CONNECTION);
			transaction.setDirection(DirectionType.SEND_COMMAND);
		}
		if (archive.isAlive() == false) {
			archive.start();
		}
		connectionActive = true;
	}

}