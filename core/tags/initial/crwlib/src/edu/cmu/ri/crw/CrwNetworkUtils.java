package edu.cmu.ri.crw;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ros.address.InetAddressFactory;

/**
 * Contains network utility functions for finding hosts, interfaces, etc. 
 * 
 * @author pkv
 */
public class CrwNetworkUtils {
	
	/**
	 * This is a utility class, it contains only static methods, so the
	 * constructor is hidden as private.
	 */
	private CrwNetworkUtils() {}

	/**
	 * Does a best-effort search for the first local, non-loopback IPv4 address 
	 * registered to the first network interface that is able to reach the 
	 * specified destination address.  This method uses an ICMP echo to 
	 * establish reachability, so it can be affected by firewalls and NATs. 
	 * 
	 * @param destination a destination address
	 * @return the local address of a network interface that was able to reach the destination address, or null if none were found
	 */
	public static InetAddress getAddrForNeighbor(InetAddress destination) {
		
		// Make a list of available network interfaces
		List<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = Collections.list(NetworkInterface
					.getNetworkInterfaces());
		} catch (SocketException e) {
			return null;
		}
		
		// Create a list of addresses that can reach the destination address
		List<InetAddress> inetAddresses = new ArrayList<InetAddress>();
		for (NetworkInterface networkInterface : networkInterfaces) {
			try {
				if (destination.isReachable(networkInterface, 0, 500)) {
					inetAddresses.addAll(Collections.list(networkInterface
							.getInetAddresses()));
				}
			} catch (IOException e) {
			}
		}
		
		// Return the first valid IP address on that interface
		for (InetAddress address : inetAddresses) {
			if (!address.isLoopbackAddress() && address.getAddress().length == 4) {
				return address;
			}
		}
		
		// If none is found, return null
		return null;
	}
	
	/**
	 * #see {@link CrwNetworkUtils#getAddrForNeighbor(InetAddress)}
	 * 
	 * @param destination string representation of destination address
	 * @return the local address of a network interface that was able to reach the destination address, or null if none were found
	 */
	public static InetAddress getAddrForNeighbor(String destination) {
		try {
			InetAddress masterAddr = InetAddress.getByName(destination);
			return CrwNetworkUtils.getAddrForNeighbor(masterAddr);
		} catch (UnknownHostException e) { 
			return null;
		}
	}
	
	/**
	 * Do a best effort search to find a local hostname that will make sense
	 * to a given external host.  Starts by using ICMP to try to reach the 
	 * external host, then tries non-loopback addresses, then loopback 
	 * addresses.
	 * 
	 * @param externalHost the external host that the hostname should be visible to
	 * @return a local hostname that the external host would be able to use
	 */
	public static String getLocalhost(String externalHost) {
		InetAddress addr;
		
		// Get a legitimate hostname, first by trying ICMP echo to master URI
		addr = CrwNetworkUtils.getAddrForNeighbor(externalHost);
		
		// If that fails, go for any non-loopback IPv4 address
		if (addr == null) addr = InetAddressFactory.newNonLoopback();
		
		// If that fails, settle for a loopback address
		if (addr == null) addr = InetAddressFactory.newLoopback();
		
		// If THAT fails, we are done here
		return (addr == null) ? null : addr.getHostAddress();
	}
}
