package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SetupReadMessage extends TransactionState {
	private boolean isCommand;
//	private final Logger log = Logger.getLogger(SetupReadMessage.class);

	public SetupReadMessage(TransactionStateNames state,
			StateActionsProcessor sap, boolean isCommand,
			TransactionStateNames next) {
		super(state, sap, next);
		this.isCommand = isCommand;
	}

	@Override
	public void execute(Transaction transaction) {
		sap.setUpRead(transaction, isCommand, next);
	}

}
