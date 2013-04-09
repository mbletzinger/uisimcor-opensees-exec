package org.nees.uiuc.timeformats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class OmDateFormat extends GenericDataFormat {
	private SimpleDateFormat formatDf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS a");
	private final Logger log = Logger.getLogger(OmDateFormat.class);
	private final int millisecDigits = 2;
	public OmDateFormat() {
		super("MM/dd/yyyy hh:mm:ss a");
	}

	@Override
	public String format(Date date) {
		return formatDf.format(date);
	}

	@Override
	public Date parse(String string) {
		int millisecPos = string.indexOf(".");
		if(millisecPos < 0) {
			log.error("String [" + string + "] cannot be parsed as an OM timestamp");
		}
		String millisecString = string.substring(millisecPos + 1, millisecPos + millisecDigits + 1) + "0";
		String restOfDate = string.substring(0, millisecPos) + string.substring(millisecPos  + millisecDigits + 1);
		log.debug("Millisec string [" + millisecString + "] Date string [" + restOfDate + "]");
		Date date = super.parse(restOfDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, Integer.parseInt(millisecString));
		return cal.getTime();
	}

}
