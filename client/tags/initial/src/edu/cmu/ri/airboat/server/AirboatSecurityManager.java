/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.server;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Implements a custom SecurityManager that allows us to optimize certain calls
 * that should not work on the airboats.  Most notably, disables all DNS
 * resolution, to handle the very, very common case of airboats moving around
 * an ad-hoc network, where DNS is slow and unreliable.
 *
 * @author pkv
 */
public class AirboatSecurityManager extends SecurityManager {

	/**
	 * Maximum allowable lookup time for a DNS query, before DNS is disabled
	 * when using the loadIfDNSIsSlow function.
	 */
	public static final int DNS_LOOKUP_THRESHOLD = 500; 

	/**
	 * Pattern accepting xxx.xxx.xxx.xxx, where xxx is a set of 1 to 3 digits.
	 */
	static final Pattern ipv4Address = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

	/**
	 * Attempts to load an instance of this SecurityManager on the airboat.
	 */
	public static void load() {
		try {
			System.setSecurityManager(new AirboatSecurityManager());
		} catch (SecurityException se) {
			System.err.println("SecurityManager already set!");
		}
	}

	/**
	 * Checks how long a DNS lookup takes, and disables lookups if it is taking
	 * too long.
	 */
	public static void loadIfDNSIsSlow() {
		final AtomicBoolean isSlow = new AtomicBoolean(true);

		// Try a DNS lookup which will set a flag low
		new Thread(new Runnable() {
			@Override
			public void run() {
				try { InetAddress.getByName("www.google.com"); } 
				catch (UnknownHostException e) {}
				isSlow.set(false);
			}
		}).start();

		// Wait a bit, if DNS still didn't finish, DNS is too slow to use
		try { Thread.sleep(DNS_LOOKUP_THRESHOLD); } catch (InterruptedException e) {}
		if (isSlow.get()) { load(); }
	}

	/**
	 * Disallows the calling thread from performing any DNS resolution.
	 * Does not affect actual connections.
	 *
	 * @param host
	 * @param port
	 */
	@Override
	public void checkConnect(String host, int port) {

		// Listen for DNS resolutions and stop them!
		if (port == -1) {
			throw new AccessControlException("No DNS allowed: tried to lookup " + host + ".");
		}

		return;
	}

	// Provide null implementations of EVERYTHING ELSE
	public void checkCreateClassLoader() {}
	public void checkAccess(Thread g) {}
	public void checkAccess(ThreadGroup g) {}
	public void checkExit(int status) {}
	public void checkExec(String cmd) {}
	public void checkLink(String lib) {}
	public void checkRead(FileDescriptor fd) {}
	public void checkRead(String file) {}
	public void checkRead(String file, Object context) {}
	public void checkWrite(FileDescriptor fd) {}
	public void checkWrite(String file) {}
	public void checkDelete(String file) {}
	public void checkConnect(String host, int port, Object context) {}
	public void checkListen(int port) {}
	public void checkAccept(String host, int port) {}
	public void checkMulticast(InetAddress maddr) {}
	public void checkMulticast(InetAddress maddr, byte ttl) {}
	public void checkPropertiesAccess() {}
	public void checkPropertyAccess(String key) {}
	public boolean checkTopLevelWindow(Object window) { return true; }
	public void checkPrintJobAccess() {}
	public void checkSystemClipboardAccess() {}
	public void checkAwtEventQueueAccess() {}
	public void checkPackageAccess(String pkg) {}
	public void checkPackageDefinition(String pkg) {}
	public void checkSetFactory() {}
	public void checkMemberAccess(Class<?> clazz, int which) {}
	public void checkSecurityAccess(String provider) {}
}
