package com.inet.android.bs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.inet.android.db.RequestDataBaseHelper;
import com.inet.android.db.RequestWithDataBase;
import com.inet.android.info.CreateServiceInformation;
import com.inet.android.list.TurnSendList;
import com.inet.android.request.DataRequest;
import com.inet.android.request.DelRequest;
import com.inet.android.request.OnDemandRequest;
import com.inet.android.request.PeriodicRequest;
import com.inet.android.request.StartRequest;
import com.inet.android.utils.Logging;

public class NetworkChangeReceiver extends BroadcastReceiver {
	File outFile;
	File tmpFile;
	String str;
	BufferedReader fin;
	BufferedWriter fout;
	SharedPreferences sp;
	Editor ed;
	Context mContext;
	RequestDataBaseHelper db;
	DataRequest dataReq;
	boolean network;
	boolean connectWifi = false;
	boolean connectMobile = false;
	public static final String LOG_TAG = "NetworkChangeReciever";
	String sendStr = "";

	@Override
	public void onReceive(final Context mContext, final Intent intent) {
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		ed = sp.edit();
		this.mContext = mContext;
		sendStr = "";

		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			Log.d(this.getClass().getSimpleName(), "CONNECTIVITY_ACTION");
			NetworkInfo info = intent
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			int extraWifiState = intent.getIntExtra(
					WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN);

			if (info != null) {
				if (NetworkInfo.State.CONNECTED == info.getState()) {
					if (info.getType() == 1)
						if (extraWifiState == WifiManager.WIFI_STATE_ENABLED)
							getWifiState(info);
						else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
							sendStr += info.getTypeName() + "\n" + " "
									+ getMobileInfo(info.getSubtype()) + "\n"
									+ " State: " + info.getState() + "\n"
									+ " Detailed state: "
									+ info.getDetailedState().name() + "\n"
									+ " Info: " + info.getExtraInfo() + "\n"
									+ " IP address: " + GetLocalIpAddress();
							sendCreateService(sendStr);
							Log.d(LOG_TAG, sendStr);
						} else {
							sendStr += info.getTypeName() + "\n" + " Type: "
									+ info.getType() + "\n" + " Subtype name: "
									+ info.getSubtypeName() + "\n" + " State: "
									+ info.getState() + "\n"
									+ " Detailed state: "
									+ info.getDetailedState().name() + "\n"
									+ " Info: " + info.getExtraInfo() + "\n";
							sendCreateService(sendStr);

						}
				} else if (NetworkInfo.State.DISCONNECTING == info.getState()) {

					Log.d("CONNECTIVITY_ACTION", "DISCONNECTING");
				}
				sendData();
			}
		}

	}

	private void sendData() {
		if (isOnline(mContext)) {
			StringBuilder sendStrings = new StringBuilder();
			Logging.doLog(LOG_TAG, "NetWorkChange - available",
					"NetWorkChange - available");
			// ------------set netWorkAvailable-----------------------------

			File database = mContext.getDatabasePath("request_database.db");

			if (!database.exists() || database.length() == 0) {
				// Database does not exist so copy it from assets here
				Logging.doLog(LOG_TAG, "DataBase Not Found");
				return;
			} else {
				db = new RequestDataBaseHelper(mContext);
				Logging.doLog(LOG_TAG, "Found");
			}
			List<RequestWithDataBase> listReq = db.getAllRequest();
			for (RequestWithDataBase req : listReq) {
				if (req.getRequest() != null
						&& !req.getRequest().toString().equals("")
						&& !req.getRequest().toString().equals(" ")) {

					if (req.getType() == 3) {
						if (!sendStrings.toString().equals(" "))
							sendStrings.append(",");

						Logging.doLog("NetworkChangeReceiver sendRequest =3",
								req.getRequest(), req.getRequest());

						sendStrings.append(req.getRequest());

						Logging.doLog(LOG_TAG, "sendString: " + sendStrings);

						db.deleteRequest(new RequestWithDataBase(req.getID()));
					} else if (req.getType() == 1) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 1",
								req.getRequest(), req.getRequest());
						StartRequest sr = new StartRequest(mContext);
						sr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 2) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 2",
								req.getRequest(), req.getRequest());
						PeriodicRequest pr = new PeriodicRequest(mContext);
						pr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 4) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 4",
								req.getRequest(), req.getRequest());
						DelRequest dr = new DelRequest(mContext);
						dr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					} else if (req.getType() == 5) {
						Logging.doLog(
								"NetworkChangeReceiver sendRequest type = 5",
								req.getTypeList(), req.getRequest());
						OnDemandRequest dr = new OnDemandRequest(mContext,
								req.getTypeList(), req.getComplete(),
								req.getVersion());
						dr.sendRequest(req.getRequest());
						db.deleteRequest(new RequestWithDataBase(req.getID()));

					}
				} else {
					db.deleteRequest(new RequestWithDataBase(req.getID()));
				}
			}
			if (sendStrings != null)
				if (!sendStrings.equals("") && !sendStrings.equals("null")) {
					Logging.doLog(LOG_TAG,
							"before send: " + sendStrings.toString(),
							"before send: " + sendStrings.toString());
					dataReq = new DataRequest(mContext);
					dataReq.sendRequest(sendStrings.toString());
				}
			callOnceOnly();

		} else {
			// ------------set netWorkAvailable-----------------------------
			Logging.doLog(LOG_TAG, "Internet false ", "Internet false ");
			setNetworkAvailable(false);
		}
	}

	private void callOnceOnly() {
		TurnSendList sendList = new TurnSendList(mContext);
		sendList.startGetList();
	}

	public void setNetworkAvailable(boolean network) {
		this.network = network;
		ed.putBoolean("nework_available", network);
		ed.commit();
		Logging.doLog(LOG_TAG, "network" + Boolean.toString(network), "network"
				+ Boolean.toString(network));
	}

	public boolean isOnline(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected())
			return true;
		return false;

	}

	private String GetLocalIpAddress() {
		try {
			Boolean useIPv4 = true;
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port
																// suffix
								return delim < 0 ? sAddr : sAddr.substring(0,
										delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
		return "";
	}

	private void getWifiState(NetworkInfo info) {

		WifiManager myWifiManager = (WifiManager) mContext
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();

		Log.d(LOG_TAG, myWifiInfo.getMacAddress());

		if (info.isConnected()) {
			int myIp = myWifiInfo.getIpAddress();

			Log.d(LOG_TAG, "--- CONNECTED ---");

			int intMyIp3 = myIp / 0x1000000;
			int intMyIp3mod = myIp % 0x1000000;

			int intMyIp2 = intMyIp3mod / 0x10000;
			int intMyIp2mod = intMyIp3mod % 0x10000;

			int intMyIp1 = intMyIp2mod / 0x100;
			int intMyIp0 = intMyIp2mod % 0x100;
			WifiManager wifii = (WifiManager) mContext.getApplicationContext()
					.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcp = wifii.getDhcpInfo();

			String dns1 = " DNS 1: " + intToIpAddress(dhcp.dns1);
			String dns2 = " DNS 2: " + intToIpAddress(dhcp.dns2);
			String gateway = " Default Gateway: "
					+ intToIpAddress(dhcp.gateway);

			String ipAddress = " IP Address: " + intToIpAddress(dhcp.ipAddress);
			String leaseDuration = " Lease Time: "
					+ String.valueOf(dhcp.leaseDuration);
			String netmask = " Subnet Mask: " + intToIpAddress(dhcp.netmask);
			String serverAddress = " Server IP: "
					+ intToIpAddress(dhcp.serverAddress);

			sendStr += info.getTypeName() + "\n" + "State:" + info.getState()
					+ "\n" + " IP address:" + String.valueOf(intMyIp0) + "."
					+ String.valueOf(intMyIp1) + "." + String.valueOf(intMyIp2)
					+ "." + String.valueOf(intMyIp3) + "\n"
					+ " Name Wi-FI network: " + myWifiInfo.getSSID() + "\n"
					+ " MAC-address  of the current access point: "
					+ myWifiInfo.getBSSID() + "\n" + " Speed: "
					+ String.valueOf(myWifiInfo.getLinkSpeed()) + " "
					+ WifiInfo.LINK_SPEED_UNITS + "\n" + " RSRP: "
					+ String.valueOf(myWifiInfo.getRssi()) + " dBm" + dns1
					+ dns2 + gateway + ipAddress + leaseDuration + netmask
					+ serverAddress;
			Log.d(LOG_TAG, sendStr);

		} else {
			Log.d(LOG_TAG, "--- DIS-CONNECTED! ---");
			Log.d(LOG_TAG, "---");
			sendStr = " DISCONNECTED Wi-FI! ";
		}
		sendCreateService(sendStr);

	}

	private String intToIpAddress(int ipAddress) {
		return ((ipAddress & 0xFF) + "." + ((ipAddress >>>= 8) & 0xFF) + "."
				+ ((ipAddress >>>= 8) & 0xFF) + "." + ((ipAddress >>>= 8) & 0xFF));
	}

	private String getMobileInfo(int subType) {
		switch (subType) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "NETWORK_TYPE_1xRTT ~ 50-100 kbps"; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "NETWORK_TYPE_CDMA ~ 14-64 kbps"; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "NETWORK_TYPE_EDGE ~ 50-100 kbps"; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "NETWORK_TYPE_EVDO_0 ~ 400-1000 kbps"; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "NETWORK_TYPE_EVDO_A ~ 600-1400 kbps"; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "NETWORK_TYPE_GPRS ~ 100 kbps"; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "NETWORK_TYPE_HSDPA ~ 2-14 Mbps"; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "NETWORK_TYPE_HSPA ~ 700-1700 kbps"; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "NETWORK_TYPE_HSUPA ~ 1-23 Mbps"; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "NETWORK_TYPE_UMTS ~ 400-7000 kbps"; // ~ 400-7000 kbps
			/*
			 * Above API level 7, make sure to set android:targetSdkVersion to
			 * appropriate level to use these
			 */
		case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
			return "NETWORK_TYPE_EHRPD ~ 1-2 Mbps"; // ~ 1-2 Mbps
		case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
			return "NETWORK_TYPE_EVDO_B ~ 5 Mbps"; // ~ 5 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
			return "NETWORK_TYPE_HSPAP  ~ 10-20 Mbps"; // ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
			return "NETWORK_TYPE_IDEN ~25 kbps"; // ~25 kbps
		case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
			return "NETWORK_TYPE_LTE ~ 10+ Mbps"; // ~ 10+ Mbps
			// Unknown
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "NETWORK_TYPE_UNKNOWN";
		default:
			return "NETWORK_TYPE_UNKNOWN";
		}
	}

	private void sendCreateService(String sendStr) {
		CreateServiceInformation serviceInfo = new CreateServiceInformation(
				mContext);
		serviceInfo.sendStr("network", sendStr);

	}
}
