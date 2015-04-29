package com.inet.android.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecursiveSearch {
	private static String LOG_TAG = RecursiveSearch.class.getSimpleName();

	/**
	 * ����������� ����� ����� � ����������
	 */

	public static List<String> recursiveFileFind(String file) {

		// �������� �������
		final File home = new File(file);

		// ������ ������ ��� �������� ��������� ����� � ������
		List<String> files = new ArrayList<String>();
		// ���������� ���� �����
		if (home != null && home.length() > 0)
			searchFile(home, files);
		Logging.doLog(LOG_TAG, files.toString());

		return files;
	}

	/**
	 * ���������� ���� �����
	 * 
	 * @param folder
	 *            the ������� ����� � ������� ���������� �����
	 * @param files
	 *            the ������ ��� �������� ��������� ����� � ������
	 */
	private static void searchFile(final File folder, final List<String> files) {

		// ���������� ��� �������� � ����������, ��� ����� ���� � ����� �
		// ����������
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				if (file.getName().contains("Media")
						|| file.getName().contains("Images")
						|| file.getName().contains("Pictures")
						|| file.getName().contains("DCIM"))
					files.add(file.getAbsolutePath());

				searchFile(file, files);
			}

		}
	}
}
