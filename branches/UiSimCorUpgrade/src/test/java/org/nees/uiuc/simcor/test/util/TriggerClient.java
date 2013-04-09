package org.nees.uiuc.simcor.test.util;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.UiSimCorTcp.ConnectType;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimCorMsg;

public class TriggerClient extends Thread {
	private String clientId;

	private boolean connected = false;

	private boolean done = false;

	private final Logger log = Logger.getLogger(TriggerClient.class);
	private TcpParameters params;

	protected UiSimCorTcp simcor;

	public TriggerClient(TcpParameters params, String systemDescription) {
		simcor = new UiSimCorTcp(ConnectType.TRIGGER_CLIENT, "MDL-00-01",
				systemDescription);
		this.params = params;
		this.clientId = systemDescription;
	}
	public synchronized String getClientId() {
		return clientId;
	}
	public synchronized boolean isDone() {
		return done;
	}
	@Override
	public void run() {
		String lclient = getClientId();
		startConnection();
		if(connected == false) {
			stopConnection();
			setDone(true);
		}
		while (isDone() == false) {
			simcor.startTransaction(6000);
			TransactionStateNames state = simcor.isReady();
			while (state.equals(TransactionStateNames.COMMAND_AVAILABLE) == false) {

				state = simcor.isReady();
				
				try {
					sleep(200);
				} catch (InterruptedException e) {
				}
				
				log.debug("Trigger receiving " + simcor.getTransaction() + (isDone() ? " DONE" : " STILL RUNNING"));
				if (state.equals(TransactionStateNames.TRANSACTION_DONE)) {
					log.error("Trigger " + lclient + " has an error ");
					break;
				}
				
				if(isDone()) {
					log.debug("Breaking out of loop for exit");
					break;
				}
				
			}
			
			if(isDone()) {
				log.debug("Breaking out of loop for exit");
				continue;
			}
			if(simcor.getTransaction().getError().getType().equals(TcpErrorTypes.NONE) == false) {
				log.info("Trigger " +lclient + " is closing" + simcor.getTransaction());
				setDone(true);
				continue;				
			}
			
			if (simcor.getTransaction().getCommand().getCommand().equals("close-session")) {
				log.info("Trigger " +lclient + " is closing" + simcor.getTransaction());
				setDone(true);
				continue;
			}
			if(done) {
				break;
			}
			log.debug("Received command " + simcor.getTransaction());
			SimCorMsg response = simcor.getSap().getTf().createTriggerResponse(
					simcor.getTransaction().getCommand());
			simcor.continueTransaction(response);
			state = simcor.isReady();
			while (state.equals(TransactionStateNames.READY) == false) {
				state = simcor.isReady();
				try {
					sleep(200);
				} catch (InterruptedException e) {
				}
				log.debug("Trigger sending " + simcor.getTransaction());
			}
		}
		stopConnection();
	}
	public synchronized void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public synchronized void setDone(boolean done) {
		this.done = done;
	}

	private void startConnection() {
		simcor.startup(params);
		String lclient = getClientId();
		TransactionStateNames state = simcor.isReady();
		int count = 0;
		while (state.equals(TransactionStateNames.READY) == false && count < 10) {
			state = simcor.isReady();
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
			log.debug("Trigger " + lclient+ "  connecting "
					+ simcor.getTransaction());
		}
		if (count == 10) {
			log.error(clientId + " Could not connect");
			stopConnection();
			return;
		}
		connected = true;
		log.debug("Trigger" + lclient + "  connected");
	}

	private void stopConnection() {
		simcor.shutdown();
		String lclient = getClientId();

		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			state = simcor.isReady();
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
			log.debug("Trigger discconnectiong " + simcor.getTransaction());
		}
		connected = false;
		log.debug("Trigger" + lclient + "  disconnected");
	}
}
