package com.inet.android.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;

import com.inet.android.utils.Logging;

/**
 * Class for make http post request
 * 
 * @author johny homicide
 *
 */
public class Caller {
	private final static String LOG_TAG = Caller.class.getSimpleName()
			.toString();
	static Context mContext;

	/**
	 * Performs HTTP POST
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public static String doMake(String postRequest, String token,
			String addition, boolean setHeader,
			ArrayList<NameValuePair> postParameters, Context context)
			throws IOException {

		String data = null;
		mContext = context;

		// Create HttpClient и PostHandler
		HttpClient httpclient = new DefaultHttpClient();
		URI uri = null;
		HttpPost httppost = null;
		/*
		 * set uri
		 */
		try {
			uri = new URI(ConstantValue.MAIN_LINK + addition);
			httppost = new HttpPost(uri);
			Logging.doLog(LOG_TAG, uri.toASCIIString());
		} catch (URISyntaxException e1) {
			Logging.doLog(LOG_TAG, "bad in uri");
			e1.printStackTrace();
		}
		/*
		 * set header post request
		 */

		if (setHeader) {
			Logging.doLog(LOG_TAG, "setHeader", "setHeader");
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader(HTTP.CONTENT_TYPE, "application/json");
			httppost.setHeader("Authorization", "Bearer " + token);
		}

		/*
		 * set parameters post request for first token
		 */

		if (postParameters != null) {
			// Setup the request parameters
			httppost.setEntity(new UrlEncodedFormEntity(postParameters));
			httppost.setHeader("Content-Type", "multipart/form-data");
		}
		/*
		 * set postRequest
		 */
		if (postRequest != null) {
			Logging.doLog(LOG_TAG, "postRequest", "postRequest");

			StringEntity se = new StringEntity(postRequest, HTTP.UTF_8);
			se.setContentType("application/json");
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			se.setContentType("");
			httppost.setEntity(se);

			Logging.doLog(LOG_TAG,
					"doMake: " + EntityUtils.toString(httppost.getEntity()),
					"doMake: " + EntityUtils.toString(httppost.getEntity()));
		}
		// Выполним запрос
		HttpResponse response = httpclient.execute(httppost);

		if (response != null) {
			data = getEntity(response);
			if (data == null)
				data = getStatus(response);
		} else {
			Logging.doLog(LOG_TAG, "http response equals null",
					"http response equals null");
		}
		return data;
	}

	private static String getEntity(HttpResponse response) {
		String str = null;
		try {
			HttpEntity httpEntity = response.getEntity();

			if (httpEntity != null) {
				InputStream inputStream = httpEntity.getContent();
				str = convertStreamToString(inputStream);
			}

			Logging.doLog(LOG_TAG, "response: " + str, "response: " + str);

			if (str.indexOf("code") == -1 && str.indexOf("access_token") == -1) {
				Logging.doLog(LOG_TAG, "something wrong in the answer",
						"something wrong in the answer");
				return null;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	private static String getStatus(HttpResponse response) {
		String status = null;
		try {
			int st = response.getStatusLine().getStatusCode();
			status = String.valueOf(st);
			Logging.doLog(LOG_TAG, "response getStatus: " + status,
					"response getStatus: " + status);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return status;
	}

	private static String convertStreamToString(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
