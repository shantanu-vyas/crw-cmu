/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general;

import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.PoseListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.WaypointListener;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpVehicleServer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import robotutils.Pose3D;

/**
 * @todo Need a flag for autonomous or under human control
 *
 * @author pscerri
 */
public class BoatProxy extends Thread {

    // @todo Needs to be configurable
    private double atWaypointTolerance = 30.0;
    private final String name;
    final AtomicBoolean isConnected = new AtomicBoolean(false);
    // Tasking stuff
    // private ArrayList<RoleAgent> roles = new ArrayList<RoleAgent>();
    private ProxyManager proxyManager = new ProxyManager();
    private Position currWaypoint = null;
    private Position currLoc = null;
    private UTMCoord currUtm = null;
    // private RoleAgent currRole = null;
    private int lngZone = -1;
    // Simulated
    private double fuelLevel = 1.0;
    private double fuelUsageRate = 1.0e-2;
    // ROS update
    private boolean _waypointsWereUpdated;
    PoseListener _stateListener;
    SensorListener _sensorListener;
    WaypointListener _waypointListener;
    ArrayList<BoatProxyListener> listeners = new ArrayList<BoatProxyListener>();
    int _boatNo;
    UtmPose _pose;
    volatile boolean _isShutdown = false;
    // Latest image returned from this boat
    private BufferedImage latestImg = null;
    // Tell the boat to go slowly
    private boolean goSlow = false;
    private boolean goSlowExecuting = false;

    public void sample() {
        System.out.println("Calling sample on server");
        _server.captureImage(100, 100, null);
    }

    //
    // New Control variables
    //
    public enum StateEnum {

        IDLE, WAYPOINT, PATH, AREA
    };
    // Set this to false to turn off the false safe
    final boolean USE_SOFTWARE_FAIL_SAFE = true;
    UtmPose home = null;
    private StateEnum state = StateEnum.IDLE;
    final Queue<UtmPose> _waypoints = new LinkedList<UtmPose>();
    Iterable<Position> _waypointsPos = null;
    private UtmPose currentWaypoint = null;
    UdpVehicleServer _server;
    private Polygon currentArea = null;
    private Polyline currentPath = null;
    private Color color = null;
    //private URI masterURI = null;
    Marker marker = null;
    Marker waypointMarker = null;

    public static enum AutonomousSearchAlgorithmOptions {

        RANDOM, LAWNMOWER, MAX_UNCERTAINTY
    };
    public static AutonomousSearchAlgorithmOptions autonomousSearchAlgorithm = AutonomousSearchAlgorithmOptions.MAX_UNCERTAINTY;
    // Stuff for simulated data creation
    static double base = 100.0;
    static double distFactor = 0.01;
    static double valueFactor = 10.0;
    static double sigmaIncreaseRate = 0.00;
    static double valueDecreaseRate = 1.00;
    static double addRate = 0.01;
    static ArrayList<Double> xs = new ArrayList<Double>();
    static ArrayList<Double> ys = new ArrayList<Double>();
    static ArrayList<Double> vs = new ArrayList<Double>();
    static ArrayList<Double> sigmas = new ArrayList<Double>();
    static boolean simpleData = false;
    static boolean hysteresis = true;

    static public double computeGTValue(double lat, double lon) {
        double v = base;
        synchronized (xs) {
            for (int i = 0; i < xs.size(); i++) {

                double dx = xs.get(i) - lon;
                double dy = ys.get(i) - lat;
                double distSq = dx * dx + dy * dy;

                double dv = vs.get(i) * (1.0 / Math.sqrt(2.0 * Math.PI * sigmas.get(i) * sigmas.get(i))) * Math.pow(Math.E, -(distSq / (2.0 * sigmas.get(i) * sigmas.get(i))));
                // if (i == 0) System.out.println("Delta at dist " + Math.sqrt(distSq) + " for " + sigmas.get(i) + " is " + dv);
                v += dv;
            }
        }
        return v;
    }

    // End stuff for simulated data creation
    public BoatProxy(final String name, Color color, final int boatNo, InetSocketAddress addr) {

        System.out.println("Boat proxy created");

        // this.masterURI = masterURI;
        this.name = name;
        this.color = color;

        //Initialize the boat by initalizing a proxy server for it
        // Connect to boat
        _boatNo = boatNo;

        if (addr == null) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$ addr falied");
        }

        _server = new UdpVehicleServer(addr);

        _stateListener = new PoseListener() {
            public void receivedPose(UtmPose upwcs) {

                // Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Boat pose update", this);

                _pose = upwcs.clone();

                if (home == null && USE_SOFTWARE_FAIL_SAFE) {
                    home = upwcs.clone();
                }

                // System.out.println("Pose: [" + _pose.pose.position.x + ", " + _pose.pose.position.y + "], zone = " + _pose.utm.zone);

                try {

                    int longZone = _pose.origin.zone;

                    // Convert hemisphere to arbitrary worldwind codes
                    // Notice that there is a "typo" in South that exists in the WorldWind code
                    // String wwHemi = (_pose.origin.isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwdind.avkey.South";
                    String wwHemi = (_pose.origin.isNorth) ? AVKey.NORTH : AVKey.SOUTH;

                    // Fill in UTM data structure
                    // System.out.println("Converting from " + longZone + " " + wwHemi + " " + _pose.pose.getX() + " " + _pose.pose.getY());
                    UTMCoord boatPos = UTMCoord.fromUTM(longZone, wwHemi, _pose.pose.getX(), _pose.pose.getY());                    

                    LatLon latlon = new LatLon(boatPos.getLatitude(), boatPos.getLongitude());

                    // System.out.println("boatPos " + boatPos.getLatitude() + " " +  boatPos.getLongitude() + " latlon " + latlon.latitude.degrees + " " + latlon.longitude.degrees + " " + latlon);

                    Position p = new Position(latlon, 0.0);

                    // Update state variables
                    currLoc = p;
                    currUtm = boatPos;

                    for (BoatProxyListener boatProxyListener : listeners) {
                        boatProxyListener.poseUpdated();
                    }

                } catch (Exception e) {
                    System.err.println("BoatProxy: Invalid pose received: " + e + " Pose: [" + _pose.pose.getX() + ", " + _pose.pose.getY() + "], zone = " + _pose.origin.zone);
                    // e.printStackTrace();
                }

            }
        };

        System.out.println("New boat created, boat # " + _boatNo);

        //add Listeners
        _server.addPoseListener(_stateListener, null);

        _server.addWaypointListener(new WaypointListener() {
            public void waypointUpdate(WaypointState ws) {

                if (ws.equals(WaypointState.DONE)) {
                    // Handle the go slow
                    if (goSlowExecuting) {
                        timeLastGoSlowDone = System.currentTimeMillis();
                        goSlowRun();
                    }
                    // This is intentionally rechecking, because goSlowRun() will change the boolean
                    // if it is done
                    if (!goSlowExecuting) {
                        // Notify listeners
                        for (BoatProxyListener boatProxyListener : listeners) {
                            boatProxyListener.waypointsComplete();
                        }
                    }
                }

            }
        }, null);

        _server.addImageListener(new ImageListener() {
            public void receivedImage(byte[] ci) {
                // Take a picture, and put the resulting image into the panel
                try {
                    BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(ci));
                    System.out.println("Got image ... ");

                    if (image != null) {
                        // Flip the image vertically
                        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
                        tx.translate(0, -image.getHeight(null));
                        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                        image = op.filter(image, null);

                        if (image == null) {
                            System.err.println("Failed to decode image.");
                        }

                        // ImagePanel.addImage(image, _pose);

                        latestImg = image;
                    } else {
                        System.out.println("Image was null in receivedImage");
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to decode image: " + ex);
                }

            }
        }, null);

        // startCamera();

        // Cheating dummy data, another version of this is in SimpleBoatSimulator, 
        // effectively overridden by overridding addSensorListener in FastSimpleBoatSimulator
        // because no access to that code from here.
        // @todo Only should be on for simulation

        // ABHINAV COMMENT OUT THIS THREAD BEFORE RUNNING ON THE REAL BOATS!!
        if (addr.getHostName().equalsIgnoreCase("localhost")) {
            (new Thread() {
                Random rand = new Random();

                public void run() {

                    System.out.println("\n\n\nGENERATING FAKE SENSOR DATA for simulated boat\n\n\n");

                    while (true) {

                        double[] prev = null;

                        if (currLoc != null) {
                            SensorData sd = new SensorData();

                            /*
                            // @todo Observation
                            if (rand.nextBoolean()) {
                                sd.type = SensorType.TE;
                            } else {
                                sd.type = SensorType.UNKNOWN;
                            }
                            */
                            
                            sd.type = SensorType.UNKNOWN;
                            
                            sd.data = new double[2];

                            if (simpleData) {
                                for (int i = 0; i < sd.data.length; i++) {
                                    if (prev == null || !hysteresis) {
                                        sd.data[i] = Math.abs(currLoc.longitude.degrees); //  + rand.nextDouble();
                                    } else {
                                        sd.data[i] = (Math.abs(currLoc.longitude.degrees) + prev[i]) / 2.0;
                                    }
                                }
                            } else {
                                double v = computeGTValue(currLoc.latitude.degrees, currLoc.longitude.degrees);
                                // System.out.println("Created data = " + v);
                                for (int i = 0; i < sd.data.length; i++) {
                                    sd.data[i] = v;
                                }

                                synchronized (xs) {
                                    // Possibly add another
                                    if ((rand.nextDouble() < addRate && xs.size() < 20) || (xs.size() == 0)) {
                                        System.out.println(">>>>>>>>>>>>>> Creating");
                                        double lon = currLoc.longitude.degrees + (distFactor * (rand.nextDouble() - 0.5));
                                        double lat = currLoc.latitude.degrees + (distFactor * (rand.nextDouble() - 0.5));
                                        double value = rand.nextDouble() * valueFactor;
                                        if (rand.nextBoolean()) {
                                            value = -value;
                                        }

                                        xs.add(lon);
                                        ys.add(lat);
                                        vs.add(value);
                                        sigmas.add(0.01);
                                    }

                                    // Decay 
                                    for (int i = 0; i < xs.size(); i++) {
                                        sigmas.set(i, sigmas.get(i) + sigmaIncreaseRate);
                                        vs.set(i, vs.get(i) * valueDecreaseRate);
                                        if (Math.abs(vs.get(i)) <= 0.001) {
                                            System.out.println("xxxxxxxxxxxxxxxxxxxxxxx Removing");
                                            xs.remove(i);
                                            ys.remove(i);
                                            vs.remove(i);
                                            sigmas.remove(i);
                                            i--;
                                        }
                                    }
                                }
                            }

                            if (_sensorListener != null) {
                                // System.out.println("SENDING Data");
                                _sensorListener.receivedSensor(sd);
                            } else {
                                // System.out.println("NO SENSOR LISTENER");
                            }
                            prev = sd.data;
                        }

                        try {
                            sleep(500L);
                        } catch (InterruptedException e) {
                        }

                    }
                }
            }).start();
        }
    }

    public int getBoatNo() {
        return _boatNo;
    }

    public boolean isIsShutdown() {
        return _isShutdown;
    }

    public UtmPose getPose() {
        return _pose;
    }

    public Queue<UtmPose> getWaypoints() {
        return _waypoints;
    }

    public Iterable<Position> getWaypointsAsPositions() {
        return _waypointsPos;
    }

    public Position getCurrLoc() {
        return currLoc;
    }

    public AtomicBoolean getIsConnected() {
        return isConnected;
    }

    public boolean isGoSlow() {
        return goSlow;
    }

    public void setGoSlow(boolean goSlow) {
        this.goSlow = goSlow;
    }

    public void addSensorListener(int channel, SensorListener l) {
        _server.addSensorListener(channel, l, null);

        // @todo This only allows one sensor, generalize (but I think this is only for the fake data ...)
        _sensorListener = l;

        // System.out.println("Setting SENSOR LISTENER TO: " + l);
    }

    public void addPoseListener(PoseListener l) {
        _server.addPoseListener(l, null);
    }

    public void addWaypointListener(WaypointListener l) {
        _server.addWaypointListener(l, null);
    }

    public void addListener(BoatProxyListener l) {
        listeners.add(l);
    }

    public void removeListener(BoatProxyListener l) {
        listeners.remove(l);
    }

    public void startCamera() {

        (new Thread() {
            public void run() {

                try {
                    System.out.println("SLEEPING BEFORE CAMERA START");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                System.out.println("DONE SLEEPING BEFORE CAMERA START");

                _server.startCamera(0, 30.0, 640, 480, null);

                System.out.println("Image listener started");
            }
        }).start();
    }

    public void addImageListener(ImageListener l) {
        _server.addImageListener(l, null);
        startCamera();
    }

    public void remove() {
        stopBoat();
    }

    public void stopBoat() {

        _waypoints.clear();
        _server.stopWaypoints(null);
        _server.stopCamera(null);

        state = StateEnum.IDLE;
    }

    @Override
    public void run() {
        // startCamera();
    }

    public void setExternalVelocity(Twist t) {
        _server.setVelocity(t, new FunctionObserver<Void>() {
            public void completed(Void v) {
                System.out.println("Set velocity succeeded");
            }

            public void failed(FunctionError fe) {
                System.out.println("Set velocity failed");
            }
        });
    }

    public void setWaypoints(Polyline pLine) {
        currentPath = pLine;
        pLine.setColor(color);
        setWaypoints(pLine.getPositions());
        state = StateEnum.PATH;
    }

    public StateEnum getMode() {
        return state;
    }

    public void setWaypoints(Iterable<Position> ps) {

        if (goSlow && ps.iterator().hasNext()) {
            System.out.println("GO SLOW MODE");
            setWaypointsGoSlow(ps);
            return;
        }

        _waypoints.clear();

        // Stored to help out the OperatorConsole (i.e., save a couple of translations)
        _waypointsPos = ps;

        for (Position position : ps) {
            UTMCoord utm = UTMCoord.fromLatLon(position.latitude, position.longitude);
            UtmPose pose = new UtmPose(new Pose3D(utm.getEasting(), utm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(utm.getZone(), utm.getHemisphere().contains("North")));
            _waypoints.add(pose);
        }

        // setWaypoint(_waypoints.poll());

        _server.setAutonomous(true, null);
        _server.startWaypoints(_waypoints.toArray(new UtmPose[_waypoints.size()]), "POINT_AND_SHOOT", new FunctionObserver() {
            public void completed(Object v) {

                System.out.println("Completed called");
            }

            public void failed(FunctionError fe) {
                // @todo Do something when start waypoints fails
                System.out.println("START WAYPOINTS FAILED");
            }
        });

    }
    Iterator<Position> goSlowWPs = null;
    UtmPose lastSentGoSlowPose = null;
    long timeLastGoSlowSent = 0L;
    long timeLastGoSlowDone = 0L;
    UtmPose goSlowCurrentTarget = null;
    
    // TO CHANGE
    long goSlowRestTime = 20000L;
    long goSlowToWaypointTime = 20000L;
    double maxWPDist = 30.0;
    // END TO CHANGE
    

    private void setWaypointsGoSlow(Iterable<Position> ps) {

        // Stored to help out the OperatorConsole (i.e., save a couple of translations)
        _waypointsPos = ps;

        goSlowWPs = ps.iterator();
        goSlowExecuting = true;
        goSlowRun();
    }

    private void goSlowRun() {

        if (System.currentTimeMillis() - timeLastGoSlowSent > goSlowRestTime
                && System.currentTimeMillis() - timeLastGoSlowDone > goSlowRestTime) {

            _waypoints.clear();

            try {

                // Get a new target point, if we don't have one
                if (goSlowCurrentTarget == null) {
                    Position position = goSlowWPs.next();
                    UTMCoord utm = UTMCoord.fromLatLon(position.latitude, position.longitude);
                    goSlowCurrentTarget = new UtmPose(new Pose3D(utm.getEasting(), utm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(utm.getZone(), utm.getHemisphere().contains("North")));
                }

                // If we are too far from that target point, pick some intermediate point
                double dist = _pose.pose.getEuclideanDistance(goSlowCurrentTarget.pose);
                System.out.println("DISTANCE IS " + dist);
                UtmPose realTarget = null;

                if (dist > maxWPDist) {
                    System.out.println("Creating an intermediate point in go slow");
                    double x = _pose.pose.getX() + (maxWPDist / dist) * (goSlowCurrentTarget.pose.getX() - _pose.pose.getX());
                    double y = _pose.pose.getY() + (maxWPDist / dist) * (goSlowCurrentTarget.pose.getY() - _pose.pose.getY());
                    realTarget = new UtmPose(new Pose3D(x, y, _pose.pose.getZ(), _pose.pose.getRotation()), new Utm(currUtm.getZone(), currUtm.getHemisphere().contains("North")));
                    _waypoints.add(realTarget);
                } else {
                    // We are done with this point
                    _waypoints.add(goSlowCurrentTarget);
                    goSlowCurrentTarget = null;
                }

                timeLastGoSlowSent = System.currentTimeMillis();
                
                // This thread monitors whether it took too long to get to the waypoint
                (new Thread() {
                    public void run() {
                        try {
                            sleep(goSlowToWaypointTime);                            
                        } catch (InterruptedException e) {}
                        if (System.currentTimeMillis() - timeLastGoSlowSent > goSlowToWaypointTime) {
                            System.out.println("Failed to reach waypoint in time, " + goSlowRestTime + " resting");
                            
                            _server.stopWaypoints(new FunctionObserver<Void>() {

                                public void completed(Void v) {
                                    System.out.println("Resting waypoints due to too long");
                                }

                                public void failed(FunctionError fe) {
                                    System.out.println("Failed to rest: " + fe);
                                }
                            });
                            
                            goSlowRun();
                        }
                    }
                }).start();

                _server.setAutonomous(true, null);
                _server.startWaypoints(_waypoints.toArray(new UtmPose[_waypoints.size()]), "POINT_AND_SHOOT", new FunctionObserver() {
                    public void completed(Object v) {
                        System.out.println("Successfully sent a waypoint in Go Slow: " + _waypoints.peek());
                    }

                    public void failed(FunctionError fe) {
                        // @todo Do something when start waypoints fails
                        System.out.println("START WAYPOINTS FAILED");
                    }
                });

            } catch (NoSuchElementException e) {
                System.out.println("Go slow is done! " + e);
                goSlowExecuting = false;
            }

        } else {

            System.out.println("Too soon to send next waypoint");

            (new Thread() {
                public void run() {
                    try {
                        sleep(goSlowRestTime / 2);
                        System.out.println("Thread awoke");
                    } catch (InterruptedException e) {
                    }
                    goSlowRun();
                }
            }).start();
        }

    }

    public void setWaypoint(Position p) {

        ArrayList<Position> ps = new ArrayList<Position>();
        ps.add(p);
        setWaypoints(ps);
    }

    /**
     * @deprecated @param wputm
     */
    public void setWaypoint(UtmPose wputm) {

        currentWaypoint = wputm;

        // ArrayList<UtmPose> 

        // @todo Register a waypoint listener to get the same status updates (and know at the end of the waypoints)
        _server.setAutonomous(true, null);
        _server.startWaypoints(new UtmPose[]{wputm}, "POINT_AND_SHOOT", new FunctionObserver() {
            public void completed(Object v) {
                System.out.println("Waypoint call completed");
            }

            public void failed(FunctionError fe) {
                // @todo Do something when start waypoints fails
                System.out.println("START WAYPOINTS FAILED");
            }
        });

    }

    public void setArea(Polygon poly) {

        ArrayList<Position> ps = new ArrayList<Position>();

        for (LatLon ll : poly.getOuterBoundary()) {
            Position pos = new Position(ll, 0.0);
            ps.add(pos);
        }

        setWaypoints(ps);

        state = StateEnum.AREA;
    }

    public Position getCurrWaypoint() {
        return currWaypoint;
    }

    public void asyncGetWaypointStatus(FunctionObserver<WaypointState> fo) {
        _server.getWaypointStatus(fo);
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public Color getColor() {
        return color;
    }

    public BufferedImage getLatestImg() {
        return latestImg;
    }

    private boolean at(Position p1, Position p2) {
        UTMCoord utm1 = UTMCoord.fromLatLon(p1.latitude, p1.longitude);
        UTMCoord utm2 = UTMCoord.fromLatLon(p2.latitude, p2.longitude);

        // @todo This only really works for short distances
        double dx = utm1.getEasting() - utm2.getEasting();
        double dy = utm1.getNorthing() - utm2.getNorthing();

        double dist = Math.sqrt(dx * dx + dy * dy);

        //System.out.println("Dist to waypoint now: " + dist);

        if (utm1.getHemisphere().equalsIgnoreCase(utm2.getHemisphere()) && utm1.getZone() == utm2.getZone()) {
            return dist < atWaypointTolerance;
        } else {
            return false;
        }

    }

    public UdpVehicleServer getVehicleServer() {
        return _server;
    }

    /**
     * From: http://forum.worldwindcentral.com/showthread.php?t=20739
     *
     * @param point
     * @param positions
     * @return
     */
    public static boolean isLocationInside(LatLon point, ArrayList<? extends LatLon> positions) {
        if (point == null) {
            System.out.println("Cannot check null point");
            return false;
        }

        boolean result = false;
        LatLon p1 = positions.get(0);
        for (int i = 1; i < positions.size(); i++) {
            LatLon p2 = positions.get(i);

            // Developed for clarity
            //            double lat = point.getLatitude().degrees;
            //            double lon = point.getLongitude().degrees;
            //            double lat1 = p1.getLatitude().degrees;
            //            double lon1 = p1.getLongitude().degrees;
            //            double lat2 = p2.getLatitude().degrees;
            //            double lon2 = p2.getLongitude().degrees;
            //            if ( ((lat2 <= lat && lat < lat1) || (lat1 <= lat && lat < lat2))
            //                    && (lon < (lon1 - lon2) * (lat - lat2) / (lat1 - lat2) + lon2) )
            //                result = !result;

            if (((p2.getLatitude().degrees <= point.getLatitude().degrees
                    && point.getLatitude().degrees < p1.getLatitude().degrees)
                    || (p1.getLatitude().degrees <= point.getLatitude().degrees
                    && point.getLatitude().degrees < p2.getLatitude().degrees))
                    && (point.getLongitude().degrees < (p1.getLongitude().degrees - p2.getLongitude().degrees)
                    * (point.getLatitude().degrees - p2.getLatitude().degrees)
                    / (p1.getLatitude().degrees - p2.getLatitude().degrees) + p2.getLongitude().degrees)) {
                result = !result;
            }

            p1 = p2;
        }
        return result;
    }

    public static boolean isLocationInside(LatLon point, Iterable<? extends LatLon> positions) {
        ArrayList<LatLon> boundary = new ArrayList<LatLon>();

        for (LatLon latLon : positions) {
            boundary.add(latLon);
        }

        return isLocationInside(point, boundary);
    }

    @Override
    public String toString() {
        return name + "@" + _server.getVehicleService();
        // (masterURI == null ? "Unknown" : masterURI.toString());
    }
}
