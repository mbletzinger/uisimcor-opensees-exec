package org.nees.uiuc.simcor.matlab.test;

import java.util.ArrayList;
import java.util.List;

public class MatlabTester {

	private List<String> stringData = new ArrayList<String>();

	public MatlabTester() {
		stringData.add("Test1");
		stringData.add("Test2");
		stringData.add("Another Test1");
		stringData.add("Test1234");
		stringData.add("Dumpy on test 1");
	}

	public int getLength() {
		return stringData.size();
	}
	public String getString(int i) {
		return stringData.get(i);
	}
	
	public String [] getStringArrayData() {
		return stringData.toArray(new String[0]);
	}

	public List<String> getStringData() {
		return stringData;
	}
	public void setString(String s, int i) {
		stringData.set(i, s);
	}
	
	public void setStringArray(List<String> array) {
		stringData = array;
	}
}
