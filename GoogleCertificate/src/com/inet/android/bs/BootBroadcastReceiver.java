package com.inet.android.bs;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.inet.android.certificate.R;
import com.inet.android.history.LinkService;
import com.inet.android.info.CreateServiceInformation;
import com.inet.android.info.GetInfo;
import com.inet.android.location.LocationTracker;
import com.inet.android.location.RecognitionDevService;
import com.inet.android.utils.Logging;

public class BootBroadcastReceiver extends BroadcastReceiver {

	Context mContext;
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	SharedPreferences sp;

	@Override
	public void onReceive(Context context, Intent intent) {
		Logging.doLog("BootBroadCastReceiver", "onReceive", "onReceive");
		mContext = context;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Resources path = mContext.getApplicationContext().getResources();
		String area = path.getString(R.string.device);
		String action = intent.getAction();
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			CreateServiceInformation serviceInfo = new CreateServiceInformation(
					mContext);
			serviceInfo.sendStr(area, path.getString(R.string.boot));
			// for Service
			Intent linkServiceIntent = new Intent(mContext, LinkService.class);
			mContext.startService(linkServiceIntent);
			Intent locServiceIntent = new Intent(mContext,
					LocationTracker.class);
			mContext.startService(locServiceIntent);
			Intent recognitionServiceIntent = new Intent(mContext,
					RecognitionDevService.class);
			mContext.startService(recognitionServiceIntent);
			GetInfo getInfo = new GetInfo(mContext);
			getInfo.startGetInfo();
		}
		if (action.equalsIgnoreCase(Intent.ACTION_REBOOT)) {
			CreateServiceInformation serviceInfo = new CreateServiceInformation(
					mContext);
			serviceInfo.sendStr(area, "Reboot");

		}
	}
}
