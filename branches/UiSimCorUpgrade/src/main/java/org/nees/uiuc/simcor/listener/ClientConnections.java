package org.nees.uiuc.simcor.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.TriggerResponse;

public class ClientConnections {
	private final List<ClientIdWithConnection> clients = new ArrayList<ClientIdWithConnection>();
	private final Logger log = Logger.getLogger(ClientConnections.class);
	private int msgTimeout = 3000;
	private final List<ClientIdWithConnection> newClients = new ArrayList<ClientIdWithConnection>();
	private final List<ClientIdWithConnection> waitingForResponse = new ArrayList<ClientIdWithConnection>();

	public ClientConnections() {
	}

	public synchronized void addClient(ClientIdWithConnection client) {
		if(client == null) {
			log.error("Null client received");
		}
		newClients.add(client);
	}

	public synchronized void assembleTriggerMessages(
			BroadcastTransaction transaction) {
		if(transaction == null) {
			log.error("null transaction received");
			return;
		}
		mergeClients(transaction);
		setMsgTimeout(transaction.getTimeout());

		for (ClientIdWithConnection c : clients) {
			if(c == null) {
				log.error("Null client received");
				continue;
			}
			sendMsg(c.connection, transaction.getCommand(), transaction.getId());
		}
	}

	private TriggerResponse checkResponse(ClientIdWithConnection cid) {
		Connection client = cid.connection;
		if (client.isBusy()) {
			return null;
		}
		TriggerResponse result = new TriggerResponse(client.getFromRemoteMsg()
				.getMsg());
		result.setRemoteId(new ClientId(cid));
		result.setError(client.getFromRemoteMsg().getError());
		return result;
	}

	private void closeClient(Connection client) {
		TcpActionsDto cmd = new TcpActionsDto();
		cmd.setAction(ActionsType.CLOSE);
		client.setToRemoteMsg(cmd);
	}

	public int getMsgTimeout() {
		return msgTimeout;
	}

	private void mergeClients(BroadcastTransaction transaction) {
		clients.addAll(newClients);
		checkForNullClients();
		String message = null;
		for (ClientId c : newClients) {
			if (message == null) {
				message = "";
			}
			if(c == null) {
				log.error("Null connection received");
				continue;
			}
			if(c.system == null) {
				log.error("Null system for new connection" );
				continue;
			}
			if(c.remoteHost == null) {
				log.error("Null remote host for new connection" );
				continue;
			}
			message += c.system + " at " + c.remoteHost + " is connected.\n";
		}
		if (message != null) {
			transaction.setBroadcastMsg(message);
			TcpError err = new TcpError();
			err.setType(TcpErrorTypes.BROADCAST_CLIENTS_ADDED);
			err.setText(message);
			transaction.setError(err);
		}
		newClients.clear();
	}

	private void readMsg(Connection c) {
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		c.setMsgTimeout(msgTimeout);
		c.setToRemoteMsg(action);
	}

	private boolean closeConnection(Connection c) {
		if (c == null) {
			return true;
		}
		if (c.getState().equals(ConnectionStatus.BUSY) == false && c.isAlive()) {
			TcpActionsDto cmd = new TcpActionsDto();
			cmd.setAction(ActionsType.CLOSE);
			c.setToRemoteMsg(cmd);
		}
		return c.getConnectionStatus().equals(ConnectionStatus.CLOSED)
				|| (c.isAlive() == false);
	}

	private void sendMsg(Connection client, SimCorMsg msg,
			TransactionIdentity id) {
		client.setMsgTimeout(msgTimeout);
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.WRITE);
		Msg2Tcp m2t = action.getMsg();
		m2t.setId(id);
		m2t.setMsg(msg);
		client.setToRemoteMsg(action);
	}

	public void setMsgTimeout(int msgTimeout) {
		this.msgTimeout = msgTimeout;
	}

	public synchronized void setupResponsesCheck(
			BroadcastTransaction transaction) {
		for (ClientIdWithConnection c : clients) {
			readMsg(c.connection);
		}
		waitingForResponse.addAll(clients);
		transaction.getResponses().clear();
	}

	public synchronized boolean closeClientConnections() {
		boolean allClosed = true;
		for (ClientIdWithConnection c : clients) {
			allClosed = closeConnection(c.connection) && allClosed;
		}
		return allClosed;
	}

	public synchronized boolean waitForBroadcastFinished() {
		for (ClientIdWithConnection c : clients) {
			if (c.connection.isBusy()) {
				return false;
			}
		}
		return true;
	}

	public synchronized boolean waitForResponsesFinished(
			BroadcastTransaction transaction) {
		boolean result = true;
		String message = transaction.getResponseMsg();
		List<ClientIdWithConnection> lostClients = new ArrayList<ClientIdWithConnection>();
		List<ClientIdWithConnection> responseReceived = new ArrayList<ClientIdWithConnection>();
		for (ClientIdWithConnection c : waitingForResponse) {
			log.debug("Checking Client: " + c.system);
			TriggerResponse rsp = checkResponse(c);
			if (rsp == null) {
				result = false;
				continue;
			}
			if (rsp.getError().errorsExist()) {
				if (message == null) {
					message = "";
				}
				message += "Lost contact with " + c.system + " at "
						+ c.remoteHost + " because " + rsp.getError().getText()
						+ "\n";
				transaction.setResponseMsg(message);
				TcpError err = new TcpError();
				err.setType(TcpErrorTypes.BROADCAST_CLIENTS_LOST);
				err.setText(message);
				transaction.setError(err);
				lostClients.add(c);
				closeClient(c.connection);
			} else {
				transaction.getResponses().add(rsp);
			}
			log.debug("Response received from " + c.system);
			responseReceived.add(c);
		}

		for (ClientIdWithConnection i : lostClients) {
			clients.remove(i);
		}
		for (ClientIdWithConnection i : responseReceived) {
			waitingForResponse.remove(i);
		}
		return result;
	}
	private void checkForNullClients() {
		clients.removeAll(Collections.singleton(null));
	}
}
