package com.inet.android.request;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.inet.android.utils.DialogShower;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

/**
 * Start request class
 * 
 * @author johny homicide
 * 
 */
public class StartRequest extends DefaultRequest {
	private final String LOG_TAG = StartRequest.class.getSimpleName()
			.toString();
	private Context mContext;
	private SharedPreferences sp;
	private Editor ed;

	public StartRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.registerOnSharedPreferenceChangeListener(prefListener);
		ed = sp.edit();
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
					sendRequest();
				}
			}
		}
	};

	/**
	 * This is the request you have for get device number
	 */
	@Override
	public void sendRequest() {
		final Header[] headers = {
				new BasicHeader("Accept", "application/json"),
				new BasicHeader("Authorization", "Bearer "
						+ sp.getString("access_first_token", "")) };
		Logging.doLog(
				LOG_TAG,
				"send start request, account: "
						+ sp.getString("account", "account"));
		RequestParams params = new RequestParams();
		params.put("account", sp.getString("account", "0000"));
		params.put("imei", sp.getString("imei", "imei"));
		params.put("model", sp.getString("model", "0000"));

		final TestCaller caller = TestCaller.getInstance();
		caller.makeRequest(mContext, AppConstants.INITIAL_LINK, headers,
				params, new RequestListener() {

					@Override
					public void onSuccess(int arg0, Header[] arg1,
							JSONObject timeline) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFailure(int arg0, byte[] errorResponse) {
						// TODO Auto-generated method stub
						if (arg0 == 401) {
							caller.sendRequestForFirstToken(mContext);
							try {
								// TimeUnit.SECONDS.sleep(1);
								TimeUnit.MILLISECONDS.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							sendRequest();
						}
					}

					@Override
					public void onSuccess(int arg0, Header[] arg1,
							byte[] response) {
						// TODO Auto-generated method stub
						Parser parser = new Parser(mContext);
						String[] initial = null;
						try {
							initial = parser.parsing(response);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (initial[0] != null)
							if (initial[0].equals("1")) {
								ed.putString("code_initial", "1");
								ed.putString("device", initial[1]);
								ed.commit();
								sp.unregisterOnSharedPreferenceChangeListener(prefListener);
								CheckRequest check = new CheckRequest(mContext);
								check.sendRequest();
							} else {
								ed.putString("account", "account");
								ed.commit();
							}

					}

				});

	}

	@Override
	protected void sendPostRequest(String postRequest) {

	}

	@Override
	protected void getRequestData(String string) {
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRequest(String request) {
		// TODO Auto-generated method stub

	}
}