package org.nees.uiuc.simcor.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.TriggerConnectionsClient;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class TriggerTest  {
	private int clientIdx = 0;
	private List<TriggerConnectionsClient> clients = new ArrayList<TriggerConnectionsClient>();
	private TcpParameters cparams = new TcpParameters();
	private final Logger log = Logger.getLogger(TriggerTest.class);
	private TcpParameters lparams = new TcpParameters();
	private int number = 0;
	private StateActionsProcessorWithCc sap;
	private TransactionFactory tf;

	private BroadcastTransaction broadcast() {
		number++;
		BroadcastTransaction transaction;
		if (number % 2 == 0) {
			transaction = tf.createBroadcastTransaction(
					number, 0, 0, 5000);
			} else {
				transaction = tf.createBroadcastTransaction(
						number, 3, 12,"subtrigger","SAMPLE CONTENT", 5000);			
			}
		sap.assembleTriggerCommands(transaction,
				TransactionStateNames.BROADCAST_COMMAND, false);
		log.debug("Assemble Broadcast " + transaction);
		TransactionStateNames prevState = TransactionStateNames.READY;
		while (transaction.getState().equals(
				TransactionStateNames.BROADCAST_COMMAND)) {
			sap.broadcastCommands(transaction,
					TransactionStateNames.SETUP_TRIGGER_READ_RESPONSES);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			if (transaction.getState().equals(prevState) == false) {
				// log.debug("Commanding broadcast " + transaction);
				prevState = transaction.getState();
			}
		}
		log.debug("Broadcast done" + transaction);
		sap.setupTriggerResponses(transaction,
				TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES);
		log.debug("Setup Responses " + transaction);
		while (transaction.getState().equals(
				TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES)) {
			checkMsgs();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			sap.waitForTriggerResponse(transaction,
					TransactionStateNames.TRANSACTION_DONE);
			if (transaction.getState().equals(prevState) == false) {
				// log.debug("Collecting Responses " + transaction);
				prevState = transaction.getState();
			}
			log.debug("Collecting Responses done" + transaction);
		}
		return transaction;
	}

	private void checkClientList(int expected) {
		String clientsStr = "";
		int activeC = 0;
		for (TriggerConnectionsClient c : clients) {
			clientsStr += c.getClientId() + "\n";
			if (c.isDone() == false) {
				activeC++;
			}
		}
		log.debug("Client List:\n" + clientsStr + "\nend list");
		Assert.assertEquals(expected, activeC);
	}

	private void checkMsgs() {
		for (TriggerConnectionsClient c : clients) {
			c.checkForMessages();
		}
	}

	private void checkTransaction(BroadcastTransaction transaction,
			boolean bmsgExpected, boolean rmsgExpected, int expected) {
		if (bmsgExpected) {
			Assert.assertNotNull(transaction.getBroadcastMsg());
		} else {
			Assert.assertNull(transaction.getBroadcastMsg());
		}
		if (rmsgExpected) {
			Assert.assertNotNull(transaction.getResponseMsg());
		} else {
			Assert.assertNull(transaction.getResponseMsg());
		}
		Assert.assertEquals(expected, transaction.getResponses().size());
	}

	private void endClient() {
		for (TriggerConnectionsClient client : clients) {
			if (client.isDone() == false) {
				client.closeConnection();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
				return;
			}
		}
	}

	@Before
	public void setUp() throws Exception {
		cparams.setRemoteHost("127.0.0.1");
		cparams.setRemotePort(6445);
		cparams.setTcpTimeout(5000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(5000);
		ListenerStateMachine lsm = new ListenerStateMachine(
				new ClientConnections(), false, "MDL-00-00","BROADCASTER");
		sap = new StateActionsProcessorWithCc(lsm);
		sap.setParams(lparams);
		tf = sap.getTf();
		tf.setSystemDescription("Broadcaster");
		TransactionIdentity id = tf.createTransactionId(0, 0, 0);
		tf.setId(id);
		SimpleTransaction transaction = tf.createSendCommandTransaction(null,2000);
		log.debug("Start transaction: " + transaction);
		sap.startListener(transaction, TransactionStateNames.TRANSACTION_DONE);
	}

	private void startClient() {
		String sys = "Client " + clientIdx;
		TriggerConnectionsClient client = new TriggerConnectionsClient(cparams,
				sys);
		client.connect();
		clients.add(client);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		clientIdx++;
	}

	@After
	public void tearDown() throws Exception {
		SimpleTransaction transaction = tf.createSendCommandTransaction(null,2000);
		transaction.setState(TransactionStateNames.STOP_LISTENER);
		while (transaction.getState().equals(
				TransactionStateNames.STOP_LISTENER)) {
			sap.stopListener(transaction,
					TransactionStateNames.TRANSACTION_DONE);
			Thread.sleep(1000);
			log.debug("Still Stopping Listener");
		}
		log.debug("TEST DONE");
	}

	@Test
	public void test01OneTriggering() {
		startClient();
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		endClient();
		checkClientList(0);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 0);
	}

	@Test
	public void test02TwoTriggering() {
		startClient();
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		startClient();
		checkClientList(2);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 2);
		endClient();
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 1);
		endClient();
		checkClientList(0);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 0);
	}
	@Test
	public void test03BatchTriggering() {
		startClient();
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		startClient();
		startClient();
		startClient();
		checkClientList(4);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 4);
		endClient();
		endClient();
		endClient();
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 1);
		endClient();
		checkClientList(0);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 0);
	}
	@Test
	public void test04BatchWitCloseTriggering() {
		startClient();
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		startClient();
		startClient();
		startClient();
		checkClientList(4);
		transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 4);
		int count = 0;
		transaction = sap.getTf().createCloseTriggerTransaction(sap.getTf().getTransactionTimeout());
		sap.assembleTriggerCommands(transaction,
				TransactionStateNames.BROADCAST_CLOSE_COMMAND, true);
		log.debug("Assemble Close Broadcast " + transaction);
		TransactionStateNames prevState = TransactionStateNames.READY;
		while (transaction.getState().equals(
				TransactionStateNames.BROADCAST_CLOSE_COMMAND)) {
			sap.broadcastCommands(transaction,
					TransactionStateNames.CLOSE_TRIGGER_CONNECTIONS);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			if (transaction.getState().equals(prevState) == false) {
				 log.debug("Commanding broadcast " + transaction);
				prevState = transaction.getState();
			}
		}
		checkMsgs();
		log.debug("Broadcast done" + transaction);
		while(transaction.getState().equals(TransactionStateNames.CLOSE_TRIGGER_CONNECTIONS) && count < 50) {
			sap.closeTriggerConnections(transaction, TransactionStateNames.STOP_LISTENER);
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
			}
			log.debug("Closing connection " + transaction);
		}
		Assert.assertTrue("count is less than 50", count < 50);
		checkClientList(0);
	}
}
