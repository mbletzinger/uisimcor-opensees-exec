package org.nees.uiuc.simcor.states.listener;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ListenForConnections extends TransactionState {
	private final Logger log = Logger.getLogger(ListenForConnections.class);

	public ListenForConnections(StateActionsProcessorWithLcf sap,
			TransactionStateNames next) {
		super(TransactionStateNames.LISTEN_FOR_CONNECTIONS, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		if (transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple", new Exception());
		}
		StateActionsProcessorWithLcf sapwl = (StateActionsProcessorWithLcf) sap;
		sapwl.listenForConnection((SimpleTransaction) transaction, next);
	}

}
