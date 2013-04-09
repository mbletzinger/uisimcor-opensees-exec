package org.nees.uiuc.simcor.states.p2p;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLsm;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CheckListenerOpenConnection extends TransactionState {

	public CheckListenerOpenConnection(StateActionsProcessor sap) {
		super(TransactionStateNames.CHECK_LISTENER_OPEN_CONNECTION, sap, TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		StateActionsProcessorWithLsm sapwl = (StateActionsProcessorWithLsm) sap;
		sapwl.checkListenerForConnection(transaction, next);

	}

}
