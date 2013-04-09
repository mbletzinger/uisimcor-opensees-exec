package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.factories.ListenerConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class StateActionsProcessorWithLcf extends StateActionsProcessor {
	private ListenerConnectionFactory lcf;
	public StateActionsProcessorWithLcf() {
		super();
		lcf = new ListenerConnectionFactory();
	}

	public StateActionsProcessorWithLcf(ListenerConnectionFactory cf, TransactionFactory tf,
			ConnectionManager cm, Archiving archive) {
		super();
		this.lcf = cf;
	}

	public ListenerConnectionFactory getLcf() {
		return lcf;
	}

	public void listenForConnection(SimpleTransaction transaction,
			TransactionStateNames next) {
		TcpError er = new TcpError();
		Connection connection = null;
		connection = lcf.checkForListenerConnection();
		if (connection == null) {
			return;
		}
		er = lcf.checkForErrors();
		cm.setConnection(connection);
		connection.setMsgTimeout(transaction.getTimeout());
		setStatus(transaction, er, next);
		log.debug("Listen For Connection " + transaction);
	}

	public void setLcf(ListenerConnectionFactory cf) {
		this.lcf = cf;
	}

	public void startListening(SimpleTransaction transaction) {
		log.debug("Start listening");
		cm.setParams(params);
		lcf.setParams(params);
		lcf.startListener();
		TcpError err = lcf.checkForErrors();
		saveError(err);
		setStatus(transaction, err,
				TransactionStateNames.OPENING_CONNECTION,TransactionStateNames.STOP_LISTENER);
		log.debug("Start Listening " + transaction);

	}
	
	public void stopListening(SimpleTransaction transaction) {
		boolean stopped = lcf.stopListener();
		if (stopped == false) {
			return;
		}
		TcpError er = lcf.checkForErrors();
		setStatus(transaction, er, TransactionStateNames.TRANSACTION_DONE, TransactionStateNames.TRANSACTION_DONE);
		log.debug("Stop Listening " + transaction);

	}

}
