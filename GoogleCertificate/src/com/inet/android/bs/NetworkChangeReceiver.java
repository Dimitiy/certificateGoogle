package com.inet.android.bs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

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
			List<RequestWithDataBase> listReq = db.getAllRequest();
			for (RequestWithDataBase req : listReq) {
				if (req.getType() == 2) {
					Logging.doLog("NetworkChangeReceiver sendRequest",
							sendStrings.toString(), sendStrings.toString());
					sendStrings.append(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));
				} else if (req.getType() == 1) {
					Logging.doLog("NetworkChangeReceiver sendRequest type = 1",
							sendStrings.toString(), sendStrings.toString());
					StartRequest sr = new StartRequest(context);
					sr.sendRequest(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));

				} else if (req.getType() == 3) {
					Logging.doLog("NetworkChangeReceiver sendRequest type = 3",
							sendStrings.toString(), sendStrings.toString());
					PeriodicRequest pr = new PeriodicRequest(context);
					pr.sendRequest(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));

				} else {
					Logging.doLog("NetworkChangeReceiver sendRequest type = 4",
							sendStrings.toString(), sendStrings.toString());
					DelRequest dr = new DelRequest(context);
					dr.sendRequest(req.getRequest());
					db.deleteRequest(new RequestWithDataBase(req.getID()));

				}
			}
			dataReq = new DataRequest(context);
			dataReq.sendRequest(sendStrings.toString());
			Logging.doLog("NetworkChangeReceiver sendRequest",
					sendStrings.toString(), sendStrings.toString());

		}
	}
}
