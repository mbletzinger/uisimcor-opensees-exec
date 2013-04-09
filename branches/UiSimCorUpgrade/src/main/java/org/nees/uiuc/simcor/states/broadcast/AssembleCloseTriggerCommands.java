package org.nees.uiuc.simcor.states.broadcast;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class AssembleCloseTriggerCommands extends TransactionState {

	public AssembleCloseTriggerCommands(StateActionsProcessor sap) {
		super(TransactionStateNames.ASSEMBLE_CLOSE_TRIGGER_COMMANDS, sap,TransactionStateNames.BROADCAST_CLOSE_COMMAND);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithCc)sap).assembleTriggerCommands((BroadcastTransaction) transaction, next, true);
	}

}
