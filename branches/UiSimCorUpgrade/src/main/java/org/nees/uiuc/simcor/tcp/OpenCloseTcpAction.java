package org.nees.uiuc.simcor.tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;

public class OpenCloseTcpAction {
	private TcpError error;

	private TcpLinkDto link;

	private final Logger log = Logger.getLogger(OpenCloseTcpAction.class);

	private final TcpParameters parameters;

	public OpenCloseTcpAction(TcpLinkDto link, TcpParameters parameters) {
		super();
		this.link = link;
		this.parameters = parameters;
	}

	public OpenCloseTcpAction(TcpParameters parameters) {
		super();
		this.parameters = parameters;
	}

	public boolean close() {
		if (link == null || link.getSocket() == null) {
			error = new TcpError();
			error.setText("Connection does not exist");
			error.setType(TcpErrorTypes.UNKNOWN_REMOTE_HOST);
			log.error(error.getText());
			return false;
		}
		java.net.Socket socket = link.getSocket();
		String remoteHost = link.getRemoteHost();
		try {
			socket.close();
		} catch (IOException e) {
			String msg = "Closing connection to " + remoteHost + " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return false;
		}
		error = new TcpError();
		error.setType(TcpErrorTypes.NONE);
		return true;
	}
	
	public boolean connect() {
		Socket socket = null;
		try {
			socket = new Socket(parameters.getRemoteHost(), parameters
					.getRemotePort());
		} catch (UnknownHostException e) {
			String emsg = "Unknown host " + parameters.getRemoteHost() + ":"
					+ parameters.getRemotePort();
			log.error(emsg, e);
			error = new TcpError();
			error.setText(emsg);
			error.setType(TcpErrorTypes.UNKNOWN_REMOTE_HOST);
			return false;

		} catch (IOException e) {
			String emsg = "Host " + parameters.getRemoteHost() + ":"
					+ parameters.getRemotePort() + " returned an I/O error";
			log.error(emsg, e);
			error = new TcpError();
			error.setText(emsg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return false;
		}
		link = new TcpLinkDto();
		link.setSocket(socket);
		link.setRemoteHost(parameters.getRemoteHost());
		log.debug("Connect to Host " + parameters.getRemoteHost() + ":"
				+ parameters.getRemotePort() + " successful");
		error = new TcpError();
		error.setType(TcpErrorTypes.NONE);
		return true;
	}
	
	/**
	 * @return the error
	 */
	public TcpError getError() {
		return error;
	}
	
	/**
	 * @return the link
	 */
	public TcpLinkDto getLink() {
		return link;
	}

	/**
	 * @return the parameters
	 */
	public TcpParameters getParameters() {
		return parameters;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(TcpError error) {
		this.error = error;
	}
	
	/**
	 * @param link the link to set
	 */
	public void setLink(TcpLinkDto link) {
		this.link = link;
	}

}
