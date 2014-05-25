package com.inet.android.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class RequestDataBaseHelper extends SQLiteOpenHelper implements
		BaseColumns {
	// ��������� ��� ������������
	private static final String DATABASE_NAME = "request_database.db";
	private static final int DATABASE_VERSION = 1;
	public static final String COLUMN_REQUEST = "request";
	private static final String DATABASE_TABLE = "request_table";
	public static final String COLUMN_ID = BaseColumns._ID;
	
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ DATABASE_TABLE + " (" + COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_REQUEST
			+ " text not null);";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ DATABASE_TABLE;

	public RequestDataBaseHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO ������������� ��������� �������� ������
		Log.d("RequestDataBase","create");	
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO ������������� ��������� �������� ������
		// TODO Auto-generated method stub
		Log.w("LOG_TAG", "���������� ���� ������ � ������ " + oldVersion
				+ " �� ������ " + newVersion
				+ ", ������� ������ ��� ������ ������");
		// ������� ���������� ������� ��� ��������
		db.execSQL(SQL_DELETE_ENTRIES);
		// ������ ����� ��������� �������
		onCreate(db);
	}

	/**
	 *  �������� ������ � ����
	*/
	public void addRequest(RequestWithDataBase request) {
		SQLiteDatabase db = this.getWritableDatabase();
		 
	    ContentValues values = new ContentValues();
	    values.put(COLUMN_REQUEST, request.getRequest());
		Log.d("RequestDataBase","values");	
		
	    // ��������� ������ � �������
	    db.insert(DATABASE_TABLE, null, values);
		Log.d("RequestDataBase","insert");	
		
	    db.close();
	}

	/**
	 *  �������� ������
	*/
	public RequestWithDataBase getRequest(String request) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(DATABASE_TABLE, new String[] { COLUMN_ID,
				COLUMN_REQUEST }, COLUMN_ID + "=?",
				new String[] { String.valueOf(COLUMN_ID) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		RequestWithDataBase getRequest = new RequestWithDataBase(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1));
		return getRequest;
	}

	/**
	 *  ��� ����������� �������
	*/
	public List<RequestWithDataBase> getAllRequest() {
		 List<RequestWithDataBase> requestList = new ArrayList<RequestWithDataBase>();
		    // �������� ��� �������
		    String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;
		 
		    SQLiteDatabase db = this.getWritableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		 
		    // �������� �� ���� ������� � ��������� � ������
		    if (cursor.moveToFirst()) {
		        do {
		            RequestWithDataBase request = new RequestWithDataBase();
		            request.setID(Integer.parseInt(cursor.getString(0)));
		            request.setRequest(cursor.getString(1));
		            requestList.add(request);
		        } while (cursor.moveToNext());
		    }
		    return requestList;
	}

	/**
	 *  ����� �������� � ����
	*/
	public int getRequestCount() {
		String countQuery = "SELECT  * FROM " + DATABASE_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		return cursor.getCount();
	}

	/**
	 *  �������� ������
	*/
	public int updateRequest(RequestWithDataBase request) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COLUMN_REQUEST, request.getRequest());
		
		// ��������� ������
		return db.update(DATABASE_TABLE, values, COLUMN_ID + " = ?",
				new String[] { String.valueOf(request.getID()) });
	}

	/**
	 *  ������� ������
	*/
	public void deleteRequest(RequestWithDataBase request) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(DATABASE_TABLE, COLUMN_ID + " = ?",
				new String[] { String.valueOf(request.getID()) });
		db.close();
	}
}
