/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client;

/**
 *
 * @author pscerri
 */
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Earth.USGSDigitalOrtho;
import gov.nasa.worldwind.layers.Earth.USGSUrbanAreaOrtho;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is the most basic World Wind program.
 *
 * @version $Id: HelloWorldWind.java 4869 2008-03-31 15:56:36Z tgaskins $
 */
public class HelloWorldWind {
    // An inner class is used rather than directly subclassing JFrame in the main class so
    // that the main can configure system properties prior to invoking Swing. This is
    // necessary for instance on OS X (Macs) so that the application name can be specified.

    private static class AppFrame extends javax.swing.JFrame {

        public AppFrame() {
            final WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
            wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
            this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
            this.pack();

            wwd.setModel(new BasicModel());

            final ConcurrentLinkedQueue<Marker> markers = new ConcurrentLinkedQueue<Marker>();            

            final MarkerLayer layer = new MarkerLayer();
            layer.setOverrideMarkerElevation(true);
            layer.setKeepSeparated(false);
            layer.setElevation(1000d);
            layer.setMarkers(markers);

            wwd.getModel().getLayers().add(layer);
            wwd.redraw();

            final USGSDigitalOrtho usgslayer = new USGSDigitalOrtho();
            //final USGSUrbanAreaOrtho usgslayer = new USGSUrbanAreaOrtho();            
            wwd.getModel().getLayers().add(usgslayer);
            wwd.redraw();            

            /*
            (new Thread()         {

                public void run() {

                    try {
                        sleep(3000);
                    } catch (Exception e) {
                    }


                    Angle lat = Angle.fromDegrees(30.0);
                    Angle lon = Angle.fromDegrees(-30.0);
                    LatLon latLon = new LatLon(lat, lon);
                    View view = wwd.getView();
                    System.out.println("Eyepoint: " + view.getEyePoint());
                    System.out.println("Center: " + view.getCenterPoint());
                    double distance = view.getCenterPoint().distanceTo3(view.getEyePoint());
                    System.out.println("Distance:  " + distance);
                    // double distance = 100.0;
                    view.goTo(new Position(latLon, 0), distance);

                    try {
                        sleep(3000);
                    } catch (Exception e) {
                    }
                    
                    
                    double latitude = 30.0;
                    for (int i = 0; i < 50; i++) {

                        Marker marker = new BasicMarker(Position.fromDegrees(latitude, -30.0, 0),
                                new BasicMarkerAttributes(Material.YELLOW, BasicMarkerShape.ORIENTED_CYLINDER_LINE, 0.9));
                        markers.add(marker);
                        wwd.redraw();

                        latitude += 1.0;
                        try {
                            sleep(3000);
                        } catch (Exception e) {
                        }
                    }
                }
            }).start();
             */

        }
    }

    public static void main(String[] args) {

        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Hello World Wind");


        java.awt.EventQueue.invokeLater(new Runnable()
                {

            public void run() {
                // Create an AppFrame and immediately make it visible. As per Swing convention, this
                // is done within an invokeLater call so that it executes on an AWT thread.
                new AppFrame().setVisible(true);
            }
        });
    }
}