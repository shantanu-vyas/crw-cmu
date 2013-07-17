/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.airboat.general.BoatProxyListener;
import edu.cmu.ri.airboat.general.OperatorConsoleInterface;
import edu.cmu.ri.airboat.general.ConfigureBoatsFrame;
import edu.cmu.ri.airboat.general.ProxyManager;
import edu.cmu.ri.airboat.general.ProxyManagerListener;
import edu.cmu.ri.crw.CrwSecurityManager;
import edu.cmu.ri.crw.ImageListener;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author pscerri
 */
/**
 *
 *
 * Adapted from:
 *
 * @version $Id: HelloWorldWind.java 4869 2008-03-31 15:56:36Z tgaskins $
 */
public class OperatorConsole implements OperatorConsoleInterface, ProxyManagerListener {

    static AppFrame frame = null;
    BoatPanel boatPanel = new BoatPanel();
    ImagePanel imgPanel = new ImagePanel();
    //AutonomyPanel autoPanel = new AutonomyPanel();
    IntelligencePanel autoPanel = new IntelligencePanel();
    BoatProxy selectedProxy = null;
    ArrayList<Marker> boatMarkers = new ArrayList<Marker>();
    static RenderableLayer polyLayer = new RenderableLayer();
    static public SurfaceImageLayer imageLayer = new SurfaceImageLayer();
    MarkerLayer ml = null;
    // @todo Clean up modality stuff on OperatorConsole
    static private boolean assigningArea = false;
    static private boolean assigningSensingArea = false;
    static private boolean assigningBuoyDetectionArea = false;
    static private boolean assigningPath = false;
    static private boolean settingWaterLevel = false;

    public static void setAssigningArea(boolean assigningArea) {
        OperatorConsole.assigningArea = assigningArea;
        if (assigningArea) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            frame.setCursor(Cursor.getDefaultCursor());
        }
    }

    public static void setAssigningBuoyDetectionArea(boolean assigningBuoyDetectionArea) {
        OperatorConsole.assigningBuoyDetectionArea = assigningBuoyDetectionArea;
        if (assigningBuoyDetectionArea) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            frame.setCursor(Cursor.getDefaultCursor());
        }
    }

    public static void setAssigningPath(boolean assigningPath) {
        OperatorConsole.assigningPath = assigningPath;
        if (assigningPath) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            frame.setCursor(Cursor.getDefaultCursor());
        }
    }

    public static void setAssigningSensingArea(boolean assigningSensingArea) {
        OperatorConsole.assigningSensingArea = assigningSensingArea;
        if (assigningSensingArea) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            frame.setCursor(Cursor.getDefaultCursor());
        }

    }

    public OperatorConsole() {

        // Created to make sure listeners are started
        new IntelligenceAlgorithms();

        // System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Airboat Control");

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Create an AppFrame and immediately make it visible. As per Swing convention, this
                // is done within an invokeLater call so that it executes on an AWT thread.
                frame = new AppFrame();
                frame.setVisible(true);

                try {
                    InetAddress addr = InetAddress.getLocalHost();

                    String ipAddrS = addr.getHostAddress();

                    frame.setTitle("Operator console @ " + ipAddrS);

                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            (new ProxyManager()).shutdown();
                        }
                    });

                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            super.windowClosing(we);
                            System.out.println("Shutting down");
                            (new ProxyManager()).shutdown();
                            System.exit(0);
                        }
                    });

                } catch (java.security.AccessControlException e) {
                    System.out.println("No DNS available, setting offline mode");
                    Configuration.setValue(AVKey.OFFLINE_MODE, "true");
                } catch (Exception e) {
                    System.out.println("Problem getting local IP address " + e);
                }
            }
        });

        (new ProxyManager()).addListener(this);

    }

    public void redraw() {
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
            // Pittsburgh
            Configuration.setValue(AVKey.INITIAL_LATITUDE, 40.44515205369163);
            Configuration.setValue(AVKey.INITIAL_LONGITUDE, -80.01877404355538);
            
            // Doha
            //Configuration.setValue(AVKey.INITIAL_LATITUDE, 25.3);            
            //Configuration.setValue(AVKey.INITIAL_LONGITUDE, 51.5333);
            
            // Matilda Bay
            //Configuration.setValue(AVKey.INITIAL_LATITUDE, -31.98);            
            //Configuration.setValue(AVKey.INITIAL_LONGITUDE, 115.82);
            
            Configuration.setValue(AVKey.INITIAL_ALTITUDE, 3000.0);

            // Set this when offline
            try {
                if (InetAddress.getByName("google.com").isReachable(1000)) {
                    Configuration.setValue(AVKey.OFFLINE_MODE, "false");
                } else {
                    Configuration.setValue(AVKey.OFFLINE_MODE, "true");
                }
            } catch (Exception e) {
                System.out.println("No network: " + e);
                Configuration.setValue(AVKey.OFFLINE_MODE, "true");
            }

            wwd = new WorldWindowGLJPanel();
            wwd.setPreferredSize(new java.awt.Dimension(1000, 800));

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new FlowLayout());
            topPanel.add(boatPanel, java.awt.BorderLayout.NORTH);
            topPanel.add(autoPanel, java.awt.BorderLayout.NORTH);
            this.getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);
            this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
            this.getContentPane().add(imgPanel, java.awt.BorderLayout.EAST);
            this.pack();

            wwd.setModel(new BasicModel());

            ml = new MarkerLayer();
            ml.setOverrideMarkerElevation(true);
            ml.setKeepSeparated(false);
            ml.setElevation(10d);
            ml.setMarkers(boatMarkers);
            ml.setPickEnabled(true);

            wwd.getModel().getLayers().add(ml);

            /*
             for (Layer l: wwd.getModel().getLayers()) {
             System.out.println("Layer type: " + l.getName() + " " + l.getClass());
             }
             */

            /*
            URL url = getClass().getResource("logo_msve.png");
            if (url == null) {
                System.out.println("FAILED! for " + url);
            }
            Icon bug = new ImageIcon(url);
            */

            // @todo Make layer an option (e.g., a combobox)
            
            // Virtual Earth
            for (Layer layer : wwd.getModel().getLayers()) {
                if (layer.getName().equals("MS Virtual Earth Aerial")) {
                    layer.setEnabled(true);
                }
            }

//             // Yahoo imagergy
//            final YahooMapsLayer ya = new YahooMapsLayer();
//            wwd.getModel().getLayers().add(ya);
//            
//             // Open maps
//            final OSMMapnikLayer ol = new OSMMapnikLayer();
//            wwd.getModel().getLayers().add(ol);
//    
//            final OSMCycleMapLayer  ol2 = new OSMCycleMapLayer();
//            wwd.getModel().getLayers().add(ol2);
//            
//            final OSMMapnikTransparentLayer  ol3 = new OSMMapnikTransparentLayer();
//            wwd.getModel().getLayers().add(ol3);
//    
//             // USGS
//            final USGSDigitalOrtho usgslayer = new USGSDigitalOrtho();
//            wwd.getModel().getLayers().add(usgslayer);
//            
//            final USGSTopographicMaps usgsTopoLow = new USGSTopographicMaps();
//            wwd.getModel().getLayers().add(usgsTopoLow);
//            
//            final USGSTopoHighRes usgsTopoHigh = new USGSTopoHighRes();
//            wwd.getModel().getLayers().add(usgsTopoHigh);

            wwd.getModel().getLayers().add(polyLayer);
            wwd.getModel().getLayers().add(imageLayer);

            wwd.redraw();

            /*
             String TEST_PATTERN = "/Users/pscerri/Documents/Code/crw-cmu/Laguna.png";
             SurfaceImage si2 = new SurfaceImage(TEST_PATTERN, new ArrayList<LatLon>(Arrays.asList(
             LatLon.fromDegrees(14.8677, 121.1668),
             LatLon.fromDegrees(14.8677, 120.8332),
             LatLon.fromDegrees(15.1321, 120.8326),
             LatLon.fromDegrees(15.1321, 121.1674))));
             polyLayer.addRenderable(si2);
             */

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
                                    setSelected(((BoatMarker) po.getObject()).getProxy());
                                    // boatPanel.setProxy(((BoatMarker) po.getObject()).getProxy());
                                    // selectedProxy = ((BoatMarker) po.getObject()).getProxy();                                    

                                } /*else if (po.getObject() instanceof TaskMarker) {
                                 System.out.println("Got task marker, any action commented out");
                                 // taskPanel.setCurrTOP(((TaskMarker) po.getObject()).getTOP());
                                 } */

                            }
                        }
                    } else if (event.getEventAction()
                            .equals(SelectEvent.RIGHT_CLICK)) {
                        System.out.println("Right click " + event.toString() + " XXXX " + event.getObjects());
                    }
                }
            });

            wwd.addMouseListener(new MouseAdapter() {
                ArrayList<Position> shapeParams = new ArrayList<Position>();
                Polyline pLine = null;
                Polyline tempLine = null;
                gov.nasa.worldwind.render.Ellipsoid ellipsoid = null;
                Hashtable<BoatProxy, Renderable> lastRender = new Hashtable<BoatProxy, Renderable>();

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {

                    super.mousePressed(e);

                    // System.out.println("Mouse pressed");

                }
                MouseMotionAdapter motionListener = new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent me) {
                        // System.out.println("Movement");
                        if (tempLine != null) {
                            polyLayer.removeRenderable(tempLine);
                        }
                        ArrayList<Position> tempShapeParams = new ArrayList<Position>();
                        tempShapeParams.add(shapeParams.get(shapeParams.size() - 1));
                        tempShapeParams.add(getPickPosition());
                        tempLine = new Polyline(tempShapeParams);
                        tempLine.setFollowTerrain(true);
                        tempLine.setLineWidth(4.0);
                        polyLayer.addRenderable(tempLine);

                        // System.out.println("Added line from " + tempShapeParams.get(0) + " " + tempShapeParams.get(1) + " " + polyLayer.getNumRenderables());
                    }
                };

                private Position getPickPosition() {
                    Position pickPos = wwd.getCurrentPosition();

                    if (pickPos == null) {
                        Point p = wwd.getMousePosition();

                        Line ray = wwd.getView().computeRayFromScreenPoint(p.x, p.y);

                        Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(ray);
                        if (intersections != null && intersections.length > 0) {
                            pickPos = wwd.getView().getGlobe().computePositionFromPoint(intersections[0].getIntersectionPoint());
                        }
                    }
                    return pickPos;
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    super.mouseReleased(me);

                    Position pickPos = getPickPosition();

                    if (pickPos == null) {
                        return;
                    }

                    // @todo Change this, should not be able to set buoys or sensing, if no proxy exists (which means one would be selected)
                    if (selectedProxy == null && (!settingWaterLevel && !assigningSensingArea && !assigningBuoyDetectionArea)) {

                        System.out.println("Clicked point is " + pickPos);
                        return;
                    }

                    // Waypoints
                    if (me.isControlDown()) {

                        // @todo Consider shifting this control to ProxyManager
                        selectedProxy.setWaypoint(pickPos);
                        System.out.println("Current boat given new waypoint: " + pickPos);
                        me.consume();

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
                                // ellipsoid = new Ellipsoid(pickPos, 50, 150, 150);
                                // polyLayer.addRenderable(ellipsoid);

                                wwd.addMouseMotionListener(motionListener);
                            } else if (shapeParams.size() > 1) {

                                pLine = new Polyline(shapeParams);
                                pLine.setFollowTerrain(true);
                                pLine.setLineWidth(4.0);
                                polyLayer.addRenderable(pLine);
                            }

                            me.consume();

                            if (me.getClickCount() > 1) {
                                
                                // Remove previous
                                Renderable prev = lastRender.get(selectedProxy);
                                if (prev != null) {
                                    polyLayer.removeRenderable(prev);
                                }
                                lastRender.put(selectedProxy, pLine);

                                System.out.println("Finished creating path!");
                                selectedProxy.setWaypoints(pLine);
                                setAssigningPath(false);
                                shapeParams.clear();
                                pLine = null;

                                wwd.removeMouseMotionListener(motionListener);
                                if (tempLine != null) {
                                    polyLayer.removeRenderable(tempLine);
                                }

                            }
                        }
                    } else if (assigningArea || assigningBuoyDetectionArea || assigningSensingArea) {

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
                                // ellipsoid = new Ellipsoid(pickPos, 50, 150, 150);
                                // polyLayer.addRenderable(ellipsoid);

                                wwd.addMouseMotionListener(motionListener);

                            } else if (shapeParams.size() > 1) {

                                /*
                                 if (shapeParams.size() == 2) {
                                 wwd.removeMouseMotionListener(motionListener);
                                 }
                                 */

                                pLine = new Polyline(shapeParams);
                                pLine.setFollowTerrain(true);
                                pLine.setLineWidth(4.0);
                                polyLayer.addRenderable(pLine);
                            }

                            me.consume();

                        } else {
                            System.out.println("FINISHED!");
                            me.consume();

                            wwd.removeMouseMotionListener(motionListener);
                            if (tempLine != null) {
                                polyLayer.removeRenderable(tempLine);
                            }

                            if (shapeParams.size() > 2) {

                                if (pLine != null) {
                                    polyLayer.removeRenderable(pLine);
                                }

                                Polygon pgon = new Polygon(shapeParams);
                                pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND); //Change this to absolute and set it to 716ft (from google earth)
                                ShapeAttributes normalAttributes = new BasicShapeAttributes();
                                normalAttributes.setInteriorOpacity(0.5);
                                pgon.setAttributes(normalAttributes);
                                polyLayer.addRenderable(pgon);

                                shapeParams.clear();

                                if (assigningArea) {
                                    selectedProxy.setArea(pgon);
                                    setAssigningArea(false);
                                    (new IntelligenceAlgorithms()).setArea(pgon);
                                    autoPanel.startB.setEnabled(true);
                                    autoPanel.modelB.setEnabled(true);
                                    autoPanel.configureAdvanced();
                                } else if (assigningBuoyDetectionArea) {
                                    System.out.println("Set buoy detection area");
                                    normalAttributes = new BasicShapeAttributes();
                                    normalAttributes.setInteriorOpacity(0.0);
                                    pgon.setAttributes(normalAttributes);
                                    setAssigningBuoyDetectionArea(false);
                                    AutonomyPanel.setBuoyDetectionArea(pgon);
                                } else if (assigningSensingArea) {
                                    System.out.println("Assigning sensing area");
                                    normalAttributes = new BasicShapeAttributes();
                                    normalAttributes.setInteriorOpacity(0.5);
                                    normalAttributes.setInteriorMaterial(null);
                                    pgon.setAttributes(normalAttributes);
                                    setAssigningSensingArea(false);
                                    AutonomyPanel.setSensorArea(pgon);
                                }

                                Renderable prev = lastRender.get(selectedProxy);
                                if (prev != null) {
                                    polyLayer.removeRenderable(prev);
                                }
                                lastRender.put(selectedProxy, pgon);
                            }
                        }
                    } else if (settingWaterLevel) {

                        System.out.println("Setting water level");

                        final Position fPickPos = pickPos;
                        (new Thread() {
                            public void run() {
                                Polygon water = getWaterLevel(fPickPos);
                                polyLayer.addRenderable(water);
                                settingWaterLevel = false;
                            }

                            private Polygon getWaterLevel(Position fPickPos) {
                                ArrayList<Position> shapeParams = new ArrayList<Position>();

                                // Dummy code - Arnav replace this
                                int height = 1000;
                                Position f0 = new Position(fPickPos.getLatitude(), fPickPos.getLongitude(), fPickPos.elevation + height);
                                Position f1 = new Position(fPickPos.getLatitude().addDegrees(1.0), fPickPos.getLongitude(), fPickPos.elevation + height);
                                Position f2 = new Position(fPickPos.getLatitude().addDegrees(1.0), fPickPos.getLongitude().addDegrees(1.0), fPickPos.elevation + height);
                                Position f3 = new Position(fPickPos.getLatitude(), fPickPos.getLongitude().addDegrees(1.0), fPickPos.elevation + height);
                                shapeParams.add(f0);
                                shapeParams.add(f1);
                                shapeParams.add(f2);
                                shapeParams.add(f3);
                                // End dummy code

                                Polygon p = new Polygon(shapeParams);
                                p.setAltitudeMode(WorldWind.ABSOLUTE);

                                return p;
                            }
                        }).start();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent me) {
                    // System.out.println("Mouse click ignored");
                }

                @Override
                public void mouseDragged(MouseEvent me) {
                    //System.out.println("Dragged");
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
        
        public void redraw() {
            wwd.redraw();
        }
    }

    public void proxyAdded(final BoatProxy bp) {

        final BoatMarker bm = new BoatMarker(bp, bp.getCurrLoc(), new BasicMarkerAttributes(new Material(bp.getColor()), BasicMarkerShape.ORIENTED_SPHERE, 0.9));
        bm.setPosition(bp.getCurrLoc());
        bp.addListener(new BoatProxyListener() {
            boolean first = true;
            ArrayList<Position> shapeParams = new ArrayList<Position>();
            Polyline line = null;

            public void poseUpdated() {
                bm.setPosition(bm.getProxy().getCurrLoc());
                bm.setHeading(Angle.fromRadians(Math.PI / 2.0 - bm.getProxy().getPose().pose.getRotation().toYaw()));

                if (line != null) {
                    // System.out.println("Removing!");
                    polyLayer.removeRenderable(line);
                    line = null;
                }
                if (bm.getProxy().getWaypointsAsPositions() != null) {
                    shapeParams.clear();
                    // System.out.println("HERE ");

                    for (Position position : bm.getProxy().getWaypointsAsPositions()) {
                        shapeParams.add(position);
                    }

                    if (shapeParams.size() < 2) {
                        shapeParams.add(0, bm.getProxy().getCurrLoc());
                    }

                    line = new Polyline(shapeParams);
                    line.setFollowTerrain(true);
                    line.setOffset(10.0);
                    line.setLineWidth(3.0);
                    line.setColor(bm.getProxy().getColor());
                    polyLayer.addRenderable(line);

                } else {
                    // System.out.println("No current waypoint");
                }

                if (first) {
                    boatMarkers.add(bm);
                    first = false;
                }
                frame.redraw();
            }

            public void waypointsComplete() {
                // @todo Operator console ignores waypoint complete
            }
        });

        bp.addImageListener(new ImageListener() {
            public void receivedImage(byte[] ci) {
                // Take a picture, and put the resulting image into the panel
                try {
                    BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(ci));
                    System.out.println("Got image ... ");

                    // @todo Image processing is getting done twice
                    if (image != null) {
                        // Flip the image vertically
                        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
                        tx.translate(0, -image.getHeight(null));
                        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                        image = op.filter(image, null);

                        if (image == null) {
                            System.err.println("Failed to decode image.");
                        }

                        ImagePanel.addImage(image, bp.getPose());

                    } else {
                        System.out.println("Image was null in receivedImage");
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to decode image: " + ex);
                }

            }
        });

        bp.startCamera();


        if (selectedProxy == null) {
            setSelected(bp);
        }

    }

    public void setSelected(BoatProxy proxy) {
        boatPanel.setProxy(proxy);
        selectedProxy = proxy;
        (new IntelligenceAlgorithms()).setSelectedProxy(selectedProxy);
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

        CrwSecurityManager.loadIfDNSIsSlow();

        ConfigureBoatsFrame config = new ConfigureBoatsFrame();
        config.setVisible(true);

        OperatorConsole oc = new OperatorConsole();
        (new ProxyManager()).setConsole(oc);

    }
}
