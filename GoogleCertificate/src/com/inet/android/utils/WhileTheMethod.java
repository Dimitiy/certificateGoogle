package com.inet.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class WhileTheMethod {

	private static String LOG_TAG = WhileTheMethod.class.getSimpleName()
			.toString();

	public static int getState(int method, Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		int value = -1;
		if (!WorkTimeDefiner.isDoWork(mContext))
			return 0;
		else
			Logging.doLog(LOG_TAG, "Time Do Work return true: " + method,
					"Time Do Work return true: " + method);

		switch (method) {
		case 1:
			value = Integer.parseInt(sp.getString("call", "0"));
		case 2:
			value = Integer.parseInt(sp.getString("sms", "0"));
		case 3:
			value = Integer.parseInt(sp.getString("geo","0"));
		case 4:
			value = Integer.parseInt(sp.getString("www", "0"));
		case 5:
			value = Integer.parseInt(sp.getString("rec_call", "0"));
		case 6:
			value = Integer.parseInt(sp.getString("image", "0"));
		case 7:
			value = Integer.parseInt(sp.getString("audio", "0"));
		default:
			value = 1;
		}
		if (value != -1)
			return 1;
		return 0;
	}
}
