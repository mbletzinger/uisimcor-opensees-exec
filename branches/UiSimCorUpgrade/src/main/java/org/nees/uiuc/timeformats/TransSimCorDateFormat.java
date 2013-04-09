package org.nees.uiuc.timeformats;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TransSimCorDateFormat extends GenericDataFormat {
	private SimpleDateFormat formatDf = new SimpleDateFormat("'trans'yyyyMMddHHmmss.SSS");

	public TransSimCorDateFormat() {
		super("'trans'yyyyMMddHHmmss.SSS");
	}

	@Override
	public String format(Date date) {
		return formatDf.format(date);
	}

	@Override
	public Date parse(String string) {
		return super.parse(string);
	}

}
