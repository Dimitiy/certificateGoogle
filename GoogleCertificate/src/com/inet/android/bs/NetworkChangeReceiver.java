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
<<<<<<< HEAD
	RequestDataBaseHelper db;
	DataRequest dataReq;
=======
>>>>>>> refs/remotes/origin/war
	public static final String LOG_TAG = "NetworkChangeReciever";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
<<<<<<< HEAD

		StringBuilder sendStrings = new StringBuilder();
		Logging.doLog(LOG_TAG, sendStrings.toString(), sendStrings.toString());
		Logging.doLog(LOG_TAG, "NetWorkChange - begin", "NetWorkChange - begin");

=======
		Request req = new Request(context);
		StringBuilder sendStrings = new StringBuilder(); // Using default 16
														// character size
		String funcRecStr = null;
		Log.d(LOG_TAG, "begin");
		FileLog.writeLog("NetWorkChangeReciver -> begin");
	
>>>>>>> refs/remotes/origin/war
		if (wifi.isAvailable() || mobile.isConnectedOrConnecting()) {
<<<<<<< HEAD
			Logging.doLog(LOG_TAG, "NetWorkChange - available",
					"NetWorkChange - available");
			File database = context.getDatabasePath("request_database.db");
=======
			Log.d(LOG_TAG, "available");
			FileLog.writeLog("NetWorkChangeReciver -> available");
		
			outFile = new File(Environment.getExternalStorageDirectory(),
					"/conf");
			if (outFile.exists() == false) {
				Log.d(LOG_TAG, "outfile not exist");
				FileLog.writeLog("NetWorkChangeReciver -> outfile not exist");
>>>>>>> refs/remotes/origin/war

			if (!database.exists() || database.length() == 0) {
				// Database does not exist so copy it from assets here
				Logging.doLog(LOG_TAG, "DataBase Not Found");
				return;
			} else {
				db = new RequestDataBaseHelper(context);
				Logging.doLog(LOG_TAG, "Found");
			}
<<<<<<< HEAD
			// JSONArray sArray = new JSONArray();
			// JSONObject strObj = new JSONObject();
			List<RequestWithDataBase> listReq = db.getAllRequest();
			for (RequestWithDataBase req : listReq) {
				if (req.getRequest() != null
						&& !req.getRequest().toString().equals("")
						&& !req.getRequest().toString().equals(" ")) {
=======
			if (outFile.length() == 0) {
				Log.d(LOG_TAG, "outfile is empty");
				FileLog.writeLog("NetWorkChangeReciver -> outfile is empty");
				
				return;
			}
			tmpFile = new File(Environment.getExternalStorageDirectory(),
					"/tmpSendFile.txt");
>>>>>>> refs/remotes/origin/war

					if (req.getType() == 3) {
						if (!sendStrings.toString().equals(" "))
							sendStrings.append(",");

<<<<<<< HEAD
						Logging.doLog("NetworkChangeReceiver sendRequest =3",
								req.getRequest(), req.getRequest());

						sendStrings.append(req.getRequest());

						Logging.doLog(LOG_TAG, "sendString: " + sendStrings);

						db.deleteRequest(new RequestWithDataBase(req.getID()));
					} else if (req.getType() == 1) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 1",
								req.getRequest(), req.getRequest());
						StartRequest sr = new StartRequest(context);
						sr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 2) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 2",
								req.getRequest(), req.getRequest());
						PeriodicRequest pr = new PeriodicRequest(context);
						pr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 4) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 4",
								req.getRequest(), req.getRequest());
						DelRequest dr = new DelRequest(context);
						dr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

=======
				}
			} catch (FileNotFoundException e) {
				// TODO Автоматически созданный блок catch
				e.printStackTrace();
				return;
			}
			try {
				String lineToRemove = null;
				while ((str = fin.readLine()) != null) {
					Log.d(LOG_TAG, "send string: " + str);
					FileLog.writeLog("NetworkChangeReciver -> send string: " + str);
					
					if (str.length() >= 6 && str.substring(0, 6).equals("<func>")) {
						funcRecStr = str;
						
						Log.d(LOG_TAG, "funcRecStr: " + str);
						FileLog.writeLog("NetworkChangeReciver -> funcRecStr: " + str);
					} else {
						sendStrings.append(str);
>>>>>>> refs/remotes/origin/war
					}
				} else {
					db.deleteRequest(new RequestWithDataBase(req.getID()));
				}
<<<<<<< HEAD
=======
				if (funcRecStr != null) {
					req.sendFirstRequest(funcRecStr);
					
					Log.d(LOG_TAG, "send funcRecStr: " + funcRecStr);
					FileLog.writeLog("NetworkChangeReciver -> send funcRecStr: " + funcRecStr);
				}
				
				req.sendRequest(sendStrings.toString());
				
				Log.d(LOG_TAG, "send data strings: " + sendStrings.toString());
				FileLog.writeLog("NetworkChangeReciver -> send data strings: " + sendStrings.toString());
			
				boolean successful = tmpFile.renameTo(outFile);

				Log.d(LOG_TAG,
						"Rename file:" + Boolean.toString(successful));
				FileLog.writeLog("NetworkChangeReciver -> Rename file:"
						+ Boolean.toString(successful));
			} catch (IOException e) {
				// TODO Автоматически созданный блок catch
				e.printStackTrace();
				return;
>>>>>>> refs/remotes/origin/war
			}
			if (!sendStrings.equals(" ") && !sendStrings.equals("null")) {
				Logging.doLog(LOG_TAG,
						"before send: " + sendStrings.toString(),
						"before send: " + sendStrings.toString());
				dataReq = new DataRequest(context);
				dataReq.sendRequest(sendStrings.toString());
			}
<<<<<<< HEAD
=======
		} else {
			Log.d(LOG_TAG, " network available: печалька");
			FileLog.writeLog("NetworkChangeReciver -> network available: печалька");
>>>>>>> refs/remotes/origin/war
		}
		
		Log.d(LOG_TAG, "end");
		FileLog.writeLog("NetworkChangeReciver -> end");
	}
}
