/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim;

import edu.cmu.ri.airboat.gulfsim.tasking.PlanAgent;
import edu.cmu.ri.airboat.gulfsim.tasking.TOP;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.Marker;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import javax.swing.JOptionPane;

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

    // @todo Keep track of plan agents
    public void injectPlan(TOP top) {
        System.out.println("Proxy manager gets top: " + top);
        PlanAgent pa = new PlanAgent(top);
    }

    public BoatSimpleProxy getRandomProxy() {
        
        if (instance.boatProxies.size() == 0) {
            return null;
        }
        
        return instance.boatProxies.get(rand.nextInt(instance.boatProxies.size()));
    }

    public boolean createSimulatedBoatProxy(int port) {
        
        instance.createBoatProxy("http://localhost:"+port, Material.CYAN);
        
        return true;
    }

    public boolean createVBSBoatProxy(int port) {
        
        instance.createBoatProxy("http://localhost:"+port, Material.YELLOW);
        
        return true;
    }

    public boolean createPhysicalBoatProxy(String host) {
        
        System.out.println("Creating physical boat proxy");
        instance.createBoatProxy(host, Material.CYAN);
        
        return true;
    }

    
    private static class Singleton {

        public ArrayList<Marker> markers = new ArrayList<Marker>();
        ArrayList<BoatSimpleProxy> boatProxies = new ArrayList<BoatSimpleProxy>();
        Hashtable<String, BoatSimpleProxy> boatMap = new Hashtable<String, BoatSimpleProxy>();
        OperatorConsole console = null;

        public Singleton() {

            /*
            (new Thread()   {

                public void run() {

                    String ipAddrStr = "http://localhost:5000";

                    // Both VBS and Dummy are connecting to a local server, so we can be lazy and skip the dialog
                    if (OperatorConsole.type.equalsIgnoreCase("Boat")) {
                        
                        // Something odd happens when this dialog pops up before 
                        try {
                            sleep(10000);
                        } catch (Exception e) {
                        }
                        ipAddrStr = (String) JOptionPane.showInputDialog(null, "Enter URL of Server", "Connect to Airboat", JOptionPane.QUESTION_MESSAGE, null, null, ipAddrStr);
                    }

                    if (ipAddrStr == null) {
                        System.exit(0);
                    }

                    createBoatProxy(ipAddrStr, Material.PINK);
                    // createBoatProxy("http://localhost:5001", Material.CYAN);
                }
            }).start();
             * 
             */
        }

        public void createBoatProxy(String ipAddrStr, Material material) {
            BoatSimpleProxy proxy = new BoatSimpleProxy(ipAddrStr, markers, material);
            boatProxies.add(proxy);
            boatMap.put(ipAddrStr, proxy);
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
