package org.nees.uiuc.simcor.transaction;

import java.util.HashSet;
import java.util.Set;

public class BroadcastTransaction extends Transaction {
	private String broadcastMsg = null;
	private String responseMsg = null;
	private final Set<TriggerResponse> responses = new HashSet<TriggerResponse>();

	public void addResponse(TriggerResponse msg) {
		responses.add(msg);
	}

	public void clearResponses() {
		responses.clear();
	}

	public synchronized String getBroadcastMsg() {
		return broadcastMsg;
	}

	public synchronized String getResponseMsg() {
		return responseMsg;
	}

	public Set<TriggerResponse> getResponses() {
		return responses;
	}

	public synchronized void setBroadcastMsg(String broadcastMsg) {
		this.broadcastMsg = broadcastMsg;
	}

	public synchronized void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	@Override
	public String toString() {
		String result = "/state=" + state + "/dir=" + direction + "\n";
		result += "/transId=" + id + "/error=" + error + "/bmsg="
				+ getBroadcastMsg() + "/rmsg=" + getResponseMsg() + "\n";
		result += "/command=" + command;
		for (TriggerResponse tr : getResponses()) {
			result += "/response=" + tr;
		}
		result += "/timeout=" + getTimeout();
		return result;
	}
}
