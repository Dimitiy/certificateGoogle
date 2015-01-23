package com.inet.android.request;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.inet.android.db.OperationWithRecordInDataBase;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

public class ParseToError {
	private static String LOG_TAG = ParseToError.class.getSimpleName()
			.toString();

	public static void setError(String response, String request, int type,
			int infoType, String complete, String version, Context mContext) {
		if (type != -1)
			OperationWithRecordInDataBase.insertRecord(request, type, infoType,
					complete, version, mContext);

		if (response != null) {
			Logging.doLog(LOG_TAG, "response: " + response, "response: "
					+ response);
			// ------------401 error------------------
			if (response.equals(String.valueOf(HttpStatus.SC_UNAUTHORIZED))) {
				Logging.doLog(LOG_TAG, "response: SC_UNAUTHORIZED",
						"response: SC_UNAUTHORIZED");
				RequestList.sendRequestForSecondToken(mContext);
			}
		}
	}
	
	public static void setError(int response, RequestParams params, int type, final Context mContext) {
		
		Logging.doLog(LOG_TAG, "response: " + params.APPLICATION_JSON, "response: "
				+ params.APPLICATION_JSON);
		OperationWithRecordInDataBase.insertRecord(params.toString(), type, -1,
					null, null, mContext);

		if (response != -1) {
			Logging.doLog(LOG_TAG, "response: " + response, "response: "
					+ response);
			// ------------401 error------------------
			if (response == HttpStatus.SC_UNAUTHORIZED) {
				Logging.doLog(LOG_TAG, "response: SC_UNAUTHORIZED",
						"response: SC_UNAUTHORIZED");
				RequestList.sendRequestForSecondToken(mContext);
			}
		}
	}

	public static void setError(String response, Context mContext) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = null;
		String str = null;

		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			if (response == null) {
				Logging.doLog(LOG_TAG, "json null", "json null");
			}
			return;
		}
		try {
			str = jsonObject.getString("error");
		} catch (JSONException e) {
			str = null;
		}
		switch (Integer.parseInt(str)) {
		case 1:
			Logging.doLog(LOG_TAG, "device not found", "device not found");
			break;
		case 2:
			Logging.doLog(LOG_TAG, "is not available for this operation",
					"is not available for this operation");
			break;
		case 3:
			Logging.doLog(LOG_TAG, "the wrong key", "the wrong key");
			break;
		case 4:
			Logging.doLog(LOG_TAG, "missing or incorrect type/mode",
					"missing or incorrect type/mode");
			break;
		case 5:
			Logging.doLog(LOG_TAG, "List version not found",
					"List version not found");
			break;
		case 6:
			Logging.doLog(
					LOG_TAG,
					"List packet type does not match the version on the server",
					"List packet type does not match the version on the server");
			break;
		case 7:
			Logging.doLog(
					LOG_TAG,
					"List attempt to write data to the already completed package",
					"List attempt to write data to the already completed package");
			break;
		case 8:
			Logging.doLog(LOG_TAG, "other", "other");
			break;
		default:
			Logging.doLog(LOG_TAG, "error:" + str, "error" + str);
			break;
		}
	}
}
