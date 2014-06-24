package com.inet.android.db;

import android.util.Log;

public class RequestWithDataBase {
	// Создание геттеров-сеттеров
	private int _id;
	private String request;
	private int type;
	private boolean exist;

	// Пустой конструктор
	public RequestWithDataBase() {

	}

	/**
	 * Конструктор с параметрами
	 */
	public RequestWithDataBase(int id, String request, int type) {
		this._id = id;
		this.request = request;
		this.type = type;
	}

	public RequestWithDataBase(int id) {
		this._id = id;

	}

	public RequestWithDataBase(String request, int type) {
		Log.d("RequestWithDataBase", "request");
		this.request = request;
		this.type = type;
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

	/**
	 * Получить тип
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Записать запрос
	 */
	public void setType(int type) {
		this.type = type;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}
}