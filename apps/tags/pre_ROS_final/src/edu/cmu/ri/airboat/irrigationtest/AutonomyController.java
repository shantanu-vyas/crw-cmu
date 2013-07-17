/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class AutonomyController implements IrrigationTestInterface.IrrigationTestInterfaceListener {

    IrrigationTestInterface boats = new InterfaceTester();
    String algorithm = "Lawnmower";
    Hashtable<Integer, double[]> locations = new Hashtable<Integer, double[]>();
    Hashtable<Integer, double[][]> plans = new Hashtable<Integer, double[][]>();
    ArrayList<Observation> observations = new ArrayList<Observation>();
    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    LocationInfo[][] locInfo = null;
    double[] ul = null;
    double[] lr = null;
    private int xCount;
    private int yCount;
    double dx = 0;
    double dy = 0;
    Random rand = new Random();
    // boolean initialPlan = false;

    public AutonomyController(double[] ul, double[] lr, int xCount, int yCount) {
        boats.setExtent(ul, lr);
        boats.addListener(this);

        this.xCount = xCount;
        this.yCount = yCount;

        setExtent(ul, lr);

        /*
        // Tell autonomy about any obstacles
        Rectangle r = new Rectangle(25, 25, 5, 25);
        obstacles.add(new Obstacle(r));
        
        Rectangle r2 = new Rectangle(55, 55, 5, 25);
        obstacles.add(new Obstacle(r2));
         */

        boatDone(0);

    }

    public double getWidth() {
        return lr[0] - ul[0];
    }

    public double getHeight() {
        return ul[1] - lr[1];
    }

    public LocationInfo[][] getLocInfo() {
        return locInfo;
    }

    public Random getRand() {
        return rand;
    }

    public Hashtable<Integer, double[][]> getPlans() {
        return plans;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public void setObstacles(ArrayList<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public void setxCount(int xCount) {
        this.xCount = xCount;
        setExtent(ul, lr);
    }

    public void setyCount(int yCount) {
        this.yCount = yCount;
        setExtent(ul, lr);
    }

    public void setExtent(double[] ul, double[] lr) {
        boats.setExtent(ul, lr);
        this.ul = ul;
        this.lr = lr;

        // Compute box widths and heights (assumes northern hemisphere lr is > on x-axis)
        dx = (lr[0] - ul[0]) / ((double) xCount);
        dy = (ul[1] - lr[1]) / ((double) yCount);

        locInfo = new LocationInfo[xCount][yCount];

        ArrayList<Observation> cobs = (ArrayList<Observation>) observations.clone();
        observations.clear();
        for (Observation obs : cobs) {
            newObservation(obs);
        }
    }

    public void newObservation(Observation o) {
        observations.add(o);

        int bx = (int) ((o.getWaypoint()[0] - ul[0]) / dx);
        int by = (int) ((o.getWaypoint()[1] - lr[1]) / dy);

        try {
            if (locInfo[bx][by] == null) {
                locInfo[bx][by] = new LocationInfo();
            }

            // System.out.println("Added obs to " + bx + " " + by);

            locInfo[bx][by].addObs(o);

            notifyListeners();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("OUT OF EXTENT");
        }
    }

    public void newBoatPosition(int boatNo, double[] pose) {
        locations.put(boatNo, pose);
        notifyListeners();
        /*if (!initialPlan) {
        boatDone(boatNo);
        initialPlan = true;
        }*/
    }

    public void setAlgorithm(String algorithm) {

        this.algorithm = algorithm;
        // @todo Only changes algorithm for boat 0
        boatDone(0);
    }

    public void boatDone(int boatNo) {

        System.out.println("Boat planning");

        if (ul == null) {
            System.out.println("No extents, no plan");

            return;
        }

        double[][] poses = null;

        if (algorithm.equalsIgnoreCase("Lawnmower")) {

            poses = new double[(yCount * 2)][2];

            double[] curr = locations.get(boatNo);
            if (curr != null) {
                poses[0][0] = curr[0];
                poses[0][1] = curr[1];
            } else {
                // Zeros
            }

            boolean left = true, same = true;
            double y = lr[1];
            for (int i = 1; i < poses.length; i++) {
                if (!same) {
                    left = !left;
                    same = true;
                } else {
                    same = false;
                }
                if (left) {
                    poses[i][0] = ul[0];
                } else {
                    poses[i][0] = lr[0];
                }
                if (same) {
                    y += dy;
                }
                poses[i][1] = y;

                // System.out.println("y " + i + " is " + y + " of " + poses.length);
            }
        } else if (algorithm.equalsIgnoreCase("Max Uncertainty")) {

            double[] curr = locations.get(boatNo);

            // @todo Take into account the number of sampels.

            poses = new double[2][2];
            int bx = 0, by = 0;
            double best = 0.0;
            for (int i = 0; i < locInfo.length; i++) {
                for (int j = 0; j < locInfo[i].length; j++) {

                    double v = locInfo[i][j] == null ? Double.MAX_VALUE : locInfo[i][j].valueOfMoreObservations();
                    if (v > best) {
                        best = v;
                        bx = i;
                        by = j;
                    }
                }
            }

            System.out.println("Going to " + bx + " " + by + " with std. dev. " + best);

            poses[0][0] = curr[0];
            poses[0][1] = curr[1];

            poses[1][0] = ul[0] + (bx * dx);
            poses[1][1] = lr[1] + (by * dy);

        }


        poses = avoidObstacles(poses);

        boats.setWaypoints(boatNo, poses);
        plans.put(boatNo, poses);
    }

    /**
     * @todo This fails (obviously) in some cases, including when a wp is in an obstacle.
     * 
     * @param poses
     * @return 
     */
    private double[][] avoidObstacles(double[][] poses) {
        System.out.println("XXXXXXXXXXXX");
        for (int i = 1; i < poses.length; i++) {
            double[] es = poses[i];
            double[] ss = poses[i - 1];
            //System.out.print("S(" + obstacles.size() + ")");
            for (Obstacle o : obstacles) {
                boolean b = o.intersects(ss[0], ss[1], es[0], es[1]);
                if (b) {
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! Plan has collision");
                    poses = insertWPAt(poses, i);

                    boolean ok = false;
                    double deltay = 1.0;
                    double x = o.r.getCenterX(); // (ss[0] + es[0])/2.0;

                    poses[i][0] = x;

                    while (!ok) {
                        if (!o.intersects(ss[0], ss[1], x, ss[1] + deltay) && !o.intersects(x, ss[1] + deltay, es[0], es[1])) {
                            ok = true;
                            poses[i][1] = ss[1] + deltay;
                            System.out.println("Adding wayopint " + poses[i][0] + " " + poses[i][1] + " " + ss[1] + " " + es[1]);
                        } else if (!o.intersects(ss[0], ss[1], x, ss[1] - deltay) && !o.intersects(x, ss[1] - deltay, es[0], es[1])) {
                            ok = true;
                            poses[i][1] = ss[1] - deltay;
                            System.out.println("Adding wayopint " + poses[i][0] + " " + poses[i][1] + " " + ss[1] + " " + es[1]);
                        }

                        deltay++;
                        // System.out.println("y now " + deltay);
                    }

                    // Need this to recheck
                    i--;
                } else {
                    System.out.print(".");
                }
            }
        }

        return poses;
    }

    private double[][] insertWPAt(double[][] p, int i) {

        // System.out.println("insert called with " + i + " " + p[0].length);

        double[][] f = new double[p.length + 1][p[0].length];

        // System.out.println("f[0][0] = " + f[0][0] + "f[0][1] = " + f[0][1]);

        for (int j = 0; j < i; j++) {
            System.arraycopy(p[j], 0, f[j], 0, p[j].length);
        }

        for (int j = i + 1; j < f.length; j++) {
            System.arraycopy(p[j - 1], 0, f[j], 0, p[j - 1].length);
        }

        // System.out.println("f[0][0] = " + f[0][0] + "f[0][1] = " + f[0][1]);

        return f;
    }

    public double randLon() {
        return (rand.nextDouble() * (lr[0] - ul[0])) + ul[0];
    }

    public double randLat() {
        return (rand.nextDouble() * (lr[1] - ul[1])) + ul[1];
    }

    public double[] getBoatLocation(int i) {
        return locations.get(i);
    }

    public Hashtable<Integer, double[]> getLocations() {
        return locations;
    }

    private void notifyListeners() {
        for (AutonomyEventListener autonomyEventListener : listeners) {
            autonomyEventListener.changed();
        }
    }
    ArrayList<AutonomyEventListener> listeners = new ArrayList<AutonomyEventListener>();

    public void addListener(AutonomyEventListener l) {
        listeners.add(l);
    }

    public interface AutonomyEventListener {

        public void changed();
    }

    public class LocationInfo {

        double mean = 0.0;
        double tot = 0.0;
        int count = 0;
        ArrayList<Observation> obs = new ArrayList<Observation>();

        public void addObs(Observation o) {
            obs.add(o);
            tot += o.getValue();
            count++;
            mean = tot / count;
        }

        public double getMean() {
            return mean;
        }

        public double getStdDev() {

            double ss = 0.0;

            //for (Observation observation : obs) {
            ListIterator<Observation> lo = obs.listIterator();
            while (lo.hasNext()) {
                Observation observation = lo.next();
                ss += (mean - observation.getValue()) * (mean - observation.getValue());
            }

            ss /= obs.size();

            return Math.sqrt(ss);
        }

        public double valueOfMoreObservations() {
            double d = 0.0;

            double s = getStdDev();

            if (count == 0 || count == 1) {
                d = Double.MAX_VALUE;
            } else {
                d = (s * getStdDev()) * Math.pow(0.99, count);
            }

            return d;
        }
    }

    public class Obstacle {

        Rectangle r = null;

        public Obstacle(Rectangle r) {
            this.r = r;
        }

        public boolean intersects(double x1, double y1, double x2, double y2) {
            return r.intersectsLine(x1, y1, x2, y2);
        }
    }
}
