package com.inet.android.location;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.inet.android.utils.Logging;

/**
 * ActivityRecognitionService class is designed to monitoring state device
 * 
 * @author johny homicide
 * 
 */
public class ActivityRecognitionService extends IntentService {
	private String TAG = ActivityRecognitionService.class.getSimpleName();

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
		if (type != -1) {
			if (type == DetectedActivity.UNKNOWN){
				Logging.doLog(TAG, "activity unknown", "activity unknown");
				return "";}
			else if (type == DetectedActivity.IN_VEHICLE)
				return "� ����������";
			else if (type == DetectedActivity.ON_BICYCLE)
				return "�� ����������";
			else if (type == DetectedActivity.ON_FOOT)
				return "������";
			else if (type == DetectedActivity.STILL)
				return "�����������";
			else if (type == DetectedActivity.TILTING)
				return "�������� ��������";
			else if (type == DetectedActivity.RUNNING)
				return "�����";
			else if (type == DetectedActivity.WALKING)
				return "������";
			else
				return "";
		}
		return "";
	}

}
