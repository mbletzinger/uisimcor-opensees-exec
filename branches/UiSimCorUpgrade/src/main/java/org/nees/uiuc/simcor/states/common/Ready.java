package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class Ready extends TransactionState {

	public Ready( StateActionsProcessor sap) {
		super(TransactionStateNames.READY, sap, TransactionStateNames.READY);
	}

	@Override
	public void execute(Transaction transaction) {

	}

}
