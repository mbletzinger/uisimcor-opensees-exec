package org.nees.uiuc.simcor.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.TriggerBroadcastVamp;
import org.nees.uiuc.simcor.UiSimCorTriggerBroadcast;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.tcp.TcpListen;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.ReConnectingTriggerClient;
import org.nees.uiuc.simcor.test.util.TriggerClient;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class BroadcastTest  {
	private int clientIdx = 0;
	private List<TriggerClient> clients = new ArrayList<TriggerClient>();
	private TcpParameters cparams = new TcpParameters();
	private final Logger log = Logger.getLogger(BroadcastTest.class);
	private TcpParameters lparams = new TcpParameters();
	private int number = 0;

	private UiSimCorTriggerBroadcast simcor;

	private BroadcastTransaction broadcast() {
		number++;
		TransactionFactory tf = simcor.getTf();
		BroadcastTransaction transaction;
		if (number % 2 == 0) {
		transaction = tf.createBroadcastTransaction(
				number, 0, 0, 5000);
		} else {
			transaction = tf.createBroadcastTransaction(
					number, 3, 12,"subtrigger","SAMPLE CONTENT", 5000);			
		}
		log.debug("Assemble Broadcast " + transaction);
		simcor.startTransaction(transaction);
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.TRANSACTION_DONE) == false) {
			state = simcor.isReady();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			log.debug("Broadcasting " + simcor.getTransaction());
		}
		return transaction;
	}

	private void checkClientList(int expected) {
		String clientsStr = "";
		int activeC = 0;
		for (TriggerClient c : clients) {
			clientsStr += c.getClientId() + "\n";
			if (c.isDone() == false) {
				activeC++;
			}
		}
		log.debug("Client List:\n" + clientsStr + "\nend list");
		Assert.assertEquals(expected, activeC);
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
		for (TriggerClient client : clients) {
			if (client.isDone() == false) {
				client.setDone(true);
				try {
					Thread.sleep(2000);
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
		simcor = new UiSimCorTriggerBroadcast("MDL-00-00", "Broadcaster Test");
	}

	private void startSimCor() {
		simcor.startup(lparams);
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			state = simcor.isReady();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			log.debug("Start Listening " + simcor.getTransaction());
		}
	}
	private void startClient(boolean isReconnecting) {
		String sys = "Client " + clientIdx;
		TriggerClient client;
		if(isReconnecting) {
			client = new ReConnectingTriggerClient(cparams, sys);			
		} else {
			client = new TriggerClient(cparams, sys);
		}
		client.start();
		clients.add(client);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		clientIdx++;
	}

	@After
	public void tearDown() throws Exception {
		for (TriggerClient client : clients) {
			client.setDone(true);
			log.debug("Close " + client.getClientId()
					+ (client.isDone() ? " DONE" : " STILL RUNNING"));
		}

		boolean allDone = false;
		while (allDone == false) {
			allDone = true;
			for (TriggerClient client : clients) {
				if (client.isAlive()) {
					allDone = false;
					log.debug("Waithing for " + client.getClientId()
							+ " to die"
							+ (client.isDone() ? " DONE" : " STILL RUNNING"));
				}
				Thread.sleep(500);
			}
			simcor.shutdown();
			TransactionStateNames state = simcor.isReady();
			while (state.equals(TransactionStateNames.READY) == false) {
				state = simcor.isReady();
				Thread.sleep(500);
				log.debug("Still Stopping Listener " + simcor.getTransaction());
			}

		}
		log.debug("TEST DONE");
	}

	@Test
	public void test00NoClientTriggering() {
		startSimCor();
		checkClientList(0);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 0);
	}

	@Test
	public void test01OneTriggering() {
		startSimCor();
		startClient(false);
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		endClient();
		checkClientList(0);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 0);
	}

	@Test
	public void test02TwoTriggering() {
		startSimCor();
		startClient(false);
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		startClient(false);
		checkClientList(2);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 2);
		endClient();
		checkClientList(1);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 1);
		endClient();
		checkClientList(0);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 0);
	}

	@Test
	public void test03BatchWithCloseTriggering() {
		startSimCor();
		startClient(false);
		startClient(false);
		startClient(false);
		startClient(false);
		checkClientList(4);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 4);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 4);
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			state = simcor.isReady();
			log.debug("Closing down " + simcor.getTransaction());
		}
		checkClientList(0);
	}

	@Test
	public void test031BatchWithCloseReconnectTriggering() {
		startSimCor();
		startClient(false);
		startClient(true);
		checkClientList(2);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 2);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 2);
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			state = simcor.isReady();
			log.debug("Closing down " + simcor.getTransaction());
		}
		checkClientList(0);
	}
	@Test
	public void test04StartListenerFail() {
		TcpListen block = new TcpListen(lparams);
		Assert.assertTrue(block.startListening());
		simcor.startup(lparams);
		TransactionStateNames state = simcor.isReady();
		while(state.equals(TransactionStateNames.TRANSACTION_DONE) == false) {
			state = simcor.isReady();
			log.debug("Starttng up " + simcor.getTransaction());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		Transaction transaction = simcor.getTransaction();
		log.debug("Started " + transaction);
		Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction.getError().getType());
		simcor.shutdown();
		state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			state = simcor.isReady();
			log.debug("Closing down " + simcor.getTransaction());
		}
		Assert.assertTrue(block.stopListening());
	}
	@Test
	public void test05OneClientWithDeadClientClose() {
		startSimCor();
		startClient(false);
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		endClient();
		checkClientList(0);
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			state = simcor.isReady();
			log.debug("Closing down " + simcor.getTransaction());
		}
	}
	@Test
	public void test06BatchWithVamp() throws InterruptedException {
		startSimCor();
		startClient(false);
		startClient(false);
		checkClientList(2);
		TriggerBroadcastVamp vmp = new TriggerBroadcastVamp(simcor);
		BroadcastTransaction vmpT = simcor.getTf().createBroadcastTransaction(100, 10, 11,3000);
		vmp.startVamp(1, 3000,vmpT);
		TcpErrorTypes err = vmp.getError().getType();
		while(err.equals(TcpErrorTypes.NONE)) {
			Thread.sleep(100);
			err = vmp.getError().getType();
			log.debug("Waiting for added clients " + err);
		}
		Assert.assertEquals(TcpErrorTypes.BROADCAST_CLIENTS_ADDED, err);
		while(err.equals(TcpErrorTypes.BROADCAST_CLIENTS_ADDED)) {
			Thread.sleep(100);
			err = vmp.getError().getType();
			log.debug("Waiting for NONE message " + err);
		}
		Assert.assertEquals(TcpErrorTypes.NONE, err);
		log.debug("Killing a client");
		endClient();
		err = vmp.getError().getType();
		while(err.equals(TcpErrorTypes.NONE)) {
			Thread.sleep(100);
			err = vmp.getError().getType();
			log.debug("Waiting for lost clients " + err);
		}
		Assert.assertEquals(TcpErrorTypes.BROADCAST_CLIENTS_LOST, err);
		while(vmp.stopVamp() == false) {
			Thread.sleep(100);
			log.debug("Waiting for vamp to stop");
		}
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			state = simcor.isReady();
			log.debug("Closing down " + simcor.getTransaction());
		}
		checkClientList(0);
	}
}
