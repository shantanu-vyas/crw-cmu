/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class InterfaceTester implements IrrigationTestInterface {

    public static double speed = 1.0;
    public static long sleepTime = 1000;
    private Random rand = new Random();
    Hashtable<Integer, SimBoat> boats = new Hashtable<Integer, SimBoat>();
    ArrayList<IrrigationTestInterfaceListener> listeners = new ArrayList<IrrigationTestInterfaceListener>();
    ArrayList<Gaussian2D> model = new ArrayList<Gaussian2D>();
    double[] ul = null;
    double[] lr = null;

    public void setWaypoints(int boatNo, double[][] poses) {
        if (ul == null || lr == null) {
            System.out.println("SET EXTENT FIRST!");
            return;
        }

        SimBoat boat = boats.get(boatNo);
        if (boat == null) {
            double lon = randLon();
            double lat = randLat();
            boat = new SimBoat(boatNo, lon, lat);
            boats.put(boatNo, boat);

            System.out.println("New boat created, boat # " + boatNo);
        }

        boat.setWaypoints(poses);
    }

    public void addListener(IrrigationTestInterfaceListener l) {
        listeners.add(l);
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
        this.ul = ul;
        this.lr = lr;

        createModel();
    }

    public double randLon() {
        return (rand.nextDouble() * (lr[0] - ul[0])) + ul[0];
    }

    public double randLat() {
        return (rand.nextDouble() * (lr[1] - ul[1])) + ul[1];
    }
    boolean created = false;

    private void createModel() {

        if (!created) {        
            for (int i = 0; i < 1; i++) {
                Gaussian2D g = new Gaussian2D(randLon(), randLat(), 100.0 * (rand.nextDouble() - 0.5), -(Math.abs(2.0*rand.nextGaussian())));
                System.out.println("Model has: " + g);
                model.add(g);
            }
            created = true;
        }
    }

    private class SimBoat extends Thread {

        private final int no;
        private double lon;
        private double lat;
        final double baseline = 100.0;
        double[][] poses = null;
        int waypointIndex = 0;
        final VehicleServer servers[];

        public SimBoat(int no, double lon, double lat) {
            this.no = no;
            this.lon = lon;
            this.lat = lat;

            start();
        }

        public void setWaypoints(double[][] poses) {
            System.out.println("New waypoints: " + poses.length);
            this.poses = poses;
            waypointIndex = 0;
            doneReported = false;
        }
        // No need to report that the boat starts with nothing
        boolean doneReported = true;

        public void run() {

            while (true) {
                if (poses != null && waypointIndex < poses.length) {
                    double pose[] = poses[waypointIndex];
                    double dx = pose[0] - lon;
                    double dy = pose[1] - lat;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > speed) {
                        double d = dist / speed;
                        dx /= d;
                        dy /= d;
                    } else {
                        waypointIndex++;
                    }

                    lat += dy;
                    lon += dx;

                    double[] p = new double[2];
                    p[0] = lon;
                    p[1] = lat;

                    reportLoc(no, p);

                    double v = baseline;
                    for (Gaussian2D g : model) {
                        v += g.getValueAt(p[0], p[1]);
                    }

                    // System.out.println("Obs value = " + v);
                    Observation o = new Observation("Blah", v, p, 0, true);
                    reportObs(o);

                } else {
                    System.out.println("Boat " + no + " idle, " + poses + " " + waypointIndex + " " + doneReported);
                    if (!doneReported) {
                        doneReported = true;
                        reportDone(no);
                    }
                }

                try {
                    sleep(sleepTime);
                } catch (Exception e) {
                }

            }

        }
    }

    public class Gaussian2D {

        double x, y, v, decay;

        public Gaussian2D(double x, double y, double v, double decay) {
            this.x = x;
            this.y = y;
            this.v = v;
            this.decay = decay;
        }

        public double getValueAt(double x2, double y2) {
            double dx = x2 - x;
            double dy = y2 - y;

            double dist = Math.sqrt(dx * dx + dy * dy);

            double ret = (0.1 * rand.nextGaussian()) + 1.0;

            ret *= v * Math.exp((dist / Math.floor(dist)) * decay);

            // System.out.println("Returning " + ret + " for v = " + v + ", decay = " + decay + " and dist = " + dist);

            return ret;

        }

        public String toString() {
            return "Gaussian @ " + x + "," + y + " v= " + v + " decay = " + decay;
        }
    }
}
