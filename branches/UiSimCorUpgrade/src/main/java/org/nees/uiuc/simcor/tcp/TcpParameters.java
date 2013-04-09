package org.nees.uiuc.simcor.tcp;

public class TcpParameters {
	private boolean lfcrSendEom = true;
	private int localPort = -1;
	private String remoteHost;
	private int remotePort;
	private int tcpConnectionTimeout = 3000;

	public TcpParameters() {
		super();
	}

	public TcpParameters(String remoteHost, int remotePort, int localPort,
			int tcpReadTimeout, boolean lfcrSendEom) {
		super();
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
		this.tcpConnectionTimeout = tcpReadTimeout;
		this.lfcrSendEom = lfcrSendEom;
	}

	public TcpParameters(TcpParameters orig) {
		this(orig.remoteHost, orig.remotePort, orig.localPort,
				orig.tcpConnectionTimeout, orig.lfcrSendEom);
	}

	public int getLocalPort() {
		return localPort;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public int getTcpTimeout() {
		return tcpConnectionTimeout;
	}

	public boolean isLfcrSendEom() {
		return lfcrSendEom;
	}

	public boolean isListener() {
		return localPort > 0;
	}

	public void setLfcrSendEom(boolean lfcrSendEom) {
		this.lfcrSendEom = lfcrSendEom;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public void setTcpTimeout(int tcpReadTimeout) {
		this.tcpConnectionTimeout = tcpReadTimeout;
	}
}
