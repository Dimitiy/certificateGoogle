package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.bs.Caller;
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
		getRequestData(Caller.doMake(postRequest));
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
			e.printStackTrace();
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
			ed.putString("code", "");
		}
				
		if (str.equals("1")) {
			try {
				str = jsonObject.getString("device");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (str != null) {
				ed.putString("ID", str);
			} else {
				ed.putString("ID", "");
			}	
		}
		
		if (str.equals("0")) {
			try {
				str = jsonObject.getString("error");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (str != null) {
				ed.putString("error", str);
			} else {
				ed.putString("error", "");
			}
			if (str.equals("0")) 
				Logging.doLog(LOG_TAG, "account �� ������", "account �� ������");
			if (str.equals("1"))
				Logging.doLog(LOG_TAG, "imei ����������� ��� ����� �������� ������", 
						"imei ����������� ��� ����� �������� ������");
			if (str.equals("2")) 
				Logging.doLog(LOG_TAG, "���������� � ��������� imei ��� ����", 
						"���������� � ��������� imei ��� ����");
		}
				
		ed.commit();
	}
}
