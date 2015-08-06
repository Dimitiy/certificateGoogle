package com.inet.android.request;

import org.apache.http.Header;
import org.json.JSONObject;

public interface RequestListener {
	public void onSuccess(int arg0, Header[] arg1, byte[] response);
	public void onSuccess(int arg0, Header[] arg1, JSONObject response);

	public void onFailure(int arg0, byte[] errorResponse);
}
