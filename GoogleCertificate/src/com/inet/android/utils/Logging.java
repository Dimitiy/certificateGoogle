package com.inet.android.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.os.Environment;
import android.util.Log;

/** Class logging
 * 
 * @author johny homicide
 *
 */
public class Logging {
	
	private final static long logFileSize = 5242880;
	
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
		long fileSize = outFile.length();
		
		if ((fileSize + str.length()) > logFileSize) {
			deleteStr(outFile, str.length());
		}
		
		try {
			FileWriter wrt = new FileWriter(outFile, true);
			wrt.append(getCurrentTime() + " : " + str + "\n");
			wrt.flush();
			wrt.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Delete the first string if log file size more then need.
	 * @param inputFile
	 * @param strLength
	 */
	private static void deleteStr(File inputFile, int strLength) {
		Log.d("logging", "log file size: " + inputFile.length());
		Log.d("logging", "str.length: " + strLength);
		
		File tempFile = new File(Environment.getExternalStorageDirectory(), 
				"myTempFile.txt");

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(tempFile));
			boolean firstString = true;
			String currentLine;

			while((currentLine = reader.readLine()) != null) {
			    if (firstString) {
			    	firstString = false;
			    	continue;
			    }
			    writer.write(currentLine.trim() + System.getProperty("line.separator"));
			}
			writer.close(); 
			reader.close();
			firstString = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		tempFile.renameTo(inputFile);
		
		if ((inputFile.length() + strLength) > logFileSize) {
			Log.d("logging", "One another. fileSize: " + inputFile.length());
			Log.d("logging", "One another. str.lentgh: " + strLength);
			deleteStr(inputFile, strLength);
		} else {
			Log.d("logging", "No another. fileSize: " + inputFile.length());
			Log.d("logging", "No another. str.lentgh: " + strLength);
		}
	}

	private static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        int ms = calendar.get(Calendar.MILLISECOND);
        return String.format("%02d.%02d.%04d %02d:%02d:%02d.%03d", 
        		day, month, year, hour, minute, second, ms);
	}
}