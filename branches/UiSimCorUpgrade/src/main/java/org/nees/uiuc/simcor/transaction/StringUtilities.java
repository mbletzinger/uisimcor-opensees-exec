package org.nees.uiuc.simcor.transaction;

import java.util.ArrayList;
import java.util.List;

public class StringUtilities {

	static List<String> array2List(String [] array) {
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < array.length; i++) {
			result.add(array[i]);
		}
		return result;
	}
	static public boolean equals(String a, String b) {
		if (a == null && b != null) {
			return false;
		}
		if (b == null && a != null) {
			return false;
		}
		if (a == null && b == null) {
			return true;
		}
		return a.equals(b);
	}
	static public int hashcode(String a) {
		if(a == null) {
			return 0;
		}
		return a.hashCode();
	}
}
