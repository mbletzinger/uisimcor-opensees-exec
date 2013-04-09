package org.nees.uiuc.timeformats;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateCalculations {
	private Logger log = Logger.getLogger(DateCalculations.class);
	private  int millisecWindow = 500;

	public DateCalculations() {
		super();
	}

	public DateCalculations(int millisecWindow) {
		super();
		this.millisecWindow = millisecWindow;
	}

	public int compareWithWindow(Date myDate, Date otherDate) {
		Calendar myTime = Calendar.getInstance();
		myTime.setTime(myDate);
		Calendar oTime = Calendar.getInstance();
		oTime.setTime(otherDate);
		myTime.add(Calendar.MILLISECOND, -millisecWindow/2);
		log.debug("Comparing " + oTime.getTimeInMillis() + " to " + myTime.getTimeInMillis());
		if(oTime.after(myTime)) {
			myTime.add(Calendar.MILLISECOND, millisecWindow);
			log.debug("Comparing " + oTime.getTimeInMillis() + " to " + myTime.getTimeInMillis());
			if(oTime.before(myTime)) {
				return 0;
			}
		}
		return myDate.compareTo(otherDate);
	}
	
	public long diffMillsec(Date myDate, Date otherDate) {
		return myDate.getTime() - otherDate.getTime();		
	}
	
	public float diffSec(Date myDate, Date otherDate) {
		return (myDate.getTime() - otherDate.getTime()) / 1000;		
	}

	public int getMillisecWindow() {
		return millisecWindow;
	}
	public void setMillisecWindow(int millisecWindow) {
		this.millisecWindow = millisecWindow;
	}

}
