package org.nees.uiuc.simcor.states.p2p;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForResponse extends TransactionState {
//	private final Logger log = Logger.getLogger(WaitForResponse.class);

	public WaitForResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE, sap,
				TransactionStateNames.RESPONSE_AVAILABLE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead((SimpleTransaction) transaction, false, next);
	}

}
