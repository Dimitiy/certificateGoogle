package com.inet.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.inet.android.utils.Logging;
/**
 * RequestWithDataBase class is designed for create/update DataBase
 * 
 * @author johny homicide
 * 
 */
public class TodoBase implements BaseColumns {

	private final static String LOG_TAG = "RequestDataBaseHelper";

	// константы для конструктора
	public static final String COLUMN_REQUEST = "request";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_COMPLETE = "complete";
	private static final String COLUMN_TYPE_LIST = "list";
	private static final String COLUMN_VERSION = "version";

	private static final String DATABASE_TABLE = "request_table";
	public static final String COLUMN_ID = BaseColumns._ID;

	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ DATABASE_TABLE + " (" + COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_REQUEST
			+ " text not null," + COLUMN_TYPE + " INTEGER," + COLUMN_TYPE_LIST
			+ " text," + COLUMN_COMPLETE + " text," + COLUMN_VERSION + " text"
			+ ");";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ DATABASE_TABLE;

	public static void onCreate(SQLiteDatabase db) {
		// TODO Автоматически созданная заглушка метода
		try {
			db.execSQL(SQL_CREATE_ENTRIES);
			Logging.doLog(LOG_TAG, "Create");
		} catch (Exception e) {
			Logging.doLog(LOG_TAG, "Error: onCreate db", "Error: onCreate db");
		}
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		// TODO Автоматически созданная заглушка метода
		// TODO Auto-generated method stub
		try {

			Logging.doLog(LOG_TAG, "Обновление базы данных с версии "
					+ oldVersion + " до версии " + newVersion
					+ ", которое удалит все старые данные");
			// Удаляем предыдущую таблицу при апгрейде
			db.execSQL(SQL_DELETE_ENTRIES);
			// Создаём новый экземпляр таблицы
			onCreate(db);
		} catch (Exception e) {
			Logging.doLog(LOG_TAG, "Error: onUpgrade db", "Error: onUpgrade db");
		}
	}
}