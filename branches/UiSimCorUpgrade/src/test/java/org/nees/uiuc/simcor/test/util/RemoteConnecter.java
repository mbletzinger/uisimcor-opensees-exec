package org.nees.uiuc.simcor.test.util;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.Address;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class RemoteConnecter extends Thread {
	public enum RemoteConnectionStatus {
		CONNECTING, READING, STOPPED, WRITING
	}
	private Connection connection;

	private TransactionIdentity id;

	private final Logger log = Logger.getLogger(RemoteConnecter.class);

	private String message;

	private TcpParameters params;

	private boolean running = false;

	private RemoteConnectionStatus status = RemoteConnectionStatus.STOPPED;

	public RemoteConnecter(TcpParameters params) {
		super();
		this.params = params;
	}

	/**
	 * @return the params
	 */
	public synchronized TcpParameters getParams() {
		return params;
	}

	/**
	 * @return the status
	 */
	public synchronized RemoteConnectionStatus getStatus() {
		return status;
	}
	/**
	 * @return the running
	 */
	public synchronized boolean isRunning() {
		return running;
	}
	private RemoteConnectionStatus readMessage() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.READ);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy() && running == true) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				count = 0;
				log.debug("Still waiting on read");
			}
			count++;
		}
		ConnectionStatus stat = connection.getConnectionStatus();
		dto = connection.getFromRemoteMsg();

		if (stat.equals(ConnectionStatus.IN_ERROR)) {
			log.error("Read has an error" + dto.getError());
			return shutdownConnection();
		}
		message = dto.getMsg().assemble();
		id = dto.getMsg().getId();
		return RemoteConnectionStatus.WRITING;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		running = true;
		status = RemoteConnectionStatus.CONNECTING;
		log.debug("Starting");
		while (running == true) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			if (status.equals(RemoteConnectionStatus.CONNECTING)) {
				log.debug("Trying to connect");
				status = connect2Local();
				if (connection == null) {
					continue;
				}
				status = RemoteConnectionStatus.READING;
				continue;
			}
			if(status.equals(RemoteConnectionStatus.READING)) {
				log.debug("Reading");
				status = readMessage();
				continue;
			}
			if(status.equals(RemoteConnectionStatus.WRITING)) {
				log.debug("Writing");
				status = writeMessage();
				continue;
			}
		}
		shutdownConnection();
		log.info("Remote Connection Stopped");
	}
	/**
	 * @param params
	 *            the params to set
	 */
	public synchronized void setParams(TcpParameters params) {
		this.params = params;
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public synchronized void setRunning(boolean running) {
		this.running = running;
	};

	private RemoteConnectionStatus shutdownConnection() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.EXIT);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusyOrErrored()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				count = 0;
				log.debug("Still waiting on disconnect");
			}
			count++;
		}
		return RemoteConnectionStatus.STOPPED;
	}

	private RemoteConnectionStatus connect2Local() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.CONNECT);
		connection = new Connection(params);
		connection.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy() && running == true) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				log.debug("Waiting for local connect");
				count = 0;
			}
			count++;
		}
		if (connection == null) {
			return RemoteConnectionStatus.STOPPED;
		}
		ConnectionStatus stat = connection.getConnectionStatus();

		if (stat.equals(ConnectionStatus.IN_ERROR)) {
			log.error("Connect has an error" + dto.getError());
			return RemoteConnectionStatus.STOPPED;
		}
		message = dto.getMsg().assemble();
		return RemoteConnectionStatus.READING;
	}
	private RemoteConnectionStatus writeMessage() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.WRITE);
		Msg2Tcp m2t = new Msg2Tcp();
		m2t.setId(id);
		SimCorMsg msg = new SimCorMsg();
		msg.setContent("\"" + message + "\" was sent");
		msg.setAddress(new Address("MDL-Response"));
		m2t.setMsg(msg);
		dto.setMsg(m2t);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy() && running == true) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				count = 0;
				log.debug("Still waiting on write");
			}
			count++;
		}
		ConnectionStatus stat = connection.getConnectionStatus();

		if (stat.equals(ConnectionStatus.IN_ERROR)) {
			log.error("Read has an error" + dto.getError());
			return shutdownConnection();
		}
		message = dto.getMsg().assemble();
		return RemoteConnectionStatus.READING;
	}

}
