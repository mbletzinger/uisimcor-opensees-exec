package org.nees.uiuc.timeformats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class NeesDateFormat extends GenericDataFormat {
	private SimpleDateFormat formatDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private final Logger log = Logger.getLogger(NeesDateFormat.class);
	private final int millisecDigits = 3;
	public NeesDateFormat() {
		super("yyyy-MM-dd'T'HH:mm:ss'Z'");
		getFormat().setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public String format(Date date) {
		return formatDf.format(date);
	}

	
	
	@Override
	public Date parse(String string) {
		int millisecPos = string.indexOf(".");
		if(millisecPos < 0) {
			log.error("String [" + string + "] cannot be parsed as an NEES timestamp");
		}
		String millisecString = string.substring(millisecPos + 1, millisecPos + millisecDigits + 1);
		String restOfDate = string.substring(0, millisecPos) + "Z";
		log.debug("Millisec string [" + millisecString + "] Date string [" + restOfDate + "]");
		Date date = super.parse(restOfDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, Integer.parseInt(millisecString));
		return cal.getTime();
	}

}
