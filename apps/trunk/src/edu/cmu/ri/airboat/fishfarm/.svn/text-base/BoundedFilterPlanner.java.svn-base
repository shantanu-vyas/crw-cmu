/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.fishfarm;

import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 *
 * @author pscerri
 */
public class BoundedFilterPlanner {

    static final int horizon = 5;
    static final double maxChange = 1.0;
    static PriorityQueue<Point> queue = new PriorityQueue<Point>();
    
    public static ArrayList<Position> getBoundedPlan(int sx, int sy, double curr, double [][] lower, double [][] upper) {
        ArrayList<Position> p = new ArrayList<Position>();
        
        Point o = new Point(sx, sy);
        o.sensor = curr;
        int count = 0;
        queue.offer(o);
        
        Point best = o;
        
        System.out.println("Starting planning");
        while (!queue.isEmpty() && count < 10) {
            Point cp = queue.poll();
            if (cp.value > o.value) {
                best = cp;
                System.out.println("New best: " + cp);
            }
            ArrayList<Point> ex = getExpansions(cp.x, cp.y, upper.length);
            for (Point point : ex) {                
                point.value = cp.value + getValue(lower[point.x][point.y], upper[point.x][point.y], cp.sensor);
                // Work out the expectation of the sensor
                double change = ((upper[point.x][point.y] - lower[point.x][point.y])/2.0) - cp.sensor;
                if (change < 0) change = Math.max(-maxChange, change);
                if (change > 0) change = Math.min(maxChange, change);
                point.sensor = cp.sensor + change;
                point.prev = cp;
                point.depth = cp.depth + 1;
                queue.offer(point);
                // System.out.println("Offered: " + point);
            }
            count++;
        }              
        System.out.println("Done planning");
        
        queue.clear();
        
        while (best != null) {
            
        }
        
        return p;
    }
    
    /**
     * Intuition here is the bigger the gap the more important, but further curr is from 
     * midpoint of gap the worse.
     * 
     * @todo Take into account multiple visits
     * 
     * @param l
     * @param u
     * @param c
     * @return 
     */
    private static double getValue(double l, double u, double c) {
        double d = u - l;
        double m = (u + l)/2.0;
        double v = Math.abs(m - c);
        return Math.min(0.0, d - v);
    }
    
    static private class Point implements Comparable {
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
                Point op = (Point)o;
                return op.x == x && op.y == y;
            }
            return super.equals(o);
        }

        public int compareTo(Object t) {
            if (t instanceof Point) {
                Point tp = (Point)t;
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
    
    static ArrayList<Point> getExpansions (int x, int y, int divisions) {
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
