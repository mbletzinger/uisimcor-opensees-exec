package org.nees.uiuc.simcor.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

public class Props extends Properties {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8964482783061808511L;
	Logger log = Logger.getLogger(Props.class);

	public List<String> getPropertyList(String key) {
		List<String> result = new ArrayList<String>();
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		String[] tokens = value.split(",");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("null")) {
				result.add(null);
				continue;
			}
			result.add(tokens[i]);
		}
		return result;
	}

	@Override
	public synchronized Enumeration<Object> keys() {
		Enumeration<Object> keysEnum = super.keys();
		Vector<Object> keyList = new Vector<Object>();
		while (keysEnum.hasMoreElements()) {
			keyList.add((String) keysEnum.nextElement());
		}
		return keyList.elements();

	}

	public String load(String file) {
		File fileF = new File(file);
		if (fileF.exists() == false) {
			String msg = "[" + file + "] does not exist";
			log.error(msg);
			return msg;
		}
		if (fileF.canRead() == false) {
			String msg = "[" + file + "] cannot be read";
			log.error(msg);
			return msg;
		}
		FileInputStream reader = null;
		try {
			reader = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			String msg = "[" + file + "] does not exist";
			log.error(msg);
			return msg;
		}
		try {
			super.load(reader);
		} catch (IOException e) {
			String msg = "[" + file + "] cannot be read";
			log.error(msg);
			return msg;
		}
		return null;
	}

	public String save(String file) {

		FileOutputStream writer;
		try {
			writer = new FileOutputStream(file);
			super.store(writer, null);
		} catch (IOException e) {
			String msg = "[" + file + "] cannot be written";
			log.error(msg);
			return msg;
		}
		return null;
	}

	public void setPropertyList(String key, List<String> val) {
		String value = propertyList2String(val);
		setProperty(key, value);
	}

	public String propertyList2String(List<String> val) {
		String value = val.get(0);
		for (int i = 1; i < val.size(); i++) {
			value += "," + val.get(i);
		}
		return value;
	}
}
