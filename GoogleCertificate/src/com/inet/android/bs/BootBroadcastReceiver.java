package com.inet.android.bs;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inet.android.history.LinkService;
import com.inet.android.info.GetInfo;
import com.inet.android.location.LocationTracker;
import com.inet.android.utils.Logging;

public class BootBroadcastReceiver extends BroadcastReceiver {

	Context mContext;
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	SharedPreferences sp;
	private static final String LOG_TAG = "BootBroadcastReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Logging.doLog("BootBroadCastReceiver", "onReceive", "onReceive");
		mContext = context;
		sp = PreferenceManager
				.getDefaultSharedPreferences(context);
	
		String action = intent.getAction();
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			// ��� Service
			
			Log.d(LOG_TAG, "start services after boot");
			FileLog.writeLog(LOG_TAG + " -> start services after boot");
			
			Intent linkServiceIntent = new Intent(context, LinkService.class);
			context.startService(linkServiceIntent);
			Intent locServiceIntent = new Intent(context, LocationTracker.class);
			context.startService(locServiceIntent);
			GetInfo getInfo = new GetInfo(mContext);
			getInfo.getInfo();
//			Intent request4ServiceIntent = new Intent(context, Request4.class);
//			context.startService(request4ServiceIntent);

<<<<<<< HEAD
=======
			Request req = new Request(context);
			req.sendRequest(diag);
			Log.d(LOG_TAG, "end start services after boot");
			FileLog.writeLog(LOG_TAG + " -> end start services after boot");
>>>>>>> refs/remotes/origin/war
		}
	}

	@SuppressLint("SimpleDateFormat")
	private static String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}
