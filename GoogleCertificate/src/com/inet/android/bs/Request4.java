package com.inet.android.bs;

import java.util.Calendar;

import com.inet.android.utils.Logging;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/** Класс сервиса отправки периодического запроса
 * 
 * @author johny homicide
 *
 */
public class Request4 extends Service {

		private static final int SERVICE_REQUEST_CODE = 35;
		final String LOG_TAG = "request4";
		SharedPreferences sPref;
		int period = 10; // периодичность запросов

		public void onCreate() {
			super.onCreate();

			Logging.doLog(LOG_TAG, "onCreate", "onCreate");
		}

		public int onStartCommand(Intent intent, int flags, int startId) {
			Logging.doLog(LOG_TAG, "onStartCommand", "onStartCommand");
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String linkEnd = sp.getString("ACTION", "OK");

			if (linkEnd.equals("REMOVE")) {
				Logging.doLog(LOG_TAG, "REMOVE", "REMOVE");
				return 0;
			}
			
			requestTask(); // запрос каждые 4 часа

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, period);// через period минут

			PendingIntent servicePendingIntent = PendingIntent.getService(this,
					SERVICE_REQUEST_CODE, new Intent(this, Request4.class),
					PendingIntent.FLAG_UPDATE_CURRENT);

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
					servicePendingIntent);
			
			super.onStartCommand(intent, flags, startId);
			return Service.START_STICKY;
		}

		private void requestTask() {
			Logging.doLog(LOG_TAG, "requestTask start", "requestTask start");
			
			RequestMakerImpl.diagRequest(getApplicationContext());
			
			Logging.doLog(LOG_TAG, "requestTask end", "requestTask end");
		}

		public void onDestroy() {
			super.onDestroy();
			Logging.doLog(LOG_TAG, "onDestroy", "onDestroy");
		}

		public IBinder onBind(Intent intent) {
			return null;
		}

		
	}