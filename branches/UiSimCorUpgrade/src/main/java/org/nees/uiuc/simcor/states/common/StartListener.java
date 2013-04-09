package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLsm;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StartListener extends TransactionState {

	public StartListener(StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.START_LISTENER, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithLsm)sap).startListener( transaction, next);
	}

}
