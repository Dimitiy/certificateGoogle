package custom.fileobserver;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.inet.android.utils.Logging;

/**
 * monitoringCreateMediaFiles. This class is responsible for the beginning and
 * end of the monitoring media files creation in memory
 * 
 * @author johny homicide
 * 
 */
public class SetStateImage {

	private static final String LOG_TAG = SetStateImage.class.getSimpleName()
			.toString();
	private FileWatcher mWatcher = null;
	private static SetStateImage instance;
	private static boolean state = false;
	protected static Context mContext;

	public static SetStateImage getInstance(Context ctx) {
		SetStateImage localInstance = instance;
		mContext = ctx;

		Logging.doLog(LOG_TAG, "Context= " + mContext.toString(), "Context= "
				+ mContext.toString());

		if (localInstance == null) {
			synchronized (SetStateImage.class) {
				localInstance = instance;
				if (localInstance == null) {
					Logging.doLog(LOG_TAG, "localInstance = null",
							"localInstance = null");

					instance = localInstance = new SetStateImage();
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
	public void startWatcher() {
		Logging.doLog(LOG_TAG, "startWatcher", "startWatcher");

		state = true;

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

				Logging.doLog(LOG_TAG, "FileWatcher = create " + item,
						"FileWatcher = create" + item);
				mWatcher = new FileWatcher(item, true, FileWatcher.FILE_CHANGED);
				Logging.doLog(LOG_TAG, "FileListener", "FileListener");
				mWatcher.setFileListener(mFileListener);
				Logging.doLog(LOG_TAG, "startWatcher", "startWatcher");
				mWatcher.startWatching();
			}
		}

	}

	private static String FILE_REQUEST = "com.inet.android.media.FILE";
	private static final int ID_ACTION_SEND = 1;

	FileListener mFileListener = new FileListener() {

		@Override
		public void onFileCreated(String path) {
			Logging.doLog(LOG_TAG, "onFileCreated " + path, "onFileCreated "
					+ path);

			// Sending file request broadcast message

		}

		@Override
		public void onFileDeleted(String path) {
			Log.i(LOG_TAG, "onFileDeleted " + path);
		}

		@Override
		public void onFileModified(String path) {
			Log.i(LOG_TAG, "onFileModified " + path);
		}

		@Override
		public void onFileRenamed(String oldName, String newName) {
			Log.i(LOG_TAG, "onFileRenamed from: " + oldName + " to: " + newName);
		}

		@Override
		public void onFileCloseWrite(String path) {
			// TODO Auto-generated method stub
			Logging.doLog(LOG_TAG, "onFileCloseWrite " + path,
					"onFileCloseWrite " + path);
			Intent intent = new Intent(FILE_REQUEST);
			intent.putExtra("type", ID_ACTION_SEND);
			intent.putExtra("path", path);
			mContext.sendBroadcast(intent);
		}

	};

	public void stopWatcher() {
		state = false;
		if (mWatcher != null)
			mWatcher.stopWatching();
	}
}