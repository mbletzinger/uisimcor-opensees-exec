package org.nees.uiuc.simcor;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class TriggerBroadcastVamp {
	public class VampThread extends Thread {
		long lastVamp = System.currentTimeMillis();

		@Override
		public void run() {
			log.info("Vamp is running");
			sendVamp();
			while (isOn()) {
				long time = (System.currentTimeMillis() - lastVamp);
				if (time > (vampTimeInSeconds * 1000)) {
					sendVamp();
					lastVamp = System.currentTimeMillis();
				}
				try {
					sleep(100);
				} catch (InterruptedException e) {
				}
			}
			log.info("Vamp is stopped");
		}

		private void sendVamp() {
			BroadcastTransaction trans = bcast.getTf().createVampTransaction(
					msgTimeout);
			log.info("Broadcasting Vamp " + trans);
			bcast.startTransaction(trans);
			TransactionStateNames state = bcast.isReady();
			while (state.equals(TransactionStateNames.TRANSACTION_DONE) == false) {
				if (isOn() == false) {
					break;
				}
				try {
					sleep(100);
				} catch (InterruptedException e) {
				}
				state = bcast.isReady();
				// log.debug("Still broadcasting Vamp " +
				// bcast.getTransaction());
			}
			log.debug("Done broadcasting Vamp " + bcast.getTransaction());
			setError(bcast.getTransaction().getError());
			bcast.isReady();
		}
	}

	private UiSimCorTriggerBroadcast bcast;
	private TcpError error = new TcpError();
	private Logger log = Logger.getLogger(TriggerBroadcastVamp.class);
	private int msgTimeout = 2000;
	private boolean on;
	private VampThread vamp = new VampThread();
	private int vampTimeInSeconds = 30;

	public TriggerBroadcastVamp(UiSimCorTriggerBroadcast bcast) {
		super();
		this.bcast = bcast;
	}

	public synchronized TcpError getError() {
		return error;
	}

	public synchronized boolean isOn() {
		return on;
	}

	public synchronized void setError(TcpError error) {
		this.error = error;
	}

	public synchronized void setOn(boolean on) {
		this.on = on;
	}

	public void startVamp(int vampTimeInSeconds, int msgTimeoutInMils,
			BroadcastTransaction transaction) {
		this.vampTimeInSeconds = vampTimeInSeconds;
		this.msgTimeout = msgTimeoutInMils;
		bcast.getTf().setVampMsg(transaction);
		setOn(true);
		vamp.start();
	}

	public boolean stopVamp() {
		setOn(false);
		return vamp.isAlive() == false;
	}
}
