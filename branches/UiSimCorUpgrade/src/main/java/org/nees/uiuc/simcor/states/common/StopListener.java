package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLsm;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StopListener extends TransactionState {

	public StopListener( StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.STOP_LISTENER, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithLsm)sap).stopListener(transaction, next);
	}

}
