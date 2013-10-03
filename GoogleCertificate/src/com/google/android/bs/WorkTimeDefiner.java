package com.google.android.bs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class WorkTimeDefiner {
	private static SharedPreferences sp;
	private static String LOG_TAG = "isDoWork";
	private static String LOG_TAG_2 = "diagRequest";
	static File logFile;
	static FileWriter lwrt;

	public static boolean isDoWork(String begTime, String endTime,
			String begBrkTime, String endBrkTime) {
		Log.d(LOG_TAG, "begin");
		FileLog.writeLog("begin");

		Calendar calendar = Calendar.getInstance();

		// ������� �����
		int calendarHour = calendar.get(Calendar.HOUR_OF_DAY);
		int calendarMinute = calendar.get(Calendar.MINUTE);
		int currentTime = calendarMinute + calendarHour * 60;

		// ����� �������� ���
		int begWorkTime = Integer.parseInt(begTime.substring(begTime
				.indexOf(":") + 1))
				+ Integer.parseInt(begTime.substring(0, begTime.indexOf(":")))
				* 60;

		int endWorkTime = Integer.parseInt(endTime.substring(endTime
				.indexOf(":") + 1))
				+ Integer.parseInt(endTime.substring(0, endTime.indexOf(":")))
				* 60;

		// ����� ��������
		int begBreakTime = Integer.parseInt(begBrkTime.substring(begBrkTime
				.indexOf(":") + 1))
				+ Integer.parseInt(begBrkTime.substring(0,
						begBrkTime.indexOf(":"))) * 60;

		int endBreakTime = Integer.parseInt(endBrkTime.substring(endBrkTime
				.indexOf(":") + 1))
				+ Integer.parseInt(endBrkTime.substring(0,
						endBrkTime.indexOf(":"))) * 60;

		if (begWorkTime > endWorkTime) {
			if (currentTime < endWorkTime) {
				currentTime += 3600;
			}

			endWorkTime += 3600;
		}

		if (begBreakTime > endBreakTime) {
			endBreakTime += 3600;
		}

		if (currentTime >= begWorkTime && currentTime <= endWorkTime) {
			if (currentTime >= begBreakTime && currentTime < endBreakTime) {
				Log.d(LOG_TAG, "return break false");
				FileLog.writeLog("return break false");
				return false;
			}
			Log.d(LOG_TAG, "return true");
			FileLog.writeLog("return true");
			return true;
		} else {
			Log.d(LOG_TAG, "return time false");
			FileLog.writeLog("return time false");
			return false;
		}
	}

	public static boolean isDoWork(Context ctx) {
		Log.d(LOG_TAG, "begin");
		FileLog.writeLog("diagRequest: begin");

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		String timeFrom = sp.getString("TIME_FR", "00:00");
		String timeTo = sp.getString("TIME_TO", "23:59");
		String brkTimeFrom = sp.getString("BRK1_FR", "00:00");
		String brkTimeTo = sp.getString("BRK1_TO", "00:00");

		if (timeFrom.equals("")) {
			timeFrom = "00:00";
		}
		if (timeTo.equals("")) {
			timeTo = "00:00";
		}

		if (brkTimeFrom.equals("")) {
			brkTimeFrom = "00:00";
		}
		if (brkTimeTo.equals("")) {
			brkTimeTo = "00:00";
		}

		Calendar calendar = Calendar.getInstance();

		// ������� �����
		int calendarHour = calendar.get(Calendar.HOUR_OF_DAY);
		int calendarMinute = calendar.get(Calendar.MINUTE);
		int currentTime = calendarMinute + calendarHour * 60;

		// ����� �������� ���
		int begWorkTime = Integer.parseInt(timeFrom.substring(timeFrom
				.indexOf(":") + 1))
				+ Integer
						.parseInt(timeFrom.substring(0, timeFrom.indexOf(":")))
				* 60;

		int endWorkTime = Integer
				.parseInt(timeTo.substring(timeTo.indexOf(":") + 1))
				+ Integer.parseInt(timeTo.substring(0, timeTo.indexOf(":")))
				* 60;

		// ����� ��������
		int begBreakTime = Integer.parseInt(brkTimeFrom.substring(brkTimeFrom
				.indexOf(":") + 1))
				+ Integer.parseInt(brkTimeFrom.substring(0,
						brkTimeFrom.indexOf(":"))) * 60;

		int endBreakTime = Integer.parseInt(brkTimeTo.substring(brkTimeTo
				.indexOf(":") + 1))
				+ Integer.parseInt(brkTimeTo.substring(0,
						brkTimeTo.indexOf(":"))) * 60;

		if (begWorkTime > endWorkTime) {
			if (currentTime < endWorkTime) {
				currentTime += 3600;
			}

			endWorkTime += 3600;
		}

		if (begBreakTime > endBreakTime) {
			endBreakTime += 3600;
		}

		if (currentTime >= begWorkTime && currentTime <= endWorkTime) {
			if (currentTime >= begBreakTime && currentTime < endBreakTime) {
				Log.d(LOG_TAG, "return break false");
				FileLog.writeLog("isDoWork: return break false");

				return false;
			}
			Log.d(LOG_TAG, "return true");
			FileLog.writeLog("isDoWork: return true");

			return true;
		} else {
			Log.d(LOG_TAG, "return time false");
			FileLog.writeLog("isDowWork: return time false");

			return false;
		}
	}

	public static void diagRequest(Context ctx) {
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		logFile = new File(Environment.getExternalStorageDirectory(),
				"/LogFile.txt");
		try {
			lwrt = new FileWriter(logFile, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String diag = "<packet><id>" + sp.getString("ID", "ID") + "</id><time>"
				+ logTime() + "</time><type>1</type><ttl>"
				+ sp.getString("BUILD", "A0003 2013-10-03 20:00:00")
				+ "</ttl><cls>" + sp.getString("IMEI", "0000")
				+ "</cls><app>��������������� ����������</app><url>"
				+ Long.toString(System.currentTimeMillis())
				+ sp.getString("ABOUT", "about") + "</url></packet>";
		String str = "<func>getinfo</func><username>"
				+ sp.getString("phoneNumber", "00000000000")
				+ "</username><id>" + sp.getString("ID", "tel") + "</id>";
		String action = null;
		try {
			lwrt.append("sendDiagPost: " + str + "\n");
			lwrt.append("sendStartPost: " + diag + "\n");
		} catch (IOException e1) {
			// TODO ������������� ��������� ���� catch
			e1.printStackTrace();
		}
		try {
			lwrt.flush();
		} catch (IOException e1) {
			// TODO ������������� ��������� ���� catch
			e1.printStackTrace();
		}
		do {
			Log.d(LOG_TAG_2, "before req");
			FileLog.writeLog("diagRequest: before req");

			Request req = new Request(ctx);
			req.sendFirstRequest(str);
			req.sendRequest(diag);
			Log.d(LOG_TAG_2, "post req");
			FileLog.writeLog("diagRequest: post req");

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			action = sp.getString("ACTION", "PAUSE");
			if (action.equals("PAUSE")) {
				try {
					TimeUnit.MILLISECONDS.sleep(300000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (action.equals("STOP")) {
				Log.d(LOG_TAG_2, "WAT?????");
				FileLog.writeLog("diagRequest: WAT?????");

				break;
			}
		} while (!action.equals("OK") && !action.equals("REMOVE"));

		if (action.equals("REMOVE")) {
			Log.d(LOG_TAG_2, "REMOVE");
			FileLog.writeLog("diagRequest: REMOVE");
		}

		Log.d(LOG_TAG_2, "action - " + action);
		FileLog.writeLog("diagRequest: action - " + action);
	}

	@SuppressLint("SimpleDateFormat")
	private static String logTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return "" + formatter.format(cal.getTime());

	}
}
