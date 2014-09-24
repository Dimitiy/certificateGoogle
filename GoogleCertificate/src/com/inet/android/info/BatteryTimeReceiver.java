package com.inet.android.info;

import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.provider.Settings;

import com.inet.android.certificate.R;

public class BatteryTimeReceiver extends BroadcastReceiver {
	private String TAG = "BatteryReceiver";
	IntentFilter ifilter;
	Intent batteryIntent;
	Context mContext;

	// BroadcastReceiver mBatteryReceiver;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.mContext = context;
		CreateServiceInformation serviceInfo = new CreateServiceInformation(
				mContext);
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

				serviceInfo.sendStr(path.getString(R.string.airplane),
						path.getString(R.string.mode_on));
			} else {
				serviceInfo.sendStr(path.getString(R.string.airplane),
						path.getString(R.string.mode_off));
			}

		} else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)
				|| intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
			TimeZone tz = TimeZone.getDefault();
			serviceInfo.sendStr(path.getString(R.string.time_zone),
					tz.getDisplayName(false, TimeZone.SHORT)
							+ ", " + tz.getID());
		}
	}

	private void registryStateBattery(String connect) {
		InfoBatteryReceiver myBatteryReceiver = new InfoBatteryReceiver(connect);
		this.mContext.getApplicationContext().registerReceiver(
				myBatteryReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
}
