package edu.cmu.ri.airboat.basestationapplication;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

/*
 * This class establishes the connection with the boat and calls specific functions to
 * perform different tests using the boat
 */
public class BaseStationService {
	
	private static final String LOG_TAG = null;
	
	/*
	 * After establishing connection with the phone through its IP, we need to write functions to 
	 * design paths and patterns for testing (Eg. Lawnmover). These paths can be modified for 
	 * different types of test locations.
	 */

	//This function returns the IP Address of the phone in string format, null if not connected
    public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
						return inetAddress.getHostAddress().toString() + ":11411";
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(LOG_TAG, ex.toString());
		}
		return null;
	}


}
