package com.inet.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inet.android.request.ConstantValue;

public class ValueWork {

	private static String LOG_TAG = ValueWork.class.getSimpleName().toString();

	public static int getState(int method, Context mContext) {
		if (!WorkTimeDefiner.isDoWork(mContext))
			return 0;
		return getMethod(method, mContext);

	}

	public static int getMethod(int method, Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Logging.doLog(LOG_TAG, "method" + method, "method" + method);
		int value = -1;

		switch (method) {
		case ConstantValue.TYPE_INCOMING_CALL_REQUEST:
			value = Integer.parseInt(sp.getString("call", "0"));
			break;
		case ConstantValue.TYPE_INCOMING_SMS_REQUEST:
			value = Integer.parseInt(sp.getString("sms", "0"));
			break;
		case ConstantValue.TYPE_HISTORY_BROUSER_REQUEST:
			value = Integer.parseInt(sp.getString("www", "0"));
			break;
		case ConstantValue.TYPE_LOCATION_TRACKER_REQUEST:
			value = Integer.parseInt(sp.getString("geo", "0"));
			break;
		case ConstantValue.LOCATION_TRACKER_MODE:
			value = Integer.parseInt(sp.getString("geo_mode", "0"));
			break;
		case ConstantValue.TYPE_IMAGE_REQUEST:
			value = Integer.parseInt(sp.getString("image", "0"));
			break;
		case ConstantValue.TYPE_AUDIO_REQUEST:
			value = Integer.parseInt(sp.getString("audio", "0"));
			break;
		case ConstantValue.RECORD_CALL:
			value = Integer.parseInt(sp.getString("rec_call", "0"));
			break;
		case ConstantValue.RECORD_ENVORIMENT:
			value = Integer.parseInt(sp.getString("rec_env", "0"));
			break;
		case ConstantValue.RECORD_ENVORIMENT_CALL:
			value = Integer.parseInt(sp.getString("rec_env_call", "0"));
			break;
		case ConstantValue.TYPE_LIST_CALL:
			value = sp.getInt("list_call", 0);
			Logging.doLog(LOG_TAG, "value" + value, "value" + value);
			break;
		case ConstantValue.TYPE_LIST_SMS:
			value = sp.getInt("list_sms", 0);
			Logging.doLog(LOG_TAG, "value" + value, "value" + value);	
			break;
		case ConstantValue.TYPE_LIST_CONTACTS:
			value = sp.getInt("list_contact", 0);
			Logging.doLog(LOG_TAG, "value" + value, "value" + value);	
			break;
		case ConstantValue.TYPE_LIST_APP:
			value = sp.getInt("list_app", 0);
			Logging.doLog(LOG_TAG, "value" + value, "value" + value);	
			break;
		}
		if (value == -1)
			return 1;
		Logging.doLog(LOG_TAG, "return value" + value, "return value" + value);	
		
		return value;
	}

	public static String getKeyForRecord(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		return sp.getString("key_rec", "0");
	}
}
