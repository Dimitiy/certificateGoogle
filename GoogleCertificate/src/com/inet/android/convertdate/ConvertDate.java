package com.inet.android.convertdate;

import java.text.SimpleDateFormat;

public class ConvertDate {
	String date;

	

	public String getData(long date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);

	}

}