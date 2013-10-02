package com.google.android.bs;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class WorkTimeDefiner {
	 private static SharedPreferences sp;
	 private static String LOG_TAG = "isDoWork"; 
	 private static String LOG_TAG_2 = "diagRequest";

	public static boolean isDoWork(String begTime, String endTime, String begBrkTime, String endBrkTime) {
		Log.d(LOG_TAG, "begin");
		
		  Calendar calendar = Calendar.getInstance();
		  
		  // текущее время
		  int calendarHour = calendar.get(Calendar.HOUR_OF_DAY);
		  int calendarMinute = calendar.get(Calendar.MINUTE);
		  int currentTime = calendarMinute + calendarHour * 60;
//		  System.out.println(currentTime);
		  
		  // время рабочего дня
		  int begWorkTime = Integer.parseInt(begTime.substring(begTime.indexOf(":") + 1)) +
				  Integer.parseInt(begTime.substring(0, begTime.indexOf(":"))) * 60;
//		  System.out.println(begWorkTime);
		  
		  int endWorkTime = Integer.parseInt(endTime.substring(endTime.indexOf(":") + 1)) +
				  Integer.parseInt(endTime.substring(0, endTime.indexOf(":"))) * 60;
//		  System.out.println(endWorkTime);
		  
		  // время перерыва
		  int begBreakTime = Integer.parseInt(begBrkTime.substring(begBrkTime.indexOf(":") + 1)) +
				  Integer.parseInt(begBrkTime.substring(0, begBrkTime.indexOf(":"))) * 60;
//		  System.out.println(begBreakTime);
		  
		  int endBreakTime = Integer.parseInt(endBrkTime.substring(endBrkTime.indexOf(":") + 1)) +
				  Integer.parseInt(endBrkTime.substring(0, endBrkTime.indexOf(":"))) * 60;
//		  System.out.println(endBreakTime);
		  
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
				  return false;
			  }
			  Log.d(LOG_TAG, "return true");
			  return true;
		  } else {
			  Log.d(LOG_TAG, "return time false");
			  return false;
		  }
	  }
	
	public static boolean isDoWork(Context ctx) {
		Log.d(LOG_TAG, "begin");
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
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
		  
		  // текущее время
		  int calendarHour = calendar.get(Calendar.HOUR_OF_DAY);
		  int calendarMinute = calendar.get(Calendar.MINUTE);
		  int currentTime = calendarMinute + calendarHour * 60;
		  
		  // время рабочего дня
		  int begWorkTime = Integer.parseInt(timeFrom.substring(timeFrom.indexOf(":") + 1)) +
				  Integer.parseInt(timeFrom.substring(0, timeFrom.indexOf(":"))) * 60;
		  
		  int endWorkTime = Integer.parseInt(timeTo.substring(timeTo.indexOf(":") + 1)) +
				  Integer.parseInt(timeTo.substring(0, timeTo.indexOf(":"))) * 60;
		  
		  // время перерыва
		  int begBreakTime = Integer.parseInt(brkTimeFrom.substring(brkTimeFrom.indexOf(":") + 1)) +
				  Integer.parseInt(brkTimeFrom.substring(0, brkTimeFrom.indexOf(":"))) * 60;
		  
		  int endBreakTime = Integer.parseInt(brkTimeTo.substring(brkTimeTo.indexOf(":") + 1)) +
				  Integer.parseInt(brkTimeTo.substring(0, brkTimeTo.indexOf(":"))) * 60;
		  
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
				  return false;
			  }
			  Log.d(LOG_TAG, "return true");
			  return true;
		  } else {
			  Log.d(LOG_TAG, "return time false");
			  return false;
		  }
	  }
	 
	 public static void diagRequest(Context ctx) {
		 sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		 String str = "<func>getinfo</func><id>"+ sp.getString("ID", "ID") +"</id>";
		 String action = null;	
			do {
				Log.d(LOG_TAG_2, "before req");
				
				Request req = new Request(ctx);
				req.sendFirstRequest(str);
				
				Log.d(LOG_TAG_2, "post req");
				
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
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
					break;
				}
			} while (!action.equals("OK") && !action.equals("REMOVE"));
			
			if (action.equals("REMOVE")) {
				Log.d(LOG_TAG_2, "REMOVE");
			}
			
			Log.d(LOG_TAG_2, "action - " + action);
	 }
}
