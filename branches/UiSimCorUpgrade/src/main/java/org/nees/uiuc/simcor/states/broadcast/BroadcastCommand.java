package org.nees.uiuc.simcor.states.broadcast;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class BroadcastCommand extends TransactionState {

	public BroadcastCommand(StateActionsProcessor sap,TransactionStateNames next) {
		super(TransactionStateNames.BROADCAST_COMMAND, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithCc)sap).broadcastCommands((BroadcastTransaction) transaction, next);
	}

}
