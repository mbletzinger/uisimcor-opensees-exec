package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForResponsePosting extends TransactionState {
//	private final Logger log = Logger.getLogger(WaitForResponsePosting.class);

	public WaitForResponsePosting(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE_POSTING, sap,
				TransactionStateNames.ASSEMBLE_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForPosted((SimpleTransaction) transaction, next);
	}

}
