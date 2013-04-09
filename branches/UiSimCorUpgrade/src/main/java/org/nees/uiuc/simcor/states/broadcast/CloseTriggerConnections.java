package org.nees.uiuc.simcor.states.broadcast;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CloseTriggerConnections extends TransactionState {

	public CloseTriggerConnections(StateActionsProcessor sap) {
		super(TransactionStateNames.CLOSE_TRIGGER_CONNECTIONS, sap, TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithCc)sap).closeTriggerConnections(transaction, next);
	}

}
