package org.nees.uiuc.simcor.states.p2p;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ShutdownConnection extends TransactionState {

	public ShutdownConnection(StateActionsProcessor sap) {
		super(TransactionStateNames.SHUTDOWN_CONNECTION, sap, TransactionStateNames.STOP_LISTENER);
		}

	@Override
	public void execute(Transaction transaction) {
		sap.closingConnection((SimpleTransaction) transaction, next);

	}

}
