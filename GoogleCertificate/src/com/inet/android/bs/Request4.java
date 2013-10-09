package com.inet.android.bs;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;


public class Request4 extends Service {

		private static final int SERVICE_REQUEST_CODE = 35;
		final String LOG_TAG = "requestService";
		SharedPreferences sPref;

		public void onCreate() {
			super.onCreate();
			Log.d(LOG_TAG, "onCreate Request4");
			FileLog.writeLog("onCreate Request4");
		}

		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.d(LOG_TAG, "onStartCommand Request4");
			FileLog.writeLog("onStartCommand Request4");
			
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
			FileLog.writeLog("requestTask start");
			
			WorkTimeDefiner.diagRequest(getApplicationContext());
			
			Log.d(LOG_TAG, "requestTask end");
			FileLog.writeLog("requestTask end");
		}

		public void onDestroy() {
			super.onDestroy();
			Log.d(LOG_TAG, "onDestroy");
			FileLog.writeLog("onDestroy");
		}

		public IBinder onBind(Intent intent) {
			Log.d(LOG_TAG, "onBind");
			FileLog.writeLog("onBind");
			return null;
		}

		
	}