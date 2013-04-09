package org.nees.uiuc.simcor.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;

public class TcpListen {
	private TcpError error = new TcpError();
	private final Logger log = Logger.getLogger(TcpListen.class);
	private TcpParameters params;
	private ServerSocket server = null;

	public TcpListen() {
		super();
	}

	public TcpListen(TcpParameters p) {
		super();
		this.params = p;
	}

	public TcpError getError() {
		return error;
	}

	public TcpParameters getParams() {
		return params;
	}

	public TcpLinkDto listen() {
		Socket clientSocket = null;
		try {
			clientSocket = server.accept();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				log.debug("socket accept timed out");
				return null;
			}
			log.error("Accept " + ":" + params.getLocalPort()
					+ " failed because", e);
			String msg = "Host " + ":" + params.getLocalPort()
					+ " returned an I/O error";
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return null;
		}
		TcpLinkDto clientConnection = new TcpLinkDto();
		clientConnection.setSocket(clientSocket);
		clientConnection.extractRemoteHost();
		log.info("Accepting connection from "
				+ clientConnection.getRemoteHost());
		return clientConnection;
	}

	public void setError(TcpError error) {
		this.error = error;
	}

	public void setParams(TcpParameters params) throws Exception {
		this.params = params;
	}

	public boolean startListening() {
		try {
			server = new ServerSocket();
			server.setSoTimeout(params.getTcpTimeout());
			server.setReuseAddress(true);
			server.bind(new InetSocketAddress(params.getLocalPort()));
		} catch (IOException e) {
			log.error("Listening at " + ":" + params.getLocalPort()
					+ " failed because", e);
			String msg = "Listen " + ":" + params.getLocalPort()
					+ " returned an I/O error";
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return false;
		}
		return true;
	}

	public boolean stopListening() {
		if (server == null) {
			return true;
		}
		try {
			server.close();
		} catch (IOException e) {
			log.error("Listener close " + ":" + params.getLocalPort()
					+ " failed because", e);
			String msg = "Host " + ":" + params.getLocalPort()
					+ " returned an I/O error";
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
		}
		log.info("Listener signing off");
		server = null;
		return true;
	}

}
