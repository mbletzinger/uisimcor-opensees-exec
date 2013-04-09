package org.nees.uiuc.simcor.test.util;

import java.util.Date;

import org.nees.uiuc.simcor.transaction.Address;
import org.nees.uiuc.simcor.transaction.SimCorMsg;

public class SimCorMsgWithTestFlags extends SimCorMsg {

	private boolean addressExists = false;
	private boolean commandExists = false;
	private boolean contentExists = false;
	private boolean timestampExists = false;
	private boolean typeExists = false;

	public boolean isAddressExists() {
		return addressExists;
	}

	public boolean isCommandExists() {
		return commandExists;
	}

	public boolean isContentExists() {
		return contentExists;
	}

	public boolean isTimestampExists() {
		return timestampExists;
	}

	public boolean isTypeExists() {
		return typeExists;
	}

	@Override
	public void setAddress(Address address) {
		addressExists = true;
		super.setAddress(address);
	}

	@Override
	public void setCommand(String cmd) {
		commandExists = true;
		super.setCommand(cmd);
	}

	@Override
	public void setContent(String text) {
		contentExists = true;
		super.setContent(text);
	}

	@Override
	public void setTimestamp(Date timestamp) {
		timestampExists = true;
		super.setTimestamp(timestamp);
	}

	@Override
	public void setType(MsgType type) {
		typeExists = true;
		super.setType(type);
	}

}
