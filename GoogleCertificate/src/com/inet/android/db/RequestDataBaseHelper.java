package com.inet.android.db;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
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

	// ��������� ��� ������������
	private static final String DATABASE_NAME = "request_database.db";
	private static final int DATABASE_VERSION = 1;
	public static final String COLUMN_REQUEST = "request";
	private static final String COLUMN_TYPE = "type";
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
		// TODO ������������� ��������� �������� ������
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
		// TODO ������������� ��������� �������� ������
		Logging.doLog(LOG_TAG, "Update Base", "Update Base");
		TodoBase.onUpgrade(db, oldVersion, newVersion);

	}

	/**
	 * �������� ������ � ����
	 */
	public void addRequest(RequestWithDataBase request) {
		if (openDatabaseWrite() != null) {
			db.beginTransaction();
			try {
				ContentValues values = new ContentValues();
				values.put(COLUMN_REQUEST, request.getRequest());
				values.put(COLUMN_TYPE, request.getType());
				Logging.doLog(LOG_TAG, "values", "values");

				// ��������� ������ � �������
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
	 * �������� ������ Read
	 */
	public RequestWithDataBase getRequest(int id) {
		RequestWithDataBase getRequest = null;
		if (openDatabaseRead() != null) {

			Cursor cursor = db.query(DATABASE_TABLE, new String[] { COLUMN_ID,
					COLUMN_REQUEST, COLUMN_TYPE }, COLUMN_ID + "=?",
					new String[] { String.valueOf(COLUMN_ID) }, null, null,
					null, null);
			if (cursor != null)
				cursor.moveToFirst();

			getRequest = new RequestWithDataBase(Integer.parseInt(cursor
					.getString(0)), cursor.getString(1), cursor.getInt(2));
			Logging.doLog(LOG_TAG, getRequest.toString(), getRequest.toString());

		}
		closeDatabase(db);
		return getRequest;

	}

	/**
	 * ��� ����������� ������� Write
	 */
	public List<RequestWithDataBase> getAllRequest() {
		RequestWithDataBase request = null;
		List<RequestWithDataBase> requestList = null;
		if (openDatabaseRead() != null) {

			requestList = new ArrayList<RequestWithDataBase>();
			// �������� ��� �������
			String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;

			Cursor cursor = db.rawQuery(selectQuery, null);

			// �������� �� ���� ������� � ��������� � ������
			if (cursor.moveToFirst()) {
				do {
					request = new RequestWithDataBase();
					request.setID(Integer.parseInt(cursor.getString(0)));
					request.setRequest(cursor.getString(1));
					request.setType(cursor.getInt(2));
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
	 * ����� �������� � ���� Read
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
	 * �������� ������� Read
	 */
	public int updateRequest(RequestWithDataBase request) {
		openDatabaseRead();

		ContentValues values = new ContentValues();
		values.put(COLUMN_REQUEST, request.getRequest());
		values.put(COLUMN_TYPE, request.getType());

		// ��������� ������
		return db.update(DATABASE_TABLE, values, COLUMN_ID + " = ?",
				new String[] { String.valueOf(request.getID()) });
	}

	/**
	 * ������� ������ Write
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
				Logging.doLog(LOG_TAG, "deleteRequest db.endTransaction", "deleteRequest db.endTransaction");
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

			Logging.doLog(LOG_TAG, "openDatabaseRead() != null ",
					"openDatabaseRead() != null ");
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
