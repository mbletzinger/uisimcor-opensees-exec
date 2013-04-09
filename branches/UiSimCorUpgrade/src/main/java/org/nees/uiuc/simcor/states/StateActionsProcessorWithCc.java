package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;

public class StateActionsProcessorWithCc extends StateActionsProcessorWithLsm {

	private ClientConnections cc;

	public StateActionsProcessorWithCc(ListenerStateMachine lsm) {
		super(lsm);
		this.cc = lsm.getCc();
		lsm.getSap().setTf(tf);
		tf.setDirection(DirectionType.SEND_COMMAND);
		lsm.getSap().setCm(cm);
	}

	public void assembleTriggerCommands(BroadcastTransaction transaction,
			TransactionStateNames next, boolean isClose) {
		if (isClose) {
			SimCorMsg msg = tf.createSessionCommand(false);
			transaction.setCommand(msg);
		}
		cc.assembleTriggerMessages(transaction);
		log.debug("During Assemble " + transaction);
		setStatus((Transaction) transaction, transaction.getError(), next, next);
		log.debug("After Assemble " + transaction);
	}

	public void broadcastCommands(BroadcastTransaction transaction,
			TransactionStateNames next) {
		TransactionStateNames state = transaction.getState();
		if (cc.waitForBroadcastFinished()) {
			state = next;
			log.debug("Broadcasts are done");
		} else {
			log.debug("Still broadcasting");
		}
		setStatus((Transaction) transaction, transaction.getError(), state,
				state);
	}

	public void closeTriggerConnections(Transaction transaction,
			TransactionStateNames next) {
		boolean allClosed = cc.closeClientConnections();
		if (allClosed == false) {
			return;
		}
		setStatus((Transaction) transaction, transaction.getError(), next, next);

	}

	public void setupTriggerResponses(Transaction transaction,
			TransactionStateNames next) {
		cc.setupResponsesCheck((BroadcastTransaction) transaction);
		setStatus(transaction, transaction.getError(), next, next);

	}

	public void delayForCloseCommands(Transaction transaction,
			TransactionStateNames next, int delayInMilliseconds) {
		try {
			Thread.sleep(delayInMilliseconds);
		} catch (InterruptedException e) {
		}
		setStatus(transaction, transaction.getError(), next, next);
	}

	public void waitForTriggerResponse(BroadcastTransaction transaction,
			TransactionStateNames next) {
		TransactionStateNames state = transaction.getState();
		if (cc.waitForResponsesFinished(transaction)) {
			state = next;
			log.debug("Responses are done");
		} else {
			log.debug("Still waiting for responses");
		}
		setStatus(transaction, transaction.getError(), state, state);
	}

}
