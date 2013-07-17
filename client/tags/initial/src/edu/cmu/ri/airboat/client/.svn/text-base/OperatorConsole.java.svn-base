/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client;

/**
 *
 * @author pscerri
 */
import edu.cmu.ri.airboat.client.tasking.TestTaskGenerator;
import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Earth.USGSDigitalOrtho;
import gov.nasa.worldwind.layers.Earth.USGSTopoHighRes;
import gov.nasa.worldwind.layers.Earth.USGSTopographicMaps;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.pick.PickedObject;
import java.awt.event.MouseAdapter;

/**
 * 
 *
 * Adapted from: @version $Id: HelloWorldWind.java 4869 2008-03-31 15:56:36Z tgaskins $
 */
public class OperatorConsole {

    AppFrame frame = null;
    BoatPanel boatPanel = new BoatPanel();
    TaskPanel taskPanel = new TaskPanel();

    public OperatorConsole() {

        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Airboat Control");

        java.awt.EventQueue.invokeLater(new Runnable()   {

            public void run() {
                // Create an AppFrame and immediately make it visible. As per Swing convention, this
                // is done within an invokeLater call so that it executes on an AWT thread.
                frame = new AppFrame();
                frame.setVisible(true);
            }
        });

        (new ProxyManager()).setConsole(this);

        // For task management testing
        new TestTaskGenerator();

    }

    void redraw() {
        if (frame != null && frame.wwd != null) {
            frame.wwd.redraw();
        } else {
            System.out.println("redraw failed");
        }
    }

    // An inner class is used rather than directly subclassing JFrame in the main class so
    // that the main can configure system properties prior to invoking Swing. This is
    // necessary for instance on OS X (Macs) so that the application name can be specified.
    private class AppFrame extends javax.swing.JFrame {

        // public ArrayList<Marker> markers = new ArrayList<Marker>();
        private AirboatControl controller = null;
        private AirboatCommand commander;
        WorldWindowGLCanvas wwd = null;

        public AppFrame() {
            wwd = new WorldWindowGLCanvas();
            wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
            // this.getContentPane().add(taskPanel, java.awt.BorderLayout.NORTH);
            this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
            this.getContentPane().add(boatPanel, java.awt.BorderLayout.SOUTH);
            this.pack();

            wwd.setModel(new BasicModel());

            MarkerLayer ml = new MarkerLayer();
            ml.setOverrideMarkerElevation(true);
            ml.setKeepSeparated(false);
            ml.setElevation(10d);
            ml.setMarkers((new ProxyManager()).getMarkers());
            ml.setPickEnabled(true);

            wwd.getModel().getLayers().add(ml);

            final USGSTopographicMaps m2 = new USGSTopographicMaps();

            /*
            for (Layer l: wwd.getModel().getLayers()) {
            System.out.println("Layer type: " + l.getName() + " " + l.getClass());
            }
             */

            final USGSDigitalOrtho usgslayer = new USGSDigitalOrtho();
            final USGSTopoHighRes usgsTopo = new USGSTopoHighRes();

            // @todo Make this an option (e.g., a combobox)
            //wwd.getModel().getLayers().add(usgslayer);
            //wwd.getModel().getLayers().add(usgsTopo);

            wwd.getModel().getLayers().add(m2);

            wwd.redraw();

            // Example selection code
            wwd.addSelectListener(new SelectListener()     {

                public void selected(SelectEvent event) {

                    // System.out.println("Select event 1");

                    if (event.getEventAction().equals(SelectEvent.HOVER) && event.getObjects() != null) {
                        System.out.printf("%d objects\n", event.getObjects().size());
                        if (event.getObjects().size() > 1) {
                            for (PickedObject po : event.getObjects()) {
                                System.out.println(po.getObject().getClass().getName());

                                if (po.getObject() instanceof BoatMarker) {
                                    System.out.println("Got boat marker: " + ((BoatMarker) po.getObject()).getProxy().toString());
                                    boatPanel.setProxy(((BoatMarker) po.getObject()).getProxy());
                                }
                            }
                        }
                    }
                }
            });


            wwd.addSelectListener(new SelectListener()     {

                public void selected(SelectEvent event) {

                    // System.out.println("Select event 2");

                    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {
                        // This is a left click                                                

                        System.out.println(event.toString());

                        if (event.hasObjects() && event.getTopPickedObject().hasPosition()) {
                            // There is a picked object with a position
                            //if (event.getTopObject().getClass().equals(pickedObjClass)) {
                            // This object class we handle and we have an orbit view
                            Position targetPos = event.getTopPickedObject().getPosition();
                            View view = wwd.getView();
                            // Use a PanToIterator to iterate view to target position
                            double distance = view.getCenterPoint().distanceTo3(view.getEyePoint());
                            if (view != null) {
                                // The elevation component of 'targetPos' here is not the surface elevation,
                                // so we ignore it when specifying the view center position.                                
                                view.goTo(new Position(targetPos, 0), targetPos.getElevation() + Math.max(200, distance / 2.0));
                            }

                            //}
                        }
                    }
                }
            });

            wwd.addMouseListener(new MouseAdapter()     {

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {

                    if (e.isControlDown()) {
                        System.out.println("Mouse pressed");
                        Position targetPos = wwd.getCurrentPosition();
                        // @todo Consider shifting this control to ProxyManager
                        boatPanel.setWaypoint(targetPos);
                    }
                }
            });
        }
    }
    /**
     * Public static variable to allow hacking based on actual server
     */
    public static String type = null;

    public static void main(String[] args) {

        new ProxyManager();
        new OperatorConsole();

        ConfigureBoatsFrame cf = new ConfigureBoatsFrame();
        cf.setVisible(true);

        /*
        JComboBox combo = new JComboBox(new String[]{"Dummy", "VBS", "Boats"});
        int ret = JOptionPane.showConfirmDialog(null, combo, "Configuration", JOptionPane.OK_CANCEL_OPTION);
        if (ret == JOptionPane.OK_OPTION) {
        type = (String) combo.getSelectedItem();
        
        System.out.println("Starting in mode : " + type);
        
        if (type.equalsIgnoreCase("Dummy")) {
        AirboatDummy dummy = new AirboatDummy();
        
        try {
        XmlRpcServer _server = new XmlRpcServer(5000);
        _server.registerProxyingHandler(null, "^control\\.(.*)", dummy);
        _server.registerProxyingHandler(null, "^command\\.(.*)", dummy);
        _server.start();
        } catch (IOException ex) {
        Logger.getLogger(AirboatDummy.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Server started");
        } else if (type.equalsIgnoreCase("VBS")) {
        double[] position = {12786.2, 15830.7, 0.00140381, 0.0, 0.0, 0.0};
        
        AirboatVbs.startRpcInstance("mandilaria.cimds.ri.cmu.edu", 5000, position);
        } else {
        
        // Connecting to boat, no server required.
        }
        }
         */

    }
}
