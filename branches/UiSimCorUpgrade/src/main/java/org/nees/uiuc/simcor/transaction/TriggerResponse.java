package org.nees.uiuc.simcor.transaction;

import org.nees.uiuc.simcor.listener.ClientId;
import org.nees.uiuc.simcor.tcp.TcpError;

public class TriggerResponse extends SimCorMsg {

	protected TcpError error;
	private ClientId remoteId;

	public TriggerResponse(Msg2Tcp msg) {
		super(msg.getMsg());
	}

	public TcpError getError() {
		return error;
	}

	public synchronized ClientId getRemoteId() {
		return remoteId;
	}

	public void setError(TcpError error) {
		this.error = error;
	}

	public synchronized void setRemoteId(ClientId cid) {
		this.remoteId = cid;
	}
	@Override
	public String toString() {
		String result = "/client=" + getRemoteId();
		result += super.toString();
		return result;
	}
}
