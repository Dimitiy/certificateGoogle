package com.inet.android.request;

import org.apache.http.HttpStatus;

import com.inet.android.db.OperationWithRecordInDataBase;
import com.inet.android.utils.Logging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class DisassemblyErrors {
	private static String LOG_TAG = DisassemblyErrors.class.getSimpleName().toString();

	public static void setError(String response, String request, int type, int infoType, String complete, int version,
			Context mContext) {
		if (type != -1)
			OperationWithRecordInDataBase.insertRecord(request, type, infoType, complete, version, mContext);

		if (response != null) {
			Logging.doLog(LOG_TAG, "response: " + response + " " + type + " " + version,
					"response: " + response + " " + type + " " + version);
			// ------------401 error------------------
			if (response.equals(String.valueOf(HttpStatus.SC_UNAUTHORIZED))) {
				Logging.doLog(LOG_TAG, "response: SC_UNAUTHORIZED", "response: SC_UNAUTHORIZED");
				Caller caller = Caller.getInstance();
				caller.sendRequestForSecondToken(mContext);
			}
		}
	}

	public static void setError(int response, int type, final Context mContext) {

		if (response != -1) {
			Logging.doLog(LOG_TAG, "response: " + response, "response: " + response);
			// ------------401 error------------------
			if (response == HttpStatus.SC_UNAUTHORIZED) {
				Caller caller = Caller.getInstance();

				Logging.doLog(LOG_TAG, "response: SC_UNAUTHORIZED", "response: SC_UNAUTHORIZED");
				if (type == AppConstants.TYPE_FIRST_TOKEN_REQUEST) {
					caller.sendRequestForFirstToken(mContext);
				}
				if (type == AppConstants.TYPE_SECOND_TOKEN_REQUEST)
					caller.sendRequestForSecondToken(mContext);
			}
		}

	}

	public static void setError(String response, Context mContext) {
		// TODO Auto-generated method stub

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();
		switch (response) {
		case "0":
			Logging.doLog(LOG_TAG, "device not found", "device not found");
			ed.putString("account", "account");

			break;
		case "1":
			Logging.doLog(LOG_TAG, "device not found", "device not found");
			ed.putString("error_initial", "-1");
			break;
		case "2":
			Logging.doLog(LOG_TAG, "is not available for this operation", "is not available for this operation");
			break;
		case "3":
			Logging.doLog(LOG_TAG, "the wrong key", "the wrong key");
			break;
		case "4":
			Logging.doLog(LOG_TAG, "missing or incorrect type/mode", "missing or incorrect type/mode");
			break;
		case "5":
			Logging.doLog(LOG_TAG, "List version not found", "List version not found");
			break;
		case "6":
			Logging.doLog(LOG_TAG, "List packet type does not match the version on the server",
					"List packet type does not match the version on the server");
			break;
		case "7":
			Logging.doLog(LOG_TAG, "List attempt to write data to the already completed package",
					"List attempt to write data to the already completed package");
			break;
		case "8":
			Logging.doLog(LOG_TAG, "other", "other");
			break;
		default:
			Logging.doLog(LOG_TAG, "error:" + response, "error" + response);
			break;
		}
		ed.commit();

	}

}
