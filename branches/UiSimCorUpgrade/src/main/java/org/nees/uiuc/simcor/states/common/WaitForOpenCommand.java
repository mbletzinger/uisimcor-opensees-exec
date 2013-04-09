package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForOpenCommand extends TransactionState {
//private final Logger log = Logger.getLogger(WaitForOpenCommand.class);
	public WaitForOpenCommand(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_OPEN_COMMAND, sap,
				TransactionStateNames.ASSEMBLE_OPEN_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForSessionMsgRead((SimpleTransaction) transaction, true, next);
	}

}
