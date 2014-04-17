package com.inet.android.bs;

/**
 * Java interface for making request<br>
 * 
 * <b>USAGE:</b><br>
 * Basically<br>
 * <br>
 * <code>
 * RequestMaker service = new RequestMakerImpl(); 
 * </code><br>
 * <br>
 * <b>NOTE:</b><br> 
 * This is horosho <br>
 * 
 * 
 * @author johny homicide
 */
public interface RequestMaker {

	/**
	 * Make periodic request<br> 
	 */
	void sendPeriodicRequest(String str);   
	
	/**
	 * Make request with data<br>
	 */
	void sendDataRequest(String str);
	
	/**
	 * Make start request<br>
	 */
	void sendStartRequest(String str);
}
