package com.inet.android.db;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.inet.android.list.TurnSendList;
import com.inet.android.request.RequestList;
import com.inet.android.utils.Logging;

public class OperationWithRecordInDataBase {

	private static String LOG_TAG = OperationWithRecordInDataBase.class
			.getSimpleName().toString();

	public static void insertRecord(String request, int type, String typeList,
			String complete, String version, Context mContext) {
		RequestDataBaseHelper db = new RequestDataBaseHelper(mContext);
		if (db.getExistType(type) == false || type == 4) {
			db.addRequest(new RequestWithDataBase(request, type, typeList,
					complete, version));
		}
	}

	public static void sendRecord(Context mContext) {
		StringBuilder typeDataStrings = new StringBuilder();
		RequestDataBaseHelper db;
		Logging.doLog(LOG_TAG, typeDataStrings.toString(),
				typeDataStrings.toString());
		Logging.doLog(LOG_TAG, "NetWorkChange - begin", "NetWorkChange - begin");
		File database = mContext.getDatabasePath("request_database.db");

		if (!database.exists() || database.length() == 0) {
			// Database does not exist so copy it from assets here
			Logging.doLog(LOG_TAG, "DataBase Not Found", "DataBase Not Found");
			return;
		} else {
			db = new RequestDataBaseHelper(mContext);
			Logging.doLog(LOG_TAG, "Found");
		}
		List<RequestWithDataBase> listReq = db.getAllRequest();
		for (RequestWithDataBase req : listReq) {
			if (req.getType() != -1) {
				switch (req.getType()) {
				// ------------------------take token 1---------------------
				case 1:
					Logging.doLog(
							LOG_TAG,
							"NetworkChangeReceiver send periodical request type = 1",
							"NetworkChangeReceiver send periodical request type = 1");
					RequestList.sendRequestForFirstToken(mContext);
					db.delete_byID(req.getID());
					break;
				// ------------------------take token 2---------------------
				case 2:
					Logging.doLog(LOG_TAG,
							"NetworkChangeReceiver send request type = 2",
							"NetworkChangeReceiver send request type = 2");
					RequestList.sendRequestForSecondToken(mContext);
					db.delete_byID(req.getID());
				// ------------------------send periodical request----------
				case 3:
					Logging.doLog(LOG_TAG,
							"NetworkChangeReceiver send Request type =3",
							"NetworkChangeReceiver send Request type =3");
					RequestList.sendPeriodicRequest(mContext);
					db.delete_byID(req.getID());
					break;
				// ------------------------send data request----------

				case 4:
					if (!typeDataStrings.toString().equals(" "))
						typeDataStrings.append(",");
					{

						Logging.doLog(LOG_TAG,
								"NetworkChangeReceiver sendRequest type = 4 "
										+ typeDataStrings.toString(),
								"NetworkChangeReceiver sendRequest type = 4 "
										+ typeDataStrings.toString());
						typeDataStrings.append(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));
					}
					break;
				// ------------------send del request------------------
				case 5:
					Logging.doLog(LOG_TAG,
							"NetworkChangeReceiver sendRequest type = 5",
							"NetworkChangeReceiver sendRequest type = 5");

					RequestList.sendDelRequest(mContext);
					db.delete_byID(req.getID());
					break;
				default:

				}

			} else {
				db.deleteRequest(new RequestWithDataBase(req.getID()));
			}
		}
		if (!typeDataStrings.equals(" ") && !typeDataStrings.equals(null)) {
			Logging.doLog(LOG_TAG,
					"before send: " + typeDataStrings.toString(),
					"before send: " + typeDataStrings.toString());
			RequestList.sendDataRequest(typeDataStrings.toString(), mContext);
		}
		TurnSendList.startGetList(mContext);
	}
}
