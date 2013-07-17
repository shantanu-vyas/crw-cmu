/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.generalAlmost;

import edu.cmu.ri.airboat.floodtest.OperatorConsole;
import edu.cmu.ri.crw.CrwNetworkUtils;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Color;
import java.net.InetSocketAddress;
import java.net.URI;
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

    public void redraw() {
        instance.redraw();
    }

    public void setConsole(OperatorConsole c) {
        instance.setConsole(c);
    }

    public ArrayList<Marker> getMarkers() {
        return instance.getMarkers();
    }

    public static void setCameraRates(double d) {
        instance.setCameraRates(d);
    }

    public BoatSimpleProxy getRandomProxy() {

        if (instance.boatProxies.isEmpty()) {
            return null;
        }

        return instance.boatProxies.get(rand.nextInt(instance.boatProxies.size()));
    }

    public ArrayList<BoatSimpleProxy> getAll() {
        return instance.boatProxies;
    }

    public static void remove(BoatSimpleProxy proxy) {
        instance.remove(proxy);
    }

    public boolean createSimulatedBoatProxy(String name, InetSocketAddress addr, Color color) {

        instance.createBoatProxy(name, addr, color);

        return true;
    }

    public boolean createVBSBoatProxy(int port) {

        /*
        try {
        instance.createBoatProxy(new URI("http://localhost:" + port), Material.YELLOW);
        } catch (URISyntaxException e) {
        System.out.println("Problem with URI: " + e);
        }
         * */

        System.out.println("\n\n\nUnimplemented create VBS proxy!!!!!\n\n\n");
        return true;
    }

    public boolean createPhysicalBoatProxy(String name, String host, Color color) {

        System.out.println("Creating physical boat proxy");
        instance.createBoatProxy(name, CrwNetworkUtils.toInetSocketAddress(host), color);

        return true;
    }

    public void shutdown() {
        instance.shutdown();
    }

    private static class Singleton {

        public ArrayList<Marker> markers = new ArrayList<Marker>();
        ArrayList<BoatSimpleProxy> boatProxies = new ArrayList<BoatSimpleProxy>();
        HashMap<InetSocketAddress, BoatSimpleProxy> boatMap = new HashMap<InetSocketAddress, BoatSimpleProxy>();
        OperatorConsole console = null;

        public Singleton() {
        }

        public void createBoatProxy(String name, InetSocketAddress addr, Color color) {

            try {
                BoatSimpleProxy proxy = new BoatSimpleProxy(name, markers, color, 1, addr);
                boatProxies.add(proxy);
                boatMap.put(addr, proxy);

                if (console != null) {
                    console.setSelected(proxy);
                }

                proxy.start();

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

        public void setConsole(OperatorConsole console) {
            this.console = console;
        }

        public ArrayList<Marker> getMarkers() {
            return markers;
        }

        public void setCameraRates(double d) {
            System.out.println("Setting camera speeds to " + d);
            for (BoatSimpleProxy p : boatProxies) {
                p._server.stopCamera(null);
                p._server.startCamera(0, d, 640, 480, null);
            }
        }

        private void remove(BoatSimpleProxy proxy) {
            boatProxies.remove(proxy);
            // @todo Proxies are not removed from hash table, expecting that something else with a new URI will override
            // boatMap.remove(proxy.)
        }

        private void shutdown() {
            for (BoatSimpleProxy p : boatProxies) {
                p._server.stopCamera(null);
                p._server.shutdown();
            }
        }
    }
}
