package org.nees.uiuc.simcor.states.broadcast;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SetupReadTriggerResponses extends TransactionState {

	public SetupReadTriggerResponses(StateActionsProcessor sap) {
		super(TransactionStateNames.SETUP_TRIGGER_READ_RESPONSES, sap, TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithCc)sap).setupTriggerResponses(transaction, next);
	}

}
