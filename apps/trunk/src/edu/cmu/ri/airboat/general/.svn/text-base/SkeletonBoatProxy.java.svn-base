/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general;

import edu.cmu.ri.airboat.general.widgets.Core;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.PoseListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.WaypointListener;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpVehicleServer;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @todo Need a flag for autonomous or under human control
 *
 * @author pscerri
 */
public class SkeletonBoatProxy extends Thread {

    private final String name;
    final AtomicBoolean isConnected = new AtomicBoolean(false);

    private Position currLoc = null;
    private UTMCoord currUtm = null;
    
    private int lngZone = -1;
    // Simulated
    private double fuelLevel = 1.0;
    private double fuelUsageRate = 1.0e-2;
    // ROS update
    private boolean _waypointsWereUpdated;
    PoseListener _stateListener;
    SensorListener _sensorListener;
    WaypointListener _waypointListener;
    int _boatNo;
    UtmPose _pose;
    volatile boolean _isShutdown = false;
    // Latest image returned from this boat
    private BufferedImage latestImg = null;

    
    public void sample() {
        // System.out.println("Calling sample on server");
        _server.captureImage(100, 100, null);
    }

    // Set this to false to turn off the false safe
    final boolean USE_SOFTWARE_FAIL_SAFE = true;
    UtmPose home = null;
    final Queue<UtmPose> _waypoints = new LinkedList<UtmPose>();
    private UtmPose currentWaypoint = null;
    UdpVehicleServer _server;

    private URI masterURI = null;

    SkeletonBoatProxy self = null;
    
    public SkeletonBoatProxy(final String name, final int boatNo, InetSocketAddress addr) {

        self = this;
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "SkeletonBoatProxy created", this);

        this.masterURI = masterURI;
        this.name = name;

        //Initialize the boat by initalizing a proxy server for it
        // Connect to boat
        _boatNo = boatNo;

        if (addr == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Address failed!", this);
        }

        _server = new UdpVehicleServer(addr);

        _stateListener = new PoseListener() {

            public void receivedPose(UtmPose upwcs) {
                
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Boat pose update", this);
                
                _pose = upwcs.clone();

                if (home == null && USE_SOFTWARE_FAIL_SAFE) {
                    home = upwcs.clone();
                }

                // System.out.println("Pose: [" + _pose.pose.position.x + ", " + _pose.pose.position.y + "], zone = " + _pose.utm.zone);

                try {

                    int longZone = _pose.origin.zone;

                    // Convert hemisphere to arbitrary worldwind codes
                    String wwHemi = (_pose.origin.isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South";

                    // Fill in UTM data structure
                    // System.out.println("Converting from " + longZone + " " + wwHemi + " " + _pose.pose.position.x + " " + _pose.pose.position.y);
                    UTMCoord boatPos = UTMCoord.fromUTM(longZone, wwHemi, _pose.pose.getX(), _pose.pose.getY());

                    LatLon latlon = new LatLon(boatPos.getLatitude(), boatPos.getLongitude());

                    // System.out.println("boatPos " + boatPos.getLatitude() + " " +  boatPos.getLongitude() + " latlon " + latlon.latitude.degrees + " " + latlon.longitude.degrees + " " + latlon);
                    
                    Position p = new Position(latlon, 0.0);

                    // Update state variables
                    currLoc = p;

                    (new Core()).proxyUpdated(self);
                    
                } catch (Exception e) {
                    System.err.println("BoatSimpleProxy: Invalid pose received: " + e + " Pose: [" + _pose.pose.getX() + ", " + _pose.pose.getY() + "], zone = " + _pose.origin.zone);
                }

            }
        };

        System.out.println("New boat created, boat # " + _boatNo);

        //add Listeners
        _server.addPoseListener(_stateListener, null);        

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

    public Position getCurrLoc() {
        return currLoc;
    }

    public AtomicBoolean getIsConnected() {
        return isConnected;
    }

    public void addSensorListener(int channel, SensorListener l) {
        _server.addSensorListener(channel, l, null);

        // @todo This only allows one sensor, generalize
        _sensorListener = l;
    }

    public void addPoseListener(PoseListener l) {
        _server.addPoseListener(l, null);
    }

    public void addWaypointListener(WaypointListener l) {
        _server.addWaypointListener(l, null);
    }

    private void startCamera() {

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
    }

    @Override
    public void run() {
        // startCamera();
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public BufferedImage getLatestImg() {
        return latestImg;
    }

    public UdpVehicleServer getVehicleServer() {
        return _server;
    }
   
    @Override
    public String toString() {
        return name + "@" + (masterURI == null ? "Unknown" : masterURI.toString());
    }
}
