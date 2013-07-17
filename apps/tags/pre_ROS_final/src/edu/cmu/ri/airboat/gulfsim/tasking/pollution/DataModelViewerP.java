/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.gulfsim.UtmUtils;
import edu.cmu.ri.airboat.gulfsim.tasking.pollution.DataModel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author pscerri
 */
public class DataModelViewerP extends JPanel {

    private DataModel model;
    private final UTMCoord ll;
    private final UTMCoord ur;
    private Dimension minSize = new Dimension(500, 500);

    public DataModelViewerP(DataModel model, UTMCoord ll, UTMCoord ur) {
        this.model = model;
        this.ll = ll;
        this.ur = ur;
    }

    public void setModel(DataModel model) {
        this.model = model;
        repaint();
    }

    /**
     * This all assumes same zone ... I think this is fixed (Paul)
     * @param g 
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        double divisions = 100.0;
        double valueScale = 200.0;
        g2.scale(getSize().width / divisions, getSize().height / divisions);

        // Assume a square
        double h = UtmUtils.dist(ur, ll);
        double s = Math.sqrt(h*h/2);
        
        /*
        double dx = (ur.getEasting() - ll.getEasting())/divisions;
        double dy = (ur.getNorthing() - ll.getNorthing())/divisions;
         */
        
        double dx = s / divisions;
        double dy = s / divisions;
        
        System.out.println("dx = " + dx + " dy = " + dy);
        
        for (int i = 0; i < divisions; i++) {
            for (int j = 0; j < divisions; j++) {
                try {
                    UTMCoord c = UTMCoord.fromUTM(ll.getZone(), ll.getHemisphere(), ll.getEasting() + (dx * j), ll.getNorthing() + (dy * i));
                    double v = Math.min(1.0, valueScale * model.getValue(c, 500));

                    float r = 0.0f, gr = 0.0f, b = 0.0f;
                    if (v < 0.5) {
                        gr = 1.0f - (float) (2.0f * v);
                    } else {
                        r = (float) ((v - 0.5) * 2.0);
                    }
                    Color color = new Color(r, gr, b);

                    //System.out.println("V = " + v + " R = " + r + " G = " + gr);
                    g2.setColor(color);
                    g2.fillRect(j, (int)(divisions - i - 1), 1, 1);
                } catch (Exception e) {
                    System.out.println("UTM issue? " + e);
                    break;
                }
            }
        }
        
    }

    @Override
    public void setMinimumSize(Dimension minimumSize) {
        super.setMinimumSize(minSize);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(minSize);
    }
}
