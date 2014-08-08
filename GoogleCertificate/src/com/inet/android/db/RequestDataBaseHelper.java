package com.inet.android.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.inet.android.utils.Logging;

public class RequestDataBaseHelper extends SQLiteOpenHelper implements
		BaseColumns {
	private final static String LOG_TAG = "RequestDataBaseHelper";

	// константы для конструктора
	private static final String DATABASE_NAME = "request_database.db";
	private static final int DATABASE_VERSION = 1;
	private static final String COLUMN_REQUEST = "request";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_COMPLETE = "complete";
	private static final String COLUMN_TYPE_LIST = "list";
	private static final String COLUMN_VERSION = "version";

	private static final String DATABASE_TABLE = "request_table";
	public static final String COLUMN_ID = BaseColumns._ID;
	SQLiteDatabase db;
	private int activeDatabaseCount = 0;
	private int type = -1;

	public RequestDataBaseHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Автоматически созданная заглушка метода
		Logging.doLog(LOG_TAG, "Create Base");
		TodoBase.onCreate(db);
	}

	public synchronized SQLiteDatabase openDatabaseWrite() {
		try {
			db = getWritableDatabase(); // always returns the
										// same connection
										// instance
			activeDatabaseCount++;
			Logging.doLog(LOG_TAG, "activeDatabaseWrite: "
					+ activeDatabaseCount,
					Integer.toString(activeDatabaseCount));

		} catch (SQLiteException e) {
			Logging.doLog(LOG_TAG, "Open Base Error:" + e.getMessage(),
					"Open Base Error:" + e.getMessage());

		}
		return db;
	}

	public synchronized SQLiteDatabase openDatabaseRead() {
		try {
			db = getReadableDatabase(); // always returns the
										// same connection
										// instance
			activeDatabaseCount++;
			Logging.doLog(LOG_TAG,
					"activeDatabaseRead: " + activeDatabaseCount,
					"activeDatabaseRead: " + activeDatabaseCount);

		} finally {
			Logging.doLog(LOG_TAG, "Open Base Error", "Open Base Error");

		}
		return db;
	}

	public synchronized void closeDatabase(SQLiteDatabase db) {
		activeDatabaseCount--;
		if (activeDatabaseCount == 0) {
			if (db != null) {
				if (db.isOpen()) {
					db.close();
				}
			}
		}
		Logging.doLog(LOG_TAG, "Close Base: " + activeDatabaseCount,
				"Close Base: " + activeDatabaseCount);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Автоматически созданная заглушка метода
		Logging.doLog(LOG_TAG, "Update Base", "Update Base");
		TodoBase.onUpgrade(db, oldVersion, newVersion);

	}

	/**
	 * Записать запрос в базу
	 */
	public void addRequest(RequestWithDataBase request) {
		if (openDatabaseWrite() != null) {
			Logging.doLog(LOG_TAG, "addRequest" + request.getRequest()
					+ request.getType(), "Get_Type_list != null");
			db.beginTransaction();
			try {
				ContentValues values = new ContentValues();
				values.put(COLUMN_REQUEST, request.getRequest());
				values.put(COLUMN_TYPE, request.getType());
				values.put(COLUMN_TYPE_LIST, request.getTypeList());
				values.put(COLUMN_COMPLETE, request.getComplete());
				values.put(COLUMN_VERSION, request.getVersion());
				Logging.doLog(LOG_TAG, "values", "values");

				// Вставляем строку в таблицу
				db.insert(DATABASE_TABLE, null, values);
				Logging.doLog(LOG_TAG, "insert", "insert");
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
		closeDatabase(db);
	}

	/**
	 * Получить запрос Read
	 */
	public RequestWithDataBase getRequest(int id) {
		RequestWithDataBase getRequest = null;
		if (openDatabaseRead() != null) {

			Cursor cursor = db.query(DATABASE_TABLE, new String[] { COLUMN_ID,
					COLUMN_REQUEST, COLUMN_TYPE, COLUMN_TYPE_LIST,
					COLUMN_COMPLETE, COLUMN_VERSION }, COLUMN_ID + "=?",
					new String[] { String.valueOf(COLUMN_ID) }, null, null,
					null, null);
			if (cursor != null)
				cursor.moveToFirst();

			getRequest = new RequestWithDataBase(Integer.parseInt(cursor
					.getString(0)), cursor.getString(1),
					Integer.parseInt(cursor.getString(2)), cursor.getString(3),
					cursor.getString(4), cursor.getString(5));
			Logging.doLog(LOG_TAG, "ReadRequest" + getRequest.toString(),
					"ReadRequest" + getRequest.toString());

		}
		closeDatabase(db);
		return getRequest;

	}

	/**
	 * Все сохраненные запросы Write
	 */
	public List<RequestWithDataBase> getAllRequest() {
		RequestWithDataBase request = null;
		List<RequestWithDataBase> requestList = null;
		if (openDatabaseRead() != null) {

			requestList = new ArrayList<RequestWithDataBase>();
			// Выбираем всю таблицу
			String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;

			Cursor cursor = db.rawQuery(selectQuery, null);

			// Проходим по всем строкам и добавляем в список
			if (cursor.moveToFirst()) {
				do {
					request = new RequestWithDataBase();
					request.setID(Integer.parseInt(cursor.getString(0)));
					request.setRequest(cursor.getString(1));
					request.setType(Integer.parseInt(cursor.getString(2)));
					request.setTypeList(cursor.getString(3));
					request.setComplete(cursor.getString(4));
					request.setVersion(cursor.getString(5));
					
					requestList.add(request);
				} while (cursor.moveToNext());
				Logging.doLog(LOG_TAG, requestList.toString(),
						requestList.toString());
			}
		}
		closeDatabase(db);

		return requestList;
	}

	/**
	 * Число запросов в Базе Read
	 */
	public int getRequestCount() {
		openDatabaseRead();

		String countQuery = "SELECT  * FROM " + DATABASE_TABLE;
		Cursor cursor = db.rawQuery(countQuery, null);
		Logging.doLog(LOG_TAG, Integer.toString(cursor.getCount()));
		cursor.close();
		closeDatabase(db);

		return cursor.getCount();
	}

	/**
	 * Обновить запросы Read
	 */
	public int updateRequest(RequestWithDataBase request) {
		openDatabaseRead();

		ContentValues values = new ContentValues();
		values.put(COLUMN_REQUEST, request.getRequest());
		values.put(COLUMN_TYPE, request.getType());
		values.put(COLUMN_TYPE_LIST, request.getTypeList());
		values.put(COLUMN_VERSION, request.getVersion());
		values.put(COLUMN_COMPLETE, request.getComplete());
		// обновляем строку
		return db.update(DATABASE_TABLE, values, COLUMN_ID + " = ?",
				new String[] { String.valueOf(request.getID()) });
	}

	/**
	 * Удалить запрос Write
	 */
	public void deleteRequest(RequestWithDataBase request) {
		if (openDatabaseWrite() != null) {
			try {
				db.beginTransaction();

				db.delete(DATABASE_TABLE, COLUMN_ID + " = ?",
						new String[] { String.valueOf(request.getID()) });
				db.setTransactionSuccessful();
				Logging.doLog(LOG_TAG, "deleteRequest", "deleteRequest");
			} finally {
				db.endTransaction();
				Logging.doLog(LOG_TAG, "deleteRequest db.endTransaction",
						"deleteRequest db.endTransaction");
			}
		}
		closeDatabase(db);
	}

	public void delete_byID(int id) {
		if (openDatabaseWrite() != null) {
			try {
				db.beginTransaction();
				db.delete(DATABASE_TABLE, COLUMN_ID + "=" + id, null);
				db.setTransactionSuccessful();
				Logging.doLog(LOG_TAG, "deleteRequest", "deleteRequest");
			} finally {
				db.endTransaction();
			}
		}
		closeDatabase(db);

	}

	public int deleteAll() {
		return db.delete(DATABASE_TABLE, null, null);
	}

	public boolean getExistType(int type) {
		Logging.doLog(LOG_TAG, "getExistType " + Integer.toString(type),
				"getExistType " + Integer.toString(type));
		RequestWithDataBase request = null;
		List<RequestWithDataBase> requestList = null;
		this.type = type;
		String existStr = null;
		Cursor cursor = null;
		if (openDatabaseRead() != null) {
			Logging.doLog(LOG_TAG, "openDatabaseRead() != null ",
					"openDatabaseRead() != null ");
			requestList = new ArrayList<RequestWithDataBase>();
			cursor = db.query(DATABASE_TABLE, new String[] { COLUMN_TYPE },
					COLUMN_TYPE + "=?",
					new String[] { Integer.toString(type) }, null, null, null,
					null);

			if (cursor.moveToFirst()) {
				do {
					request = new RequestWithDataBase();
					request.setType(cursor.getInt(0));
					requestList.add(request);
					if (request.getType() == type) {
						Logging.doLog(LOG_TAG, "getType ", "getType ");
						closeDatabase(db);
						return true;
					}
				} while (cursor.moveToNext());
				Logging.doLog(LOG_TAG, requestList.toString(),
						requestList.toString());
			}
			existStr = cursor.toString();
			Logging.doLog(LOG_TAG, cursor.toString(), cursor.toString());
			closeDatabase(db);
		}
		return false;
	}
}
