/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.floodtest.*;
import edu.cmu.ri.airboat.general.LocationInfo;
import edu.cmu.ri.airboat.general.Observation;
import edu.cmu.ri.airboat.generalAlmost.BoatSimpleProxy;
import edu.cmu.ri.airboat.generalAlmost.ProxyManager;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Polygon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class DataDisplay {

    // AutonomyController autonomy = null;
    private Dimension minSize = new Dimension(500, 500);
    private boolean showMean = true;
    // Hack version for keeping mean value for drawing
    double mean = 0.0;
    double maxExtent = 1.0;
    DecimalFormat df = new DecimalFormat("#.###");
    ArrayList<double[]> prev = new ArrayList<double[]>();
    double width = 1000.0;
    double height = 1000.0;
    ArrayList<Observation> observations = new ArrayList<Observation>();
    ArrayList<LocationInfo[][]> locInfo = null;
    double[] ul = null;
    double[] lr = null;
    private int xCount = 10;
    private int yCount = 10;
    double dx = 10.0;
    double dy = 10.0;
    boolean loggingOn = false;
    private final Polygon pgon;

    /**
     * Some of these numbers are confusing, ul and lr only work in one
     * hemisphere, translation should fix it.
     *
     * @param ul
     * @param lr
     */
    public DataDisplay(double[] ul, double[] lr, Polygon pgon) {
        this.ul = ul;
        this.lr = lr;

        // Initialize model
        locInfo = new ArrayList<LocationInfo[][]>();

        if (ul[0] > lr[0]) {
            double d = ul[0];
            ul[0] = lr[0];
            lr[0] = d;
        }
        dx = (lr[0] - ul[0]) / xCount;

        if (ul[1] < lr[1]) {
            double d = ul[1];
            ul[1] = lr[1];
            lr[1] = d;
        }
        dy = (ul[1] - lr[1]) / yCount;

        if (loggingOn) {
            (new Thread() {
                public void run() {
                    while (true) {
                        try {
                            // Change this to change how often data is logged
                            sleep(10000);
                        } catch (InterruptedException e) {
                        }
                        int index = 0;
                        // Play with this to change format
                        System.out.println("Log data @ " + System.currentTimeMillis());
                        for (LocationInfo[][] model : locInfo) {
                            System.out.print("Index: " + index++ + " ");
                            for (int i = 0; i < model.length; i++) {
                                System.out.print("[");
                                for (int j = 0; j < model[0].length; j++) {
                                    if (model[i][j] != null) {
                                        System.out.print(" " + model[i][j].getMean());
                                    } else {
                                        System.out.print(" NaN");
                                    }
                                }
                                System.out.print("] ");
                            }
                            System.out.println("");
                        }
                    }
                }
            }).start();
        }
        this.pgon = pgon;
    }

    private LocationInfo[][] initLocInfo() {
        return new LocationInfo[xCount][yCount];
    }

    public int getxCount() {
        return xCount;
    }

    public synchronized void setxCount(int xCount, boolean reset) {
        this.xCount = xCount;
        dx = (lr[0] - ul[0]) / xCount;
        if (reset) {
            changedCount();
        }
    }

    public int getyCount() {
        return yCount;
    }

    public synchronized void setyCount(int yCount, boolean reset) {
        this.yCount = yCount;
        dy = (ul[1] - lr[1]) / yCount;
        if (reset) {
            changedCount();
        }
    }

    private void changedCount() {
        locInfo = new ArrayList<LocationInfo[][]>();

        ArrayList<Observation> prev = (ArrayList<Observation>) observations.clone();
        observations.clear();

        for (Observation o : observations) {
            newObservation(o, o.index);
        }
    }

    /**
     * x and y are normalized so that 0,0 is bottom, left and 1,1 is top, right.
     *
     * @param x
     * @param y
     * @param index
     * @return
     */
    public double getValueAt(double x, double y, int index) {

        int xi = (int) Math.floor(x * xCount);
        int yi = (int) Math.floor(y * yCount);

        try {
            return locInfo.get(index)[xi][yi].getMean();
        } catch (NullPointerException e) {
            return Double.NaN;
        }

    }
    int prevIndex = -1;

    public synchronized BufferedImage makeBufferedImage(int index) {

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

        // @todo Work out scaling

        // double dx = width / autonomy.getWidth();
        // double dy = height / autonomy.getHeight();
        double dx = 1.0;
        double dy = 1.0;

        g2.clearRect(0, 0, (int) width, (int) height);

        LocationInfo[][] model = null;

        try {
            model = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        int bx = (int) (width / model.length);
        int by = (int) (height / model[0].length);

        int c = 0;
        mean = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null) {
                    mean += model[i][j].getMean();
                    c++;
                }
            }
        }
        mean /= (double) c;

        maxExtent = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null) {
                    double diff = Math.abs(mean - model[i][j].getMean());
                    if (diff > maxExtent) {
                        maxExtent = diff;
                    }
                }
            }
        }

        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {

                if (model[i][j] != null) {

                    double v = 0.0;

                    if (showMean) {
                        v = model[i][j].getMean();
                    } else {
                        v = model[i][j].getStdDev();
                    }

                    g2.setColor(Color.black);
                    g2.drawString(df.format(v), bx * i, (int) (height - by * (j + 1)));

                    double alpha = Math.abs((mean - v) / maxExtent);
                    alpha = Math.min(1.0, alpha);

                    if (v < mean) {
                        g2.setColor(new Color(1.0f, 0.0f, 0.0f, (float) alpha));
                    } else {
                        g2.setColor(new Color(0.0f, 1.0f, 0.0f, (float) alpha));
                    }

                    // System.out.println("v = " + v + " mean = " + mean + " maxExtent=" + maxExtent + " alpha=" + alpha);

                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                }

                g2.fillRect(bx * i, (int) (height - by * (j + 1)), bx, by);

            }
        }

        return bimage;
    }

    public void setShowMean(boolean showMean) {
        this.showMean = showMean;
    }
    private Hashtable<String, Integer> baseIndicies = new Hashtable<String, Integer>();
    int prevX = -1, prevY = -1, obsInCell = 0;

    public synchronized void newObservation(Observation o, int index) {
        o.index = index;
        observations.add(o);

        LocationInfo[][] li = null;

        // @todo This will work because there is a loop sending the pieces of data, but too dangerous
        Integer baseI = baseIndicies.get(o.variable);
        if (baseI == null) {
            baseI = locInfo.size();
            baseIndicies.put(o.variable, baseI);
        }
        index += baseI;

        try {
            li = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
        }

        if (li == null) {
            while (locInfo.size() <= index) {
                locInfo.add(initLocInfo());
            }
            li = locInfo.get(index);
        }

        int bx = (int) ((o.getWaypoint()[0] - ul[0]) / dx);
        int by = (int) ((o.getWaypoint()[1] - lr[1]) / dy);

        try {
            if (li[bx][by] == null) {
                li[bx][by] = new LocationInfo(Double.MIN_VALUE, Double.MAX_VALUE);
            }

            li[bx][by].addObs(o);

            //System.out.println("Added " + index + " " + o.variable + " obs to " + bx + " " + by + " mean " + li[bx][by].getMean() + " std. dev. " + li[bx][by].getStdDev() + " count " + li[bx][by].getCount() + " bounds mid " + li[bx][by].getBoundsMidpoint());

        } catch (ArrayIndexOutOfBoundsException e) {
            // System.out.println("OUT OF EXTENT: " + bx + " " + li.length + " " + by + " " + li[0].length);
        }

        /*
        // Ugly version of kicking the boat back into action if a waypoint done isn't received for a while
        if (prevX == bx && prevY == by) {
            ++obsInCell;
            if (obsInCell > 50) {
                ProxyManager pm = new ProxyManager();
                for (BoatSimpleProxy bsp : pm.getAll()) {
                    if (bsp.autonomousSensingBoats.contains(bsp)) {
                        bsp.planAutonomousSense();
                        System.out.println("WOKE BOAT FROM MISSED WAYPOINT");
                    }
                }
                obsInCell = 0;
            }
        } else {
            prevX = bx;
            prevY = by;
            obsInCell = 0;
        }
        */
    }
    
    Random rand = new Random();

    /**
     * Picks the next point for inspection
     *
     * @param currLoc
     * @param sensors The list of boats doing autonomous sensing
     * @return
     */
    public ArrayList<Position> getWaypoints(Position currLoc, BoatSimpleProxy self, ArrayList<BoatSimpleProxy> sensors) {

        switch (BoatSimpleProxy.autonomousSearchAlgorithm) {

            case MAX_UNCERTAINTY:
                return getMaxUncertaintyPlan(currLoc);

            case RANDOM:

                int i = rand.nextInt(xCount - 1);
                int j = rand.nextInt(yCount - 1);
                Position p = positionForIndex(currLoc, i, j);
                while (!pointInPolygon(p, pgon)) {
                    i = rand.nextInt(xCount - 1);
                    j = rand.nextInt(yCount - 1);
                    p = positionForIndex(currLoc, i, j);
                }
                return indexToPath(currLoc, i, j);


            case LAWNMOWER:
                return getLawnmowerPlan(currLoc, self, sensors);

            default:
                System.out.println("UNKNOWN SENSING ALGORITHM: " + BoatSimpleProxy.autonomousSearchAlgorithm);
                return null;
        }

    }

    private ArrayList<Position> getLawnmowerPlan(Position currLoc, BoatSimpleProxy self, ArrayList<BoatSimpleProxy> sensors) {

        int count = sensors.size();
        int index = sensors.indexOf(self);

        int yPer = (int) Math.max(2.0, Math.ceil((double) yCount / (double) count));

        ArrayList<Position> path = new ArrayList<Position>();

        for (int i = yPer * index; i < (yPer * (index + 1)); i += 2) {

            int ri = Math.min(yCount - 2, i);

            int xb = 0;
            int xt = xCount;

            Position p = positionForIndex(currLoc, xb, ri);
            while (!pointInPolygon(p, pgon)) {
                xb++;
                p = positionForIndex(currLoc, xb, ri);
                if (xb >= xt) {
                    System.out.println("Invalid shape, xb > xt");
                    System.exit(0);
                }
            }

            p = positionForIndex(currLoc, xt, ri);
            while (!pointInPolygon(p, pgon)) {
                xt--;
                p = positionForIndex(currLoc, xt, ri);
                if (xt <= xb) {
                    System.out.println("Invalid shape, xt < xb");
                    System.exit(0);
                }
            }

            path.add(positionForIndex(currLoc, xb, ri));
            path.add(positionForIndex(currLoc, xt, ri));
            path.add(positionForIndex(currLoc, xt, ri + 1));
            path.add(positionForIndex(currLoc, xb, ri + 1));

        }

        return path;

    }

    private ArrayList<Position> getMaxUncertaintyPlan(Position currLoc) {

        /*
        int [] best = getMaxuncertaintyPoint(currLoc, null);
        

        if (best[0] >= 0 && best[1] >= 0) {
            return indexToPath(currLoc, best[0], best[1]);
        } else {
            System.out.println("No best value for sensing, fails");
        }
        */
        
        return null;
    }

    private ArrayList<Position> indexToPath(Position currLoc, int i, int j) {
        ArrayList<Position> path = new ArrayList<Position>();
        path.add(positionForIndex(currLoc, i, j));
        return path;
    }

    public Position getMaxuncertaintyPoint(Position currLoc, ArrayList<Position> dests) {

        int bestI = -1;
        int bestJ = -1;
        double bestValue = -1.0;

        ArrayList<int[]> ex = null;
        if (dests != null) {
            ex = new ArrayList<int []>();
            
            for (Position position : dests) {
                UTMCoord utm = UTMCoord.fromLatLon(position.latitude, position.longitude);
                int bx = (int) ((utm.getEasting() - ul[0]) / dx);
                int by = (int) ((utm.getNorthing() - lr[1]) / dy);
                
                ex.add(new int[] { bx, by});
            }
        }
        
        Position p = null;
        
        if (locInfo.size() > 0) {
            LocationInfo[][] data = locInfo.get(0);
            
            for (int i = 0; i < data.length - 1; i++) {
                for (int j = 0; j < data[i].length - 1; j++) {
                    p = positionForIndex(currLoc, i, j);
                    LocationInfo locationInfo = data[i][j];
                    double v = locationInfo == null ? Double.MAX_VALUE : locationInfo.valueOfMoreObservations();

                    if (v > bestValue || (v == bestValue && rand.nextBoolean())) {

                        // Check to make sure not already used
                        boolean taken = false;
                        if (ex != null) {
                            for (int[] is : ex) {
                                if (is[0] == i && is[1] == j) {
                                    taken = true;
                                }
                            }
                        }

                        if (!taken && pointInPolygon(p, pgon)) {
                            bestI = i;
                            bestJ = j;
                            bestValue = v;
                                                        
                        } else {
                            // System.out.println("REJECTING, not in poly");
                        }
                    }
                }
            }

            p = positionForIndex(currLoc, bestI, bestJ);
            
        } else {
            System.out.println("No data for sensing, defaulting");
            bestI = -1;
            bestJ = -1;
            p = null;
            do {
                p = positionForIndex(currLoc, ++bestI, ++bestJ);
            } while (!pointInPolygon(p, pgon));
        }

        System.out.println("Chose " + bestI + " " + bestJ + " " + bestValue);
        
        return p;
    }

    /*
     * Adapted from http://en.wikipedia.org/wiki/Point_in_polygon
     */
    private boolean pointInPolygon(Position p, Polygon poly) {
        Position zero = new Position(LatLon.ZERO, 0.0);
        Position p1 = null;
        Position first = null;
        int count = 0;
        for (LatLon l : poly.getOuterBoundary()) {
            Position p2 = new Position(l, 0.0);
            if (p1 == null) {
                p1 = p2;
                first = p2;
            } else {
                boolean intersects = segmentsIntersectA(p1, p2, zero, p);
                if (intersects) {
                    count++;
                }
                p1 = p2;
            }
        }
        boolean intersects = segmentsIntersectA(first, p1, zero, p);
        if (intersects) {
            count++;
        }

        //System.out.println("Counts = " + count);

        return count % 2 != 0;
    }

    /**
     * Adapted from http://paulbourke.net/geometry/lineline2d/
     */
    private boolean segmentsIntersect(Position s1, Position e1, Position s2, Position e2) {

        double d = ((e2.latitude.degrees - s2.latitude.degrees) * (s1.longitude.degrees - s2.longitude.degrees) - (e2.longitude.degrees - s2.longitude.degrees) * (s2.latitude.degrees - s1.latitude.degrees));
        double ua = ((e2.longitude.degrees - s2.longitude.degrees) * (s1.latitude.degrees - s2.latitude.degrees) - (e2.latitude.degrees - s2.latitude.degrees) * (s1.longitude.degrees - s2.longitude.degrees)) / d;
        double ub = ((e1.longitude.degrees - s1.longitude.degrees) * (s1.latitude.degrees - s2.latitude.degrees) - (e1.latitude.degrees - s1.latitude.degrees) * (s1.longitude.degrees - s2.longitude.degrees)) / d;

        boolean ret = (ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0);

        // System.out.println("Line segment: " + ret + " " + s1 + "-" + e1 + " against " + s2 + " - " + e2);

        return ret;
    }

    private boolean segmentsIntersectA(Position s1, Position e1, Position s2, Position e2) {

        double x1 = s1.longitude.degrees;
        double x2 = e1.longitude.degrees;
        double x3 = s2.longitude.degrees;
        double x4 = e2.longitude.degrees;

        double y1 = s1.latitude.degrees;
        double y2 = e1.latitude.degrees;
        double y3 = s2.latitude.degrees;
        double y4 = e2.latitude.degrees;

        double d = ((y4 - y3) * (x2 - x1)) - ((x4 - x3) * (y2 - y1));
        double ua = (((x4 - x3) * (y1 - y3)) - ((y4 - y3) * (x1 - x3))) / d;
        double ub = (((x2 - x1) * (y1 - y3)) - ((y2 - y1) * (x1 - x3))) / d;

        boolean ret = (ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0);

        //System.out.println("Line segment: " + ret + " " + s1 + "-" + e1 + " against " + s2 + " - " + e2);

        return ret;
    }

    /**
     * @todo Offsets are for Pittsburgh, need to have signs reversed for other
     * hemispheres
     *
     * @param curr
     * @param bestI
     * @param bestJ
     * @return
     */
    private Position positionForIndex(Position curr, int bestI, int bestJ) {

        double easting = ul[0] + ((double) (bestI + 0.5) * dx);
        double northing = lr[1] + ((double) (bestJ + 0.5) * dy);

        UTMCoord utm = UTMCoord.fromLatLon(curr.latitude, curr.longitude);

        UTMCoord destC = UTMCoord.fromUTM(utm.getZone(), utm.getHemisphere(), easting, northing);

        return new Position(destC.getLatitude(), destC.getLongitude(), 0.0);
    }
}
