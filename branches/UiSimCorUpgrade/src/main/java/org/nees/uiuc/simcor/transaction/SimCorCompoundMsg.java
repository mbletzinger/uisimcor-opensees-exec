package org.nees.uiuc.simcor.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class SimCorCompoundMsg extends SimCorMsg {
	private List<Address> addresses = new ArrayList<Address>();

	private Map<Address, String> contents = new HashMap<Address, String>();

//	@Override
//	public int hashCode() {
//		int result = super.hashCode();
//		for (Iterator<Address> i = addresses.iterator(); i.hasNext();) {
//			Address a = i.next();
//			result += a.hashCode();
//			result += contents.get(a).hashCode();
//		}
//		log.debug("Message: " + this + " has hash " + result);
//		return result;
//	}

	private final Logger log = Logger.getLogger(SimCorCompoundMsg.class);
	public SimCorCompoundMsg() {
	}

	public SimCorCompoundMsg(SimCorCompoundMsg m) {
		super(m);
		for (Iterator<Address> i = m.addresses.iterator(); i.hasNext();) {
			Address a = i.next();
			setContent(a, m.contents.get(a));
		}
	}

	public void clearContents() {
		contents.clear();
		addresses.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimCorCompoundMsg == false) {
			return false;
		}
		if (super.equals(obj) == false) {
			return false;
		}
		SimCorCompoundMsg msg = (SimCorCompoundMsg) obj;
		log.debug("Comparing [" + this + "] with [" + msg + "]");
		if (addresses.size() != msg.addresses.size()) {
			return false;
		}
		for (int i = 0; i < addresses.size(); i++) {
			if (addresses.get(i).equals(msg.addresses.get(i)) == false) {
				return false;
			}
			if (contents.get(addresses.get(i)).equals(
					msg.contents.get(msg.addresses.get(i))) == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Address getAddress() {
		return addresses.get(0);
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	@Override
	public String getContent() {
		return contents.get(addresses.get(0));
	}

	public String getContent(Address addr) {
		return contents.get(addr);
	}

	public Map<Address, String> getContents() {
		return contents;
	}

	public void removeContent(Address addr) {
		contents.remove(addr);
		addresses.remove(addr);
	}

	public void setContent(Address addr, String cnt) {
		contents.put(addr, cnt);
		addresses.add(addr);
	}

	@Override
	public String toString() {
		String result = getType().toString();
		if (getType() == MsgType.COMMAND) {
			result += "/command="
					+ (getCommand() != null ? getCommand() : "null");
		}
		for (Iterator<Address> i = addresses.iterator(); i.hasNext();) {
			Address a = i.next();
			result += "/address=" + a;
			result += "/content=" + contents.get(a);
		}
		return result;
	}
}
