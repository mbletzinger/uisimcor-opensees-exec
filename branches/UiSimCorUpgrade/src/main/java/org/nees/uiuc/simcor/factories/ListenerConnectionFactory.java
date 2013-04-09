package org.nees.uiuc.simcor.factories;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpLinkDto;
import org.nees.uiuc.simcor.tcp.TcpListen;
import org.nees.uiuc.simcor.tcp.TcpParameters;

public class ListenerConnectionFactory {

	private TcpListen listener;
	private final Logger log = Logger.getLogger(ListenerConnectionFactory.class);
	private TcpParameters params;
	public TcpError checkForErrors() {
		TcpError result = new TcpError();
		if (listener != null) {
			result = listener.getError();
		}
		return result;
	}
	public TcpError checkForErrors(Connection c) {
		return c.getFromRemoteMsg().getError();
	}
	public Connection checkForListenerConnection() {
		TcpLinkDto client = listener.listen();
		if (client == null) {
			return null;
		}
		Connection c = new Connection(client, params);
		try {
			c.start();
			Thread.sleep(200); // Give the connection time to start
		} catch (Exception e) {
			log.error("Check for Listener Connection " + c.getRemoteHost()
					+ " failed", e);
		}
		return c;
	}

	public TcpListen getListener() {
		return listener;
	}

	public TcpParameters getParams() {
		return params;
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}

	public void startListener() {
		listener = new TcpListen();
		try {
			listener.setParams(params);
			listener.startListening();
		} catch (Exception e) {
			log.error("Start Listener failed",e);
		}
	}

	public boolean stopListener() {
		return listener.stopListening();
	}
}
