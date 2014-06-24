package com.inet.android.location;

import java.math.BigDecimal;

public class LocationValue {

	private double latitude; // latitude
	private double longitude; // longitude
	private float accuracy;
	private String provider;
	private int sats;
	private boolean canGetLocation;
	private boolean gpsEnable;
	private boolean gpsFix;
	private boolean gpsLoc;

	public LocationValue() {

	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		// return latitude
		return this.latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {

		// return longitude
		return this.longitude;
	}

	/**
	 * Function to set latitude
	 * */
	public void setLatitude(double latitude) {
		if (latitude != BigDecimal.ZERO.doubleValue()) {
			this.latitude = latitude;
		}
	}

	/**
	 * Function to set latitude
	 * */
	public void setLongitude(double longitude) {
		if (longitude != BigDecimal.ZERO.doubleValue()) {
			this.longitude = longitude;
		}
	}

	/**
	 * set provider
	 */
	public void setProvider(String provider) {
		if (provider != null) {
			this.provider = provider;
		}

	}

	/**
	 * get provider
	 */
	public String getProvider() {

		return provider;
	}

	/**
	 * set provider
	 */
	public void setAccuracy(float accuracy) {
		if (accuracy != BigDecimal.ZERO.doubleValue()) {

			this.accuracy = accuracy;
		}
	}

	/**
	 * get provider
	 */
	public float getAccuracy() {

		return accuracy;
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
	 * Function to check GPS enabled
	 * 
	 * @return
	 * 
	 * @return boolean
	 * */
	public void setGPSLocation(boolean gpsEnable) {
		this.gpsEnable = gpsEnable;
	}

	/**
	 * Function to get GPS enabled/disabled
	 * 
	 * @return boolean
	 * */
	public boolean getGPSLocation() {
		return this.gpsEnable;
	}

	/**
	 * Function to set GPSfix
	 * 
	 * @return boolean
	 * */
	public void setGPSFix(boolean gpsFix) {
		this.gpsFix = gpsFix;
	}

	/**
	 * Function to get GPSfix
	 * 
	 * @return boolean
	 * */
	public boolean getGPSFix() {
		return this.gpsFix;
	}

	public void setSatsAvailable(int sats) {
		this.sats = sats;
	}

	public int getSatsAvailable() {
		return sats;
	}

	public void setGPSLoc(boolean gpsLoc) {
		this.gpsLoc = gpsLoc;
	}

	public boolean getGPSLoc() {
		return gpsLoc;
	}
}
