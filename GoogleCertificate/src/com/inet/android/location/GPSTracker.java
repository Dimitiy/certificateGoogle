package com.inet.android.location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.inet.android.bs.FileLog;
import com.inet.android.bs.MainActivity;
import com.inet.android.bs.Request;
import com.inet.android.bs.WorkTimeDefiner;
import com.inet.android.sms.SMSBroadcastReceiver;
import com.inet.android.sms.SmsSentObserver;

public class GPSTracker extends Service implements LocationListener {

	private static final String TAG = "locationService";
	// flag for GPS status
	boolean isGPSEnabled = false;
	SharedPreferences sp;
	// flag for network status
	boolean isNetworkEnabled = false;
	String bestProvider = null;
	// flag for GPS status
	boolean canGetLocation = false;
	private Context context;
	Editor e;
	Location location; // location
	double latitude; // latitude
	double longitude; // longitude
	String locMetod;
	MainActivity simInfo;
	String ID;
	String nameId;
	int minute;
	private static final int SERVICE_REQUEST_CODE = 15;
	Request req;
	String provider;
	StringBuilder sendStrings;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 100
																	// meters

	// The minimum time between updates in milliseconds
	private static long MIN_TIME_BW_UPDATES = 1000 * 30 * 1; // 30 minute
	private static long MIN_TIME_BW_UPDATES1 = 1000 * 60 * 60 * 24; // 24 часа
	// Declaring a Location Manager
	protected LocationManager locationManager;

	private int timeUp = 0;
	private SmsSentObserver smsSentObserver = null;
	
	@Override
	public void onCreate() {
		Log.d(TAG, ">>>onCreate() GPS");
		FileLog.writeLog("locationManager: >>>onCreate() GPS");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		contentObserved();
		Log.d(TAG, "onStartCommand gpsTracker");
		FileLog.writeLog("locationManager: onStartCommand gpsTracker");

		sp = PreferenceManager.getDefaultSharedPreferences(context);
		String gpsEnd = sp.getString("ACTION", "OK");
		MIN_TIME_BW_UPDATES = Integer.parseInt(sp.getString("GEO", "5")) * 1000 * 60;
		timeUp = Integer.parseInt(sp.getString("GEO", "5"));
		if (gpsEnd.equals("REMOVE")) {
			Log.d(TAG, "REMOVE");
			FileLog.writeLog("locationManager: REMOVE");
			return 0;
		}
		Log.d(TAG, ">>>onStartCommand()");
		FileLog.writeLog("locationManager: " + ">>>onStartCommand()");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, timeUp);// через 5 минут
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, GPSTracker.class),// SERVICE_REQUEST_CODE
																			// -
																			// уникальный
																			// int
																			// сервиса
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);
		boolean isWork = WorkTimeDefiner.isDoWork(getApplicationContext());
		if (!isWork) {
			Log.d(TAG, "isWork return " + Boolean.toString(isWork));
			Log.d(TAG, "after isWork retrun 0");
			FileLog.writeLog("locationManager: isWork return "
					+ Boolean.toString(isWork));
			FileLog.writeLog("locationManager: after isWork retrun 0");
			return 0;
		} else {
			Log.d(TAG, Boolean.toString(isWork));
			FileLog.writeLog("locationManager: isWork - "
					+ Boolean.toString(isWork));
		}
		getLocation();
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) context
					.getSystemService(LOCATION_SERVICE);
			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			Log.d("isGPS", Boolean.toString(isGPSEnabled));

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			Log.d("isNetwork", Boolean.toString(isNetworkEnabled));

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				Log.d(TAG, "!isGPS&&isNetwork");
				sendNoLoc();
			} else {
				this.canGetLocation = true;
				getLast();
				// if GPS Enabled get lat/long using GPS Services
				if (isOnline() == true) {
					Log.d("Network", "isOnline");
					if (isNetworkEnabled == true) {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("Network", "Network");
					}
				}
			}
			if (Double.toString(latitude).equals("0.0")
					&& Double.toString(longitude).equals("0.0")) {
				Log.d(TAG, "equaels = 0.0");
				sendNoLoc();
			} else
				Log.d(TAG, "equaels good");
			sendLoc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void getLast() {
		float accuracy;
		long time;

		Location bestResult = null;
		long bestTime = 0;
		float bestAccuracy = 1000;
		long minTime = 0;
		List<String> matchingProviders = locationManager.getAllProviders();
		Log.d("LocMan", matchingProviders.toString());
		sendStrings = new StringBuilder();
		for (String provider : matchingProviders) {
			location = locationManager.getLastKnownLocation(provider);
			Log.d(TAG, "last location" + provider);
			if (location != null) {
				accuracy = location.getAccuracy();
				time = location.getTime();
				SimpleDateFormat TIMESTAMP1 = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");

				// Выводим Дату по шаблону
				String date2 = TIMESTAMP1.format(time);
				sendStrings.append("<" + provider + ">" + "<t>" + date2
						+ "</t>" + "<a>" + accuracy + "</a>" + "<c>"
						+ location.getLatitude() + " "
						+ location.getLongitude() + "</c>" + "</" + provider
						+ ">");
				Log.d(TAG, "last update" + provider + " " + date2);
				long timeSys = System.currentTimeMillis();
				minTime = timeSys - MIN_TIME_BW_UPDATES1;
				Log.d("sravnenie", Boolean.toString(time > minTime) + minTime);
				Log.d("time + accuracy",
						Long.toString(time) + " " + Float.toString(accuracy));
				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;
					SimpleDateFormat TIMESTAMP = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

					// Выводим Дату по шаблону
					String date1 = TIMESTAMP.format(bestTime);
					Log.d(TAG, "bestAccuracy" + date1 + " " + accuracy + " "
							+ bestResult);
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE
						&& time > bestTime) {
					bestResult = location;
					bestTime = time;
					Log.d(TAG, "else " + Long.toString(bestTime) + " "
							+ bestResult);
				}
				if (bestResult != null) {
					locMetod = location.getProvider();
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					Log.d(TAG, "locationNet != null " + locMetod);

				}

			}

		}

	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public void netLoc() {
		// locMetod = "network";
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
		Log.d("Network netLoc", "Network");
		if (locationManager != null) {

			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Log.d(TAG, "locationMannet != null ");

			if (location != null) {
				if (Double.toString(latitude).equals("0.0")
						&& Double.toString(longitude).equals("0.0")) {

				} else {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					Log.d(TAG, "locationNet != null ");
				}
			}
		}
	}

	public void sendLoc() {
		if (Double.toString(latitude).equals("0.0")
				&& Double.toString(longitude).equals("0.0")) {
			Log.d(TAG, "equaels = 0.0");
			sendNoLoc();
		} else {

			sp = PreferenceManager.getDefaultSharedPreferences(this);
			String sendStr = "<packet><id>" + sp.getString("ID", "ID")
					+ "</id><time>" + logTime()
					+ "</time><type>5</type><cdata1>" + sendStrings
					+ "Last Update network: " + getLatitude() + " "
					+ getLongitude() + "</cdata1><ttl>" + locMetod
					+ "</ttl></packet>";

			req = new Request(context);
			req.sendRequest(sendStr);

			Log.d(TAG, sendStr);
			FileLog.writeLog("locationManager: " + sendStr);
		}
	}

	public void sendNoLoc() {
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String sendStr = "<packet><id>"
				+ sp.getString("ID", "ID")
				+ "</id><time>"
				+ logTime()
				+ "</time><type>5</type><app>координаты неизвестны"
				+ "</app><ttl>Определение местонахождения не поддерживается</ttl></packet>";

		req = new Request(context);
		req.sendRequest(sendStr);

		Log.d(TAG, sendStr);
		FileLog.writeLog("locationManager: " + sendStr);
	}

	@SuppressLint("SimpleDateFormat")
	private String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			Log.d(TAG, "loc change = network");
			locMetod = "network";
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			locationManager.removeUpdates(this);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		/* This is called when the GPS status alters */
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			Log.v(TAG, "Status Changed: Out of Service");

			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.v(TAG, "Status Changed: Temporarily Unavailable");

			break;
		case LocationProvider.AVAILABLE:
			Log.v(TAG, "Status Changed: Available");

			break;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	public void contentObserved() {
		if (smsSentObserver == null) {
			smsSentObserver = new SmsSentObserver(new Handler(), context);
			context.getContentResolver().registerContentObserver(
					Uri.parse("content://sms"), true, smsSentObserver);
			}
	}
}
