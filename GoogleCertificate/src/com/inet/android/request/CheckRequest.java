package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.inet.android.utils.DialogShower;
import com.inet.android.utils.Logging;

public class CheckRequest extends DefaultRequest {
	private final String LOG_TAG = CheckRequest.class.getSimpleName()
			.toString();

	Context mContext;
	private SharedPreferences sp;

	public CheckRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.registerOnSharedPreferenceChangeListener(prefListener);
	}

	@Override
	public void sendRequest(String request) {
		RequestTask mt = new RequestTask();
		mt.execute(request);
	}

	SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			Logging.doLog(LOG_TAG, "prefs make");
			if (key.equals("code_check")) {
				Logging.doLog(LOG_TAG, prefs.getString("code_check", "-1"),
						prefs.getString("code_check", "-1"));
				if (prefs.getString("code_check", "code_check").equals("0")) {

					Logging.doLog(LOG_TAG, "prefs make: code_check");

					Toast.makeText(mContext, "Account number incorrect!!",
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent("android.intent.action.MAIN");
					intent.setClass(mContext, DialogShower.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("text", "Hello!");
					mContext.startActivity(intent);

				}

			}
		}
	};

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

		String str = null;
		// SharedPreferences sp = PreferenceManager
		// .getDefaultSharedPreferences(mContext);
		try {

			str = Caller.doMake(request,
					sp.getString("access_first_token", ""),
					ConstantValue.CHECK_LINK, true, null, mContext);
		} catch (IOException e) {
			e.printStackTrace();

		}
		if (str != null && str.length() > 3) {
			getRequestData(str);
		} else if (str.length() == 3) {
			Logging.doLog(LOG_TAG, "error: " + str, "error: " + str);
			RequestList.sendRequestForFirstToken(mContext);
		} else {
			Logging.doLog(LOG_TAG,
					"response from the server is missing or incorrect",
					"response from the server is missing or incorrect");
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
			sp.unregisterOnSharedPreferenceChangeListener(prefListener);

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
			DisassemblyErrors.setError(response, mContext);
		}
		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}

}