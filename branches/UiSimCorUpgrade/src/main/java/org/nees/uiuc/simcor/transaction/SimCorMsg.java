package org.nees.uiuc.simcor.transaction;

import java.util.Date;

import org.apache.log4j.Logger;

public class SimCorMsg {
	public enum MsgType {
		COMMAND, NULL, NOT_OK_RESPONSE, OK_RESPONSE
	}

	// address is null if not used
	private Address address = null;

	private String command = null;
	private String content = null;
	private final Logger log = Logger.getLogger(SimCorMsg.class);
	private Date timestamp;
	private MsgType type = MsgType.NULL;

	public SimCorMsg() {
		// clearMessage();
	}

	public SimCorMsg(SimCorMsg m) {
		if (m.address != null) {
			address = new Address(m.address);
		}
		if (m.command != null) {
			command = new String(m.command);
		}
		if (m.content != null) {
			content = new String(m.content);
		}
		type = m.type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimCorMsg == false) {
			return false;
		}
		// private String command;
		// private String content;
		// private MsgType type;
		SimCorMsg msg = (SimCorMsg) obj;
		log.debug("Comparing [" + this + "] with [" + msg + "]");
		if (address == null && msg.address != null) {
			return false;
		}
		if (address == null && msg.address == null) {
			return StringUtilities.equals(command, msg.command)
					&& StringUtilities.equals(content, msg.content)
					&& type.equals(msg.type);

		}
		return address.equals(msg.address)
				&& StringUtilities.equals(command, msg.command)
				&& StringUtilities.equals(content, msg.content)
				&& type.equals(msg.type);
	}

	public Address getAddress() {
		return address;
	}

	public String getCommand() {
		return command;
	}

	public String getContent() {
		return content;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public MsgType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (getAddress() != null) {
			result += getAddress().hashCode();
		}
		if (command != null) {
			result += command.hashCode();
		}
		if (getContent() != null) {
			result += getContent().hashCode();
		}
		result += type.hashCode();
		// log.debug("Message: " + this + " has hash " + result);
		return result;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setCommand(String cmd) {
		this.command = cmd;
		type = MsgType.COMMAND;
	}

	public void setContent(String text) {
		content = text;

	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(MsgType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		String result = "";
		if (type == MsgType.COMMAND) {
			result += "/command=" + (command != null ? command : "null");
		} else {
			result += "/response=" + type;
		}
		result += "/address=" + (address != null ? address : "null");
		result += "/content=" + (content != null ? content : "null");
		return result;
	}
}
