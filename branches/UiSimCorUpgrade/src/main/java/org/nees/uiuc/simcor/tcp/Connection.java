package org.nees.uiuc.simcor.tcp;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.ReadTcpAction.TcpReadStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;

public class Connection extends Thread {
	public enum ConnectionStatus {
		BUSY, READING, CLOSED, IN_ERROR, READY
	};

	private final OpenCloseTcpAction openCloseAction;
	private WriteTcpAction writer;
	private ReadTcpAction reader;
	private volatile ConnectionStatus connectionStatus = ConnectionStatus.CLOSED;
	private volatile TcpActionsDto fromRemoteMsg = new TcpActionsDto();
	private volatile TcpActionsDto toRemoteMsg = new TcpActionsDto();
	private final Logger log = Logger.getLogger(Connection.class);
	private TcpLinkDto link;
	private int logCounter = 0;
	private ActionsType logStatus;

	private int msgTimeout = 3000;

	private String remoteHost;

	private boolean running = false;

	public Connection(TcpLinkDto link, TcpParameters parameters) {
		super();
		writer = new WriteTcpAction(parameters.isLfcrSendEom(), link);
		reader = new ReadTcpAction(parameters.isLfcrSendEom(),link);
		openCloseAction = new OpenCloseTcpAction(link, parameters);
		remoteHost = link.getRemoteHost();
		this.link = link;
		setConnectionStatus(ConnectionStatus.BUSY);
	}

	public Connection(TcpParameters parameters) {
		openCloseAction = new OpenCloseTcpAction(parameters);
	}

	public synchronized ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public synchronized TcpActionsDto getFromRemoteMsg() {
		log.debug("Returning inMsg[" + fromRemoteMsg + "]");
		return new TcpActionsDto(fromRemoteMsg);
	}

	public synchronized TcpLinkDto getLink() {
		return link;
	}

	public synchronized int getMsgTimeout() {
		return msgTimeout;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public synchronized TcpActionsDto getToRemoteMsg() {
		if (getConnectionStatus() == ConnectionStatus.READY) {
			try {
				log.debug("I'm WAAAAIITING");
				wait();
			} catch (InterruptedException e1) {
			}
		}
		// log.debug("Returning outMsg[" + toRemoteMsg + "]");
		TcpActionsDto result = new TcpActionsDto(toRemoteMsg);
		toRemoteMsg.setAction(ActionsType.NONE);
		return result;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public void run() {
		running = true;
		setConnectionStatus(ConnectionStatus.BUSY);
		while (running) {
			ConnectionStatus state = getConnectionStatus();
			TcpActionsDto outM = getToRemoteMsg();
			if (outM.getAction().equals(ActionsType.EXIT)) {
				TcpActionsDto inM = new TcpActionsDto();
				openCloseAction.close();
				inM.setError(openCloseAction.getError());
				setFromRemoteMsg(inM, ActionsType.EXIT);
				running = false;
				continue;
			}
			if (state.equals(ConnectionStatus.READING)) {
				try {
					Thread.sleep(10); // Give the socket time to catch up
				} catch (InterruptedException e) {
				}
				if (doILog(ActionsType.READ)) {
					log.debug("Continue a read");
				}
				state = readAction();
				setConnectionStatus(state);
				continue;
			} else {
				state = startAction(outM);
			}
			if (state.equals(ConnectionStatus.CLOSED)) {
				running = false;
			}
			setConnectionStatus(state);
		}
		setConnectionStatus(ConnectionStatus.CLOSED);
		log.info("Goodbye");
	}

	public synchronized void setConnectionStatus(ConnectionStatus busy) {
		this.connectionStatus = busy;
	}

	public synchronized void setFromRemoteMsg(TcpActionsDto inMsg,
			ActionsType act) {
		log.debug("Setting inMsg[" + inMsg + "]");
		this.fromRemoteMsg = inMsg;
		if (inMsg.getError().getType() != TcpErrorTypes.NONE) {
			fromRemoteMsg.getError().setRemoteHost(remoteHost);
		}
	}

	public synchronized void setMsgTimeout(int msgTimeout) {
		this.msgTimeout = msgTimeout;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public boolean isBusyOrErrored() {
		ConnectionStatus status = getConnectionStatus();
		if (status.equals(ConnectionStatus.BUSY)) {
			return true;
		}
		if (status.equals(ConnectionStatus.READING)) {
			return true;
		}
		if (status.equals(ConnectionStatus.IN_ERROR)) {
			return true;
		}
		return false;
	}
	public boolean isBusy() {
		ConnectionStatus status = getConnectionStatus();
		if (status.equals(ConnectionStatus.BUSY)) {
			return true;
		}
		if (status.equals(ConnectionStatus.READING)) {
			return true;
		}
		return false;
	}

	public synchronized void setToRemoteMsg(TcpActionsDto outMsg) {
		log.debug("Setting outMsg[" + outMsg + "]");
		this.toRemoteMsg = new TcpActionsDto(outMsg);
		setConnectionStatus(ConnectionStatus.BUSY);
		notifyAll();
	}

	private ConnectionStatus startAction(TcpActionsDto outM) {
		// CONNECT, CLOSE, EXIT,READ,WRITE,IDLE
		ActionsType act = outM.getAction();
		TcpActionsDto inM = new TcpActionsDto();
		if (doILog(act)) {
			log.debug("Executing a " + act);
		}
		if (act.equals(ActionsType.CONNECT)) {
			boolean result = openCloseAction.connect();
			inM.setError(openCloseAction.getError());
			setFromRemoteMsg(inM, act);
			if (result == false) {
				return ConnectionStatus.CLOSED;
			}
			reader = new ReadTcpAction(openCloseAction.getParameters()
					.isLfcrSendEom(), openCloseAction.getLink());
			writer = new WriteTcpAction(openCloseAction.getParameters()
					.isLfcrSendEom(), openCloseAction.getLink());
			return ConnectionStatus.READY;
		}

		if (act.equals(ActionsType.CLOSE)) {
			openCloseAction.close();
			inM.setError(openCloseAction.getError());
			setFromRemoteMsg(inM, act);
			return ConnectionStatus.CLOSED;
		}
		if (act.equals(ActionsType.READ)) {
			return readAction();
		}

		if (act.equals(ActionsType.WRITE)) {
			writer.write(outM.getMsg());
			inM.setAction(ActionsType.NONE);
			inM.setError(writer.getError());
			inM.timestamp();
			setFromRemoteMsg(inM, act);
			return ConnectionStatus.READY;
		}
		return ConnectionStatus.READY;
	}

	private ConnectionStatus readAction() {
		TcpActionsDto inM = new TcpActionsDto();
		reader.setMsgTimeout(getMsgTimeout());
		TcpReadStatus status = reader.readMessage();
		inM.setAction(ActionsType.NONE);
		if (status.equals(TcpReadStatus.DONE)) {
			inM.timestamp();
			inM.setMsg(reader.getMessage());
			inM.setError(reader.getError());
			setFromRemoteMsg(inM, ActionsType.READ);
			return ConnectionStatus.READY;
		}
		if (status.equals(TcpReadStatus.ERRORED)) {
			inM.setMsg(new Msg2Tcp());
			inM.setError(reader.getError());
			setFromRemoteMsg(inM, ActionsType.READ);
			return ConnectionStatus.IN_ERROR;
		}
		return ConnectionStatus.READING;
	}

	private boolean doILog(ActionsType action) {
		if (action.equals(logStatus) == false) {
			logStatus = action;
			return true;
		}

		if (logCounter == 200) {
			logCounter = 0;
			return true;
		}
		logCounter++;
		return false;
	}
}
