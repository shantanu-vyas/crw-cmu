/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
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

    void setConsole(OperatorConsole c) {
        instance.setConsole(c);
    }

    public ArrayList<Marker> getMarkers() {
        return instance.getMarkers();
    }

    public BoatSimpleProxy getRandomProxy() {

        if (instance.boatProxies.size() == 0) {
            return null;
        }

        return instance.boatProxies.get(rand.nextInt(instance.boatProxies.size()));
    }

    public boolean createSimulatedBoatProxy(String name, URI uri, Color color) {        
        
        instance.createBoatProxy(name, uri, color);

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

        try {
            System.out.println("Creating physical boat proxy");
            instance.createBoatProxy(name, new URI(host), color);
        } catch (URISyntaxException e) {
            System.out.println("Problem with URI: " + e);
        }
        return true;
    }

    private static class Singleton {

        public ArrayList<Marker> markers = new ArrayList<Marker>();
        ArrayList<BoatSimpleProxy> boatProxies = new ArrayList<BoatSimpleProxy>();
        Hashtable<URI, BoatSimpleProxy> boatMap = new Hashtable<URI, BoatSimpleProxy>();
        OperatorConsole console = null;

        public Singleton() {
           
        }
        
        public void createBoatProxy(String name, URI uri, Color color) {
        
            try {
                BoatSimpleProxy proxy = new BoatSimpleProxy(name, markers, color, 1, uri, "vehicle" + (int) (new Random().nextInt(1000000)));               
                boatProxies.add(proxy);
                boatMap.put(uri, proxy);
            } catch (Exception e) {
                System.out.println("Creating proxy failed: " + e);
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
    }
}
