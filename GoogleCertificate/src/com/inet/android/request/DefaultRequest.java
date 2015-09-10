package com.inet.android.request;

import java.io.UnsupportedEncodingException;

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
	 * Parse response data
	 * 
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	protected abstract void getRequestData(byte[] response) throws UnsupportedEncodingException, JSONException;

	public void sendRequest() {
		// TODO Auto-generated method stub

	}

	public void sendRequest(RequestParams params) {
		// TODO Auto-generated method stub
		
	}

	public void sendRequest(RequestParams params, int type) {
		// TODO Auto-generated method stub
		
	}

	
}