package org.nees.uiuc.simcor.tcp;

public class TcpError {

	public enum TcpErrorTypes {
		BROADCAST_CLIENTS_ADDED, BROADCAST_CLIENTS_LOST, IO_ERROR, NONE, THREAD_DIED, TIMEOUT, UNKNOWN_REMOTE_HOST,
	}

	private String remoteHost;
	private String text = "";

	private TcpErrorTypes type = TcpErrorTypes.NONE;

	public TcpError() {
		super();
	}

	public TcpError(TcpError e) {
		type = e.type;
		text = new String(e.text);
	}

	public void clearError() {
		type = TcpErrorTypes.NONE;
		text = "";
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public String getText() {
		return text;
	}

	public TcpErrorTypes getType() {
		return type;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public void setText(String errorMsg) {
		this.text = errorMsg;
	}

	public void setType(TcpErrorTypes error) {
		this.type = error;
	}

	public boolean errorsExist() {
		return (getType().equals(TcpErrorTypes.NONE) == false);
	}

	public boolean isClientsAddedMsg() {
		return (getType().equals(TcpErrorTypes.BROADCAST_CLIENTS_ADDED));
	}

	@Override
	public String toString() {
		String result = "/type=" + type;
		if (text != null) {
			result += "/msg=" + text;
		}
		return result;
	}

}
