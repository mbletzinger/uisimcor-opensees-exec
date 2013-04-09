package org.nees.uiuc.timeformats;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateFormats {
	public enum TimeStampType { EXCEL,NEES,OM, TRANSSIMCOR, UISIMCOR };
	Map<TimeStampType, GenericDataFormat> formats;
	public DateFormats() {
		super();
		this.formats = new HashMap<TimeStampType, GenericDataFormat>();
		formats.put(TimeStampType.UISIMCOR, new UiSimCorDateFormat());
		formats.put(TimeStampType.NEES, new NeesDateFormat());
		formats.put(TimeStampType.EXCEL, new ExcelDateFormat());
		formats.put(TimeStampType.TRANSSIMCOR, new TransSimCorDateFormat());
		formats.put(TimeStampType.OM, new OmDateFormat());
	}
	public String format(Date date, TimeStampType type) {
		return formats.get(type).format(date);
	}

	public Date parse(String string, TimeStampType type) {
		return formats.get(type).parse(string);
	}
}
