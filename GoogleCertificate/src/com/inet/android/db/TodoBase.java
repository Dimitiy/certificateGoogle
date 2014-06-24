package com.inet.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.inet.android.utils.Logging;

public class TodoBase implements BaseColumns{

	private final static String LOG_TAG = "RequestDataBaseHelper";

	// ��������� ��� ������������
	public static final String COLUMN_REQUEST = "request";
	private static final String COLUMN_TYPE = "type";
    
	private static final String DATABASE_TABLE = "request_table";
	public static final String COLUMN_ID = BaseColumns._ID;
	
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ DATABASE_TABLE + " (" + COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_REQUEST
			+ " text not null," + COLUMN_TYPE + " INTEGER" + ");";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ DATABASE_TABLE;


	public static void onCreate(SQLiteDatabase db) {
		// TODO ������������� ��������� �������� ������
		Logging.doLog(LOG_TAG,"Create");	
		db.execSQL(SQL_CREATE_ENTRIES);
	}


	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO ������������� ��������� �������� ������
		// TODO Auto-generated method stub
		Logging.doLog(LOG_TAG, "���������� ���� ������ � ������ " + oldVersion
				+ " �� ������ " + newVersion
				+ ", ������� ������ ��� ������ ������");
		// ������� ���������� ������� ��� ��������
		db.execSQL(SQL_DELETE_ENTRIES);
		// ������ ����� ��������� �������
		onCreate(db);
	}
} 