/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.floodtest.*;
import edu.cmu.ri.airboat.general.BufferedImageWithPose;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 *
 * @author pscerri
 */
public class ImageQueue extends PriorityBlockingQueue<BufferedImageWithPose> {

    Area seen = new Area();
    
    @Override
    public boolean offer(BufferedImageWithPose e) {

        Area a = poseToArea(e);

        double s = approxArea(a, 1.0);

        return super.offer(e);
    }

    @Override
    public BufferedImageWithPose poll() {
        return super.poll();
    }

    private Area poseToArea(BufferedImageWithPose e) {
        Area a = null;

        double x = 0.0; // - The X coordinate of the upper-left corner of the arc's framing rectangle.
        double y = 0.0; // - The Y coordinate of the upper-left corner of the arc's framing rectangle.
        double w = 0.0; // - The overall width of the full ellipse of which this arc is a partial section.
        double h = 0.0; // - The overall height of the full ellipse of which this arc is a partial section.
        double start = 0.0; // - The starting angle of the arc in degrees.
        double extent = 0.0; // - The angular extent of the arc in degrees.

        Shape shape = new Arc2D.Double(x, y, w, h, start, extent, Arc2D.CHORD);

        a = new Area(shape);

        return a;
    }
    
    private double approxArea(Area area, double flatness) {
        PathIterator i = area.getPathIterator(identity, flatness);
        return approxArea(i);
    }

    private double approxArea(PathIterator i) {
        double a = 0.0, NaN = Double.NaN;
        double[] coords = new double[6];
        double startX = NaN, startY = NaN;
        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);
        while (! i.isDone()) {
            int segType = i.currentSegment(coords);
            double x = coords[0], y = coords[1];
            switch (segType) {
            case PathIterator.SEG_CLOSE:
                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);
                a += hexArea(segment);
                startX = startY = NaN;
                segment.setLine(NaN, NaN, NaN, NaN);
                break;
            case PathIterator.SEG_LINETO:
                segment.setLine(segment.getX2(), segment.getY2(), x, y);
                a += hexArea(segment);
                break;
            case PathIterator.SEG_MOVETO:
                startX = x;
                startY = y;
                segment.setLine(NaN, NaN, x, y);
                break;
            default:
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            i.next();
        }
        if (Double.isNaN(a)) {
            throw new IllegalArgumentException("PathIterator contains an open path");
        } else {
            return 0.5 * Math.abs(a);
        }
    }

    private static double hexArea(Line2D seg) {
        return seg.getX1() * seg.getY2() - seg.getX2() * seg.getY1();
    }

    private static final AffineTransform identity =
        AffineTransform.getQuadrantRotateInstance(0);
}
