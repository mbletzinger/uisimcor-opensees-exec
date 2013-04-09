package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CheckOpenConnection extends TransactionState {
//	private final Logger log = Logger.getLogger(CheckOpenConnection.class);

	public CheckOpenConnection(StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.CHECK_OPEN_CONNECTION, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.checkOpenConnection((SimpleTransaction) transaction, next);
	}

}
