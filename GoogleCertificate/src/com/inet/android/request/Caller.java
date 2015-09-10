package com.inet.android.request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inet.android.utils.Logging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

public class Caller {
	private final static String LOG_TAG = Caller.class.getSimpleName().toString();
	private static AsyncHttpClient asyncHttpClient;

	private static AsyncHttpClient syncHttpClient;
	private static Caller instance;
	private SharedPreferences sp;

	/**
	 * Performs HTTP POST
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	Caller() {
		asyncHttpClient = new AsyncHttpClient();
		syncHttpClient = new SyncHttpClient();
	}

	public static Caller getInstance() {
		if (instance == null) {
			instance = new Caller();
			Log.d(LOG_TAG, "instance");
		}
		return instance;
	}

	/**
	 * @return an async client when calling from the main thread, otherwise a
	 *         sync client.
	 */
	private static AsyncHttpClient getClient() {
		// Return the synchronous HTTP client when the thread is not prepared
		if (Looper.myLooper() == null)
			return syncHttpClient;
		return asyncHttpClient;
	}

	// You can add more parameters if you need here.
	public void makeRequest(Context context, String url, Header[] headers, RequestParams params,
			final RequestListener listener) {
		// Logging.doLog(LOG_TAG, "params: " + params, "params: " + params);
		getClient().post(context, url, headers, params, null, new AsyncHttpResponseHandler() {

			@Override
			public void onStart() {
				// called before request is started
				// Some debugging code here
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] response) {
				listener.onSuccess(statusCode, headers, response);
				try {
					Logging.doLog(LOG_TAG, "statusCode: " + statusCode + new String(response, "UTF-8"),
							"statusCode" + statusCode + new String(response, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
				// called when response HTTP status is "4XX" (eg. 401,
				// 403, 404)
				// Some debugging code here, show retry dialog, feedback
				// etc.
				listener.onFailure(statusCode, errorResponse);
				String response = null;
				try {
					if (errorResponse != null)
						response = new String(errorResponse, "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Logging.doLog(LOG_TAG, "onFailure. StatusCode: " + statusCode + response + e.toString(),
						"onFailure. StatusCode" + statusCode + response + e.toString());
			}

			@Override
			public void onRetry(int retryNo) {
				// Some debugging code here-------

			}
		});
	}

	/*
	 * Sending a request for get access API
	 */
	/**
	 * This is the first request you have to do before being able to use the
	 * API.
	 */
	public void sendRequestForFirstToken(final Context mContext) {
		Logging.doLog(LOG_TAG, "sendRequestForFirstToken");
		RequestParams params = new RequestParams();
		params.put("grant_type", "client_credentials");
		params.put("client_id", getClientId());
		params.put("scope", "client");
		params.put("client_secret", getClientSecret());
		makeRequest(mContext, AppConstants.TOKEN_LINK, null, params, new RequestListener() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, JSONObject response) {
				// TODO Auto-generated method stub
				if (arg0 == 401)
					sendRequestForFirstToken(mContext);
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] response) {
				// TODO Auto-generated method stub
				if (response != null) {
					Parser parse = new Parser(mContext);
					try {
						parse.parsingFirstToken(response);

					} catch (UnsupportedEncodingException | JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(int arg0, byte[] errorResponse) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void sendRequestForSecondToken(final Context mContext) {
		Logging.doLog(LOG_TAG, "sendRequestForSecondToken ", "sendRequestForSecondToken");
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		RequestParams params = new RequestParams();

		params.put("grant_type", "password");
		params.put("username", getUsername());
		params.put("scope", "device");
		params.put("password", getPassword());

		makeRequest(mContext, AppConstants.TOKEN_LINK, null, params, new RequestListener() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, JSONObject response) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] response) {
				// TODO Auto-generated method stub
				if (response != null) {
					Parser parse = new Parser(mContext);
					try {
						parse.parsingSecondToken(response);

					} catch (UnsupportedEncodingException | JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(int arg0, byte[] errorResponse) {
				// TODO Auto-generated method stub
				if (arg0 == 401)
					sendRequestForSecondToken(mContext);
			}
		});
	}

	private String getUsername() {
		// TODO Auto-generated method stub
		return sp.getString("device", "0000");
	}

	private String getPassword() {
		// TODO Auto-generated method stub
		return sp.getString("time_setub", "");
	}

	private String getClientSecret() {
		// TODO Auto-generated method stub
		return AppConstants.CLIENT_SECRET;
	}

	private String getClientId() {
		// TODO Auto-generated method stub
		return AppConstants.CLIENT_ID;
	}
}
