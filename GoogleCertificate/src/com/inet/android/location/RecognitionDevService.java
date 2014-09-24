package com.inet.android.location;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;
/**
 * RecognitionDevService class is designed for start ACTIVITY_RECOGNITION
 * 
 * @author johny homicide
 * 
 */
public class RecognitionDevService extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private BroadcastReceiver receiver;
	private String TAG = "RecognitionDevService";
	private ActivityRecognitionClient arclient;
	private static final int SERVICE_REQUEST_CODE = 27;
	SharedPreferences sp;
	static long MIN_TIME_BW_UPDATES; // get minute
	private int timeUp = 0;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
		if (!servicesAvailable()) {
			stopSelf();
		}

		arclient = new ActivityRecognitionClient(this, this, this);
		arclient.connect();
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String v = "Активность :" + intent.getStringExtra("Activity")
						+ " " + "Точность : "
						+ intent.getExtras().getInt("Confidence") + "\n";
				sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor ed = sp.edit();
				ed.putString("activity", v);
				ed.commit();
				Log.i(TAG, v + "  " + sp.getString("activity", "v"));

			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.inet.android.location.ACTIVITY_RECOGNITION_DATA");
		registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// ----------restart service
		// ---------------------------------------------------
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		// ----------get geo time
		// ---------------------------------------------------
		Logging.doLog(TAG, "onStartCommand ActivityRecognitionClient",
				"onStartCommand ActivityRecognitionClient");
		if (sp.getString("geo", "5").equals("0")) {
			Logging.doLog(TAG, "ActivityRecognitionClient Stop",
					"ActivityRecognitionClient Stop");
			stopSelf();
			return 0;
		}
		MIN_TIME_BW_UPDATES = Integer.parseInt(sp.getString("geo", "5")) * 1000 * 60;
		timeUp = Integer.parseInt(sp.getString("geo", "5"));
		// ----------restart service
		// ---------------------------------------------------

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, timeUp);// через 5 минут
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this,
						RecognitionDevService.class),// SERVICE_REQUEST_CODE
				// -
				// уникальный
				// int
				// сервиса
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);
		// ----------is work ---------------------------------------------------

		boolean isWork = WorkTimeDefiner.isDoWork(getApplicationContext());
		if (!isWork) {
			Logging.doLog(TAG, "isWork return " + Boolean.toString(isWork),
					"isWork return " + Boolean.toString(isWork));
			return 0;
		} else {
			Logging.doLog(TAG, Boolean.toString(isWork),
					Boolean.toString(isWork));
		}
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;

	}

	private boolean servicesAvailable() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d(TAG, "Google Play services is available.");
			return true;
		}
		Log.e(TAG, "Google Play services NOT available.");
		return false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, ActivityRecognitionService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		arclient.requestActivityUpdates(1 * 60 * 1000, pendingIntent);

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
}
