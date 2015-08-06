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

public class CheckRequest extends DefaultRequest {
	private final String LOG_TAG = CheckRequest.class.getSimpleName()
			.toString();

	private Context mContext;
	private SharedPreferences sp;
	private Editor ed;

	public CheckRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.registerOnSharedPreferenceChangeListener(prefListener);
		ed = sp.edit();
	}

	@Override
	public void sendRequest() {
		final Header[] headers = {
				new BasicHeader("Accept", "application/json"),
				new BasicHeader("Authorization", "Bearer "
						+ sp.getString("access_first_token", "")) };
		Logging.doLog(
				LOG_TAG,
				"send CheckRequest, account: "
						+ sp.getString("account", "account"));
		RequestParams params = new RequestParams();
		params.put("account", sp.getString("account", "0000"));
		params.put("device", sp.getString("device", "0000"));

		final TestCaller caller = TestCaller.getInstance();
		caller.makeRequest(mContext, AppConstants.CHECK_LINK, headers, params,
				new RequestListener() {

					@Override
					public void onSuccess(int arg0, Header[] arg1,
							JSONObject timeline) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(int arg0, Header[] arg1,
							byte[] response) {
						// TODO Auto-generated method stub
						Parser parser = new Parser(mContext);
						String[] check = null;
						try {
							check = parser.parsing(response);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (check[0] != null) {
							ed.putString("code_check", check[0]);
							ed.commit();
							if (check[0].equals("1")) {
								Logging.doLog(LOG_TAG,
										"decision is still pending",
										"decision is still pending");
							} else if (check[0].equals("2")) {
								AppTokenRequest appToken = new AppTokenRequest(
										mContext);
								appToken.sendRequest();
								sp.unregisterOnSharedPreferenceChangeListener(prefListener);
							} else if (check[0].equals("3")) {
								ed.putString("key_removal", check[1]);
								ed.commit();
								DelRequest del = new DelRequest(mContext);
								del.sendRequest();
							} else if (check[0].equals("0"))
								DisassemblyErrors.setError(check[1], mContext);
						}
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
				});

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

	@Override
	protected void sendPostRequest(String request) {

	}

	@Override
	protected void getRequestData(String response) {

	}

	/*
	 * code = 1 - Ok, device - true; code:0, error:0 - incorrect accaunt
	 */
	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendRequest(String request) {
		// TODO Auto-generated method stub

	}

}