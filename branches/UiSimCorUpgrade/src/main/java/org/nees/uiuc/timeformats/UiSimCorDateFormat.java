package org.nees.uiuc.timeformats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class UiSimCorDateFormat extends GenericDataFormat {
	private SimpleDateFormat formatDf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
	private final Logger log = Logger.getLogger(UiSimCorDateFormat.class);
private final int millisecDigits = 3;	
	public UiSimCorDateFormat() {
		super("dd-MMM-yyyy HH:mm:ss");
	}

	@Override
	public String format(Date date) {
		return formatDf.format(date);
	}

	@Override
	public Date parse(String string) {
		int millisecPos = string.indexOf(".");
		if(millisecPos < 0) {
			log.error("String [" + string + "] cannot be parsed as a UI SimCor timestamp");
		}
		String millisecString = string.substring(millisecPos + 1, millisecPos + millisecDigits + 1);
		Date date = super.parse(string.substring(0,millisecPos));
		int millisec = Integer.parseInt(millisecString);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, millisec);
		return cal.getTime();
	}

}
