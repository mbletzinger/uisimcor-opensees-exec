package org.nees.uiuc.simcor.test.util;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;

public class StateActionsResponder extends Thread {
	public enum DieBefore {
		CLOSE_COMMAND, END, OPEN_COMMAND, OPEN_RESPONSE, PARAM_MSG_COMMAND,PARAM_MSG_RESPONSE
	}

	private DieBefore lifeSpan = DieBefore.END;;

	private final Logger log = Logger.getLogger(StateActionsResponder.class);
	private TcpParameters params;
	private StateActionsProcessor sap;
	private boolean sendSession = false;
	private SimpleTransaction transaction;

	public StateActionsResponder(DieBefore lifeSpan, TcpParameters params,
			boolean sendOpenSession) {
		super();
		this.lifeSpan = lifeSpan;
		this.params = params;
		this.sendSession = sendOpenSession;
		sap = new StateActionsProcessorWithLcf();
		sap.setIdentity("MDL-00-01", "Connection Test Responder");
	}

	public DieBefore getLifeSpan() {
		return lifeSpan;
	}

	public TcpParameters getParams() {
		return params;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	public synchronized SimpleTransaction getTransaction() {
		return transaction;
	}

	public boolean isSendSession() {
		return sendSession;
	}

	private boolean receiveSessionCommand() {
		int count = 0;
		SimpleTransaction tr = getTransaction();
		sap.setUpRead(tr, true, TransactionStateNames.WAIT_FOR_COMMAND);
		while ((tr.getState().equals(TransactionStateNames.WAIT_FOR_COMMAND))
				&& (count < 50)) {
			sap.waitForRead(tr, true, TransactionStateNames.COMMAND_AVAILABLE);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Receive failed");
			return false;
		}
		setTransaction(tr);
		return true;
	}

	private boolean receiveSessionResponse() {
		int count = 0;
		SimpleTransaction tr = getTransaction();
		sap.setUpRead(tr, false, TransactionStateNames.WAIT_FOR_RESPONSE);
		while ((tr.getState().equals(TransactionStateNames.WAIT_FOR_RESPONSE))
				&& (count < 50)) {
			sap.waitForRead(tr, true, TransactionStateNames.RESPONSE_AVAILABLE);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Receive failed");
			return false;
		}
		setTransaction(tr);
		log.debug("Received " + tr);
		return true;
	}

	@Override
	public void run() {
		sap.setParams(params);
		SimpleTransaction tr = getTransaction();
		tr = sap.getTf().createSendCommandTransaction(null, 2000);
		tr.setPosted(true);
		tr.setState(TransactionStateNames.OPENING_CONNECTION);
		int count = 0;
		while (tr.getState().equals(TransactionStateNames.OPENING_CONNECTION)
				&& count < 50) {
			sap.openConnection(tr);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
			if (count >= 50) {
				setTransaction(tr);
				log.error("Ending because count = " + count);
				shutdown();
				return;
			}
		}

		setTransaction(tr);

		if (lifeSpan.equals(DieBefore.OPEN_COMMAND)) {
			log.debug("Ending because lifespan is " + lifeSpan);
			shutdown();
			return;
		}
		log.debug("Current transaction after open connection: "
				+ getTransaction());
		if (sendSession) {
			if (sendSessionCommand(DieBefore.OPEN_COMMAND) == false) {
				log.debug("Ending because sendSendSessionCommand died");
				shutdown();
				return;
			}
			log.debug("Current transaction after send open session command: "
					+ getTransaction());
			if (lifeSpan.equals(DieBefore.OPEN_RESPONSE)) {
				log.debug("Ending because lifespan is " + lifeSpan);
				shutdown();
				return;
			}
			if (receiveSessionResponse() == false) {
				log.debug("Ending because receiveSessionResponse died");
				shutdown();
				return;
			}
			log
					.debug("Current transaction after receive open session response: "
							+ getTransaction());

		} else {
			if (receiveSessionCommand() == false) {
				log.debug("Ending because receiveSessionCommand died");
				shutdown();
				return;
			}
			log.debug("Current transaction: " + getTransaction());
			if (lifeSpan.equals(DieBefore.OPEN_RESPONSE)) {
				log.debug("Ending because lifespan is " + lifeSpan);
				shutdown();
				return;
			}
			log
					.debug("Current transaction after receive open session command: "
							+ getTransaction());
			if (sendSessionResponse(DieBefore.OPEN_RESPONSE) == false) {
				log.debug("Ending because sendSessionResponse died");
				shutdown();
				return;
			}
			log.debug("Current transaction after send open session response: "
					+ getTransaction());
		}

		if (sendSession) {
			if (sendSessionCommand(DieBefore.PARAM_MSG_COMMAND) == false) {
				log.debug("Ending because sendSendSessionCommand died");
				shutdown();
				return;
			}
			log.debug("Current transaction after send param  command: "
					+ getTransaction());
			if (lifeSpan.equals(DieBefore.PARAM_MSG_COMMAND)) {
				log.debug("Ending because lifespan is " + lifeSpan);
				shutdown();
				return;
			}
			if (receiveSessionResponse() == false) {
				log.debug("Ending because receiveSessionResponse died");
				shutdown();
				return;
			}
			log
					.debug("Current transaction after receive param  response: "
							+ getTransaction());

		} else {
			if (receiveSessionCommand() == false) {
				log.debug("Ending because receiveSessionCommand died");
				shutdown();
				return;
			}
			log.debug("Current transaction: " + getTransaction());
			if (lifeSpan.equals(DieBefore.PARAM_MSG_RESPONSE)) {
				log.debug("Ending because lifespan is " + lifeSpan);
				shutdown();
				return;
			}
			log
					.debug("Current transaction after receive param  command: "
							+ getTransaction());
			if (sendSessionResponse(DieBefore.PARAM_MSG_RESPONSE) == false) {
				log.debug("Ending because sendSessionResponse died");
				shutdown();
				return;
			}
			log.debug("Current transaction after send param  response: "
					+ getTransaction());
		}
		if (lifeSpan.equals(DieBefore.CLOSE_COMMAND)) {
			log.debug("Ending because lifespan is " + lifeSpan);
			shutdown();
			return;
		}

		if (sendSession) {
			if (sendSessionCommand(DieBefore.CLOSE_COMMAND) == false) {
				log.debug("Ending because sendSessionCommand died");
				shutdown();
				return;
			}
		} else {
			if (receiveSessionCommand() == false) {
				log.debug("Ending because receiveSessionCommand died");
				shutdown();
				return;
			}
			log
					.debug("Current transaction after receive close session command: "
							+ getTransaction());
			tr = getTransaction();
			tr.setState(TransactionStateNames.TRANSACTION_DONE);
			count = 0;
			while (tr.getState().equals(
					TransactionStateNames.CLOSING_CONNECTION)
					&& count < 50) {
				sap.closingConnection(tr,
						TransactionStateNames.TRANSACTION_DONE);
				count++;
				try {
					sleep(200);
				} catch (InterruptedException e) {
				}
			}
			setTransaction(tr);
		}
		shutdown();
	}

	private boolean sendSessionCommand(DieBefore cmdType) {
		int count = 0;
		boolean isOpen = (cmdType == DieBefore.OPEN_COMMAND);
		SimpleTransaction tr = getTransaction();
		if (cmdType.equals(DieBefore.PARAM_MSG_COMMAND) == false) {
			sap.assembleSessionMessage(tr, isOpen, true,
					TransactionStateNames.SENDING_COMMAND);
		} else {
			SimCorMsg msg = new SimCorMsg();
			msg.setCommand("set-parameter");
			msg.setContent("dummySetParam	nstep	0");
			tr.setCommand(msg);
			tr.setId(null);
			sap.setUpWrite(tr, true, TransactionStateNames.SENDING_COMMAND);
		}
		while ((tr.getState().equals(TransactionStateNames.SENDING_COMMAND))
				&& (count < 50)) {
			count++;
			sap.waitForSend(tr, TransactionStateNames.WAIT_FOR_RESPONSE);
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Send failed");
			return false;
		}
		setTransaction(tr);
		log.debug("Sent: " + tr);
		return true;
	}

	private boolean sendSessionResponse(DieBefore rspType) {
		int count = 0;
		SimpleTransaction tr = getTransaction();
		if (rspType.equals(DieBefore.PARAM_MSG_RESPONSE) == false) {		
		sap.assembleSessionMessage(tr, true, false,
				TransactionStateNames.SENDING_RESPONSE);
		} else {
			SimCorMsg resp = new SimCorMsg();
			resp.setContent("Command ignored. Carry on.");
			resp.setType(MsgType.OK_RESPONSE);
			tr.setResponse(resp);
			tr.setId(null);
			sap.setUpWrite(tr, true, TransactionStateNames.SENDING_RESPONSE);
		}
		while ((tr.getState().equals(TransactionStateNames.SENDING_RESPONSE))
				&& (count < 50)) {
			count++;
			sap.waitForSend(tr, TransactionStateNames.TRANSACTION_DONE);
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Send failed");
			return false;
		}
		setTransaction(tr);
		log.debug("Sent " + tr);
		return true;
	}

	public void setLifeSpan(DieBefore lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}

	public void setSap(StateActionsProcessor sap) {
		this.sap = sap;
	}

	public void setSendSession(boolean sendSession) {
		this.sendSession = sendSession;
	}

	public synchronized void setTransaction(SimpleTransaction transaction) {
		this.transaction = transaction;
	}

	private void shutdown() {
		SimpleTransaction tr = getTransaction();
		sap.closingConnection(tr, TransactionStateNames.TRANSACTION_DONE);
	}
}
