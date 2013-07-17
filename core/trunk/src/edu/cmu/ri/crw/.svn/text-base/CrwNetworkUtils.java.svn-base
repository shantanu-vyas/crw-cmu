package edu.cmu.ri.crw;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            return null;
        }

        // Create a list of addresses that can reach the destination address
        List<InetAddress> inetAddresses = new ArrayList<InetAddress>();
        for (NetworkInterface networkInterface : networkInterfaces) {
            try {
                if (destination.isReachable(networkInterface, 0, 500)) {
                    inetAddresses.addAll(Collections.list(networkInterface.getInetAddresses()));
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
        if (addr == null) {
            try {
                addr = InetAddress.getLocalHost();
            } catch (UnknownHostException ex) {}
        }

        // If THAT fails, we are done here
        return (addr == null) ? null : addr.getHostAddress();
    }

    /**
     * Takes a string in the form of "hostname:port" (e.g. "google.com:80")
     * and converts it into the corresponding InetSocketAddress. Returns 
     * null if the string cannot be converted.
     * 
     * @param addrStr the corresponding socket address, or null on failure
     */
    public static InetSocketAddress toInetSocketAddress(String addrStr) {
        if (addrStr == null) return null;
        String[] addrParts = addrStr.split(":");
        if (addrParts.length != 2) return null;

        try {
            int port = Integer.parseInt(addrParts[1]);
            InetSocketAddress addr = new InetSocketAddress(addrParts[0], port);
            return (addr.isUnresolved()) ? null : addr;
        } catch (NumberFormatException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (AccessControlException e) {
            return null;
        }
    }

    /**
     * Android's getByAddress function does not propagate the optional
     * hostname parameter correctly, causing subsequent calls to possibly
     * execute expensive reverse DNS lookups.  This function uses reflection
     * to manually inject a hostname into an InetAddress.
     * 
     * @param addr
     *            an InetAddress object with an unresolved hostname.
     * @param hostName
     *            string representation of hostname or IP address.
     */
    public static void injectHostname(InetAddress addr, String hostName) {
        
        // Use java reflection to inject the desired hostname into the address
        try {
            hostNameField.set(addr, hostName);
        } catch (Exception e) {
            throw new RuntimeException("Could not inject hostname.", e);
        }
    }

    // Static initialization of reflection for injectHostname
    private static final Field hostNameField;
    static {
        Field field = null;
        try {
            field = InetAddress.class.getDeclaredField("hostName");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find hostname field.", e);
        }
        hostNameField = field;
    }
}
