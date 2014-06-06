package com.inet.android.bs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.request.DataRequest;
import com.inet.android.request.DelRequest;
import com.inet.android.request.PeriodicRequest;
import com.inet.android.request.StartRequest;
import com.inet.android.utils.Logging;

public class NetworkChangeReceiver extends BroadcastReceiver {
	File outFile;
	File tmpFile;
	String str;
	BufferedReader fin;
	BufferedWriter fout;
	SharedPreferences sp;
	RequestDataBaseHelper db;
	DataRequest dataReq;
	public static final String LOG_TAG = "NetworkChangeReciever";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		StringBuilder sendStrings = new StringBuilder();
		Logging.doLog(LOG_TAG, "NetWorkChange - begin", "NetWorkChange - begin");

		if (wifi.isAvailable() || mobile.isConnectedOrConnecting()) {
			Logging.doLog(LOG_TAG, "NetWorkChange - available",
					"NetWorkChange - available");
			File database = context.getDatabasePath("request_database.db");

			if (!database.exists() || database.length() == 0) {
				// Database does not exist so copy it from assets here
				Logging.doLog(LOG_TAG, "DataBase Not Found");
				return;
			} else {
				db = new RequestDataBaseHelper(context);
				Logging.doLog(LOG_TAG, "Found");
			}
			JSONArray sArray = new JSONArray();
			JSONObject strObj = new JSONObject();
			List<RequestWithDataBase> listReq = db.getAllRequest();
			for (RequestWithDataBase req : listReq) {
				if (req.getType() == 3 && req.getRequest() != "") {
					if (sendStrings != null && !sendStrings.toString().equals(""))
						sendStrings.append(",");
					Logging.doLog("NetworkChangeReceiver sendRequest =3",
							req.getRequest(), req.getRequest());
					sendStrings.append(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));
				} else if (req.getType() == 1 && req.getRequest() != "") {
					Logging.doLog("NetworkChangeReceiver sendRequest type = 1",
							req.getRequest(), req.getRequest());
					StartRequest sr = new StartRequest(context);
					sr.sendRequest(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));

				} else if (req.getType() == 2 && req.getRequest() != "") {
					Logging.doLog("NetworkChangeReceiver sendRequest type = 2",
							req.getRequest(), req.getRequest());
					PeriodicRequest pr = new PeriodicRequest(context);
					pr.sendRequest(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));

				} else if (req.getType() == 4 && req.getRequest() != "") {
					Logging.doLog("NetworkChangeReceiver sendRequest type = 4",
							req.getRequest(), req.getRequest());
					DelRequest dr = new DelRequest(context);
					dr.sendRequest(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));

				}
			}
			Logging.doLog("NetworkChangeReceiver sendRequest",
					sendStrings.toString(), sendStrings.toString());
			sArray.put(sendStrings);
			dataReq = new DataRequest(context);
			dataReq.sendRequest(sArray.toString());

		}
	}
}
