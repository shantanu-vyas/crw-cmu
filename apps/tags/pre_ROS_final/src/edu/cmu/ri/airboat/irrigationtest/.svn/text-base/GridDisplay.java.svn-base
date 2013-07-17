/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

import edu.cmu.ri.airboat.irrigationtest.AutonomyController.AutonomyEventListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import javax.swing.JPanel;

/**
 *
 * @author pscerri
 */
public class GridDisplay extends JPanel implements AutonomyEventListener {

    AutonomyController autonomy = null;
    private Dimension minSize = new Dimension(500, 500);
    private boolean showMean = true;
    // Hack version for keeping mean value for drawing
    double mean = 0.0;
    double maxExtent = 1.0;
    DecimalFormat df = new DecimalFormat("#.###");

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        double width = getWidth();
        double height = getHeight();

        double dx = width / autonomy.getWidth();
        double dy = height / autonomy.getHeight();

        // System.out.println("Got width as " + autonomy.getWidth() + ", height " + autonomy.getHeight());

        if (autonomy == null) {
            g.drawString("No autonomy", 100, 100);
        } else {
            g2.clearRect(0, 0, (int) width, (int) height);

            AutonomyController.LocationInfo[][] model = autonomy.getLocInfo();

            int bx = (int) (width / model.length);
            int by = (int) (height / model[0].length);

            double prevMean = mean;
            double prevMaxExtent = maxExtent;

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

                        mean = (0.99 * mean) + (0.01 * v);

                        if (maxExtent < Math.abs(mean - v)) {
                            maxExtent *= 1.01;
                        } else {
                            maxExtent *= 0.999;
                        }

                        double alpha = Math.abs((prevMean - v) / prevMaxExtent);
                        alpha = Math.min(1.0, alpha);

                        if (v < prevMean) {
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

            g2.setColor(Color.black);


            // Draw boats
            for (Integer boat : autonomy.getLocations().keySet()) {
                double[] pose = autonomy.getBoatLocation(boat);

                int x = (int) (pose[0] * dx);
                int y = (int) (pose[1] * dy);

                // System.out.println("Drawing at " + x + " " + y + " based on " + pose[1] + " " + dy + " " + height + " " + autonomy.getHeight());

                g2.drawString("B" + boat, x, (int) (height - y));

                double[][] plan = autonomy.getPlans().get(boat);

                if (plan != null) {
                    
                    // g2.drawLine(x, (int)(height - y), (int) (plan[0][0] * dx), (int) (height - plan[0][1] * dy));
                    
                    for (int i = 1; i < plan.length; i++) {
                        double[] ds = plan[i - 1];
                        double[] de = plan[i];
                        g2.drawLine((int) (ds[0] * dx), (int) (height - ds[1] * dy), (int) (de[0] * dx), (int) (height - de[1] * dy));
                    }
                }
            }

            // Draw obstacles
            for (AutonomyController.Obstacle o : autonomy.getObstacles()) {
                g2.fillRect((int) (o.r.x * dx), (int) (height - (o.r.y * dy) - (o.r.height * dy)), (int) (o.r.width * dx), (int) (o.r.height * dy));
            }
        }
    }

    public void setAutonomy(AutonomyController autonomy) {
        this.autonomy = autonomy;
        autonomy.addListener(this);
    }

    @Override
    public void setMinimumSize(Dimension minimumSize) {
        super.setMinimumSize(minSize);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(minSize);
    }

    public void setShowMean(boolean showMean) {
        this.showMean = showMean;
    }

    public void changed() {
        repaint();
    }
}
