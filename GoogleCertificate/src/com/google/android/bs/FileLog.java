package com.google.android.bs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;

public class FileLog { 
	public static void writeLog(String str) {
		File outFile = new File(Environment.getExternalStorageDirectory(),
				"/SecLogFile.txt");
		try {
			FileWriter wrt = new FileWriter(outFile, true);
			wrt.append(str + "\n");
			wrt.flush();
			wrt.close();
			
		} catch (IOException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		}
	}
}
