package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.utils.Logging;

public class CheckRequest extends DefaultRequest {
	private final String LOG_TAG = CheckRequest.class.getSimpleName()
			.toString();
	final private String additionURL = "api/check";

	private final int type = 3;
	Context mContext;
	static RequestDataBaseHelper db;

	public CheckRequest(Context ctx) {
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
			
				str = Caller.doMake(request, sp.getString("access_first_token", ""),
						additionURL, true, null, mContext);
			} catch (IOException e) {
				e.printStackTrace();
				
			}
			if (str != null) {
				getRequestData(str);
			} else {
				Logging.doLog(LOG_TAG,
						"response from the server is missing or incorrect",
						"response from the server is missing or incorrect");

				Editor ed = sp.edit();
				ed.putString("code", "1");
				ed.commit();

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
			ed.putString("code_check", str);
		} else {
			ed.putString("code_check", "");
		}
		if (str.equals("1")) {
			Logging.doLog(LOG_TAG, "decision is still pending",
					"decision is still pending");
		}
		// -------------response: OK--------
		
		if (str.equals("2")) {
			RequestList.sendTokenAppRequest(mContext);
		}
		// -------------want to remove the device--------
		
		if (str.equals("3")) {
			Logging.doLog(LOG_TAG, "want to remove the device",
					"want to remove the device");
			String keyRemoval = null;
			try {
				keyRemoval = jsonObject.getString("key");
			} catch (JSONException e) {
				keyRemoval = null;
			}
			if (keyRemoval != null) {
				ed.putString("key_removal", keyRemoval);
			} else {
				ed.putString("key_removal", "");
			}

		}
		// -------------error------------------
		
		if (str.equals("0")) {
			String errstr = null;
			try {
				errstr = jsonObject.getString("error");
			} catch (JSONException e) {
				errstr = null;
			}
			if (errstr != null) {
				ed.putString("error_check", errstr);
			} else {
				ed.putString("error_check", "");
			}

			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "incorrect account number",
						"incorrect account number");

				// ed.putString("account", "account");
			}
			if (str.equals("1"))
				Logging.doLog(LOG_TAG, "device not found", "device not found");
			if (str.equals("2"))
				Logging.doLog(LOG_TAG, "is not available for this operation",
						"is not available for this operation");
		}
		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub
		
	}



}