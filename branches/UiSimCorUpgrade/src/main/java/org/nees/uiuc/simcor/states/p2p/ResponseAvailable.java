package org.nees.uiuc.simcor.states.p2p;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ResponseAvailable extends TransactionState {
//	private final Logger log = Logger.getLogger(ResponseAvailable.class);

	public ResponseAvailable(StateActionsProcessor sap) {
		super(TransactionStateNames.RESPONSE_AVAILABLE, sap,
				TransactionStateNames.TRANSACTION_DONE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForPickUp((SimpleTransaction) transaction, next);
	}

}
