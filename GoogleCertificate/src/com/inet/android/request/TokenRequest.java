package com.inet.android.request;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

public class TokenRequest extends DefaultRequest {
	private final String LOG_TAG = TokenRequest.class.getSimpleName()
			.toString();
	final private String additionURL = "oauth/token";
	Context mContext;
	private int type = 5;
	static RequestDataBaseHelper db;

	public TokenRequest(Context ctx) {
		super(ctx);
		this.mContext = ctx;
	}

	@Override
	public void sendRequest(int typeTokenRequest) {
		RequestTask mt = new RequestTask();
		mt.execute(typeTokenRequest);
	}

	class RequestTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Integer... token) {
			// TODO Auto-generated method stub
			sendPostRequest(token[0]);
			return null;
		}
	}

	protected void sendPostRequest(int typeTokenRequest) {
		Logging.doLog(LOG_TAG, "token: " + typeTokenRequest, "token: "
				+ typeTokenRequest);

		String str = null;
		try {

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

			switch (typeTokenRequest) {
			case 1:
				postParameters.add(new BasicNameValuePair("grant_type",
						"client_credentials"));
				postParameters
						.add(new BasicNameValuePair("client_id",
								"d04e6d2d6ea7e32c04f4e7c87c324458b7c9a617ddeaea731e4bcf446a604370"));
				postParameters.add(new BasicNameValuePair("scope", "client"));
				postParameters
						.add(new BasicNameValuePair("client_secret",
								"88e5df97f1926f0c3b9137dd78c38259bd460fc7c0d4bb94827c32bac386ad04"));
				break;
			case 2:
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(mContext);
				postParameters.add(new BasicNameValuePair("grant_type",
						"password"));
				postParameters.add(new BasicNameValuePair("username", sp
						.getString("device", "0000")));
				postParameters.add(new BasicNameValuePair("scope", "device"));
				postParameters.add(new BasicNameValuePair("password", sp
						.getString("time_setub", "")));
				break;
			}

			str = Caller.doMake(null, null, additionURL, false, postParameters,
					mContext);
		} catch (IOException e) {
			// Добавление в базу request
			e.printStackTrace();
		}
		if (str != null) {
			getRequestData(str);
		} else {
			Logging.doLog(LOG_TAG, "ответа от сервера нет",
					"ответа от сервера нет");
			OperationWithRecordInDataBase.insertRecord(null, type, null, null,
					null, mContext);
		}
	}

	@Override
	protected void getRequestData(String response) {

		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();

		JSONObject jsonObject = null;
		String str = null;
		String token = null;
		// ------------scope-----------------------
		try {

			jsonObject = new JSONObject(response);
			str = jsonObject.getString("scope");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("scope", str);

			if (str.equals("client")) {
				token = "first_token";
			} else if (str.equals("device")) {
				token = "second_token";
			}
		}

		// ------------invalid token-----------------------
		try {
			jsonObject = new JSONObject(response);
			str = jsonObject.getString("invalid token");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putBoolean("invalid_" + token, true);

		} else {
			ed.putBoolean("invalid_" + token, false);
		}
		// ------------token expired-----------------------

		try {
			jsonObject = new JSONObject(response);
			str = jsonObject.getString("token expired");
		} catch (JSONException e) {
			str = null;
		}

		if (str != null) {
			if (token.equals("first_token"))
				RequestList.sendRequestForFirstToken(mContext);
			else if (token.equals("second_token"))
				RequestList.sendRequestForSecondToken(mContext);
		}

		// ------------access_token----------------
		try {
			jsonObject = new JSONObject(response);
			str = jsonObject.getString("access_token");
		} catch (JSONException e) {
			str = null;
		}

		if (str != null) {
			ed.putString("access_" + token, str);
			if (token.equals("first_token")
					&& sp.getString("code_initial", "-1").equals("-1"))
				RequestList.sendStartRequest(mContext);
			else if (token.equals("second_token")
					&& !sp.getString("code_periodic", "-1").equals("2"))
				RequestList.sendPeriodicRequest(mContext);
			else if (token.equals("second_token"))
				OperationWithRecordInDataBase.sendRecord(mContext);
			
		} else {
			ed.putString("access_" + token, "-1");
		}

		// ------------code-----------------------

		try {
			jsonObject = new JSONObject(response);
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("code_" + token, str);

			if (str.equals("1")) {
				Logging.doLog(LOG_TAG, "token request: Ok", "token request: Ok");
			}

			if (str.equals("0")) {
				String errstr = null;
				try {
					errstr = jsonObject.getString("error");
				} catch (JSONException e) {
					errstr = null;
				}
				if (errstr != null) {
					ed.putString("error_" + token, errstr);
				} else {
					ed.putString("error_" + token, "");
				}
				ed.commit();
				if (str.equals("0")) {
					Logging.doLog(LOG_TAG, "incorrect account number",
							"incorrect account number");
					ed.putString("account", "account");
				}
				if (str.equals("1"))
					Logging.doLog(LOG_TAG, "device not found",
							"device not found");
				if (str.equals("2"))
					Logging.doLog(LOG_TAG,
							"device is not available for this operation",
							"device is not available for this operation");
			}
		} else {
			ed.putString("code_" + token, "");
		}
		ed.commit();

	}

	@Override
	public void sendRequest(String request) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sendPostRequest(String request) {
		// TODO Auto-generated method stub

	}

}