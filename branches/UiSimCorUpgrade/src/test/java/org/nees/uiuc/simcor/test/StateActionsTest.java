package org.nees.uiuc.simcor.test;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.StateActionsResponder;
import org.nees.uiuc.simcor.test.util.StateActionsResponder.DieBefore;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class StateActionsTest  {
	private final Logger log = Logger.getLogger(StateActionsTest.class);
	private TcpParameters lparams = new TcpParameters();
	private TcpParameters rparams = new TcpParameters();
	private StateActionsResponder rspdr;
	private StateActionsProcessorWithLcf sap;
	private SimpleTransaction transaction;

	private void read(boolean errorExpected,DieBefore msgType) {
		String cmdStr= "command";
		TransactionStateNames wastate= TransactionStateNames.WAIT_FOR_OPEN_COMMAND;
		TransactionStateNames setstate = TransactionStateNames.SETUP_READ_OPEN_COMMAND;
		TransactionStateNames next = TransactionStateNames.COMMAND_AVAILABLE;
		boolean isCommand = true;
		
		if(msgType.equals(DieBefore.OPEN_RESPONSE)){
			cmdStr = "response";
			setstate = TransactionStateNames.SETUP_READ_RESPONSE;
			wastate = TransactionStateNames.WAIT_FOR_RESPONSE;
			next = TransactionStateNames.RESPONSE_AVAILABLE;
			isCommand = false;
		}
		if(msgType.equals(DieBefore.PARAM_MSG_COMMAND)){
			cmdStr = "command";
			setstate = TransactionStateNames.SETUP_READ_COMMAND;
			wastate = TransactionStateNames.WAIT_FOR_COMMAND;
			next = TransactionStateNames.COMMAND_AVAILABLE;
			isCommand = true;
		}
		if(msgType.equals(DieBefore.PARAM_MSG_RESPONSE)) {
			cmdStr = "response";
			setstate = TransactionStateNames.SETUP_READ_RESPONSE;
			wastate = TransactionStateNames.WAIT_FOR_RESPONSE;
			next = TransactionStateNames.RESPONSE_AVAILABLE;
			isCommand = false;
		}
		
		transaction.setState(setstate);
		sap.setUpRead(transaction, false, wastate);
		while (transaction.getState().equals(wastate)) {
			transaction.setState(wastate);
			sap.waitForRead(transaction, isCommand, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			log.debug("Local Transaction during read " + cmdStr + " message: "
					+ transaction);
		}
		log.debug("Local Transaction after read " + cmdStr + " message: "
				+ transaction);
		log.debug("Remote Transaction: " + rspdr.getTransaction());
		if (errorExpected) {
			org.junit.Assert.assertEquals(
					TransactionStateNames.CLOSING_CONNECTION, transaction
							.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.TIMEOUT, transaction
					.getError().getType());
		} else {
			org.junit.Assert.assertEquals(next, transaction.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
					.getError().getType());
		}
	}

	private void feebleRead() {
		String cmdStr= "command";
		TransactionStateNames wastate= TransactionStateNames.WAIT_FOR_OPEN_COMMAND;
		TransactionStateNames setstate = TransactionStateNames.SETUP_READ_OPEN_COMMAND;
		TransactionStateNames next = TransactionStateNames.COMMAND_AVAILABLE;
		boolean isCommand = true;
				
		transaction.setState(setstate);
		sap.setUpRead(transaction, false, wastate);
		for (int i = 0; i < 2; i++) {
			transaction.setState(wastate);
			sap.waitForRead(transaction, isCommand, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			log.debug("Local Transaction during read " + cmdStr + " message: "
					+ transaction);
		}
	}

	@Before
	public void setUp() throws Exception {
		sap = new StateActionsProcessorWithLcf();
		rparams.setRemoteHost("127.0.0.1");
		rparams.setRemotePort(6445);
		rparams.setTcpTimeout(2000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(2000);
		sap.setParams(lparams);
		sap.setIdentity("MDL-00-00", "Connection Test");

		transaction = sap.getTf().createSendCommandTransaction(new SimCorMsg(),
				2000);
	}

	private void setupConnection(DieBefore lfsp, boolean sendOpenSession) {
		transaction.setState(TransactionStateNames.START_LISTENER);
		sap.startListening(transaction);

		rspdr = new StateActionsResponder(lfsp, rparams, sendOpenSession);
		rspdr.start();
		transaction.setState(TransactionStateNames.LISTEN_FOR_CONNECTIONS);

		sap.listenForConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	private void shutdown(boolean errorExpected) {
		transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		sap.closingConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction during close connection: " + transaction);
		while (transaction.getState().equals(
				TransactionStateNames.CLOSING_CONNECTION)) {
			sap.closingConnection(transaction,
					TransactionStateNames.TRANSACTION_DONE);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			log.debug("Local Transaction during close connection: "
					+ transaction);
		}
		log.debug("Local Transaction after close connection: " + transaction);
		if (errorExpected) {
			org.junit.Assert.assertEquals(
					TransactionStateNames.TRANSACTION_DONE, transaction
							.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.TIMEOUT, transaction
					.getError().getType());
		} else {
			org.junit.Assert.assertEquals(
					TransactionStateNames.TRANSACTION_DONE, transaction
							.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
					.getError().getType());
		}
		transaction.setState(TransactionStateNames.TRANSACTION_DONE);
		sap.recordTransaction(transaction, TransactionStateNames.READY);
		org.junit.Assert.assertEquals(TransactionStateNames.READY, transaction
				.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());

		transaction.setState(TransactionStateNames.STOP_LISTENER);
		sap.stopListening(transaction);
		log.debug("Local Transaction after stop listener: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	@After
	public void tearDown() throws Exception {

		while (rspdr.isAlive()) {
			log.debug("Waiting for responder shutdown");
			Thread.sleep(1000);
		}
		sap.stopListening(transaction);
	}

	@Test
	public void test01StartListenerFail() {
		transaction.setState(TransactionStateNames.START_LISTENER);
		sap.startListening(transaction);
		rspdr = new StateActionsResponder(DieBefore.OPEN_COMMAND, rparams, true); // never
		// started
		transaction.setState(TransactionStateNames.LISTEN_FOR_CONNECTIONS);
		sap.listenForConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(
				TransactionStateNames.LISTEN_FOR_CONNECTIONS, transaction
						.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
		transaction.setState(TransactionStateNames.STOP_LISTENER);
		sap.stopListening(transaction);
		while (transaction.getState().equals(
				TransactionStateNames.STOP_LISTENER)) {
			sap.stopListening(transaction);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after stop listening: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	@Test
	public void test02OpenSessionReadFail() {
		setupConnection(DieBefore.OPEN_COMMAND, true);
		read(true, DieBefore.OPEN_COMMAND);
		shutdown(true);
	}

	@Test
	public void test03OpenSessionWriteFail() {
		setupConnection(DieBefore.OPEN_RESPONSE, false);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, DieBefore.OPEN_COMMAND);
		read(true, DieBefore.OPEN_RESPONSE);
		shutdown(true);
	}

	@Test
	public void test04ParamWriteFail() {
		setupConnection(DieBefore.PARAM_MSG_RESPONSE, false);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, DieBefore.OPEN_COMMAND);
		read(false, DieBefore.OPEN_RESPONSE);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, DieBefore.PARAM_MSG_COMMAND);
		read(true, DieBefore.PARAM_MSG_RESPONSE);
		shutdown(true);
	}

	@Test
	public void test05ParamReadFail() {
		setupConnection(DieBefore.PARAM_MSG_COMMAND, true);
		read(false, DieBefore.OPEN_COMMAND);
		write(TransactionStateNames.TRANSACTION_DONE, DieBefore.OPEN_RESPONSE);
		read(false, DieBefore.PARAM_MSG_COMMAND);
		write(TransactionStateNames.TRANSACTION_DONE, DieBefore.PARAM_MSG_RESPONSE);
		read(true, DieBefore.CLOSE_COMMAND);
		shutdown(true);
	}

	@Test
	public void test06CloseSessionReadFail() {
		setupConnection(DieBefore.CLOSE_COMMAND, true);
		read(false, DieBefore.OPEN_COMMAND);
		write(TransactionStateNames.TRANSACTION_DONE, DieBefore.OPEN_RESPONSE);
		read(false, DieBefore.PARAM_MSG_COMMAND);
		write(TransactionStateNames.TRANSACTION_DONE, DieBefore.PARAM_MSG_RESPONSE);
		read(true, DieBefore.CLOSE_COMMAND);
		shutdown(true);
	}

	@Test
	public void test07CloseSessionWritePass() {
		setupConnection(DieBefore.END, false);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, DieBefore.OPEN_COMMAND);
		read(false, DieBefore.OPEN_RESPONSE);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, DieBefore.PARAM_MSG_COMMAND);
		read(false, DieBefore.PARAM_MSG_RESPONSE);
		write(TransactionStateNames.TRANSACTION_DONE, DieBefore.CLOSE_COMMAND);
		shutdown(false);
	}

	@Test
	public void test09CloseSessionReadPass() {
		setupConnection(DieBefore.END, true);
		read(false, DieBefore.OPEN_COMMAND);
		write(TransactionStateNames.TRANSACTION_DONE,DieBefore.OPEN_RESPONSE);
		read(false, DieBefore.PARAM_MSG_COMMAND);
		write(TransactionStateNames.TRANSACTION_DONE, DieBefore.PARAM_MSG_RESPONSE);
		read(false, DieBefore.CLOSE_COMMAND);
		shutdown(false);
	}

	@Test
	public void test10OpenSessionReadFailWithAbort() {
		setupConnection(DieBefore.OPEN_COMMAND, true);
		feebleRead();
		shutdown(false);
	}

	private void write(TransactionStateNames next, DieBefore msgType) {
		TransactionStateNames curstate = TransactionStateNames.ASSEMBLE_OPEN_RESPONSE;
		TransactionStateNames wastate = TransactionStateNames.WAIT_FOR_OPEN_RESPONSE;
		String openStr = "open";
		String cmdStr = "response";
		boolean isOpen = true;
		boolean isCommand = false;
		SimCorMsg msg = new SimCorMsg();

		if (msgType.equals(DieBefore.OPEN_COMMAND)) {
			curstate = TransactionStateNames.ASSEMBLE_OPEN_COMMAND;
			wastate = TransactionStateNames.WAIT_FOR_OPEN_COMMAND;
			openStr = "open";
			cmdStr = "command";
			isOpen = true;
			isCommand = false;

		}
		if (msgType.equals(DieBefore.CLOSE_COMMAND)) {
			curstate = TransactionStateNames.ASSEMBLE_CLOSE_COMMAND;
			wastate = TransactionStateNames.WAIT_FOR_COMMAND;
			openStr = "close";
			cmdStr = "command";
			isOpen = true;
			isCommand = false;

		}
		if (msgType.equals(DieBefore.PARAM_MSG_COMMAND)) {
			curstate = TransactionStateNames.ASSEMBLE_COMMAND;
			wastate = TransactionStateNames.WAIT_FOR_COMMAND;
			openStr = "param";
			cmdStr = "command";
			isOpen = false;
			isCommand = false;
			msg.setCommand("set-parameter");
			msg.setContent("dummySetParam	nstep	0");
			transaction.setCommand(msg);
			transaction.setId(null);

		}

		if (msgType.equals(DieBefore.PARAM_MSG_RESPONSE)) {
			curstate = TransactionStateNames.ASSEMBLE_RESPONSE;
			wastate = TransactionStateNames.WAIT_FOR_RESPONSE;
			openStr = "param";
			cmdStr = "response";
			isOpen = false;
			isCommand = false;
			msg.setContent("Command ignored. Carry on.");
			msg.setType(MsgType.OK_RESPONSE);
			transaction.setResponse(msg);
			transaction.setId(null);

		}
		transaction.setState(curstate);
		if ((msgType.equals(DieBefore.PARAM_MSG_COMMAND) || msgType
				.equals(DieBefore.PARAM_MSG_RESPONSE)) == false) {
			sap.assembleSessionMessage(transaction, isOpen, isCommand, wastate);
		} else {
			sap.setUpWrite(transaction, isCommand, wastate);
		}
		org.junit.Assert.assertEquals(wastate, transaction.getState());

		while (transaction.getState().equals(wastate)) {
			transaction.setState(wastate);
			sap.waitForSend(transaction, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after write " + openStr + " session "
				+ cmdStr + ": " + transaction);
		org.junit.Assert.assertEquals(next, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}
}
