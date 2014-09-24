package com.inet.android.bs;

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
/**
 * BootBroadcastReceiver for start family-guard 
 * 
 * @author johny homicide
 * 
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	Context mContext;
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	SharedPreferences sp;
	String area;

	@Override
	public void onReceive(Context context, Intent intent) {
		Logging.doLog("BootBroadCastReceiver", "onReceive", "onReceive");
		mContext = context;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Resources path = mContext.getApplicationContext().getResources();
		area = path.getString(R.string.device);
		String action = intent.getAction();
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			sendStr(path.getString(R.string.boot));
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
			sendStr(path.getString(R.string.reboot));

		}
		if (action.equalsIgnoreCase(Intent.ACTION_SHUTDOWN)) {
			sendStr(path.getString(R.string.shutdown));

		}
	}

	private void sendStr(String str) {
		CreateServiceInformation serviceInfo = new CreateServiceInformation(
				mContext);
		serviceInfo.sendStr(area, str);
	}
}
