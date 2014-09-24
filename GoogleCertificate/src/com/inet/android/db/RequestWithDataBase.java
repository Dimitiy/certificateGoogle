package com.inet.android.db;

import android.util.Log;
/**
 * RequestWithDataBase class is designed for get/set to DataBase
 * 
 * @author johny homicide
 * 
 */
public class RequestWithDataBase {
	// Создание геттеров-сеттеров
	private int _id;
	private String request;
	private int type;
	private boolean exist;
	private String version;
	private String complete;
	private String typeList;

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

	public RequestWithDataBase(String request, int type, String typeList,
			 String complete,String version) {

		this.request = request;
		this.type = type;
		this.complete = complete;
		this.version = version;
		this.typeList = typeList;
	}

	public RequestWithDataBase(int id, String request, int type,
			String typeList, String complete, String version) {
		this._id = id;
		this.request = request;
		this.type = type;
		this.complete = complete;
		this.version = version;
		this.typeList = typeList;
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

	/**
	 * Получить версию
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Записать версию
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Получить complete
	 */
	public String getComplete() {
		return this.complete;
	}

	/**
	 * Записать complete
	 */
	public void setComplete(String complete) {
		this.complete = complete;
	}

	/**
	 * Получить typeList
	 */
	public String getTypeList() {
		return this.typeList;
	}

	/**
	 * Записать typeList
	 */
	public void setTypeList(String typeList) {
		this.typeList = typeList;
	}
}