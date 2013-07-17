/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.enduser.DataRepository.ImageType;
import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.airboat.general.ProxyManager;
import edu.cmu.ri.airboat.general.ProxyManagerListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.UtmPose;
import gov.nasa.worldwind.geom.LatLon;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author pscerri
 */
public class DataManager extends JPanel implements ProxyManagerListener {

    private final ArrayList<BoatProxy> proxies;
    LatLon min = LatLon.ZERO, max = LatLon.ZERO;
    private long refreshRate = 1000;
    public DataRepository repo = new DataRepository(min, max);

    public DataManager() {
        this.proxies = new ArrayList<BoatProxy>();

        ProxyManager pm = new ProxyManager();
        for (BoatProxy boatProxy : pm.getAll()) {
            proxyAdded(boatProxy);
        }
        pm.addListener(this);
        
        setDoubleBuffered(true);

        (new Thread() {
            public void run() {
                while (true) {
                    try {
                        sleep(refreshRate);
                    } catch (InterruptedException e) {
                    }
                    repaint();
                }
            }
        }).start();

    }
    final int boatRadius = 5;

    @Override
    public void paint(Graphics grphcs) {
        Graphics2D g = (Graphics2D) grphcs;

        g.clearRect(0, 0, getWidth(), getHeight());
        // @todo Set index of interest
        g.drawImage(repo.makeBufferedImage(), 0, 0, getWidth(), getHeight(), null, null);

        /*
         for (BoatProxy bp : proxies) {
         LatLon ll = bp.getLatLon();
         if (ll != null) {
         int x = lonToScreenCoord(ll.longitude.degrees);
         int y = latToScreenCoord(ll.latitude.degrees);

         // System.out.println(ll + " translated to " + x + ", " + y);

         g.setColor(bp.getColor());
         g.fillOval(x - boatRadius, y - boatRadius, 2 * boatRadius, 2 * boatRadius);
                
         double angle = bp.getHeading();
                
         if (angle < 0.0) {
         angle = Math.PI * 2 + angle;
         }
                
         int dx = (int)(20.0 * Math.cos(angle));
         int dy = (int)(20.0 * Math.sin(angle));
                
         //
         //if (angle > 0.0 && angle < Math.PI) {
         //    dy = -dy;
         //}                
         //if (angle > Math.PI/2.0 && angle < 1.5 * Math.PI) {
         //    dx = -dx;
         //}
                
         // System.out.println(angle + " to " + dx + ", " + dy);
                
         g.drawLine(x, y, x + dx, y - dy);                
         }
         ArrayList<Position> plan = bp.getCurrPlan();
         if (plan != null && plan.size() > 1) {
         Position curr = plan.get(0);
         for (int i = 1; i < plan.size(); i++) {
         Position next = plan.get(i);
         int x1 = lonToScreenCoord(curr.longitude.degrees);
         int y1 = latToScreenCoord(curr.latitude.degrees);
                    
         int x2 = lonToScreenCoord(next.longitude.degrees);
         int y2 = latToScreenCoord(next.latitude.degrees);
                    
         g.drawLine(x1, y1, x2, y2);
                    
         curr = next;
         }
         }
         }
         */
    }

    public void setImgType(ImageType imgType) {
        repo.setImgType(imgType);
    }

    public Dimension getPreferredSize() {
        return new Dimension(700, 500);
    }

    private int latToScreenCoord(double lat) {
        double latDiff = max.latitude.degrees - min.latitude.degrees;
        double dy = (double) getHeight() / latDiff;
        double fromMin = lat - min.latitude.degrees;
        return getHeight() - (int) (dy * fromMin);
    }

    private int lonToScreenCoord(double lon) {
        double lonDiff = max.longitude.degrees - min.longitude.degrees;
        double dx = (double) getWidth() / lonDiff;
        double fromMin = lon - min.longitude.degrees;
        return (int) (dx * fromMin);
    }

    public void addData(BoatProxy proxy, SensorData sd, UtmPose pose) {
        // System.out.println("Data manager got it");
        repo.addData(proxy, sd, pose);
    }

    public double setUpperBound(double u) {
        return repo.setUpperFilterBound(u);
    }

    public double setLowerBound(double l) {
        return repo.setLowerFilterBound(l);
    }

    public void setExtent(LatLon min, LatLon max) {
        System.out.println("Extent set: " + min + " " + max);
        this.min = min;
        this.max = max;

        repo.setMaxs(max);
        repo.setMins(min);
    }

    public void proxyAdded(final BoatProxy bp) {
        proxies.add(bp);
        for (int i = 0; i < 6; i++) {
            bp.addSensorListener(i, new SensorListener() {
                public void receivedSensor(SensorData sd) {
                    addData(bp, sd, bp.getPose());
                }
            });
        }
    }
}
