package org.nees.uiuc.simcor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.UiSimCorTcp.ConnectType;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.TransactionMsgs;
import org.nees.uiuc.simcor.test.util.TransactionResponder;
import org.nees.uiuc.simcor.test.util.TransactionWithTestFlags;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class TransactionTest  {
	TransactionMsgs data = new TransactionMsgs();
	private final Logger log = Logger.getLogger(TransactionTest.class);
	private TcpParameters params = new TcpParameters();
	private TransactionResponder responder;
	private UiSimCorTcp simcor;

	private void checkResponder() {
		if (responder.isAlive() == false) {
			fail();
		}
	}
//	private void responderIsDead() {
//		if (responder.isAlive()) {
//			fail();
//		}
//	}

	@Before
	public void setUp() throws Exception {
		responder = new TransactionResponder();
		data.setUp();
		responder.setData(data);
	}

	@After
	public void teardown()  throws Exception {
		responder.connected = false;
		responder.getSimcor().shutdown();
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			Thread.sleep(1000);
			state = simcor.isReady();
			log.debug("Shutdown state " + state);
		}
		while(responder.isAlive()) {
			Thread.sleep(1000);
			log.debug("Waiting for the responder to die");
		}
		log.debug("Everything is done");

	}

	@Test
	public void testSendTransactions() throws Exception {
		responder.start();
		checkResponder();
		// wait for the responder to start
		Thread.sleep(1000);
		params.setRemoteHost("127.0.0.1");
		params.setRemotePort(6445);
		params.setTcpTimeout(5000);
		String home = System.getProperty("user.dir");
		String fs = System.getProperty("file.separator");
		simcor = new UiSimCorTcp(ConnectType.P2P_SEND_COMMAND, "MDL-00-00","SENDER");
		simcor.setArchiveFilename(home + fs + "archive.txt");
		simcor.startup(params);
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.RESPONSE_AVAILABLE) == false) {
			Thread.sleep(200);
			state = simcor.isReady();
			log.debug("Connecting " + simcor.getTransaction());
			checkResponder();
		}
		log.debug("After open response available" +simcor.pickupTransaction());
		state = simcor.isReady();
		state = simcor.isReady();
		assertEquals(TcpErrorTypes.NONE, simcor.getTransaction().getError().getType());
		assertEquals(TransactionStateNames.READY, state);

//		TransactionFactory tf = simcor.getSap().getTf();
		checkResponder();
		for (SimpleTransaction transO : data.cmdList) {
			log.debug("Original command " + transO);
			TransactionIdentity id = transO.getId();
			simcor.startTransaction(transO.getCommand(),id,5000);
			state = simcor.isReady();
			while (state != TransactionStateNames.RESPONSE_AVAILABLE) {
				try {
					checkResponder();
					Thread.sleep(200);
					state = simcor.isReady();
				} catch (InterruptedException e) {
				}
				log.debug("Sending " + simcor.getTransaction());
			}
			log.debug("Pick up response state " + state);
			SimpleTransaction transaction = simcor.pickupTransaction();
			data.checkTransaction((TransactionWithTestFlags) transO, transaction, TcpErrorTypes.NONE);
			state = simcor.isReady();
			state = simcor.isReady();
		}
		simcor.shutdown();
		state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("Sleep interrupted");
			}
			state = simcor.isReady();
			log.debug("Disconnection" + simcor.getTransaction());
		}
		assertNull(simcor.getSap().getCm().getConnection());
	}
}
