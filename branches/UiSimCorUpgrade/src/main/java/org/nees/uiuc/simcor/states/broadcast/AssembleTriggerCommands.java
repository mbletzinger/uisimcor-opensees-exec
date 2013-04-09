package org.nees.uiuc.simcor.states.broadcast;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class AssembleTriggerCommands extends TransactionState {

	public AssembleTriggerCommands(StateActionsProcessor sap) {
		super(TransactionStateNames.ASSEMBLE_TRIGGER_COMMANDS, sap, TransactionStateNames.BROADCAST_COMMAND);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithCc)sap).assembleTriggerCommands((BroadcastTransaction) transaction, next, false);
	}

}
