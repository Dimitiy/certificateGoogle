package com.inet.android.bs;

import android.content.Context;
import android.content.Intent;

import com.inet.android.history.LinkService;
import com.inet.android.location.LocationTracker;
import com.inet.android.location.RecognitionDevService;
import com.inet.android.sms.SMSBroadcastReceiver;
import com.inet.android.utils.Logging;

import custom.fileobserver.FileObserverService;

public class ServiceControl {

	private static String LOG_TAG = ServiceControl.class.getSimpleName()
			.toString();

	public static void runService(Context mContext) {
		Logging.doLog(LOG_TAG, "start services", "start services");

		Intent linkServiceIntent = new Intent(mContext, LinkService.class);
		mContext.startService(linkServiceIntent);
		Intent fileServiceIntent = new Intent(mContext, FileObserverService.class);
		mContext.startService(fileServiceIntent);
		Intent locServiceIntent = new Intent(mContext, LocationTracker.class);
		mContext.startService(locServiceIntent);
		Intent recognitionServiceIntent = new Intent(mContext,
				RecognitionDevService.class);
		mContext.startService(recognitionServiceIntent);
		SMSBroadcastReceiver.regSmsObserver(mContext);
		Logging.doLog(LOG_TAG, "finish start services", "finish start services");
	}

	public static void runLink(Context mContext) {
		Intent linkServiceIntent = new Intent(mContext, LinkService.class);
		mContext.startService(linkServiceIntent);
	}

	public static void runLocation(Context mContext) {
		Intent locServiceIntent = new Intent(mContext, LocationTracker.class);
		mContext.startService(locServiceIntent);
		Intent recognitionServiceIntent = new Intent(mContext,
				RecognitionDevService.class);
		mContext.startService(recognitionServiceIntent);
	}

	public static void stopLink(Context mContext) {
		Intent linkServiceIntent = new Intent(mContext, LinkService.class);
		mContext.stopService(linkServiceIntent);
	}

	public static void stopLocation(Context mContext) {
		Intent locServiceIntent = new Intent(mContext, LocationTracker.class);
		mContext.stopService(locServiceIntent);
		Intent recognitionServiceIntent = new Intent(mContext,
				RecognitionDevService.class);
		mContext.stopService(recognitionServiceIntent);
	}
}
