/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.fishfarm;

import gov.nasa.worldwind.geom.Position;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class BoundingFilterTest {

    int size = 10;
    int noBoats = 1;
    int moveTime = 10;
    static final int totalTime = 5000;
    double sensorDelta = 0.01;
    double maWeight = 0.9;
    double[][] realValues = null;
    double[][] upperBounds = null;
    double[][] lowerBounds = null;
    double[][] averages = null;
    double lower = 0.0, upper = 1.0;
    Boat[] boats = null;
    DecimalFormat df = new DecimalFormat("#.###");
    Random rand = new Random();
    static public double results[][] = new double[totalTime / 20][4];

    public BoundingFilterTest() {

        realValues = new double[size][size];
        
        /*
        for (int i = 0; i < realValues.length; i++) { for (int j = 0; j <
        realValues.length; j++) { realValues[i][j] = rand.nextDouble(); } }
        */
        
        int noGaussians = 3;

        double[] uxs = new double[3];
        double[] uys = new double[3];
        double[] ss = new double[3];
        double[] as = new double[3];

        for (int i = 0; i < as.length; i++) {
            uxs[i] = (double) rand.nextInt(size);
            uys[i] = (double) rand.nextInt(size);
            ss[i] = 1.0 + rand.nextDouble();
            as[i] = 5.0 * rand.nextDouble();
        }

        for (int i = 0; i < realValues.length; i++) {
            for (int j = 0; j < realValues.length; j++) {
                realValues[i][j] = 0.0;

                for (int k = 0; k < as.length; k++) {

                    double dx = uxs[k] - i;
                    double dy = uys[k] - j;
                    double dsq = dx * dx + dy * dy;

                    realValues[i][j] += as[k] * (1.0 / (ss[k] * Math.sqrt(2.0 * Math.PI))) * Math.pow(Math.E, -(dsq / (2.0 * ss[k] * ss[k])));
                }
                realValues[i][j] = Math.max(0.0, Math.min(1.0, realValues[i][j]));
            }
        }
        
        
        System.out.println("Real values");
        printArray(realValues);

        upperBounds = initArray(upper);
        lowerBounds = initArray(lower);
        averages = initArray(lower + 0.5 * (upper - lower));

        boats = new Boat[noBoats];
        for (int i = 0; i < boats.length; i++) {
            boats[i] = new Boat();
        }

        for (int i = 0; i < totalTime; i++) {
            for (int j = 0; j < boats.length; j++) {
                boats[j].step();

                /*
                 * System.out.println("Real values"); printArray(realValues);
                 * System.out.println("Avg: "); printArray(averages);
                 * System.out.println("Diff: " + comp(averages));
                 * System.out.println("Lower: "); printArray(lowerBounds);
                 * System.out.println("Diff: " + comp(lowerBounds));
                 * System.out.println("Upper: "); printArray(upperBounds);
                 * System.out.println("Diff: " + comp(upperBounds));
                 * System.out.println("Mids: " + compMids(lowerBounds,
                 * upperBounds));
                 */

                if (i % 20 == 0) {
                    // System.out.println(comp(averages) + "\t" + comp(lowerBounds) + "\t" + comp(upperBounds) + "\t" + compMids(lowerBounds, upperBounds));
                    results[i / 20][0] += comp(averages);
                    results[i / 20][1] += comp(lowerBounds);
                    results[i / 20][2] += comp(upperBounds);
                    results[i / 20][3] += compMids(lowerBounds, upperBounds);
                }
            }

        }
    }

    private double comp(double[][] a) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                s += Math.abs(a[i][j] - realValues[i][j]);
            }
        }
        return s;
    }

    private double compMids(double[][] l, double[][] u) {
        double s = 0.0;
        for (int i = 0; i < l.length; i++) {
            for (int j = 0; j < l.length; j++) {
                s += Math.abs(((u[i][j] + l[i][j]) / 2.0) - realValues[i][j]);
            }
        }
        return s;
    }

    private double[][] initArray(double v) {
        double[][] a = new double[size][size];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                a[i][j] = v;
            }
        }
        return a;
    }

    private void printArray(double[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                System.out.print(df.format(a[i][j]) + "\t");
            }
            System.out.println("");
        }
    }

    public static void main(String argv[]) {

        int runs = 100;

        randomMovement = false;
        lawnmower = false;
        for (int i = 0; i < runs; i++) {
            System.out.println("====================");
            new BoundingFilterTest();
            count = 0;
        }

        for (int i = 0; i < results.length; i++) {
            System.out.println(results[i][0] / runs + "\t" + results[i][1] / runs + "\t" + results[i][2] / runs + "\t" + results[i][3] / runs + "\t");
        }        

    }
    static int count = 0;
    static boolean randomMovement = true;
    static boolean lawnmower = true;

    private class Boat {

        int id = count++;
        int x = 0, y = 0;
        int moveTimeRemaining = 0;
        double currMeasure = 0.0;
        double gradient = 0.0;
        ArrayList<SimplePoint> plan = null;

        void step() {

            /*
             * // Boat movement if (--moveTimeRemaining <= 0) { x +=
             * rand.nextBoolean() ? -1 : 1; y += rand.nextBoolean() ? -1 : 1; x
             * = Math.max(0, Math.min(x, size - 1)); y = Math.max(0, Math.min(y,
             * size - 1)); moveTimeRemaining = moveTime; //
             * System.out.println("Boat " + id + " moved to " + x + " " + y); }
             */

            if (--moveTimeRemaining <= 0) {
                if (randomMovement) {
                    x += rand.nextBoolean() ? -1 : 1;
                    y += rand.nextBoolean() ? -1 : 1;
                    x = Math.max(0, Math.min(x, size - 1));
                    y = Math.max(0, Math.min(y, size - 1));

                } else {
                    if (plan == null || plan.size() == 0) {
                        if (lawnmower) {

                            
                        plan = new ArrayList<SimplePoint>();

                            for (int i = 0; i < size; i++) {
                                for (int j = 0; j < size; j++) {
                                    plan.add(new SimplePoint(i,j));
                                }                                
                            }
                        } else {

                            plan = getBoundedPlan(x, y, currMeasure, lowerBounds, upperBounds);

                        }
                    }
                    SimplePoint p = plan.remove(0);

                    x = p.x;
                    y = p.y;

                }
                moveTimeRemaining = moveTime;
            }

            // Update sensor reading
            double change = 0.0;
            if (currMeasure < realValues[x][y]) {
                change = Math.min(sensorDelta, Math.abs(currMeasure - realValues[x][y]));
            } else if (currMeasure > realValues[x][y]) {
                change = -Math.min(sensorDelta, Math.abs(currMeasure - realValues[x][y]));
            }
            currMeasure += change;
            gradient = change;
            // System.out.println("New curr sensor: " + df.format(currMeasure) + " gradient " + gradient + " real " + realValues[x][y]);

            // update the filters
            averages[x][y] = maWeight * averages[x][y] + (1.0 - maWeight) * currMeasure;
            if (gradient < 0.0) {
                upperBounds[x][y] = Math.min(upperBounds[x][y], currMeasure);
            } else if (gradient > 0.0) {
                lowerBounds[x][y] = Math.max(lowerBounds[x][y], currMeasure);
            }
        }
    }

    /*
     * private class SimplePoint {
     *
     * int x, y; double sensor = 0.0;
     *
     * public SimplePoint(int x, int y) { this.x = x; this.y = y; } }
     */
    private class SimplePoint implements Comparable {

        int x, y;
        int depth = 0;
        double value = 0.0;
        double sensor = 0.0;
        SimplePoint prev = null;

        public SimplePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SimplePoint) {
                SimplePoint op = (SimplePoint) o;
                return op.x == x && op.y == y;
            }
            return super.equals(o);
        }

        public int compareTo(Object t) {
            if (t instanceof SimplePoint) {
                SimplePoint tp = (SimplePoint) t;
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
    final int horizon = 6;
    final double maxChange = 0.1;
    PriorityQueue<SimplePoint> queue = new PriorityQueue<SimplePoint>();

    public ArrayList<SimplePoint> getBoundedPlan(int sx, int sy, double curr, double[][] lower, double[][] upper) {

        ArrayList<SimplePoint> p = new ArrayList<SimplePoint>();

        SimplePoint o = new SimplePoint(sx, sy);
        o.sensor = curr;
        int count = 0;
        queue.offer(o);

        SimplePoint best = o;

        // System.out.println("Starting planning");
        while (!queue.isEmpty() && count < 10000) {
            SimplePoint cp = queue.poll();
            if (cp.value > o.value) {
                best = cp;
                // System.out.println("New best: " + cp);
            }
            ArrayList<SimplePoint> ex = getExpansions(cp.x, cp.y, upper.length);
            for (SimplePoint point : ex) {
                point.value = cp.value + getValue(lower[point.x][point.y], upper[point.x][point.y], cp.sensor);
                // Work out the expectation of the sensor
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

        // System.out.println("Done planning: " + best);

        queue.clear();

        while (best != null) {
            // Don't put in the starting point
            if (best.prev != null) {
                p.add(0, best);
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
    private double getValue(double l, double u, double c, int visits) {
        double d = u - l;
        double m = (u + l) / 2.0;
        double v = Math.abs(m - c);

        // System.out.println("V = " + Math.max(0.0, (d - v)/(double)visits) + " u = " + u + " l =" + l + " d =" + d + " m = " + m + " v= " + v  + " c=" + c);

        /*
         * if (visits > 1) { System.out.println("V = " + Math.max(0.0, (d - v) /
         * (double) visits) + " u = " + u + " l =" + l + " d =" + d + " m = " +
         * m + " v= " + v + " c=" + c); }
         *
         */

        return Math.max(0.0, (d - v) / (double) visits);
    }

    private double getValue(double l, double u, double c) {
        double d = u - l;
        double m = (u + l) / 2.0;
        double v = Math.abs(m - c);
        return Math.max(0.0, d - v);


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
                if (tp.value / tp.depth > value / depth) {
                    return 1;
                } else if (tp.value / tp.depth < value / depth) {
                    return -1;
                } else {
                    return 0;
                }
            }
            return 0;
        }

        public int visits(int vx, int vy) {
            int s = ((vx == x) && (vy == y)) ? 1 : 0;
            if (prev == null) {
                return s;
            } else {
                return s + prev.visits(vx, vy);
            }
        }

        public String toString() {
            return "[" + x + "," + y + "] -> " + prev;
            // return "[" + x + "," + y + "] (value = " + value + ")";
        }
    }

    ArrayList<SimplePoint> getExpansions(int x, int y, int divisions) {
        ArrayList<SimplePoint> ps = new ArrayList<SimplePoint>();
        for (int dx = -1; dx < 2; dx += 1) {
            for (int dy = -1; dy < 2; dy += 1) {
                int nx = x + dx;
                int ny = y + dy;

                // @todo Should staying still be prevented?
                if (nx >= 0 && nx < divisions && ny >= 0 && ny < divisions && !(nx == x && ny == y)) {
                    SimplePoint p = new SimplePoint(nx, ny);
                    ps.add(p);
                }
            }
        }
        return ps;
    }
}
