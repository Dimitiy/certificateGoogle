package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.utils.Logging;

public class ParseToError {
	private static String LOG_TAG = ParseToError.class.getSimpleName()
			.toString();

	public static void setError(String response) {
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
			Logging.doLog(LOG_TAG, "List version not found", "List version not found");
			break;
		case 6:
			Logging.doLog(LOG_TAG,
					"List packet type does not match the version on the server",
					"List packet type does not match the version on the server");
			break;
		case 7:
			Logging.doLog(LOG_TAG,
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
