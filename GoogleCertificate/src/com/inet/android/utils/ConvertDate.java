package com.inet.android.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ConvertDate {
	String date;

	

	public String getData(long date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return formatter.format(date);

	}
	public String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}