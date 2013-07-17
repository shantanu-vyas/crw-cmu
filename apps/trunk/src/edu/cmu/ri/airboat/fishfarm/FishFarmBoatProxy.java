/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.fishfarm;

import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.WaypointListener;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import robotutils.Quaternion;

/**
 *
 * @author pscerri
 */
public class FishFarmBoatProxy {

    private final BoatProxy proxy;
    private final DataManager dm;
    private DataRepository repo = null;
    private boolean isAutonomous = false, isSwarming = false;

    public FishFarmBoatProxy(final BoatProxy proxy, final DataManager dm) {
        this.proxy = proxy;

        proxy.addImageListener(new ImageListener() {
            public void receivedImage(byte[] ci) {
                // Take a picture, and put the resulting image into the panel
                try {
                    BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(ci));
                    // System.out.println("Got image ... ");

                    if (image != null) {
                        // Flip the image vertically
                        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
                        tx.translate(0, -image.getHeight(null));
                        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                        image = op.filter(image, null);

                        if (image == null) {
                            System.err.println("Failed to decode image.");
                        }

                        ImagePanel.addImage(image, proxy.getPose());
                    } else {
                        System.out.println("Image was null in receivedImage");
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to decode image: " + ex);
                }

            }
        });

        // for (int i = 0; i < 3; i++) {


        proxy.addSensorListener(0, new SensorListener() {
            HashMap<String, Object> seen = new HashMap<String, Object>();

            public void receivedSensor(SensorData sd) {
                dm.addData(proxy, sd, proxy.getPose());
            }
        });


        proxy.addWaypointListener(new WaypointListener() {
            public void waypointUpdate(WaypointState ws) {


                if (ws.equals(WaypointState.DONE)) {

                    waypointWatchdog.lastWaypointTime = System.currentTimeMillis();

                    System.out.println("Waypoint done");

                    if (isAutonomous) {
                        actAutonomous();
                    } else if (isSwarming) {
                        swarm();
                    }
                }
            }
        });
        this.dm = dm;
    }

    public void setRepo(DataRepository repo) {
        this.repo = repo;
    }

    public BoatProxy getProxy() {
        return proxy;
    }

    public Color getColor() {
        return proxy.getColor();
    }

    public LatLon getLatLon() {
        return proxy.getCurrLoc();
    }

    public double getNorthing() {
        return UTMCoord.fromLatLon(proxy.getCurrLoc().latitude, proxy.getCurrLoc().longitude).getNorthing();
    }

    public double getEasting() {
        return UTMCoord.fromLatLon(proxy.getCurrLoc().latitude, proxy.getCurrLoc().longitude).getEasting();
    }

    public String toString() {
        return proxy.toString();
    }

    public double getHeading() {
        Quaternion q = proxy.getPose().pose.getRotation();
        return q.toYaw();
    }

    public void setWaypoint(Position p) {
        proxy.setWaypoint(p);
    }

    public void setWaypoints(Iterable<Position> p) {
        proxy.setWaypoints(p);
        // ABHINAV if the watchdog kicks in too much, increase this 20000L
        timeoutTime = 20000L * proxy.getWaypoints().size();
    }
    // Watchdog thread stuff
    WaypointWatchDog waypointWatchdog = new WaypointWatchDog();
    long timeoutTime = 60000L;

    class WaypointWatchDog extends Thread {

        Boolean running = false;
        public long lastWaypointTime = 0L;

        public void run() {
            while (running) {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                }
                if (running && (isAutonomous || isSwarming)) {

                    edu.cmu.ri.crw.FunctionObserver<WaypointState> fo = new edu.cmu.ri.crw.FunctionObserver<WaypointState>() {
                        public void completed(WaypointState v) {

                            System.out.println("Waypoint is complete!");

                            if (v == WaypointState.DONE) {
                                if (isAutonomous) {
                                    actAutonomous();
                                } else {
                                    swarm();
                                }
                            }
                        }

                        public void failed(FunctionError fe) {
                            System.out.println("WHAT TO DO WHEN WAYPOINT STATUS FAILS? (FishFarmBoatProxy)");
                        }
                    };

                    /*
                     long currTime = System.currentTimeMillis();
                     // Abhinav, you might want to play with this number which is how long between waypoints before it panics and replans
                     if (currTime - lastWaypointTime > timeoutTime) {
                     System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TIMED OUT");
                     actAutonomous();
                     } 
                     */

                } else {
                    synchronized (running) {
                        try {
                            running.wait();
                        } catch (InterruptedException ex) {
                            System.out.println("Watchdog woken");
                        }
                    }
                }
            }
        }

        public void start() {
            if (running == false && !isAlive()) {
                super.start();
            }
            running = true;
            synchronized (running) {
                running.notify();
            }
        }

        public void safeStop() {
            running = false;
        }
    };
    // END Watchdog thread stuff

    public void setAutonomous(boolean selected) {
        isAutonomous = selected;
        if (selected) {
            repo.addAutonomous(this);
            actAutonomous();
            waypointWatchdog.start();
        } else {
            // @todo this stops the camera, which is fine here, but probably not required behavior
            proxy.stopBoat();
            repo.removeAutonomous(this);
            waypointWatchdog.safeStop();
        }

    }
    FishFarmBoatProxy leader = null;

    public void setSwarmFollower(FishFarmBoatProxy leader) {
        isSwarming = true;
        this.leader = leader;
        swarm();
    }

    public static  double swarmDist = 0.001;
    public static double swarmSpeed = 1.0;
    private void swarm() {

        Position leaderPos = new Position(leader.getLatLon(), 0.0);

        if (distTo(leader.getLatLon()) > 0.001) {
            // Go to the leader
            ArrayList<Position> leaderPosPath = new ArrayList<Position>();
            leaderPosPath.add(leaderPos);
            currPlan = leaderPosPath;
            proxy.setWaypoints(leaderPosPath);
            waypointWatchdog.lastWaypointTime = System.currentTimeMillis();
        } else {
            (new Thread() {
                public void run() {
                    while (distTo(leader.getLatLon()) < swarmDist) {
                        // @todo Make this real numbers
                        proxy.setExternalVelocity(new Twist(swarmSpeed * Math.cos(leader.getHeading()), swarmSpeed * Math.sin(leader.getHeading())));
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                    swarm();
                }
            }).start();
        }

    }

    private double distTo(LatLon ll) {
        double dlat = ll.latitude.degrees - getLatLon().latitude.degrees;
        double dlon = ll.longitude.degrees - getLatLon().longitude.degrees;
        double d = Math.sqrt((dlat * dlat) + (dlon * dlon));
        System.out.println("Dist is " + d);
        return d;
    }

    public boolean isIsAutonomous() {
        return isAutonomous;
    }
    ArrayList<Position> currPlan = null;

    public ArrayList<Position> getCurrPlan() {
        return currPlan;
    }

    public void actAutonomous() {
        System.out.println("GETTING PLAN");
        ArrayList<Position> p = repo.getAutonomyPath(this);
        currPlan = p;
        proxy.setWaypoints(p);
        waypointWatchdog.lastWaypointTime = System.currentTimeMillis();
    }

    public AsyncVehicleServer getVehicleServer() {
        return proxy.getVehicleServer();
    }
}
