package com.inet.android.request;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.inet.android.utils.GeneratorLine;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

public class AppTokenRequest extends DefaultRequest {
	private final String LOG_TAG = AppTokenRequest.class.getSimpleName()
			.toString();
	private Context mContext;
	private SharedPreferences sp;
	private Editor ed;

	public AppTokenRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
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
				"send AppTokenRequest, account: "
						+ sp.getString("account", "account"));
		RequestParams params = new RequestParams();
		params.put("account", sp.getString("account", "0000"));
		params.put("device", sp.getString("device", "0000"));
		params.put("token", sp.getString("time_setub", ""));
		params.put("key_for_bug", GeneratorLine.createRandomString(mContext));
	
		final Caller caller = Caller.getInstance();
		caller.makeRequest(mContext, AppConstants.APP_TOKEN_LINK, headers, params,
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
							ed.putString("code_app_token", check[0]);
							ed.commit();
							if (check[0].equals("1")) {
								Logging.doLog(LOG_TAG,
										"App Token - ok",
										"App Token - ok");
								caller.sendRequestForSecondToken(mContext);
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

	@Override
	protected void getRequestData(byte[] response) {
		// TODO Auto-generated method stub
		
	}

}