package com.inet.android.request;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inet.android.utils.Logging;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

public class FileCaller {
	private final static String LOG_TAG = FileCaller.class.getSimpleName()
			.toString();
	static int response = -1;
	static String data = "";

	public static String sendRequest(final RequestParams params,
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
				new JsonHttpResponseHandler() {
					@Override
					public void onStart() {
						// called before request is started
						Logging.doLog(LOG_TAG, "onStart. StartCode",
								"onStart. StartCode");
		}

					@Override
					public void onSuccess(int arg0, Header[] arg1,
							JSONObject timeline) {
						Logging.doLog(LOG_TAG, "onSuccess. StatusCode: " + arg0
								+ " " + timeline, "onSuccess. StatusCode"
								+ arg0 + " " + timeline);
						String str = "";
						try {
							str = timeline.getString("code");
						} catch (JSONException e) {
							str = null;
						}
						if (str.equals("2")) {
							RequestList.sendPeriodicRequest(mContext);
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONArray errorResponse) {
						Logging.doLog(LOG_TAG, "onFailru. StatusCode"
								+ statusCode + " " + errorResponse,
								"onFailru. StatusCode" + statusCode + " "
										+ errorResponse);
						ParsingErrors.setError(statusCode, params,
								ConstantValue.TYPE_FILE_REQUEST, mContext);
					}
				});
		return data;
	}
}
