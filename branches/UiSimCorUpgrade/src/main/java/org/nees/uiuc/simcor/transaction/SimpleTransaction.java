package org.nees.uiuc.simcor.transaction;

import org.nees.uiuc.simcor.tcp.TcpError;

public class SimpleTransaction extends Transaction {
	boolean pickedUp = false;
	boolean posted = false;
	SimCorMsg response = null;

	public SimpleTransaction() {
	}

	public SimpleTransaction(SimpleTransaction t) {
		super(t);
		pickedUp = t.pickedUp;
		posted = t.posted;
		error = new TcpError(t.error);
		if (t.id != null) {
			id = new TransactionIdentity(t.id);
		}
		if (t.response != null) {
			if (t.response instanceof SimCorCompoundMsg) {
				response = new SimCorCompoundMsg((SimCorCompoundMsg) t.response);
			} else {
				response = new SimCorMsg(t.response);
			}
		}
	}

	public SimCorMsg getResponse() {
		return response;
	}

	public boolean isPickedUp() {
		return pickedUp;
	}

	public boolean isPosted() {
		return posted;
	}

	public void setPickedUp(boolean pickedUp) {
		this.pickedUp = pickedUp;
	}

	public void setPosted(boolean posted) {
		this.posted = posted;
	}

	public void setResponse(SimCorMsg response) {
		this.response = response;
	}

	@Override
	public String toString() {
		String result = "/state=" + state + "/dir=" + direction + "/pickedUp="
				+ pickedUp + "/posted=" + posted + "\n";
		result += "/transId=" + id + "/error=" + error + "\n";
		result += "/command=" + command;
		result += "/response=" + response;
		result += "/timeout=" + getTimeout();
		return result;
	}
}
