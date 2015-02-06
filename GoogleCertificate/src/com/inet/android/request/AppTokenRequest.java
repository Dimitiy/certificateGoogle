package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.utils.Logging;

public class AppTokenRequest extends DefaultRequest {
	private final String LOG_TAG = AppTokenRequest.class.getSimpleName()
			.toString();
	private Context mContext;
	
	public AppTokenRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
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
		if (!request.equals(" ")) {
			String str = null;
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			try {
				Logging.doLog(LOG_TAG, request, request);
				str = Caller.doMake(request,
						sp.getString("access_first_token", ""), ConstantValue.APP_TOKEN_LINK,
						true, null, mContext);
			} catch (IOException e) {
				e.printStackTrace();	
			}
			if (str != null && str.length() > 3) {
					getRequestData(str);
			} else {
				Logging.doLog(LOG_TAG,
						"response from the server is missing or incorrect",
						"response from the server is missing or incorrect");
			}
		} else {
			Logging.doLog(LOG_TAG, "request == null", "request == null");
		}
	}

	@Override
	protected void getRequestData(String response) {

		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

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
			ed.putString("code_app_token", str);
			ed.commit();
		} else {
			ed.putString("code_app_token", "");
		}
		if (str.equals("1")) {
			RequestList.sendRequestForSecondToken(mContext);
		}
		if (str.equals("0")) {
			ParsingErrors.setError(response, mContext);
		}
		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}

}