/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general;

import edu.cmu.ri.airboat.generalAlmost.*;
import edu.cmu.ri.crw.CrwNetworkUtils;
import java.awt.Color;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class ProxyManager {

    private static Singleton instance = new Singleton();
    private Random rand = new Random();

    private static ArrayList<ProxyManagerListener> listeners = new ArrayList<ProxyManagerListener>();
    
    public static void setCameraRates(double d) {
        instance.setCameraRates(d);
    }

    public BoatProxy getRandomProxy() {

        if (instance.boatProxies.isEmpty()) {
            return null;
        }

        return instance.boatProxies.get(rand.nextInt(instance.boatProxies.size()));
    }

    public ArrayList<BoatProxy> getAll() {
        return instance.boatProxies;
    }

    public static void remove(BoatProxy proxy) {
        instance.remove(proxy);
    }

    public boolean createSimulatedBoatProxy(String name, InetSocketAddress addr, Color color) {

        instance.createBoatProxy(name, addr, color);

        return true;
    }

    public boolean createPhysicalBoatProxy(String name, String host, Color color) {

        System.out.println("Creating physical boat proxy");
        instance.createBoatProxy(name, CrwNetworkUtils.toInetSocketAddress(host), color);
        //instance.createBoatProxy(name, new InetSocketAddress(host,11411), color);

        return true;
    }

    public void shutdown() {
        instance.shutdown();
    }

    public void addListener(ProxyManagerListener l) {
        listeners.add(l);
    }
    
    public void setConsole(OperatorConsoleInterface c) {
        instance.setConsole(c);
    }
    
    private static class Singleton {

        ArrayList<BoatProxy> boatProxies = new ArrayList<BoatProxy>();
        HashMap<InetSocketAddress, BoatProxy> boatMap = new HashMap<InetSocketAddress, BoatProxy>();
        OperatorConsoleInterface console = null;

        public Singleton() {
        }

        public void createBoatProxy(String name, InetSocketAddress addr, Color color) {

            try {
                BoatProxy proxy = new BoatProxy(name, color, 1, addr);
                boatProxies.add(proxy);
                boatMap.put(addr, proxy);

                proxy.start();
                
                for (ProxyManagerListener l : listeners) {
                    l.proxyAdded(proxy);
                }

            } catch (Exception e) {
                System.out.println("Creating proxy failed: " + e);
                e.printStackTrace();
            }
        }

        private void redraw() {
            if (console != null) {
                console.redraw();
            }
        }

        public void setConsole(OperatorConsoleInterface console) {
            this.console = console;
        }

        public void setCameraRates(double d) {
            System.out.println("Setting camera on all boats to have time between frames = " + d);
            for (BoatProxy p : boatProxies) {
                p._server.stopCamera(null);
                p._server.startCamera(0, d, 640, 480, null);
            }
        }

        private void remove(BoatProxy proxy) {
            boatProxies.remove(proxy);
            // @todo Proxies are not removed from hash table, expecting that something else with a new URI will override
            // boatMap.remove(proxy.)
        }

        private void shutdown() {
            for (BoatProxy p : boatProxies) {
                p._server.stopCamera(null);
                p._server.shutdown();
            }
        }
    }
        
}
