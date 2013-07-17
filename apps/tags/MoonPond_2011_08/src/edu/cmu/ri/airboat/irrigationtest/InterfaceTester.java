/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleImageListener;
import edu.cmu.ri.crw.VehicleSensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VehicleStateListener;
import edu.cmu.ri.crw.VehicleVelocityListener;
import edu.cmu.ri.crw.WaypointObserver;
import edu.cmu.ri.crw.ros.RosVehicleProxy;
import edu.cmu.ri.crw.ros.RosVehicleServer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;

/**
 * A wrapper to implement Irrigation Test Interface with Airboat Server
 *
 * @author pscerri
 * @author kss
 */
public class InterfaceTester implements IrrigationTestInterface {

    public static double speed = 1.0;
    public static long sleepTime = 1000;
    private Random rand = new Random();
    Hashtable<Integer, Boat> boats = new Hashtable<Integer, Boat>();
    ArrayList<IrrigationTestInterfaceListener> listeners = new ArrayList<IrrigationTestInterfaceListener>();
    double[] ul = null;
    double[] lr = null;
    protected final List<UtmPose> boatPoses = new ArrayList<UtmPose>();
    protected final List<Observation> observers = new ArrayList<Observation>();
    private final String MASTER_URI = "http://localhost:11311";

    public void setWaypoints(final int boatNo, double[][] poses) {

        System.out.println("Calling set waypoints");
        if (ul == null || lr == null) {
            System.out.println("SET EXTENT FIRST!");
            return;
        }

        Boat boat = boats.get(boatNo);
        if (boat == null) {               //i.e. if no Vehicle Server exists
            try {
                //i.e. if no Vehicle Server exists, intialize one, change the nodeName to avoid restarting each time
                boat = new Boat(boatNo, MASTER_URI, "vehicle" + (int) (new Random().nextInt(1000000)));


                //Add this to the list
                boats.put(boatNo, boat);
            } catch (URISyntaxException ex) {
                Logger.getLogger(InterfaceTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else //Cancel all current waypoints
        {
            //TODO Investigate why this had to be commented out.

            // boat.stopAllWaypoints();
        }

        //Send the boat the waypoints
        boat.setWaypoints(poses);

    }

    public void addListener(IrrigationTestInterfaceListener l) {
        listeners.add(l);
    }

    public void shutdown() {
        for (Boat boat : boats.values()) {
            boat.shutdown();
        }
    }

    private void reportLoc(int no, double[] pose) {
        for (IrrigationTestInterfaceListener l : listeners) {
            l.newBoatPosition(no, pose);
        }
    }

    private void reportObs(Observation o) {
        for (IrrigationTestInterfaceListener l : listeners) {
            l.newObservation(o);

        }
    }

    private void reportDone(int no) {
        for (IrrigationTestInterfaceListener l : listeners) {
            l.boatDone(no);

        }
    }

    public void setExtent(double[] ul, double[] lr) {
        this.ul = ul;//upper left corner of rectangle
        this.lr = lr;//lower right corner of rectangle

    }

    public double randLon() {
        return (rand.nextDouble() * (lr[0] - ul[0])) + ul[0];
    }

    public double randLat() {
        return (rand.nextDouble() * (lr[1] - ul[1])) + ul[1];
    }

    public static UtmPose DoubleToUtmPose(double[] pose) {
        /* if (pose.length != 7) {
        return null;

        }*/
        UtmPose _pose = new UtmPose();
        _pose.pose.position.x = pose[0];
        _pose.pose.position.y = pose[1];

        if (pose.length > 2) {
            _pose.pose.position.z = pose[2];

            _pose.pose.orientation.w = pose[3];
            _pose.pose.orientation.x = pose[4];
            _pose.pose.orientation.y = pose[5];
            _pose.pose.orientation.z = pose[6];

        } else {

            // Paul added this clause because GUI only uses x,y (at the moment)
            _pose.pose.position.z = 0.0;

            _pose.pose.orientation.w = 0.0;
            _pose.pose.orientation.x = 0.0;
            _pose.pose.orientation.y = 0.0;
            _pose.pose.orientation.z = 0.0;
        }

        return _pose;

    }

    public static double[] UtmPoseToDouble(Pose pose) {

        double[] _pose = new double[2];
        _pose[0] = pose.position.x;
        _pose[1] = pose.position.y;
        //Since Paul's code only deals with just two members
        /*
        _pose[2] = pose.position.z;

        _pose[3] = pose.orientation.w;
        _pose[4] = pose.orientation.x;
        _pose[5] = pose.orientation.y;
        _pose[6] = pose.orientation.z;*/
        return _pose;
    }

    protected class Boat {

        private boolean _waypointsWereUpdated;
        RosVehicleProxy _server;
        VehicleStateListener _stateListener;
        VehicleSensorListener _sensorListener;
        int _boatNo;
        UtmPose _pose;
        final Queue<UtmPose> _waypoints = new LinkedList<UtmPose>();
        volatile boolean _isShutdown = false;

        Boat(final int boatNo, String masterURI, String nodeName) throws URISyntaxException {
            //Initialize the boat by initalizing a proxy server for it
            // Connect to boat
            _boatNo = boatNo;
            _server = new RosVehicleProxy(new URI(masterURI), nodeName);
            _stateListener = new VehicleStateListener() {

                public void receivedState(UtmPoseWithCovarianceStamped upwcs) {
                    _pose = new UtmPose();
                    _pose.pose = upwcs.pose.pose.pose.clone();
                    _pose.utm = upwcs.utm.clone();

                    System.out.println("Pose: [" + _pose.pose.position.x+", "+_pose.pose.position.y);

                    reportLoc(_boatNo, UtmPoseToDouble(_pose.pose));
                }
            };

            _sensorListener = new VehicleSensorListener() {

                public void receivedSensor(SensorData sd) {
                    //TODO Perform Sensor value assignment correctly

                    //Since the sensor Update is called just after state update
                    //There shouldn't be too much error with regards to the
                    //position of the sampling point

                    try {
                        Observation o = new Observation(
                                "Sensor" + sd.type,
                                sd.data[0],
                                UtmPoseToDouble(_pose.pose),
                                _pose.utm.zone,
                                _pose.utm.isNorth);

                        System.out.println("Data:" + sd.data[0]);
                        reportObs(o);

                    } catch (NullPointerException e) {
                        System.out.println("Problem in receivedSensor, null pointer " + sd + " " + _pose);
                    }
                }
            };

            System.out.println("New boat created, boat # " + _boatNo);

            //add Listeners
            _server.addStateListener(_stateListener);
            _server.addSensorListener(0, _sensorListener);


            // Start update thread
            new Thread(new Runnable() {

                public void run() {
                    UtmPose waypoint = null;

                    while (!_isShutdown) {
                        _waypointsWereUpdated = false;
                        waypoint = null;
                        // Get the next waypoint that needs doing
                        synchronized (_waypoints) {
                            if (_waypoints.isEmpty()) {
                                Thread.yield();
                            } else {
                                if (_server.getWaypointStatus() != WaypointState.GOING) {
                                    waypoint = _waypoints.poll();
                                    System.out.println("Polling next waypoint");
                                } else {
                                    System.out.println("Not getting new waypoint, old one not complete: " + _server.getWaypointStatus());
                                }
                            }
                        }

                        final AtomicBoolean waypointDone = new AtomicBoolean(false);
                        // Paul added this if statement
                        if (waypoint != null) {
                            // Tell the vehicle to start doing it
                            if (!_server.isAutonomous()) {
                                _server.setAutonomous(true);
                            }
                            System.out.println("Sending new waypoint to boat: " + waypoint.pose.position.x + " " + waypoint.pose.position.y);
                            _server.startWaypoint(waypoint, new WaypointObserver() {

                                public void waypointUpdate(WaypointState state) {
                                    if (state == WaypointState.DONE) {
                                        
                                        waypointDone.set(true);
                                        // Paul added this if statement 
                                        // If no more waypoints, report done to autonomy
                                        if (_waypoints.isEmpty()) {
                                            reportDone(boatNo);
                                        }
                                    }
                                    //TODO: Investigate the reason why WaypointState was being reported as cancelled
                                    /* else if (state == WaypointState.CANCELLED) {
                                        System.out.println("CANCELLED!!!");
                                        _server.setAutonomous(false);
                                        waypointDone.set(true);
                                        reportDone(boatNo);
                                    }*/
                                }
                            });
                            System.out.println("Boat given waypoint, status: " + _server.getWaypointStatus());
                        }

                        // Wait until the waypoint is done or someone changes the waypoints
                        do {
                            Thread.yield(); // TODO: this is inefficient, but should work alright.
                            // Paul added this sleep (there are ways of rewriting this loop to avoid this, but this is OK for now.)
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                            }

                            System.out.println("waypoint " + (waypoint == null ? "NULL" : "x=" + waypoint.pose.position.x + " y=" + waypoint.pose.position.y) + " waypointDone? " + waypointDone.get() + ", update? " + _waypointsWereUpdated);
                            //System.out.println("Server says current waypoint is: " + _server.getWaypoint().pose.position.x);
                        } while (!waypointDone.get() && !_waypointsWereUpdated);
                    }
                    System.out.println ("Ending while loop due to shutdown.");
                }

            }).start();


        }

        public void setWaypoints(double[][] waypoints) {
            synchronized (_waypoints) {
                _waypoints.clear();
                //System.out.println("Setting waypoints on boat");
                for (double[] waypoint : waypoints) {
                    UtmPose pose = DoubleToUtmPose(waypoint);
                    _waypoints.add(pose);
                }
                _waypointsWereUpdated = true;
            }
        }

        public void shutdown() {
            _isShutdown = true;
            stopAllWaypoints();

            _server.removeSensorListener(0, _sensorListener);
            _server.removeStateListener(_stateListener);


            _server.shutdown();
        }

        public void stopAllWaypoints() {
            _waypoints.clear();
            _server.stopWaypoint();
            
        }
    }


}
