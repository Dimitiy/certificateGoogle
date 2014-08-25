package com.inet.android.info;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.inet.android.request.DataRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public class CreateServiceInformation {
	String area;
	String event;
	private String LOG_TAG = "CreateServiceInformation";
	Context mContext;
	public CreateServiceInformation(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		
	}

	public void sendStr(String area, String event) {
		String sendJSONStr = null;
		this.area = area;
		this.event = event;
		try {
			ConvertDate getDate = new ConvertDate();
			
			JSONObject info = new JSONObject();
			JSONObject object = new JSONObject();
			info.put("area", area);
			info.put("event", event);

			object.put("time", getDate.logTime());
			object.put("type", "12");
			object.put("info", info);
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
		}
		DataRequest dr = new DataRequest(mContext);
		dr.sendRequest(sendJSONStr);
	}
}