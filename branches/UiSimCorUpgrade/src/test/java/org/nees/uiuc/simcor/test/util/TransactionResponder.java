package org.nees.uiuc.simcor.test.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.UiSimCorTcp.ConnectType;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class TransactionResponder extends Thread {
	public static void main(String[] args) {
		TransactionResponder responder = new TransactionResponder();
		TransactionMsgs data = new TransactionMsgs();
		try {
			data.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		responder.setData(data);
		responder.start();
	}

	public boolean connected;

	private TransactionMsgs data;

	private final Logger log = Logger.getLogger(TransactionResponder.class);

	private TcpParameters params = new TcpParameters();

	private List<TransactionStateNames> readyStates = new ArrayList<TransactionStateNames>();

	private UiSimCorTcp simcor;

	public TransactionResponder() {
		readyStates.add(TransactionStateNames.TRANSACTION_DONE);
		readyStates.add(TransactionStateNames.READY);
	}

	public TransactionMsgs getData() {
		return data;
	}

	public TcpParameters getParams() {
		return params;
	}

	public UiSimCorTcp getSimcor() {
		return simcor;
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public void run() {
		params.setLocalPort(6445);
		params.setTcpTimeout(5000);
		simcor = new UiSimCorTcp(ConnectType.P2P_RECEIVE_COMMAND, "MDL-00-01",
				"RESPONDER");
		simcor.startup(params);
		connected = false;
		int count = 0;

		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false && count < 40) {
			log.info("Waiting for a connection " + state);
			state = simcor.isReady();
			count++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("My sleep was interrupted.");
			}
			log.debug("Wating for connection: " + simcor.getTransaction());
		}
		org.junit.Assert.assertTrue(count < 40);
		log.info("Open result [" + simcor.getTransaction() + "]");
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, simcor
				.getTransaction().getError().getType());
		org.junit.Assert.assertEquals(TransactionStateNames.READY, simcor
				.getTransaction().getState());
		connected = true;
		log.info("Connection established");
		log.info("Expected Messages:\n" + data.dumpExpected());
		while (connected) {
			simcor.startTransaction(5000);
			state = simcor.isReady();
			while (state.equals(TransactionStateNames.COMMAND_AVAILABLE) == false) {
				try {
					Thread.sleep(200);
					state = simcor.isReady();
				} catch (InterruptedException e) {
				}
				log.debug("Wating for command: " + simcor.getTransaction());
				TcpError error = simcor.getTransaction().getError();
				if (error.getType().equals(TcpErrorTypes.NONE) == false) {
					log.error("Responder failed because " + error);
					shutdown();
					return;
				}
			}
			log.debug("Received command: " + simcor.getTransaction());
			Transaction transaction = simcor.pickupTransaction();
			log.debug("Picked up transaction: " + transaction);
			if (transaction.getError().getType().equals(TcpErrorTypes.NONE) == false) {
				log.error("Transaction error " + transaction.getError());
				shutdown();
				return;
			}
			if (transaction.getCommand() == null) {
				connected = false;
				shutdown();
				log.error("No command was received");
				return;
			}
			if (transaction.getCommand().getCommand().equals("close-session")) {
				connected = false;
				shutdown();
				return;
			}
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
					.getError().getType());
			org.junit.Assert.assertNotNull(transaction.getCommand()
					.getCommand());
			SimpleTransaction expected = data.transactions.get(transaction
					.getCommand().toString());
			if (expected == null) {
				String hash = "";
				for (Iterator<String> s = data.transactions.keySet().iterator(); s
						.hasNext();) {
					String m = s.next();
					hash += "\t" + m + "=" + m.hashCode() + "\n";
				}
				log.error("Command [" + transaction.getCommand() + "]="
						+ transaction.getCommand().hashCode()
						+ " not recognized\n" + hash);
				org.junit.Assert.fail();
			}
			org.junit.Assert.assertNotNull(expected);
			data.checkTransaction((TransactionWithTestFlags) expected,
					transaction, TcpErrorTypes.NONE);
			SimCorMsg resp = expected.getResponse();
			SimCorMsg cmd = transaction.getCommand();
			if (cmd.getCommand().equals("propose")
					|| cmd.getCommand().equals("execute")) {
				transaction.setId(expected.getId());
			}
			simcor.continueTransaction(resp);
			state = simcor.isReady();
			log.debug("Sending response " + resp);
			while (state.equals(TransactionStateNames.READY) == false) {
				try {
					Thread.sleep(200);
					state = simcor.isReady();
				} catch (InterruptedException e) {
					log.info("My sleep was interrupted.");
				}
				log.debug("Wating for response send: "
						+ simcor.getTransaction());
			}
			transaction = simcor.getTransaction();
			if (transaction.getError().getType() != TcpErrorTypes.NONE) {
				log.error("Transaction error " + transaction.getError());
			}
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
					.getError().getType());
		}
		shutdown();

	}

	private void shutdown() {
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("Sleep interrupted");
			}
			state = simcor.isReady();
			log.debug("Disconnecting " + simcor.getTransaction());
		}

	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void setData(TransactionMsgs data) {
		this.data = data;
	}

	public void setParams(TcpParameters rparams) {
		this.params = rparams;
	}

	public void setSimcor(UiSimCorTcp simcor) {
		this.simcor = simcor;
	}

}
