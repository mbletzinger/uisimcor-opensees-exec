package org.nees.uiuc.simcor.matlab;

import java.util.ArrayList;
import java.util.List;

public class StringListUtils {
	public List<String> a2sl(String [] a) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < a.length; i++) {
			result.add(a[i]);
		}
		return result;
	}

	public String Byte2HexString(byte [] buf) {
		String str = "";
		for (int i = 0; i < buf.length; i++) {
			String prefix = ((buf[i] < (byte)16) ? "0":"");
			str += prefix + Integer.toHexString(buf[i]);
		}
		return str;
	}
	public String [] sl2a(List<String> l) {
		return l.toArray(new String [0]);
	}
}
