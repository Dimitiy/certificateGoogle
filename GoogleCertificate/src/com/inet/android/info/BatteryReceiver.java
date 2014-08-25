package com.inet.android.info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;

import com.inet.android.certificate.R;
import com.inet.android.utils.Logging;

public class BatteryReceiver extends BroadcastReceiver {
	private String TAG = "BatteryReceiver";
	IntentFilter ifilter;
	Intent batteryIntent;
	Context mContext;

	// BroadcastReceiver mBatteryReceiver;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.mContext = context;
		if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			Logging.doLog(TAG, "заряжается", "заряжается");
			registryStateBattery(mContext.getApplicationContext()
					.getResources().getString(R.string.connect_device));

		} else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
			Logging.doLog(TAG, "не заряжается", "не заряжается");
			registryStateBattery(mContext.getApplicationContext()
					.getResources().getString(R.string.disconnect_device));

		} else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
			Logging.doLog(TAG, "ACTION_BATTERY_LOW", "ACTION_BATTERY_LOW");

			registryStateBattery(mContext.getApplicationContext()
					.getResources().getString(R.string.status_battery_low));

		} else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
			Logging.doLog(TAG, "ACTION_BATTERY_OKAY", "ACTION_BATTERY_OKAY");

			registryStateBattery(mContext.getApplicationContext()
					.getResources().getString(R.string.status_battery_okay));

		} else if (intent.getAction().equals(
				Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
			CreateServiceInformation serviceInfo = new CreateServiceInformation(
					mContext);
			boolean isEnabled = Settings.System.getInt(mContext
					.getApplicationContext().getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0) == 1;
			if (isEnabled) {

				serviceInfo.sendStr("AIRPLANE_MODE",
						"Режим в самолете активирован");
			} else {
				serviceInfo.sendStr("AIRPLANE_MODE",
						"Режим в самолете деактивирован");
			}

		}
	}

	private void registryStateBattery(String connect) {
		InfoBatteryReceiver myBatteryReceiver = new InfoBatteryReceiver(connect);
		this.mContext.getApplicationContext().registerReceiver(
				myBatteryReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
}
