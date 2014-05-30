package com.inet.android.db;

import android.util.Log;

public class RequestWithDataBase {
	// Создание геттеров-сеттеров
	private int _id;
	private String request;

	// Пустой конструктор
	public RequestWithDataBase() {

	}

	/**
	 * Конструктор с параметрами
	 */
	public RequestWithDataBase(int id, String request) {
		this._id = id;
		this.request = request;
	}

	public RequestWithDataBase(int id) {
		this._id = id;

	}

	public RequestWithDataBase(String request) {
		Log.d("RequestWithDataBase", "request");
		this.request = request;
	}

	public int getID() {
		return this._id;
	}

	public void setID(int id) {
		this._id = id;
	}

	/**
	 * Получить запрос
	 */
	public String getRequest() {
		return this.request;
	}

	/**
	 * Записать запрос
	 */
	public void setRequest(String request) {
		this.request = request;
	}

}