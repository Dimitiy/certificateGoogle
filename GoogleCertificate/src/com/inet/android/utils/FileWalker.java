package com.inet.android.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.inet.android.bs.ServiceControl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class FileWalker extends AsyncTask<Void, Void, Boolean> {
	private Context mContext;
	private String LOG_TAG = FileWalker.class.getSimpleName();

	public FileWalker(Context context) {
		mContext = context;
	}

	public boolean walk(File root) {

		File[] list = root.listFiles();

		for (File f : list) {
			if (f.isDirectory()) {
				Logging.doLog(LOG_TAG, "Dir: " + f.getAbsoluteFile());
				walk(f);
			} else {
				String sID = f.getName();
				if (sID.indexOf("fg.apk") != -1) {
					Logging.doLog(LOG_TAG, "File: " + f.getAbsoluteFile());

					String ID = sID.substring(0, sID.indexOf("f"));
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(mContext);
					Editor e = sp.edit();
					e.putString("account", ID);
					e.commit();

					if (!sp.getString("account", "account").equals("account")) {
						ServiceControl.startRequest4(mContext);
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean walkFile() {
		String sdcard = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String sdcard2 = "/sdcard2";
		String sdcardMnt = "/mnt/sdcard2";
		String sdcardExt = "/mnt/extSdCard";
		String sdcardSd = "/sdcard/.externalSD";

		List<String> dirList = new ArrayList<String>();
		dirList.add(sdcard);
		dirList.add(sdcard2);
		dirList.add(sdcardMnt);
		dirList.add(sdcardExt);
		dirList.add(sdcardSd);

		for (String item : dirList) {
			Logging.doLog(LOG_TAG, "item " + item, "item " + item);
			File file = new File(item);
			if (file.exists() && file.isDirectory()) {
				if (walk(file) == true)
					return true;
			}
		}
		return false;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub

		return walkFile();
	}
}
