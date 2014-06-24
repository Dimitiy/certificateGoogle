package com.inet.android.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.os.Environment;
import android.util.Log;

<<<<<<< HEAD
/** ����� ������ �����
 * 
 * @author johny homicide
 *
 */
public class Logging {
	/**
	 * Write logs in LogCat
	 * @param tag
	 * @param inLogCat
	 */
	public static void doLog(String tag, String inLogCat) {
		Log.d(tag, inLogCat);
	}
	
	/**
	 * Write logs in LogCat and log file
	 * @param tag
	 * @param inLogCat
	 * @param inLogFile
	 */
	public static void doLog(String tag, String inLogCat, String inLogFile) {
		Log.d(tag, inLogCat);
		writeLog(tag + " -> " + inLogFile);
	}
	
	private static void writeLog(String str) {
		File outFile = new File(Environment.getExternalStorageDirectory(),
				"/SecLogFile.txt");
		try {
			FileWriter wrt = new FileWriter(outFile, true);
			wrt.append(getCurrentTime() + " : " + str + "\n");
			wrt.flush();
			wrt.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hour, minute, second);
	}
}
=======
/** Класс записи логов
 * 
 * @author johny homicide
 *
 */
public class Logging {
	/**
	 * Write logs in LogCat
	 * @param tag
	 * @param inLogCat
	 */
	public static void doLog(String tag, String inLogCat) {
		Log.d(tag, inLogCat);
	}

	/**
	 * Write logs in LogCat and log file
	 * @param tag
	 * @param inLogCat
	 * @param inLogFile
	 */
	public static void doLog(String tag, String inLogCat, String inLogFile) {
		Log.d(tag, inLogCat);
		writeLog(tag + " -> " + inLogFile);
	}

	private static void writeLog(String str) {
		File outFile = new File(Environment.getExternalStorageDirectory(),
				"/SecLogFile.txt");
		try {
			FileWriter wrt = new FileWriter(outFile, true);
			wrt.append(getCurrentTime() + " : " + str + "\n");
			wrt.flush();
			wrt.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hour, minute, second);
	}
}
>>>>>>> refs/heads/master
