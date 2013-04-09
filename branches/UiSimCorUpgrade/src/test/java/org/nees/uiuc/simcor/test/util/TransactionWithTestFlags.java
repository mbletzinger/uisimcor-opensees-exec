package org.nees.uiuc.simcor.test.util;

import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class TransactionWithTestFlags extends SimpleTransaction {
	private boolean commandExiists = false;
	private boolean directionExists = false;
	private boolean errorExists = false;
	private boolean idExists = false;
	private boolean timeoutExists = false;

	public boolean isCommandExiists() {
		return commandExiists;
	}

	public boolean isDirectionExists() {
		return directionExists;
	}

	public boolean isErrorExists() {
		return errorExists;
	}

	public boolean isIdExists() {
		return idExists;
	}

	public boolean isTimeoutExists() {
		return timeoutExists;
	}

	@Override
	public void setCommand(SimCorMsg command) {
		commandExiists = true;
		super.setCommand(command);
	}

	@Override
	public void setDirection(DirectionType direction) {
		directionExists = true;
		super.setDirection(direction);
	}

	@Override
	public void setError(TcpError error) {
		errorExists = true;
		super.setError(error);
	}

	@Override
	public void setId(TransactionIdentity id) {
		idExists = true;
		super.setId(id);
	}

	@Override
	public void setTimeout(int timeout) {
		timeoutExists = true;
		super.setTimeout(timeout);
	}

}
