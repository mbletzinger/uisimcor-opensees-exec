package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public abstract class TransactionState {

	protected TransactionStateNames next;
	protected StateActionsProcessor sap;
	protected TransactionStateNames state;

	public TransactionState(TransactionStateNames state, StateActionsProcessor sap, TransactionStateNames next) {
		this.state = state;
		this.sap = sap;
		this.next = next;
	}

	public abstract void execute(Transaction transaction);

	protected void setStatus(SimpleTransaction transaction, TcpError error) {
		TcpError err = error;
		transaction.setError(error);
		if(err.getType() != TcpErrorTypes.NONE) {
			transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		} else {
			transaction.setState(next);			
		}
	}

	@Override
	public String toString() {
		return state.toString();
	}

}