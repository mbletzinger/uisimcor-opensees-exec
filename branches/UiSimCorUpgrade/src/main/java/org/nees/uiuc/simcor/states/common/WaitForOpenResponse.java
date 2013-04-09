package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForOpenResponse extends TransactionState {
//	private final Logger log = Logger.getLogger(WaitForOpenResponse.class);

	public WaitForOpenResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_OPEN_RESPONSE, sap,
				TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForSessionMsgRead((SimpleTransaction) transaction, false, next);
	}

}
