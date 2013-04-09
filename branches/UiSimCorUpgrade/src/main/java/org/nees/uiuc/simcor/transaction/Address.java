package org.nees.uiuc.simcor.transaction;

public class Address {

	private String controlPoint;

	private String suffix;

	public Address() {
	}

	public Address(Address a) {
		controlPoint = new String(a.controlPoint);
		if(a.suffix != null) {
			suffix = new String(a.suffix);
		}
	}

	public Address(String address) {
		int r = address.indexOf("\r");
		if (r > 0) {
			address = address.substring(0,r);
		}
		int n = address.indexOf("\n");
		if (n > 0) {
			address = address.substring(0,n);
		}
		int i = address.indexOf(":");
		if (i >= 0) {
			controlPoint = address.substring(0,i);
			suffix = address.substring(i+1);
		} else {
			controlPoint = address;
		}
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Address == false) {
			return false;
		}
		Address addr = (Address) obj;
		return StringUtilities.equals(controlPoint, addr.controlPoint)
				&& StringUtilities.equals(suffix, addr.suffix);
	}
	public String getControlPoint() {
		return controlPoint;
	}

	public synchronized String getSuffix() {
		return suffix;
	}

	@Override
	public int hashCode() {
		return StringUtilities.hashcode(controlPoint)
				+ StringUtilities.hashcode(suffix);
	}

	public void setControlPoint(String address) {
		this.controlPoint = address;
	}

	public synchronized void setSuffix(String cps) {
		this.suffix = cps;
	}
	
	@Override
	public String toString() {
		String result = controlPoint;
		if(suffix != null) {
			result += ":" + suffix;
		}
		return result;
	}
}
