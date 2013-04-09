package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SendingCommand extends TransactionState {
//	private final Logger log = Logger.getLogger(SendingCommand.class);

	public SendingCommand(StateActionsProcessor sap,
			TransactionStateNames next) {
		super(TransactionStateNames.SENDING_COMMAND, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForSend((SimpleTransaction) transaction, next);
	}

}
