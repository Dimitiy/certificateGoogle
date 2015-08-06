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

import com.inet.android.bs.ServiceControl;
import com.inet.android.info.DeviceInformation;
import com.inet.android.list.Queue;
import com.inet.android.message.SmsSentObserver;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.Logging;

/**
 * Periodic request class is designed to handle the server's response
 * 
 * @author johny homicide
 * 
 */
public class PeriodicRequest extends DefaultRequest {
	private final String LOG_TAG = PeriodicRequest.class.getSimpleName()
			.toString();
	boolean periodicalFlag = true;
	private Context mContext;
	private SharedPreferences sp;
	private Editor ed;

	public PeriodicRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();
	}

	@Override
	public void sendRequest() {

		final Header[] headers = {
				new BasicHeader("Accept", "application/json"),
				new BasicHeader("Authorization", "Bearer "
						+ sp.getString("access_second_token", "")) };

		final TestCaller caller = TestCaller.getInstance();
		caller.makeRequest(mContext, AppConstants.PERIODIC_LINK, headers, null,
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
							ed.putString("code_periodic", check[0]);
							if (check[0].equals("2")) {
								Logging.doLog(LOG_TAG, "settings", "settings");
								try {
									parser.parsingSettings(response);
								} catch (UnsupportedEncodingException
										| JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if (sp.getBoolean("is_info", false) == true) {
									DeviceInformation device = new DeviceInformation(
											mContext);
									device.getInfo();
									ServiceControl.runService(mContext);
									ed.putBoolean("is_info", false);
								}

							} else if (check[0].equals("3")) {
								ed.putString("key_removal", check[1]);
								DelRequest del = new DelRequest(mContext);
								del.sendRequest();
							} else if (check[0].equals("0"))
								DisassemblyErrors.setError(check[1], mContext);

							ed.commit();
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
							sendRequest();
						}
					}
				});

	}

	@Override
	protected void sendPostRequest(String request) {

	}

	@Override
	protected void getRequestData(String response) {

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