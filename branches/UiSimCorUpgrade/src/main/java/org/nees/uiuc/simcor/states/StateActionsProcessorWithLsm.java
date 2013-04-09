package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.listener.ClientIdWithConnection;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StateActionsProcessorWithLsm extends StateActionsProcessor {
	private ListenerStateMachine lsm;

	public StateActionsProcessorWithLsm(ListenerStateMachine lsm) {
		super();
		this.lsm = lsm;
	}
	public void checkListenerForConnection(Transaction transaction, TransactionStateNames next) {
		TcpError error = lsm.getError();
		if((lsm.isClientAvailable() == false) && error.getType().equals(TcpErrorTypes.NONE)) {
			log.debug("Still Listeining " + transaction);
			return;
		}
		ClientIdWithConnection id = lsm.pickupOneClient();
		if(id != null) {
			cm.setConnection(id.connection);
		}
		setStatus(transaction, error,next);
		log.debug("Found Connection " + transaction);

	}
	
	public void startListener(Transaction transaction, TransactionStateNames next) {
		lsm.getSap().setParams(params);
		lsm.start();
		try {
			Thread.sleep(200); //Give it time to bind the address
		} catch (InterruptedException e) {
		}
		TcpError error = lsm.getError();
		setStatus(transaction, error, next, TransactionStateNames.STOP_LISTENER);
		log.debug("Start Listener " + transaction);
}
	public void stopListener(Transaction transaction, TransactionStateNames next) {
		lsm.setRunning(false);
		if(lsm.isAlive()) {
			log.debug("Still stopping listener " + transaction);
			return;
		}
		TcpError error = lsm.getError();
		setStatus(transaction, error, next, next);
		log.debug("Stop Listener " + transaction);
	}

}
