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
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.states.broadcast.AssembleCloseTriggerCommands;
import org.nees.uiuc.simcor.states.broadcast.AssembleTriggerCommands;
import org.nees.uiuc.simcor.states.broadcast.BroadcastCommand;
import org.nees.uiuc.simcor.states.broadcast.CloseTriggerConnections;
import org.nees.uiuc.simcor.states.broadcast.DelayForCloseCommands;
import org.nees.uiuc.simcor.states.broadcast.SetupReadTriggerResponses;
import org.nees.uiuc.simcor.states.broadcast.WaitForTriggerResponses;
import org.nees.uiuc.simcor.states.common.Ready;
import org.nees.uiuc.simcor.states.common.StartListener;
import org.nees.uiuc.simcor.states.common.StopListener;
import org.nees.uiuc.simcor.states.common.TransactionDone;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.TransactionIdentity.StepTypes;

public class UiSimCorTriggerBroadcast {

	public enum ConnectType {
		P2P_RECEIVE_COMMAND, P2P_SEND_COMMAND, TRIGGER_CLIENT
	}

	private Archiving archive;

	private final Logger log = Logger.getLogger(UiSimCorTriggerBroadcast.class);

	private Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();

	private StateActionsProcessorWithCc sap;
	
	private ClientConnections cc;

	private BroadcastTransaction transaction;

	private boolean connectionActive = false;

	public UiSimCorTriggerBroadcast(String mdl, String system) {
		initialize(mdl, system);
	}

	private void execute() {
		TransactionState state = machine.get(transaction.getState());
		log.debug("Executing: " + transaction);
		if(state == null) {
			log.error("State not recognized " + transaction.getState() );
		}
		state.execute(transaction);
	}

	public Archiving getArchive() {
		return archive;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	public TransactionFactory getTf() {
		return sap.getTf();
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

	public void setTransaction(BroadcastTransaction transaction) {
		this.transaction = transaction;
	}

	public void initialize(String mdl, String system) {
		cc = new ClientConnections();
		ListenerStateMachine lsm = new ListenerStateMachine(cc, false, mdl, system);
		this.sap = new StateActionsProcessorWithCc(lsm);
		this.sap.setIdentity(mdl, system);
		this.archive = sap.getArchive();
		transaction = sap.getTf().createBroadcastTransaction(0, 0, 0, 1000);
		transaction.setState(TransactionStateNames.TRANSACTION_DONE);
		sap.getTf().setMdl(mdl);
		machine.put(TransactionStateNames.ASSEMBLE_CLOSE_TRIGGER_COMMANDS, new AssembleCloseTriggerCommands(sap));
		machine.put(TransactionStateNames.BROADCAST_CLOSE_COMMAND, new BroadcastCommand(sap, TransactionStateNames.DELAY_FOR_CLOSE_COMMANDS));
		machine.put(TransactionStateNames.DELAY_FOR_CLOSE_COMMANDS, new DelayForCloseCommands(sap,2000));		
		machine.put(TransactionStateNames.CLOSE_TRIGGER_CONNECTIONS, new CloseTriggerConnections(sap));
		machine.put(TransactionStateNames.STOP_LISTENER, new StopListener(sap,TransactionStateNames.ASSEMBLE_CLOSE_TRIGGER_COMMANDS));
		machine.put(TransactionStateNames.START_LISTENER, new StartListener(sap, TransactionStateNames.TRANSACTION_DONE));
		machine.put(TransactionStateNames.ASSEMBLE_TRIGGER_COMMANDS, new AssembleTriggerCommands(sap));
		machine.put(TransactionStateNames.BROADCAST_COMMAND, new BroadcastCommand(sap, TransactionStateNames.SETUP_TRIGGER_READ_RESPONSES));
		machine.put(TransactionStateNames.SETUP_TRIGGER_READ_RESPONSES, new SetupReadTriggerResponses(sap));
		machine.put(TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES, new WaitForTriggerResponses(sap));
		machine.put(TransactionStateNames.TRANSACTION_DONE, new TransactionDone(sap, TransactionStateNames.READY));
		machine.put(TransactionStateNames.READY, new Ready(sap));
	}

	public void shutdown() {
		if (connectionActive == false) {
			log.warn("Broadcast trigger is already shut down");
			return;
		}

		transaction = sap.getTf().createCloseTriggerTransaction(sap.getTf().getTransactionTimeout());
		transaction.setState(TransactionStateNames.STOP_LISTENER);
		log.info("Shutting down broadcast trigger");
		log.info("Shutting down network logger");
		archive.logTransaction(new ExitTransaction());
		connectionActive = false;
	}

	/**
	 * Start a Transmit command transaction
	 * 
	 * @param command
	 *            - transaction containing the command. This should be created
	 *            with the transactionFactory.
	 */

	public void startTransaction(BroadcastTransaction transaction) {
		this.transaction = transaction;
		this.transaction.setState(TransactionStateNames.ASSEMBLE_TRIGGER_COMMANDS);
		this.transaction.setDirection(DirectionType.SEND_COMMAND);
		this.transaction.setError(new TcpError());
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
		transaction = transactionFactory.createBroadcastTransaction(0, 0, 0, 1000);
			transaction.setState(TransactionStateNames.START_LISTENER);
			transaction.setDirection(DirectionType.SEND_COMMAND);
		if (archive.isAlive() == false) {
			archive.start();
		}
		connectionActive = true;
	}

}