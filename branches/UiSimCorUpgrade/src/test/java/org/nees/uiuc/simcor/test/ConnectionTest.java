package org.nees.uiuc.simcor.test;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.RemoteListener;
import org.nees.uiuc.simcor.transaction.Address;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class ConnectionTest  {
	private Connection connection;
	private final Logger log = Logger.getLogger(ConnectionTest.class);
	private TcpParameters lparams;
	private Msg2Tcp m2t;
	private RemoteListener remote;
	private TcpParameters rparams;
	private TransactionIdentity tid;

	private TcpActionsDto connect2Remote() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.CONNECT);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				log.debug("Waiting for remote connect");
				count = 0;
			}
			count++;
		}
		return connection.getFromRemoteMsg();
	}

	private TcpActionsDto disconnectFromRemote() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.CLOSE);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusyOrErrored()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				log.debug("Waiting for remote disconnect");
				count = 0;
			}
			count++;
		}
		return connection.getFromRemoteMsg();
	}

	private TcpActionsDto readFromRemote() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.READ);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				log.debug("Waiting for read");
				count = 0;
			}
			count++;
		}
		return connection.getFromRemoteMsg();
	}

	private TcpActionsDto readFromRemoteWithAbort() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.READ);
		connection.setToRemoteMsg(dto);
		try {
			Thread.sleep(80);
		} catch (InterruptedException e) {
		}
		Assert.assertTrue(connection.isBusy());
		dto.setError(new TcpError());
		dto.setAction(ActionsType.EXIT);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isAlive()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				log.debug("Connection has not aborted");
				count = 0;
			}
			count++;
		}
		return connection.getFromRemoteMsg();
	}

	@Before
	public void setUp() throws Exception {
		tid = new TransactionIdentity();
		tid.setStep(2);
		tid.setSubStep(3);
		tid.setCorrectionStep(102);
		tid.createTransId();

		m2t = new Msg2Tcp();
		m2t.setId(tid);
	}

	private void createConnection(boolean sendLfcr) {
		rparams = new TcpParameters(null, 0, 6445, 2000, sendLfcr);
		lparams = new TcpParameters("127.0.0.1", 6445, 0, 2000, sendLfcr);
		remote = new RemoteListener(rparams);
		connection = new Connection(lparams);
	}
	private void startConnection() {
		connection.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
	}
	private void startRemote() {
		remote.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
	}

	@After
	public void tearDown() throws Exception {
		if (remote.isRunning()) {
			remote.setRunning(false);
			int count = 1;
			while (remote.isAlive()) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
				}
				if (count == 70) {
					count = 0;
					log.debug("Still waiting on remote shutdown");
				}
				count++;
			}
		}
	}

	@Test
	public void test01ConnectFail() {
		createConnection(true);
		startConnection();
		TcpActionsDto dto = connect2Remote();
		Assert.assertEquals(TcpErrorTypes.IO_ERROR, dto.getError().getType());
	}

	@Test
	public void test02OpenClose() {
		createConnection(true);
		startRemote();
		startConnection();
		TcpActionsDto dto = connect2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());
		dto = disconnectFromRemote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());
	}

	@Test
	public void test03WriteSuccessAndReadFail() {
		createConnection(true);
		startRemote();
		startConnection();
		TcpActionsDto dto = connect2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = write2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		remote.setRunning(false);

		dto = readFromRemote();
		Assert.assertEquals(TcpErrorTypes.TIMEOUT, dto.getError().getType());
		dto = disconnectFromRemote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

	}

	@Test
	public void test04WriteReadSuccess() {
		createConnection(true);
		startRemote();
		startConnection();
		TcpActionsDto dto = connect2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = write2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = readFromRemote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

	}

	@Test
	public void test05WriteReadSuccess2x() {
		createConnection(true);
		startRemote();
		startConnection();
		TcpActionsDto dto = connect2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = write2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = readFromRemote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = write2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = readFromRemote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

	}

	@Test
	public void test04LfEomWriteReadSuccess() {
		createConnection(false);
		startRemote();
		startConnection();
		TcpActionsDto dto = connect2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = write2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = readFromRemote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

	}

	@Test
	public void test06WriteSuccessAndReadAbort() {
		createConnection(true);
		startRemote();
		startConnection();
		TcpActionsDto dto = connect2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		dto = write2Remote();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

		remote.setRunning(false);

		dto = readFromRemoteWithAbort();
		Assert.assertEquals(TcpErrorTypes.NONE, dto.getError().getType());

	}

	private TcpActionsDto write2Remote() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.WRITE);
		tid.setStep(tid.getStep() + 1);
		SimCorMsg msg = new SimCorMsg();
		msg.setContent("This is a Test");
		msg.setAddress(new Address("MDL-Test"));
		m2t.setMsg(msg);
		dto.setMsg(m2t);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				log.debug("Waiting for write");
				count = 0;
			}
			count++;
		}
		return connection.getFromRemoteMsg();
	}

}
