package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class AssembleResponse extends TransactionState {
	private boolean isSessionResponse;
//	private final Logger log = Logger.getLogger(AssembleResponse.class);

	public AssembleResponse(TransactionStateNames state,
			StateActionsProcessor sap, boolean isSessionResponse) {
		super(state, sap, TransactionStateNames.SENDING_RESPONSE);
		this.isSessionResponse = isSessionResponse;
	}

	@Override
	public void execute(Transaction transaction) {
		if (isSessionResponse) {
			sap.assembleSessionMessage((SimpleTransaction) transaction, true, false, next);
		} else {
			sap.setUpWrite((SimpleTransaction) transaction, false, next);
		}
	}

}
