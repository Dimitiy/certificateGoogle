package com.inet.android.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecursiveSearch {
	private static String LOG_TAG = RecursiveSearch.class.getSimpleName();

	/**
	 * Рекурсивный поиск фалов в директории
	 */

	public static List<String> recursiveFileFind(String file) {

		// корневой каталог
		final File home = new File(file);

		// создаём список для хранения найденных путей к файлам
		List<String> files = new ArrayList<String>();
		// рекурсивно ищём файлы
		if (home != null && home.length() > 0)
			searchFile(home, files);
		Logging.doLog(LOG_TAG, files.toString());

		return files;
	}

	/**
	 * рекурсивно ищём файлы
	 * 
	 * @param folder
	 *            the текущая папка в которой происходит поиск
	 * @param files
	 *            the список для хранения найденных путей к файлам
	 */
	private static void searchFile(final File folder, final List<String> files) {

		// перебираем все элементы в директории, там могут быть и файлы и
		// директории
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
