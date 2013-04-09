package org.nees.uiuc.simcor.listener;

public class ClientId {

	public final String remoteHost;
	public final String system;

	public ClientId(ClientId other) {
		this.system = other.system;
		this.remoteHost = other.remoteHost;
		
	}

	public ClientId(String system,
			String remoteHost) {
		this.system = system;
		this.remoteHost = remoteHost;
	}
	@Override
	public int hashCode() {
		int result = 0;
		if(system != null) {
			result += system.hashCode();
		}
		if(remoteHost != null) {
			result += remoteHost.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		String result = "";
		if(system != null) {
			result += system;
		} else {
			result += "null";
		}
		if(remoteHost != null) {
			result += " at " + remoteHost;
		} else {
			result += " at null";
		}
		return result;
	}

}