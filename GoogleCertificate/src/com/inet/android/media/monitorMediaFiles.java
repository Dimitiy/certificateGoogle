package com.inet.android.media;

import java.io.File;
import java.util.ArrayList;

import com.inet.android.request.FileRequest;
import com.inet.android.utils.ConvertDate;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
/**
 * monitoringCreateMediaFiles. This class is responsible for the 
 * beginning and end of the monitoring media files creation in memory
 * 
 * @author johny homicide
 * 
 */
public class monitorMediaFiles {

	private FileWatcher mWatcher = null;
	final String TAG = "GetImage";
	private static monitorMediaFiles instance;
	private static boolean state = false;
	protected static Context mContext;
	private ConvertDate date;
	private String time;
	public static monitorMediaFiles getInstance(Context ctx) {
		monitorMediaFiles localInstance = instance;
		mContext = ctx;
		if (localInstance == null) {
			synchronized (monitorMediaFiles.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new monitorMediaFiles();
				}
			}
		}
		return localInstance;
	}

	public boolean State() {
		return state;
	}
	/*
	 * Start monitoring the creation of files in memory
	 */
	public void StartWatcher() {
		state = true;
		date = new ConvertDate();
		
		String sdcard = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String sdcard2 = "/sdcard2";
		String sdcardMnt = "/mnt/sdcard2";
		String sdcardExt = "/mnt/extSdCard";
		String sdcardSd = "/sdcard/.externalSD";

		ArrayList<String> dirList = new ArrayList<String>();
		dirList.add(sdcard);
		dirList.add(sdcard2);
		dirList.add(sdcardMnt);
		dirList.add(sdcardExt);
		dirList.add(sdcardSd);
	
		for (String item : dirList) {
			File file = new File(item);
			if (file.exists() && file.isDirectory()) {
				mWatcher = new FileWatcher(item, true, FileWatcher.CREATE, mContext);
				mWatcher.setFileListener(mFileListener);
				mWatcher.startWatching();
			}
		}
		
	}
	FileListener mFileListener = new FileListener() {
		
		@Override
		public void onFileCreated(String path) {
			Log.i(TAG, "onFileCreated " + path);
			time = date.logTime();
			FileRequest file = new FileRequest(mContext, path, time);
			file.sendRequest();
		}

		@Override
		public void onFileDeleted(String path) {
			Log.i(TAG, "onFileDeleted " + path);
		}

		@Override
		public void onFileModified(String path) {
			Log.i(TAG, "onFileModified " + path);
		}

		@Override
		public void onFileRenamed(String oldName, String newName) {
			Log.i(TAG, "onFileRenamed from: " + oldName + " to: " + newName);
		}

	};
	/*
	 * Stop monitoring the creation of files in memory
	 */
	public void StopWatcher() {
		state = false;
		if (mWatcher != null)
			mWatcher.stopWatching();
	}
}