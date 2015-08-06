package com.inet.android.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class with some utilities for date
 * 
 * @author johny homicide
 *
 */
public class ConvertDate {

	public static String getDate(long date) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(date);

	}

	public static String logTime() {
		Date localTime = new Date();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(localTime);
	}
}