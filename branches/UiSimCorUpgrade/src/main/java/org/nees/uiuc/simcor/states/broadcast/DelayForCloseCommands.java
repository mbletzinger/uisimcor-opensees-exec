package org.nees.uiuc.simcor.states.broadcast;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class DelayForCloseCommands extends TransactionState {

	private final int delayInMilliseconds;
	public DelayForCloseCommands(StateActionsProcessor sap,
			int delayInMilliseconds) {
		super(TransactionStateNames.DELAY_FOR_CLOSE_COMMANDS	, sap, TransactionStateNames.CLOSE_TRIGGER_CONNECTIONS);
		this.delayInMilliseconds = delayInMilliseconds;
	}
	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithCc)sap).delayForCloseCommands(transaction, next, delayInMilliseconds);
	}
}
