package org.nees.uiuc.simcor.matlab;

import java.util.HashMap;
import java.util.Iterator;

public class HashTable extends HashMap<String, Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public void put(String key, double value) {
		super.put(key, new Double(value));
	}
	public double get(String key) {
		return super.get(key).doubleValue();
	}
	public String [] keys() {
		String [] result = new String[super.keySet().size()];
		int i = 0;
		for ( Iterator<String> it = super.keySet().iterator(); it.hasNext();) {
			String k = it.next();
			result[i] = k;
			i++;
		}
			return result;
	}
	
	public boolean exists(String key) {
		return super.containsKey(key);
	}
	
	public double [] vals() {
		Double [] raw = (Double [])super.values().toArray();
		double [] result = new double[raw.length];
		for(int i = 0; i < raw.length; i++) {
			result[i] = raw[i].doubleValue();
		}
		return result;
	}
}
