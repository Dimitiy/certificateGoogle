package com.inet.android.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Class to hide a icon of the application.
 * @author johny homicide
 *
 */
public class HiddingIcon {
	Context ctx;
	public HiddingIcon(Context ctx) {
		this.ctx = ctx;
	}
	
	public void hideIcon() {
		ComponentName componentToDisable = new ComponentName(
				"com.inet.android.certificate",
				"com.inet.android.bs.MainActivity");

		ctx.getPackageManager().setComponentEnabledSetting(componentToDisable,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}
}
