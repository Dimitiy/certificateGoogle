package com.inet.android.request;

import org.json.JSONException;

import android.content.Context;

import com.loopj.android.http.RequestParams;

/**
 * Default request class
 * 
 * @author johny homicide
 *
 */
public abstract class DefaultRequest {
	Context ctx;

	public DefaultRequest(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * Invoke a thread for making request
	 * 
	 * @param request
	 */
	public abstract void sendRequest(String request);

	/**
	 * Invoke a thread for making request
	 * 
	 * @param request
	 */
	public abstract void sendRequest(int request);

	/**
	 * Method of sending request
	 * 
	 * @param request
	 */
	protected abstract void sendPostRequest(String request);

	/**
	 * Parse response data
	 * 
	 * @param response
	 * @throws JSONException
	 */
	protected abstract void getRequestData(String response)
			throws JSONException;

	public void sendRequest() {
		// TODO Auto-generated method stub

	}

	public void sendRequest(RequestParams params) {
		// TODO Auto-generated method stub
		
	}

}