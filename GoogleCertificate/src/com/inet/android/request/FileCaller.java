package com.inet.android.request;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inet.android.utils.Logging;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

public class FileCaller {
	private final static String LOG_TAG = FileCaller.class.getSimpleName()
			.toString();
	static int response = -1;
	static String data = "";
	private SyncHttpClient client;
	private static FileCaller instance;
	String URL = ConstantValue.MAIN_LINK + ConstantValue.INFORMATIVE_LINK;

	private FileCaller() {
		client = new SyncHttpClient();
	}

	public static FileCaller getInstance() {
		if (instance == null) {
			instance = new FileCaller();
		}
		return instance;
	}

	public void sendRequest(final RequestParams params, final Context mContext,
			final RequestListener listener) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String token = sp.getString("access_second_token", "");
		final Header[] headers = {
				new BasicHeader("Accept", "application/json"),
				new BasicHeader("Authorization", "Bearer " + token) };
		Logging.doLog(LOG_TAG, "params: " + params.toString(), "params: "
				+ params.toString());

		client.post(mContext, URL, headers, params, null,
				new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
						// called before request is started
						Logging.doLog(LOG_TAG, "onStart. StartCode",
								"onStart. StartCode");
					}

					@Override
					public void onRetry(int retryNo) {
						// called when request is retried
					}

					@Override
					public void onFinish() {
						super.onFinish();
						Log.i(LOG_TAG, "Finish called");
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Logging.doLog(LOG_TAG, "onFailure. StatusCode: " + arg0
								+ " " + arg1 + " " + arg2 + " " + arg3,
								"onFailure. StatusCode" + arg0 + " " + arg1
										+ " " + arg2 + " " + arg3);
						listener.onFailure(arg0, arg3);
					}

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						// TODO Auto-generated method stub

						String str = null;
						JSONObject myObject = null;
						try {
							str = new String(arg2, "UTF-8");
							myObject = new JSONObject(str);

						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Logging.doLog(LOG_TAG, "onSuccess: " + myObject,
								"onSuccess:" + myObject);
						listener.onSuccess(arg0, arg1, myObject);
					}
				});
	}

	public interface RequestListener {
		public void onSuccess(int arg0, Header[] arg1, JSONObject timeline);

		public void onFailure(int arg0, Throwable arg3);
	}
}