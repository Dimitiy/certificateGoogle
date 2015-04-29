package com.inet.android.utils;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class GeneratorLine {
	private static final String mCHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final int STR_LENGTH = 9; // длина генерируемой строки
	static StringBuffer randStr;

	public static String createRandomString(Context mContext) {

		randStr = new StringBuffer();
		for (int i = 0; i < STR_LENGTH; i++) {
			int number = getRandomNumber();
			char ch = mCHAR.charAt(number);
			randStr.append(ch);
		}
		setKeyForBug(randStr.toString(), mContext);
		return randStr.toString();
	}

	private static void setKeyForBug(String key, Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();
		ed.putString("key_for_bug", key);
		ed.commit();
	}

	private static int getRandomNumber() {
		int randomInt = 0;
		Random random = new Random();
		randomInt = random.nextInt(mCHAR.length());
		if (randomInt - 1 == -1) {
			return randomInt;
		} else {
			return randomInt - 1;
		}
	}
}
