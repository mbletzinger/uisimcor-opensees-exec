package org.nees.uiuc.simcor.transaction;

import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError;

public class Transaction {

	public enum DirectionType {
		NONE, RECEIVE_COMMAND, SEND_COMMAND
	}

	protected SimCorMsg command = null;
	protected DirectionType direction = DirectionType.NONE;
	protected TcpError error = new TcpError();
	protected TransactionIdentity id;
	protected TransactionStateNames state = TransactionStateNames.READY;
	private int timeout = 3000;
	public Transaction() {
		super();
	}
	public Transaction(Transaction t) {
		direction = t.direction;
		if (t.id != null) {
			id = new TransactionIdentity(t.id);
		}
		state = t.state;
		if (t.command != null) {
			if (t.command instanceof SimCorCompoundMsg) {
				command = new SimCorCompoundMsg((SimCorCompoundMsg) t.command);
			} else {
				command = new SimCorMsg(t.command);
			}
		}
	}

	public SimCorMsg getCommand() {
		return command;
	}

	public DirectionType getDirection() {
		return direction;
	}

	public TcpError getError() {
		return error;
	}

	public TransactionIdentity getId() {
		return id;
	}

	public TransactionStateNames getState() {
		return state;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setCommand(SimCorMsg command) {
		this.command = command;
	}

	public void setDirection(DirectionType direction) {
		this.direction = direction;
	}

	public void setError(TcpError error) {
		this.error = error;
	}

	public void setId(TransactionIdentity id) {
		this.id = id;
	}
	public void setState(TransactionStateNames status) {
		this.state = status;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


}