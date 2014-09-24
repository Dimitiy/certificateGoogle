package com.inet.android.location;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * ActivityRecognitionService class is designed to monitoring state device
 * 
 * @author johny homicide
 * 
 */
public class ActivityRecognitionService extends IntentService {

	public ActivityRecognitionService() {
		super("My Activity Recognition Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult
					.extractResult(intent);

			Intent i = new Intent(
					"com.inet.android.location.ACTIVITY_RECOGNITION_DATA");
			i.putExtra("Activity", getType(result.getMostProbableActivity()
					.getType()));
			i.putExtra("Confidence", result.getMostProbableActivity()
					.getConfidence());
			sendBroadcast(i);
		}
	}

	private String getType(int type) {
		if (type == DetectedActivity.UNKNOWN)
			return null;
		else if (type == DetectedActivity.IN_VEHICLE)
			return "В автомобиле";
		else if (type == DetectedActivity.ON_BICYCLE)
			return "На велосипеде";
		else if (type == DetectedActivity.ON_FOOT)
			return "Пешком";
		else if (type == DetectedActivity.STILL)
			return "Неподвижное";
		else if (type == DetectedActivity.TILTING)
			return "Вращение аппарата";
		else if (type == DetectedActivity.RUNNING)
			return "Бегом";
		else if (type == DetectedActivity.WALKING)
			return "Ходьба";
		else
			return "";
	}

}
