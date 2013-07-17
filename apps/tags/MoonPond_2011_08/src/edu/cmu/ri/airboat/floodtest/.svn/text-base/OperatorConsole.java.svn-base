/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Earth.USGSDigitalOrtho;
import gov.nasa.worldwind.layers.Earth.USGSTopoHighRes;
import gov.nasa.worldwind.layers.Earth.USGSTopographicMaps;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author pscerri
 */
/**
 * 
 *
 * Adapted from: @version $Id: HelloWorldWind.java 4869 2008-03-31 15:56:36Z tgaskins $
 */
public class OperatorConsole {

    AppFrame frame = null;
    BoatPanel boatPanel = new BoatPanel();
    ImagePanel imgPanel = new ImagePanel();
    BoatSimpleProxy selectedProxy = null;
    static RenderableLayer polyLayer = new RenderableLayer();
    static public boolean assigningArea = false;
    static public boolean assigningPath = false;

    public OperatorConsole() {

        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Airboat Control");

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                // Create an AppFrame and immediately make it visible. As per Swing convention, this
                // is done within an invokeLater call so that it executes on an AWT thread.
                frame = new AppFrame();
                frame.setVisible(true);
            }
        });

    }

    void redraw() {
        if (frame != null && frame.wwd != null) {
            frame.wwd.redraw();
        } else {
            System.out.println("Redraw failed");
        }
    }

    // An inner class is used rather than directly subclassing JFrame in the main class so
    // that the main can configure system properties prior to invoking Swing. This is
    // necessary for instance on OS X (Macs) so that the application name can be specified.
    private class AppFrame extends javax.swing.JFrame {

        public ArrayList<Marker> markers = new ArrayList<Marker>();
        // private AirboatControl controller = null;
        // private AirboatCommand commander;
        WorldWindowGLJPanel wwd = null;

        public AppFrame() {

            // @todo Make initial position configurable            
            Configuration.setValue(AVKey.INITIAL_LATITUDE, 14.2263);
            Configuration.setValue(AVKey.INITIAL_LONGITUDE, 121.324);
            Configuration.setValue(AVKey.INITIAL_ALTITUDE, 10000.0);

            wwd = new WorldWindowGLJPanel();
            wwd.setPreferredSize(new java.awt.Dimension(1000, 800));

            this.getContentPane().add(boatPanel, java.awt.BorderLayout.NORTH);
            this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
            this.getContentPane().add(imgPanel, java.awt.BorderLayout.EAST);
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
            wwd.getModel().getLayers().add(usgslayer);
            // wwd.getModel().getLayers().add(usgsTopo);

            // wwd.getModel().getLayers().add(m2);

            wwd.getModel().getLayers().add(polyLayer);
            wwd.redraw();

            String TEST_PATTERN = "/Users/pscerri/Desktop/1.jpg";
            SurfaceImage si2 = new SurfaceImage(TEST_PATTERN, new ArrayList<LatLon>(Arrays.asList(
                    LatLon.fromDegrees(14.8677, 121.1668),
                    LatLon.fromDegrees(14.8677, 120.8332),
                    LatLon.fromDegrees(15.1321, 120.8326),
                    LatLon.fromDegrees(15.1321, 121.1674))));
            polyLayer.addRenderable(si2);

            // Example selection code
            wwd.addSelectListener(new SelectListener() {

                public void selected(SelectEvent event) {

                    // System.out.println("Select event 1");

                    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK) && event.getObjects() != null) {
                        System.out.printf("%d objects\n", event.getObjects().size());
                        if (event.getObjects().size() > 1) {
                            for (PickedObject po : event.getObjects()) {
                                System.out.println(po.getObject().getClass().getName());

                                if (po.getObject() instanceof BoatMarker) {
                                    System.out.println("Got boat marker: " + ((BoatMarker) po.getObject()).getProxy().toString());
                                    boatPanel.setProxy(((BoatMarker) po.getObject()).getProxy());
                                    selectedProxy = ((BoatMarker) po.getObject()).getProxy();
                                } /*else if (po.getObject() instanceof TaskMarker) {
                                System.out.println("Got task marker, any action commented out");
                                // taskPanel.setCurrTOP(((TaskMarker) po.getObject()).getTOP());
                                } */
                            }
                        }
                    } else if (event.getEventAction().equals(SelectEvent.RIGHT_CLICK)) {
                        System.out.println("Right click " + event.toString() + " XXXX " + event.getObjects());
                    }
                }
            });

            wwd.addMouseListener(new MouseAdapter() {

                ArrayList<Position> shapeParams = new ArrayList<Position>();
                Polyline pLine = null;
                gov.nasa.worldwind.render.Ellipsoid ellipsoid = null;

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {

                    super.mousePressed(e);

                    // System.out.println("Mouse pressed");

                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    super.mouseReleased(me);

                    if (selectedProxy == null) {
                        return;
                    }

                    // Waypoints
                    if (me.isControlDown()) {

                        Position targetPos = wwd.getCurrentPosition();
                        // @todo Consider shifting this control to ProxyManager
                        selectedProxy.setWaypoint(targetPos);
                        System.out.println("Current boat given new waypoint: " + targetPos);
                        me.consume();

                    }


                    Position pickPos = wwd.getCurrentPosition();
                    if (pickPos == null) {
                        Point p = wwd.getMousePosition();

                        Line ray = wwd.getView().computeRayFromScreenPoint(p.x, p.y);


                        Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(ray);
                        if (intersections != null && intersections.length > 0) {
                            pickPos = wwd.getView().getGlobe().computePositionFromPoint(intersections[0].getIntersectionPoint());
                        }

                        // System.out.println("Got position from ray " + pickPos);
                    }

                    // Path
                    if (assigningPath) {

                        System.out.println("Point for path: " + pickPos);

                        if (pickPos != null) {
                            shapeParams.add(pickPos);

                            if (pLine != null) {
                                polyLayer.removeRenderable(pLine);
                            }
                            if (ellipsoid != null) {
                                polyLayer.removeRenderable(ellipsoid);
                                ellipsoid = null;
                            }

                            if (shapeParams.size() == 1) {

                                ellipsoid = new Ellipsoid(pickPos, 50, 150, 150);
                                polyLayer.addRenderable(ellipsoid);

                            } else if (shapeParams.size() > 1) {

                                pLine = new Polyline(shapeParams);
                                pLine.setFollowTerrain(true);
                                pLine.setLineWidth(4.0);
                                polyLayer.addRenderable(pLine);
                            }

                            me.consume();

                            if (me.getClickCount() > 1) {
                                System.out.println("FINISHED!");
                                selectedProxy.setWaypoints(pLine);
                                assigningPath = false;
                                shapeParams.clear();
                                pLine = null;
                            }
                        }
                    }

                    // Areas
                    if (assigningArea) {

                        if (me.getClickCount() == 1) {
                            System.out.println("Point for shape: " + pickPos);
                            shapeParams.add(pickPos);

                            if (pLine != null) {
                                polyLayer.removeRenderable(pLine);
                            }
                            if (ellipsoid != null) {
                                polyLayer.removeRenderable(ellipsoid);
                                ellipsoid = null;
                            }

                            if (shapeParams.size() == 1) {

                                ellipsoid = new Ellipsoid(pickPos, 50, 150, 150);
                                polyLayer.addRenderable(ellipsoid);

                            } else if (shapeParams.size() > 1) {

                                pLine = new Polyline(shapeParams);
                                pLine.setFollowTerrain(true);
                                pLine.setLineWidth(4.0);
                                polyLayer.addRenderable(pLine);
                            }

                            me.consume();
                        } else {
                            System.out.println("FINISHED!");
                            me.consume();

                            if (shapeParams.size() > 2) {

                                if (pLine != null) {
                                    polyLayer.removeRenderable(pLine);
                                }

                                Polygon pgon = new Polygon(shapeParams);
                                pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                                polyLayer.addRenderable(pgon);

                                shapeParams.clear();

                                selectedProxy.setArea(pgon);
                            }

                            assigningArea = false;
                        }
                    }
                }

                @Override
                public void mouseClicked(MouseEvent me) {
                    // System.out.println("Mouse click ignored");
                }

                @Override
                public void mouseDragged(MouseEvent me) {
                    System.out.println("Dragged");
                }
            });

            /*
            wwd.getInputHandler().addMouseMotionListener(new MouseMotionAdapter() {
            
            public void mouseDragged(MouseEvent event) {
            if (event.isShiftDown()) {
            System.out.println("Meta while dragging: " + event);
            event.consume();
            } else {
            // System.out.println("Dragging: " + event.isControlDown() + " " + event.isMetaDown());
            }
            }
            });
             * 
             */
        }
    }

    public static void addRenderable(Renderable r) {
        polyLayer.addRenderable(r);
    }

    public static void removeRenderable(Renderable r) {
        polyLayer.removeRenderable(r);
    }
    /**
     * Public static variable to allow hacking based on actual server
     */
    public static String type = null;

    public static void main(String[] args) {


        ConfigureBoatsFrame config = new ConfigureBoatsFrame();
        config.setVisible(true);

        OperatorConsole oc = new OperatorConsole();

        /*
        public static void main(String args[]) throws Exception {
        
        // Start a local ros core
        RosCore core = RosCore.newPublic(11411);
        NodeRunner.newDefault().run(core, NodeConfiguration.newPrivate());
        core.awaitStart();
        
        // Select if we want to use the local ros core or a remote one
        URI masterUri = core.getUri();
        //URI masterUri = new URI("http://localhost:11311");
        System.out.println("Master URI: " + masterUri);
        
        // Create a simulated boat and run a ROS server around it
        VehicleServer server = new SimpleBoatSimulator();
        RosVehicleServer rosServer = new RosVehicleServer(masterUri, "vehicle", server);
        
        // Create a ROS proxy server that accesses the same object
        RosVehicleProxy proxyServer = new RosVehicleProxy(masterUri, "vehicle_client");
        
        // Wait for someone to hit Enter
        {
        System.out.println("Press [ENTER] to begin.");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        }
        
        // TODO: put some system tests in here
        proxyServer.setAutonomous(true);
        
        UtmPose p = new UtmPose();
        p.pose.position.x = 10.0;
        p.pose.position.y = 60.0;
        proxyServer.startWaypoint(p, new WaypointObserver() {
        @Override
        public void waypointUpdate(WaypointState status) {
        System.out.println("STATUS: " + status);
        }
        });
        
        proxyServer.startCamera(0, 1.0, 640, 480, new ImagingObserver() {
        @Override
        public void imagingUpdate(CameraState status) {
        System.err.println("IMAGES: " + status);
        }
        });
        
        // Wait for someone to hit Enter
        {
        System.out.println("Press [ENTER] to continue.");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        }
        
        // Shut down everything
        try {
        proxyServer.shutdown();
        } catch (RosRuntimeException ex) {
        System.err.println("Proxy server was uncleanly shutdown.");
        }
        
        try {
        rosServer.shutdown();
        } catch (RosRuntimeException ex) {
        ex.printStackTrace();
        System.err.println("Ros VehicleServer was uncleanly shutdown.");
        }
        
        try {
        core.shutdown();
        } catch (RosRuntimeException ex) {
        System.err.println("Core was uncleanly shutdown.");
        }
        System.exit(0);
        }
         */

    }
}