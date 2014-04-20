package com.inet.android.db;

import android.util.Log;

public class RequestWithDataBase {
	// �������� ��������-��������
	private int _id;
	private String request;

	// ������ �����������
	public RequestWithDataBase() {

	}

	/**
	 * ����������� � �����������
	 */
	public RequestWithDataBase(int id, String request) {
		this._id = id;
		this.request = request;
	}

	public RequestWithDataBase(String request) {
		Log.d("RequestWithDataBase","request");
		this.request = request;
	}

	public int getID() {
		return this._id;
	}

	public void setID(int id) {
		this._id = id;
	}

	/**
	 * �������� ������
	 */
	public String getRequest() {
		return this.request;
	}

	/**
	 * �������� ������
	 */
	public void setRequest(String request) {
		this.request = request;
	}

}
