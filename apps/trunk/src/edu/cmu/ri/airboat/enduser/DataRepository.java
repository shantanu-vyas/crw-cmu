/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.airboat.general.LocationInfo;
import edu.cmu.ri.airboat.general.Observation;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.UtmPose;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.DefaultComboBoxModel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import robotutils.Pose3D;

/**
 *
 * @author pscerri
 */
public class DataRepository {

    ArrayList<SensorData> rawData = new ArrayList<SensorData>(1000);
    ArrayList<Observation> observations = new ArrayList<Observation>(1000);
    ArrayList<LocationInfo[][]> locInfo = new ArrayList<LocationInfo[][]>();
    int divisions = 100;
    double dx = 1.0, dy = 1.0;
    private Hashtable<String, Integer> baseIndicies = new Hashtable<String, Integer>();
    private LatLon mins;
    private LatLon maxs;
    private UTMCoord utmMin = null;
    private UTMCoord utmMax = null;
    boolean contoursOn = true;
    // @todo Actually set this properly
    boolean contourIsPercentile = true;
    double contourValue = 80.01;
    // @todo Make bounds settable
    double lowerFilterBound = 75.0;
    double upperFilterBound = 85.0;
    int indexOfInterest = 0;
    private DecimalFormat df = new DecimalFormat("#.###");
    private Random rand = new Random();
    private JTextPane latestTP = null;    

    public enum ImageType {

        Grid, Point, Interpolated, Uncertainty, Bounds, None, GT;
    };
    private ImageType imgType = ImageType.Grid;

    // @todo Allow changing of interpolation type
    // This will also allow changing the number of divisions
    // The problem is that interpolation is done incrementally and now needs to be erased and 
    // restarted when the interpolation type changes.
    public enum InterpolationType {

        PolynomialNN, PowerNN;
    }
    private InterpolationType interType = InterpolationType.PolynomialNN;

    public DataRepository(LatLon mins, LatLon maxs) {
        this.mins = mins;

        utmMin = UTMCoord.fromLatLon(mins.latitude, mins.longitude);

        this.maxs = maxs;

        utmMax = UTMCoord.fromLatLon(maxs.latitude, maxs.longitude);

        setds();
    }

    public void setMaxs(LatLon maxs) {
        utmMax = UTMCoord.fromLatLon(maxs.latitude, maxs.longitude);
        this.maxs = maxs;
        setds();
    }

    public void setMins(LatLon mins) {
        utmMin = UTMCoord.fromLatLon(mins.latitude, mins.longitude);
        this.mins = mins;
        setds();
    }

    public ImageType getImageType() {
        return imgType;
    }
    
    private DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
    public DefaultComboBoxModel getCBModel() {
        return cbModel;
    }
    
    public Position getPositionFor(double dx, double dy) {

        double lat = mins.latitude.degrees + (dy * (maxs.latitude.degrees - mins.latitude.degrees));
        double lon = mins.longitude.degrees + (dx * (maxs.longitude.degrees - mins.longitude.degrees));

        Position p = new Position(LatLon.fromDegrees(lat, lon), 0.0);

        System.out.println("Translated " + dx + " " + dy + " to " + p);

        return p;
    }

    public void changeValues(double dx, double dy, boolean up) {
        if (locInfo.size() > indexOfInterest) {
            LocationInfo[][] model = locInfo.get(indexOfInterest);
            if (model == null) {
                System.out.println("No data");
            } else {
                int x = (int) Math.floor(divisions * dx);
                int y = (int) Math.floor(divisions * dy);

                if (model[x][y] != null) {
                    model[x][y].userChange(up);
                }
            }
        }
    }

    public void setImgType(ImageType imgType) {
        this.imgType = imgType;
    }

    public void setLatestTP(JTextPane latestTP) {
        this.latestTP = latestTP;
    }

    private void setds() {

        dx = (utmMax.getEasting() - utmMin.getEasting()) / (double) divisions;
        dy = (utmMax.getNorthing() - utmMin.getNorthing()) / (double) divisions;

        // System.out.println("Used " + utmMax.getEasting() + " " + utmMin.getEasting() + " and " + (double) divisions + " to get " + dx);
    }
    double contourPercentile = 0.5;

    double setContourPercentOfMax() {
        return setContourPercentOfMax(contourPercentile);
    }

    public double setContourPercentOfMax(double d) {
        contourPercentile = d;
        try {
            LocationInfo[][] model = locInfo.get(indexOfInterest);
            if (model == null) {
                // @todo What to do when contour set and no data?
            } else {
                double mean = computeMean(model);
                double extent = computeExtent(mean, model);
                contourValue = (mean - extent / 2.0) + extent * d;
            }
        } catch (IndexOutOfBoundsException e) {
        }

        return contourValue;
    }

    public double getContourValue() {

        if (contourIsPercentile) {
            setContourPercentOfMax();
        }

        return contourValue;
    }

    public void setContourValue(double contourValue) {
        this.contourValue = contourValue;
    }

    public double getLowerFilterBound() {
        return lowerFilterBound;
    }

    public double setLowerFilterBound(double lowerFilterBound) {
        double prev = this.lowerFilterBound;
        double ret = 0.0;
        if (lowerFilterBound < upperFilterBound) {
            this.lowerFilterBound = lowerFilterBound;
            ret = lowerFilterBound;
        } else {
            lowerFilterBound = upperFilterBound - 1.0;
            ret = this.lowerFilterBound;
        }

        for (LocationInfo[][] ll : locInfo) {
            for (int i = 0; i < ll.length; i++) {
                for (int j = 0; j < ll[i].length; j++) {
                    if (ll[i][j] != null && ll[i][j].getLowerBound() == prev) {
                        ll[i][j].setLowerBound(ret);
                    }
                }
            }
        }

        return ret;
    }

    public double getUpperFilterBound() {
        return upperFilterBound;
    }

    public double setUpperFilterBound(double upperFilterBound) {
        double prev = this.upperFilterBound;
        double ret = 0.0;
        if (upperFilterBound > lowerFilterBound) {
            this.upperFilterBound = upperFilterBound;
            ret = upperFilterBound;
        } else {
            upperFilterBound = lowerFilterBound + 1.0;
            ret = this.upperFilterBound;
        }

        for (LocationInfo[][] ll : locInfo) {
            for (int i = 0; i < ll.length; i++) {
                for (int j = 0; j < ll[i].length; j++) {

                    if (ll[i][j] != null && ll[i][j].getUpperBound() == prev) {
                        ll[i][j].setUpperBound(ret);
                    }
                }

            }
        }

        return ret;
    }

    public void setIndexOfInterest(int index) {
        indexOfInterest = index;
    }
    
    public void setIndexOfInterest(String s) {
        String var = s.substring(0, s.indexOf(":"));
        // System.out.println("Var is " + var);
        int index = Integer.parseInt(s.substring(s.indexOf(":") + 1));
        
        // System.out.println("Index is " + index);
        int base = baseIndicies.get(var);
        indexOfInterest = base+index;
    }
    
    private static Hashtable<String, ArrayList<Double>> prevValues = new Hashtable<String, ArrayList<Double>>();
    private static Hashtable<String, MedianFilterSlidingWindow> filterHash = new Hashtable<String, MedianFilterSlidingWindow>();
    private int filterWindow = 30;
    int count = 0;

    void addData(BoatProxy proxy, SensorData sd, UtmPose _pose) {

        try {
            rawData.add(sd);

            //if (sensorToDisplay.equalsIgnoreCase(sd.type.toString()) && latestTP != null) {
            // latestTP.setText(sd.type.toString() + " = " + df.format(sd.data[indexToDisplay]));
            //}

            for (int i = 0; i < sd.data.length; i++) {
                String sensorName = "Sensor" + sd.type;
                String key = sensorName + i + proxy.toString();
                Observation o = new Observation(sensorName, sd.data[i], UtmPoseToDouble(_pose.pose), _pose.origin.zone, _pose.origin.isNorth);

                double gradient = valueToGradient(key, sd.data[i]);
                // Set the gradient
                o.setGradient(gradient);

                newObservation(proxy, o, i);
            }
        } catch (NullPointerException e) {
            System.out.println("Caught (and ignoring) null pointer exception in DataRepository.addData, printing stack trace");
            e.printStackTrace();
        }

        if (++count % 10 == 0) {

            int correct = 0;

            for (int i = 0; i < divisions; i++) {
                for (int j = 0; j < divisions; j++) {

                    Position pos = indexToPosition(i, j);

                    double gt = BoatProxy.computeGTValue(pos.latitude.degrees, pos.longitude.degrees);
                    double est = locInfo.get(indexOfInterest)[i][j].getInterpolatedValue();

                    if (contourIsPercentile) {
                        setContourPercentOfMax();
                    }

                    if ((gt > contourValue && est > contourValue) || (gt < contourValue && est < contourValue)) {
                        correct++;
                    }

                    //System.out.print("\t" + df.format(Math.abs(gt-est)));
                    // System.out.print("\t" + df.format(gt) + " " + df.format(est));

                }
                // System.out.println("");
            }
            // System.out.println("Correct: " + correct + " of " + (divisions * divisions));

        } else {
            // System.out.println("Count now: " + count);
        }
    }

    public static void main(String[] argv) {
        try {
            /*
             * DataRepository repo = new DataRepository(LatLon.ZERO,
             * LatLon.fromDegrees(2.0, 2.0)); Random rand = new Random(); for
             * (int i = 0; i < 40; i++) { double d =
             * repo.valueToGradient("test", i * rand.nextDouble());
             * System.out.println("Gradient: " + d); }
             */
            DataRepository repo = new DataRepository(LatLon.ZERO, LatLon.fromDegrees(2.0, 2.0));
            FileReader fr = new FileReader("/tmp/data");
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                StringTokenizer t = new StringTokenizer(line);
                t.nextToken(); // "Obs"
                String key = t.nextToken() + t.nextToken(); // Sensor name and index
                t.nextToken();
                t.nextToken(); // Lat lon
                double v = Double.parseDouble(t.nextToken());
                double d = repo.valueToGradient(key, v);

            }
        } catch (IOException ex) {
            Logger.getLogger(DataRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private double valueToGradient(String key, double value) {

        MedianFilterSlidingWindow mf = filterHash.get(key);
        if (mf == null) {
            mf = new MedianFilterSlidingWindow();
            filterHash.put(key, mf);
        }
        mf.addValue(value);

        double ret = mf.getGradient();

        // ABHINAV COMMENT IN System.out.println("Gradient: " + key + " = " + ret + " for " + filterHash.get(key).getDataAsString());

        return ret;

    }

    private class MedianFilterSlidingWindow {

        final int noWindows = 20;
        final int windowSize = 11;
        final double epsilon = 0.01;
        int obsToDate = 0;
        ArrayList<ArrayList<Double>> windows = new ArrayList<ArrayList<Double>>();

        {
            for (int i = 0; i < (noWindows + windowSize); i++) {
                windows.add(new ArrayList<Double>());
            }
        }

        public synchronized void addValue(double v) {

            obsToDate++;

            // Fill up the windows
            for (int i = 0; i < obsToDate && i < windows.size(); i++) {
                if (windows.get(i).size() < windowSize) {
                    _add(windows.get(i), v);
                }
            }

            // Once windows are full start deleting the first
            if (windows.get(noWindows).size() == windowSize) {
                windows.remove(0);
                windows.add(new ArrayList<Double>());
            }

            // ABHINAV COMMENT IN 
            /*
             * System.out.print("MedianFilterOutput: "); for (int i = 0; i <
             * windows.size(); i++) { if (windows.get(i).size() > 0.0) {
             * System.out.print(" " + median(windows.get(i))); } }
             * System.out.println("");
             *
             */
        }

        private void _add(ArrayList<Double> window, double v) {
            int i = 0;
            while (window.size() > i && window.get(i) > v) {
                i++;
            }
            window.add(i, v);
        }

        private double median(ArrayList<Double> window) {
            return window.get((int) Math.floor(window.size() / 2));
        }

        public String getDataAsString() {
            StringBuilder sb = new StringBuilder();
            if (obsToDate > noWindows + windowSize) {
                for (int i = 0; i < noWindows; i++) {
                    sb.append(median(windows.get(i)) + " ");
                }
            } else {
                sb.append("Too early");
            }
            return sb.toString();
        }

        /**
         * "Stolen" from
         * http://introcs.cs.princeton.edu/java/97data/LinearRegression.java.html
         *
         * @return
         */
        private double _getGradient() {

            double[] ys = new double[noWindows];
            double sumY = 0.0, sumX = 0.0;

            for (int i = 0; i < ys.length; i++) {
                ys[i] = median(windows.get(i));
                sumY += ys[i];
                sumX += i;
            }

            double avgX = sumX / noWindows;
            double avgY = sumY / noWindows;

            double xxbar = 0.0, yybar = 0.0, xybar = 0.0;

            for (int i = 0; i < noWindows; i++) {
                xxbar += (i - avgX) * (i - avgX);
                yybar += (ys[i] - avgY) * (ys[i] - avgY);
                xybar += (i - avgX) * (ys[i] - avgY);
            }
            double beta1 = xybar / xxbar;
            double beta0 = avgY - beta1 * avgX;

            // print results
            // ABHINAV COMMENT IN  System.out.println("y   = " + beta1 + " * x + " + beta0);


            return beta1;
        }

        private double getGradient() {
            if (obsToDate < noWindows + windowSize) {
                return 0.0;
            } else {
                double d = _getGradient();
                if (Math.abs(d) > epsilon) {
                    return Math.signum(d);
                } else {
                    return 0.0;
                }
            }
        }
    }

    private double[] UtmPoseToDouble(Pose3D p) {
        double[] d = new double[3];
        d[0] = p.getX();
        d[1] = p.getY();
        d[2] = p.getZ();
        return d;
    }
    Hashtable<BoatProxy, ArrayList<Double>> lastObs = new Hashtable<BoatProxy, ArrayList<Double>>();

    public synchronized void newObservation(BoatProxy proxy, Observation o, int index) {
        observations.add(o);

        LocationInfo[][] li = null;

        // @todo This will work because there is a loop sending the pieces of data, but too dangerous
        Integer baseI = baseIndicies.get(o.variable);
        if (baseI == null) {
            baseI = locInfo.size();
            baseIndicies.put(o.variable, baseI);
            
            //System.out.println("Put " + o.variable + " " + baseI);
        }
        index += baseI;

        o.index = index;

        try {
            li = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
        }

        if (li == null) {
            while (locInfo.size() <= index) {
                locInfo.add(initLocInfo());
                cbModel.addElement(o.variable + ":" + (index-baseI));
                // FishFarmF.indexCDataModel.addElement(o.index);
            }
            li = locInfo.get(index);

        }

        ArrayList<Double> prevForProxy = lastObs.get(proxy);
        if (prevForProxy == null) {
            prevForProxy = new ArrayList<Double>();
            System.out.println("ADDED for " + proxy);
            lastObs.put(proxy, prevForProxy);
        }
        try {
            prevForProxy.add(index, o.getValue());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("NON TERMINAL issue with prevForProxy in DataRepository, index = " + index + ", size = " + prevForProxy.size());
        }

        int bx = toXIndex(o.getWaypoint()[0]);
        int by = toYIndex(o.getWaypoint()[1]);

        // ABHINAV COMMENT IN System.out.println("Obs\t" + o.variable + "\t" + index + "\t" + bx + "\t" + by + "\t" + o.waypoint[0] + "\t" + o.waypoint[1] + "\t" + o.getValue() + "\t" + o.getGradient() + "\t" + System.currentTimeMillis());


        try {
            if (li[bx][by] == null) {
                li[bx][by] = new LocationInfo(lowerFilterBound, upperFilterBound);
            }

            // ABHINAV COMMENT IN System.out.print("Bounds for\t" + bx + "\t" + by + "\t");
            li[bx][by].addObs(o);

            // System.out.println("Added obs to " + bx + " " + by + " mean " + li[bx][by].getMean() + " std. dev. " + li[bx][by].getStdDev() + " count " + li[bx][by].getCount());

            // Update the inverse distance interpolation values
            for (int i = 0; i < divisions; i++) {
                for (int j = 0; j < divisions; j++) {
                    double dist = (((i - bx) * (i - bx)) + (j - by) * (j - by) + 1);
                    double contrib = 1.0 / dist;
                    if (li[i][j] == null) {
                        li[i][j] = new LocationInfo(lowerFilterBound, upperFilterBound);
                    }

                    /* Non temporal version
                     li[i][j].interpolationContributions += contrib;
                     li[i][j].interpolationValue += (o.value * contrib);
                     */

                    // Temporal version
                    li[i][j].interpolationContributions = (0.999 * li[i][j].interpolationContributions) + (1.001 * contrib);
                    li[i][j].interpolationValue = (0.999 * li[i][j].interpolationValue) + (1.001 * o.value * contrib);

                    /*
                     * if (i == 0 && j == 0 && index == 0)
                     * System.out.println("Added : " + contrib + " and " +
                     * (o.value * contrib) + " now " +
                     * li[i][j].interpolationContributions + " and " +
                     * li[i][j].interpolationValue + " for " +
                     * (li[i][j].interpolationValue /
                     * li[i][j].interpolationContributions));
                     *
                     */
                }
            }


        } catch (ArrayIndexOutOfBoundsException e) {
            // System.out.println("OUT OF EXTENT: " + bx + " " + li.length + " " + by + " " + li[0].length);
            System.out.print("-");
        }
    }

    private int toXIndex(double easting) {

        // System.out.println("Using " + utmMax.getEasting() + " " + easting + " " + dx);

        return (int) Math.floor((easting - utmMin.getEasting()) / dx);
    }

    private int toYIndex(double northing) {
        return (int) Math.floor((northing - utmMin.getNorthing()) / dy);
    }

    private LocationInfo[][] initLocInfo() {
        LocationInfo[][] newLocInfo = new LocationInfo[divisions][divisions];
        for (int i = 0; i < divisions; i++) {
            for (int j = 0; j < divisions; j++) {
                newLocInfo[i][j] = new LocationInfo(lowerFilterBound, upperFilterBound);
            }
        }

        return newLocInfo;
    }

    /*
     * Autonomous sensing algorithm stuff
     */
    public enum AutonomyAlgorithm {

        Random,
        Lawnmower, // Or as Balajee would say lawn-mover
        Uncertainty,
        Contour,
        Bounded;
    }
    private AutonomyAlgorithm alg = AutonomyAlgorithm.Random;

    public void setAlg(AutonomyAlgorithm alg) {

        System.out.println("\n\n Set autonomy algorithm to: " + alg + "\n\n");

        this.alg = alg;
    }

    public AutonomyAlgorithm getAlg() {
        return alg;
    }
    private ArrayList<BoatProxy> autonomousProxies = new ArrayList<BoatProxy>();

    /*
    public ArrayList<Position> getAutonomyPath(BoatProxy proxy) {

        int x = toXIndex(proxy.getEasting());
        int y = toYIndex(proxy.getNorthing());

        // if "out of extent" return a plan the brings it to the middle
        if (x < 0 || x > divisions || y < 0 || y > divisions) {
            ArrayList<Position> p = new ArrayList<Position>();
            // p.add(indexToPosition((int) (divisions / 2), (int) (divisions / 2)));
            x = Math.max(1, Math.min(divisions - 2, x));
            y = Math.max(1, Math.min(divisions - 2, y));
            p.add(indexToPosition(x, y));
            return p;
        }

        switch (alg) {
            case Random:
                ArrayList<Position> p = new ArrayList<Position>();
                p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
                return p;

            case Lawnmower:
                return getLawnmowerPlan(autonomousProxies.size(), autonomousProxies.indexOf(proxy));

            case Uncertainty:
                return getMaxUncertaintyPlan(autonomousProxies.size(), autonomousProxies.indexOf(proxy));

            case Contour:
                return getContourFocusPlan(autonomousProxies.size(), autonomousProxies.indexOf(proxy));

            case Bounded:
                return getBoundedPlan(proxy);

            default:
                System.out.println("Unknown autonomy algorithm: " + alg + ", using random");
                p = new ArrayList<Position>();
                p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
                return p;
        }

    }

    public void addAutonomous(FishFarmBoatProxy p) {
        autonomousProxies.add(p);

        // @todo Reassign all proxies at this point
    }

    public void removeAutonomous(FishFarmBoatProxy p) {
        autonomousProxies.remove(p);

        // @todo Reassign all proxies at this point
    }

    private ArrayList<Position> getBoundedPlan(FishFarmBoatProxy proxy) {

        System.out.println("Bounded planning called!!!");

        ArrayList<Position> p = new ArrayList<Position>();

        LocationInfo[][] data = locInfo.get(indexOfInterest);
        double currReading = 0.0;
        try {
            currReading = lastObs.get(proxy.getProxy()).get(indexOfInterest);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            System.out.println("" + lastObs.get(proxy.getProxy()) + " when looking for " + proxy + " keys: " + lastObs.keySet());
            data = null;
        }
        int x = toXIndex(proxy.getEasting());
        int y = toYIndex(proxy.getNorthing());

        if (data == null) {

            System.out.println("GOING RANDOM");

            // No data, select random point
            x = x + (rand.nextBoolean() ? 1 : -1);
            if (x < 0) {
                x = 0;
            }
            if (x >= divisions) {
                x = divisions - 1;
            }
            y = y + (rand.nextBoolean() ? 1 : -1);
            if (y < 0) {
                y = 0;
            }
            if (y >= divisions) {
                y = divisions - 1;
            }
            p.add(indexToPosition(x, y));
        } else {

            double[][] lower = new double[divisions][divisions];
            double[][] upper = new double[divisions][divisions];

            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    lower[i][j] = data[i][j].getLowerBound();
                    upper[i][j] = data[i][j].getUpperBound();
                }
            }
            p = getBoundedPlan(x, y, currReading, lower, upper);
        }

        return p;
    }

    private ArrayList<Position> getLawnmowerPlan(int count, int index) {

        System.out.println("Lawnmower planning for " + index + "th robot");

        int yPer = (int) Math.max(2.0, Math.ceil((double) (divisions - 1) / (double) count));

        ArrayList<Position> path = new ArrayList<Position>();

        for (int i = yPer * index + 1; i < (yPer * (index + 1)); i += 2) {

            int ri = Math.min(divisions - 2, i);

            int xb = 0;
            int xt = divisions - 1;

            path.add(indexToPosition(xb, ri));
            path.add(indexToPosition(xt, ri));
            path.add(indexToPosition(xt, ri + 1));
            path.add(indexToPosition(xb, ri + 1));

        }

        return path;
    }
    HashMap<Integer, Point> contourAllocations = new HashMap<Integer, Point>();

    private synchronized ArrayList<Position> getContourFocusPlan(int count, int index) {
        ArrayList<Position> p = new ArrayList<Position>();

        Point prev = contourAllocations.remove(index);

        LocationInfo[][] data = locInfo.get(indexOfInterest);
        if (data == null) {
            // No data, select random point
            p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
        } else {
            double best = -1.0;
            int bestI = -1, bestJ = -1;
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    double pureVal = data[i][j].interpolatedValueOfMoreObservations();

                    if (contourIsPercentile) {
                        setContourPercentOfMax();
                    }

                    // Want this to be 1.0 when same, 0 when very different
                    double contourDist = Math.exp(-Math.abs(contourValue - data[i][j].getInterpolatedValue())) / Math.E;

                    boolean alreadyAllocated = false;
                    for (Point point : contourAllocations.values()) {
                        if (point.x == i && point.y == j) {
                            System.out.println("Already allocated");
                            alreadyAllocated = true;
                        }
                    }

                    // if (index == 0) { System.out.println(i + " " + j + " Contour dist " + contourDist + " " + data[i][j].getInterpolatedValue() + " " + contourValue); }

                    double v = pureVal * contourDist;
                    // double v = contourDist;
                    if (!alreadyAllocated && v > best && !(prev != null && prev.x == i && prev.y == j)) {
                        best = v;
                        bestI = i;
                        bestJ = j;
                    }
                }
            }

            contourAllocations.put(index, new Point(bestI, bestJ));
            p.add(indexToPosition(bestI, bestJ));
        }

        return p;
    }
    HashMap<Integer, Point> uncertaintyAllocations = new HashMap<Integer, Point>();

    private ArrayList<Position> getMaxUncertaintyPlan(int count, int index) {
        ArrayList<Position> p = new ArrayList<Position>();

        uncertaintyAllocations.remove(index);

        LocationInfo[][] data = locInfo.get(indexOfInterest);
        if (data == null) {
            // No data, select random point
            p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
        } else {
            double best = -1.0;
            int bestI = -1, bestJ = -1;
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    double v = data[i][j].valueOfMoreObservations();

                    boolean alreadyAllocated = false;
                    for (Point point : uncertaintyAllocations.values()) {
                        if (point.x == i && point.y == j) {
                            System.out.println("Already allocated");
                            alreadyAllocated = true;
                        }
                    }

                    if (!alreadyAllocated && v > best) {
                        best = v;
                        bestI = i;
                        bestJ = j;
                    }
                }
            }
            uncertaintyAllocations.put(index, new Point(bestI, bestJ));
            p.add(indexToPosition(bestI, bestJ));
        }

        return p;
    }
    */
    
    /*
     * END Autonomous sensing algorithm stuff
     */
    private Position indexToPosition(int x, int y) {
        // System.out.println("Index to pos with " + x + " " + y);
        UTMCoord utm = UTMCoord.fromUTM(utmMin.getZone(), utmMin.getHemisphere(), utmMin.getEasting() + (dx * (x + 0.5)), utmMin.getNorthing() + (dy * (y + 0.5)));
        LatLon ll = new LatLon(utm.getLatitude(), utm.getLongitude());
        return new Position(ll, 0.0);
    }

    public synchronized BufferedImage makeBufferedImage() {
        
        switch (imgType) {
            case Grid:
                return makeGridBufferedImage(indexOfInterest, true, false);

            case Uncertainty:
                return makeGridBufferedImage(indexOfInterest, false, false);

            case Bounds:
                return makeGridBufferedImage(indexOfInterest, false, true);

            case Interpolated:
                return makeInterpolatedBufferedImage(indexOfInterest);

            case Point:
                return makePointBufferedImage(indexOfInterest);

            case GT:
                return makeGTImage();

            case None:
            default:
                System.out.println("Unsupported image type: " + imgType);
                return null;
        }

    }

    private BufferedImage makePointBufferedImage(int index) {
        int width = 500;
        int height = 500;

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

        LocationInfo[][] model = null;

        try {
            model = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        double denomX = utmMax.getEasting() - utmMin.getEasting();
        double denomY = utmMax.getNorthing() - utmMin.getNorthing();

        double mean = computeMean(model);
        double maxExtent = computeExtent(mean, model);

        for (Observation o : (Iterable<Observation>) observations.clone()) {

            if (o.index == indexOfInterest) {
                int x = (int) (((o.waypoint[0] - utmMin.getEasting()) / denomX) * width);
                int y = (int) (((o.waypoint[1] - utmMin.getNorthing()) / denomY) * height);

                double alpha = Math.abs((mean - o.value) / maxExtent);
                alpha = Math.min(1.0, alpha);

                if (o.value < mean) {
                    g2.setColor(new Color(1.0f, 0.0f, 0.0f, (float) alpha));
                } else {
                    g2.setColor(new Color(0.0f, 1.0f, 0.0f, (float) alpha));
                }

                g2.fillOval(x, (int) (height - y), (int) (1 + 5 * alpha), (int) (1 + 5 * alpha));
            }

        }
        return bimage;
    }

    double computeBoundsMean(LocationInfo[][] model) {
        int c = 0;
        double mean = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    mean += model[i][j].getBoundsMidpoint();
                    c++;
                }
            }
        }
        mean /= (double) c;

        return mean;
    }

    double computeBoundsExtent(double mean, LocationInfo[][] model) {
        double maxExtent = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    double diff = Math.abs(mean - model[i][j].getBoundsMidpoint());
                    if (diff > maxExtent) {
                        maxExtent = diff;
                    }
                }
            }
        }
        return maxExtent;
    }

    double computeMean(LocationInfo[][] model) {
        int c = 0;
        double mean = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    mean += model[i][j].getMean();
                    c++;
                }
            }
        }
        mean /= (double) c;

        return mean;
    }

    double computeExtent(double mean, LocationInfo[][] model) {
        double maxExtent = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    double diff = Math.abs(mean - model[i][j].getMean());
                    if (diff > maxExtent) {
                        maxExtent = diff;
                    }
                }
            }
        }
        return maxExtent;
    }

    double computeUncertaintyMean(LocationInfo[][] model) {
        int c = 0;
        double mean = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    mean += model[i][j].getStdDev();
                    c++;
                }
            }
        }
        mean /= (double) c;

        return mean;
    }

    double computeUncertaintyExtent(double mean, LocationInfo[][] model) {
        double maxExtent = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    double diff = Math.abs(mean - model[i][j].getStdDev());
                    if (diff > maxExtent) {
                        maxExtent = diff;
                    }
                }
            }
        }
        return maxExtent;
    }

    double computeGTMean() {
        double v = 0.0;
        for (int i = 0; i < divisions; i++) {
            for (int j = 0; j < divisions; j++) {

                Position pos = indexToPosition(i, j);
                v += BoatProxy.computeGTValue(pos.latitude.degrees, pos.longitude.degrees);
            }
        }
        return v / (double) (divisions * divisions);
    }

    double computeGTExtent(double mean) {
        double maxExtent = 0.0;
        for (int i = 0; i < divisions; i++) {
            for (int j = 0; j < divisions; j++) {

                Position pos = indexToPosition(i, j);
                double v = BoatProxy.computeGTValue(pos.latitude.degrees, pos.longitude.degrees);
                if (Math.abs(v - mean) > maxExtent) {
                    maxExtent = Math.abs(v - mean);
                }
            }
        }
        return maxExtent;
    }

    private BufferedImage makeGTImage() {
        int width = 100;
        int height = 100;

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

        double dx = 1.0;
        double dy = 1.0;

        g2.clearRect(0, 0, (int) width, (int) height);

        int bx = (int) (width / divisions);
        int by = (int) (height / divisions);

        double mean = computeGTMean();
        double maxExtent = computeGTExtent(mean);

        for (int i = 0; i < divisions; i++) {
            for (int j = 0; j < divisions; j++) {

                Position pos = indexToPosition(i, j);
                double v = BoatProxy.computeGTValue(pos.latitude.degrees, pos.longitude.degrees);

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


                g2.fillRect(bx * i, (int) (height - by * (j + 1)), bx, by);

            }
        }

        return bimage;
    }

    private BufferedImage makeGridBufferedImage(int index, boolean useMean, boolean useBounds) {
        int width = 100;
        int height = 100;

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

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

        double mean = useMean ? computeMean(model) : (useBounds ? computeBoundsMean(model) : computeUncertaintyMean(model));
        double maxExtent = useMean ? computeExtent(mean, model) : (useBounds ? computeBoundsExtent(mean, model) : computeUncertaintyExtent(mean, model));

        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {

                if (model[i][j] != null && model[i][j].getCount() > 0) {

                    double v = 0.0;

                    v = useMean ? model[i][j].getMean() : (useBounds ? model[i][j].getBoundsMidpoint() : model[i][j].getStdDev());

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
                    if (useMean) {
                        g2.setColor(Color.LIGHT_GRAY);
                    } else {
                        g2.setColor(new Color(0.0f, 1.0f, 0.0f, 1.0f));
                    }
                }

                g2.fillRect(bx * i, (int) (height - by * (j + 1)), bx, by);

            }
        }

        return bimage;
    }

    private BufferedImage makeInterpolatedBufferedImage(int index) {
        int width = 100;
        int height = 100;

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

        // @todo Work out scaling

        // double dx = width / autonomy.getWidth();
        // double dy = height / autonomy.getHeight();
        double dx = 1.0;
        double dy = 1.0;

        double epsilon = 0.00001;

        g2.clearRect(0, 0, (int) width, (int) height);

        LocationInfo[][] model = null;

        try {
            model = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        int bx = (int) (width / model.length);
        int by = (int) (height / model[0].length);

        double mean = computeMean(model);

        double maxExtent = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    double diff = Math.abs(mean - model[i][j].getMean());
                    if (diff > maxExtent) {
                        maxExtent = diff;
                    }
                }
            }
        }

        int[][] marchingSquares = new int[model.length][model.length];

        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {

                if (model[i][j] != null) {

                    double v = model[i][j].getInterpolatedValue();

                    if (v > contourValue) {
                        marchingSquares[i][j] = 1;
                    } else {
                        marchingSquares[i][j] = 0;
                    }

                    g2.setColor(Color.black);
                    g2.drawString(df.format(v), bx * i, (int) (height - by * (j + 1)));

                    double alpha = 0.0;
                    if (maxExtent > epsilon) {
                        alpha = Math.abs((mean - v) / maxExtent);
                    }
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

        if (contoursOn) {

            // g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(Color.BLUE);

            // Notice the indicies on this are a bit quirky because counts is really the corners            
            int[][] counts = new int[model.length - 1][model.length - 1];
            for (int i = 0; i < counts.length; i++) {
                for (int j = 0; j < counts[i].length; j++) {

                    counts[i][j] += 1 * marchingSquares[i][j + 1];
                    counts[i][j] += 2 * marchingSquares[i + 1][j + 1];
                    counts[i][j] += 4 * marchingSquares[i + 1][j];
                    counts[i][j] += 8 * marchingSquares[i][j];

                }
            }

            for (int i = 0; i < counts.length; i++) {
                for (int j = 0; j < counts[i].length; j++) {
                    int lineNo = counts[i][j];
                    switch (lineNo) {
                        case 0:
                        case 15:
                            break;

                        default:
                            // g2.setColor(colors[lineNo]);
                            g2.drawLine((int) (bx * (i + lines[lineNo][0])), (int) (height - (by * (j + 1.5 - lines[lineNo][1]))),
                                    (int) (bx * (i + lines[lineNo][2])), (int) (height - (by * (j + 1.5 - lines[lineNo][3]))));
                    }
                }
            }

            // g2.setStroke(new BasicStroke(1.0f));
        }

        return bimage;
    }
    double[][] lines = {
        {0.0, 0.0, 0.0, 0.0}, // zero
        {0.0, 0.5, 0.5, 0.0},
        {0.5, 0.0, 1.0, 0.5},
        {0.0, 0.5, 1.0, 0.5},
        {0.5, 1.0, 1.0, 0.5}, // four
        {0.0, 0.0, 0.0, 0.0},
        {0.5, 0.0, 0.5, 1.0},
        {0.0, 0.5, 0.5, 1.0},
        {0.0, 0.5, 0.5, 1.0}, // eight
        {0.5, 0.0, 0.5, 1.0},
        {0.0, 0.0, 0.0, 0.0},
        {0.5, 1.0, 1.0, 0.5},
        {0.0, 0.5, 1.0, 0.5}, // twelve
        {0.5, 0.0, 1.0, 0.5},
        {0.0, 0.5, 0.5, 0.0},
        {0.0, 0.0, 0.0, 0.0}};
    Color[] colors = {
        Color.BLACK, // 0
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA, // 4
        Color.ORANGE,
        Color.PINK, // 6
        Color.RED,
        Color.RED,
        Color.PINK, // 9      
        Color.ORANGE,
        Color.MAGENTA, // 11
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.BLACK // 15
    };
    final int horizon = 16;
    final double maxChange = 1.0;
    PriorityQueue<Point> queue = new PriorityQueue<Point>();

    public ArrayList<Position> getBoundedPlan(int sx, int sy, double curr, double[][] lower, double[][] upper) {
        ArrayList<Position> p = new ArrayList<Position>();

        Point o = new Point(sx, sy);
        o.sensor = curr;
        int count = 0;
        queue.offer(o);

        Point best = o;

        // System.out.println("Starting planning");
        while (!queue.isEmpty() && count < 10000) {
            System.out.print(".");
            Point cp = queue.poll();
            if (cp.value > o.value) {
                best = cp;
                // System.out.println("New best: " + cp);
            }
            ArrayList<Point> ex = getExpansions(cp.x, cp.y, upper.length);
            for (Point point : ex) {
                point.value = cp.value + getValue(lower[point.x][point.y], upper[point.x][point.y], cp.sensor);
                // Work out the expectation of the sensor
                // System.out.println("New value: " + point.value);
                double change = ((upper[point.x][point.y] + lower[point.x][point.y]) / 2.0) - cp.sensor;
                if (change < 0) {
                    change = Math.max(-maxChange, change);
                }
                if (change > 0) {
                    change = Math.min(maxChange, change);
                }
                point.sensor = cp.sensor + change;
                point.prev = cp;
                point.depth = cp.depth + 1;

                if (point.depth < horizon) {
                    queue.offer(point);
                }
                // System.out.println("Offered: " + point);
            }
            count++;
        }

        System.out.println("Done planning: " + best);

        queue.clear();

        while (best != null) {
            // Don't put in the starting point
            if (best.prev != null) {
                p.add(0, indexToPosition(best.x, best.y));
            }
            best = best.prev;
        }

        return p;
    }

    /**
     * Intuition here is the bigger the gap the more important, but further curr
     * is from midpoint of gap the worse.
     *
     * @todo Take into account multiple visits
     *
     * @param l
     * @param u
     * @param c
     * @return
     */
    private double getValue(double l, double u, double c) {
        double d = u - l;
        double m = (u + l) / 2.0;
        double v = Math.abs(m - c);
        ///System.out.println("Deets: " + u + " " + l + " " + m + " " + c);
        // @todo This isn't a very good calculation, since v might often be bigger than d
        return Math.max(0.001, d - v);


    }

    private class Point implements Comparable {

        int x, y;
        int depth = 0;
        double value = 0.0;
        double sensor = 0.0;
        Point prev = null;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Point) {
                Point op = (Point) o;
                return op.x == x && op.y == y;
            }
            return super.equals(o);
        }

        public int compareTo(Object t) {
            if (t instanceof Point) {
                Point tp = (Point) t;
                if (tp.depth >= horizon) {
                    if (depth >= horizon) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (depth >= horizon) {
                    return 1;
                } else if (tp.value > value) {
                    return 1;
                } else if (tp.value < value) {
                    return -1;
                } else {
                    return 0;
                }
            }
            return 0;
        }

        public String toString() {
            return "[" + x + "," + y + "] -> " + prev;
        }
    }

    ArrayList<Point> getExpansions(int x, int y, int divisions) {
        ArrayList<Point> ps = new ArrayList<Point>();
        for (int dx = -1; dx < 2; dx += 1) {
            for (int dy = -1; dy < 2; dy += 1) {
                int nx = x + dx;
                int ny = y + dy;

                // @todo Should staying still be prevented?
                if (nx >= 0 && nx < divisions && ny >= 0 && ny < divisions && !(nx == x && ny == y)) {
                    Point p = new Point(nx, ny);
                    ps.add(p);
                }
            }
        }
        return ps;
    }
}
