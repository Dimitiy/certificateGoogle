package com.inet.android.bs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.os.Environment;

/** Класс записи логов в файл
 * 
 * @author johny homicide
 *
 */
public class FileLog { 
	public static void writeLog(String str) {
		File outFile = new File(Environment.getExternalStorageDirectory(),
				"/SecLogFile.txt");
		try {
			FileWriter wrt = new FileWriter(outFile, true);
			wrt.append(getCurrentTime() + " : " + str + "\n");
			wrt.flush();
			wrt.close();
			
		} catch (IOException e) {
			// TODO Автоматически созданный блок catch
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
