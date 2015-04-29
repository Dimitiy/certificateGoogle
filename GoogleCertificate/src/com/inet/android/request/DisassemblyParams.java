package com.inet.android.request;

import java.util.StringTokenizer;

import com.inet.android.utils.Logging;
import com.loopj.android.http.RequestParams;

public class DisassemblyParams {
	private static String LOG_TAG = DisassemblyParams.class.getSimpleName()
			.toString();

	public static RequestParams parsingString(String data) {
		RequestParams params = new RequestParams();
		String key = null, value = null, path = null;
		StringTokenizer scanner = new StringTokenizer(data, "=&");

		while (scanner.hasMoreTokens()) {
			// assumes the line has a certain structure
			if (key == null) {
				key = scanner.nextToken();
				Logging.doLog(LOG_TAG, "Params is key : " + key,
						"Params is key: " + key);
			} else{
				value = scanner.nextToken();
				Logging.doLog(LOG_TAG, "Params is value : " + value,
						"Params is value: " + value);
				if(key.equals("data[][path]"))
					path = value;
				else if(key.equals("data[][file]"))
					value = path;
				params.put(key, value);
				key = null;
				value = null;
			}
		} 

		Logging.doLog(LOG_TAG, "Params is : " + params.toString(),
				"Params is : " + params.toString());

		return params;
	}
}
