package org.nees.uiuc.simcor.test;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.listener.ClientId;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.StateActionsResponder;
import org.nees.uiuc.simcor.test.util.StateActionsResponder.DieBefore;

public class ListenerStateMachineTest {
	private final Logger log = Logger.getLogger(ListenerStateMachineTest.class);
	private TcpParameters lparams = new TcpParameters();
	private ListenerStateMachine lsm;
	private TcpParameters rparams = new TcpParameters();
	private StateActionsResponder rspdr;

	@Before
	public void setUp() throws Exception {
		lsm = new ListenerStateMachine(null, true,"MDL-00-00", "Connection Test");
		rparams.setRemoteHost("127.0.0.1");
		rparams.setRemotePort(6445);
		rparams.setTcpTimeout(1000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(1000);
		lsm.getSap().setParams(lparams);
	}

	private void setupConnection(DieBefore lfsp, boolean sendOpenSession) {
		
		lsm.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		rspdr = new StateActionsResponder(lfsp, rparams, sendOpenSession);
		rspdr.start();

	}

	@After
	public void tearDown() throws Exception {

		while (rspdr.isAlive()) {
			log.debug("Waiting for responder shutdown");
			Thread.sleep(1000);
		}
		lsm.setRunning(false);
		while (lsm.isAlive()) {
			log.debug("Waiting for listener shutdown");
			Thread.sleep(1000);
		}
	}


	@Test
	public void test01OpenSessionReadFail() {
		setupConnection(DieBefore.OPEN_COMMAND,true);
		TransactionStateNames state = null;
		TcpError error = lsm.getError();
		while(error.getType().equals(TcpErrorTypes.NONE)) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			error = lsm.getError();
			state = lsm.getCurrentState();
//			log.debug("LSM Current State: " + state);
		}
		error = lsm.getError();
		ClientId id = lsm.pickupOneClient();
		log.debug("Result state " + state + " error: " + error + " client: " + id);
		Assert.assertEquals(TcpErrorTypes.TIMEOUT, error.getType());
	}
	@Test
	public void test02OpenSessionSuceed() {
		setupConnection(DieBefore.END,true);
		TransactionStateNames state = null;
		TcpError error = lsm.getError();
		ClientId id = lsm.pickupOneClient();
		int count = 0;
		while(id == null && count < 100) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			error = lsm.getError();
			state = lsm.getCurrentState();
			id = lsm.pickupOneClient();
			count ++;
			log.debug("LSM Current State: " + state);
		}
		error = lsm.getError();
		id = lsm.pickupOneClient();
		log.debug("Result state " + state + " error: " + error + " client: " + id);
		Assert.assertEquals(TcpErrorTypes.NONE, error.getType());
		Assert.assertTrue(count < 100);
	}
}
