package com.inet.android.request;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inet.android.db.RequestDataBaseHelper;
import com.loopj.android.http.RequestParams;

/**
 * Data request class
 * 
 * @author johny homicide
 * 
 */
public class DataRequest extends DefaultRequest {
	private final String LOG_TAG = DataRequest.class.getSimpleName().toString();

	Context mContext;
	static RequestDataBaseHelper db;
	private SharedPreferences sp;

	public DataRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public void sendRequest(final RequestParams params) {

		final Header[] headers = {
				new BasicHeader("Accept", "application/json"),
				new BasicHeader("Authorization", "Bearer "
						+ sp.getString("access_second_token", "")) };

		Log.d(LOG_TAG, params.toString());

		final TestCaller caller = TestCaller.getInstance();
		caller.makeRequest(mContext, AppConstants.INFORMATIVE_LINK, headers,
				params, new RequestListener() {

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
							if (check[0].equals("2")) {
								RequestList.sendPeriodicRequest(mContext);
							} else if (check[0].equals("0"))
								DisassemblyErrors.setError(check[1], mContext);
						}
					}

					@Override
					public void onFailure(int arg0, byte[] errorResponse) {
						// TODO Auto-generated method stub
						if (arg0 == 401) {
							caller.sendRequestForSecondToken(mContext);
							try {
								// TimeUnit.SECONDS.sleep(1);
								TimeUnit.MILLISECONDS.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							sendRequest(params);
						}
					}
				});

	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRequest(String request) {

	}

	@Override
	protected void getRequestData(String response) throws JSONException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sendPostRequest(String request) {
		// TODO Auto-generated method stub
		
	}

}