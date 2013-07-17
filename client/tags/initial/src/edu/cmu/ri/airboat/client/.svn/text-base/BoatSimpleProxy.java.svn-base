/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client;

import com.flat502.rox.client.XmlRpcClient;
import edu.cmu.ri.airboat.client.tasking.RoleAgent;
import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @todo Need a flag for autonomous or under human control
 * 
 * @author pscerri
 */
public class BoatSimpleProxy extends Thread {

    // @todo Needs to be configurable
    private final double atWaypointDist = 1.0e-4;
    private final String ipAddrStr;
    private final ArrayList<Marker> markers;
    private final ArrayList<Marker> ownMarkers = new ArrayList<Marker>();
    private final Material material;
    final AtomicBoolean isConnected = new AtomicBoolean(false);
    private AirboatControl controller = null;
    private AirboatCommand commander = null;
    // Tasking stuff
    private ArrayList<RoleAgent> roles = new ArrayList<RoleAgent>();
    private ProxyManager proxyManager = new ProxyManager();
    private Position currWaypoint = null;
    private Position currLoc = null;
    private RoleAgent currRole = null;

    public BoatSimpleProxy(String ipAddrStr, ArrayList<Marker> markers, Material material) {
        this.ipAddrStr = ipAddrStr;
        //this.ipAddrStr = "128.239.249.154";
        // Try to open this URL as XML-RPC server
        try {
            final XmlRpcClient client = new XmlRpcClient(new URL(ipAddrStr));
            controller = (AirboatControl) client.proxyObject("control.", AirboatControl.class);
            commander = (AirboatCommand) client.proxyObject("command.", AirboatCommand.class);

            isConnected.set(true);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // commander.isConnected();

        this.markers = markers;
        this.material = material;

        start();

        System.out.println("Created boat proxy: " + this);
    }

    public void run() {

        // @todo Be smarter about updating these
        System.out.println("Starting boat proxy loop");      
        int lngZone = commander.getUTMZone();
        System.out.println("Got lngZone: " + lngZone);
        String latZone = (commander.isUTMHemisphereNorth() ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South");
        System.out.println("Got latZone: " + latZone);

        while (true) {
            try {
                double[] pose = null;
                pose = commander.getPose();

                //System.out.println("Pos: " + pose[0] + ", " + pose[1] + " " + lngZone + " " + latZone);

                UTMCoord c = UTMCoord.fromUTM(lngZone, latZone, pose[1], pose[0]);
                Position p = Position.fromDegrees(c.getLatitude().degrees, c.getLongitude().degrees, 0.0);
                
                currLoc = p;
                Marker marker = new BoatMarker(this, p, new BasicMarkerAttributes(material, BasicMarkerShape.ORIENTED_CYLINDER_LINE, 0.9));

                markers.add(marker);
                ownMarkers.add(marker);

                int i = 0;
                while (i < ownMarkers.size()) {
                    Marker m = ownMarkers.get(i);
                    MarkerAttributes a = m.getAttributes();
                    double o = a.getOpacity() * 0.66;
                    if (o > 0.1) {
                        a.setOpacity(o);
                        i++;
                    } else {
                        markers.remove(m);
                        ownMarkers.remove(m);
                    }
                }

                proxyManager.redraw();
            } catch (Exception e) {
                System.out.println("Failed to update position: " + e);
            }

            try {
                // @todo make this configurable
                sleep(3000);
            } catch (Exception e) {
            }
        }
    }

    public AirboatControl getController() {
        return controller;
    }

    public String getIpAddrStr() {
        return ipAddrStr;
    }

    public AirboatCommand getCommander() {
        return commander;
    }

    @Override
    public String toString() {
        return ipAddrStr;
    }

    public boolean offerRole(RoleAgent ra) {
        roles.add(ra);

        System.out.println("Proxy offered role: " + ra);

        plan();
        return true;
    }

    private void plan() {
        if (roles.size() > 0) {
            RoleAgent ra = roles.get(0);
            System.out.println("Proxy dealing with role: " + ra);
            if (currLoc != null && at(ra.getRole().getLocation(), currLoc)) {
                if (ra.canComplete()) {
                    ra.done();
                    roles.remove(ra);
                }
            } else if (ra != currRole) {
                setWaypoint(ra.getRole().getLocation());
            }
        }
    }

    private boolean at(Position p1, Position p2) {
        Angle dy = p1.latitude.angularDistanceTo(p2.latitude);
        Angle dx = p1.longitude.angularDistanceTo(p2.longitude);

        // @todo This only really works for short distances
        double dist = Math.sqrt(dx.degrees * dx.degrees + dy.degrees * dy.degrees);

        return false;
    }

    void setWaypoint(Position targetPos) {
        double[] loc = new double[6];

        UTMCoord utm = UTMCoord.fromLatLon(targetPos.latitude, targetPos.longitude);
        loc[0] = utm.getNorthing();
        loc[1] = utm.getEasting();
        loc[2] = 0.0;
        loc[3] = 0.0;
        loc[4] = 0.0;
        loc[5] = 0.0;

        //commander.setUTMZone(utm.getZone(), (char)(utm.getHemisphere().contains("North") ? 'N' : 'S'));

        //System.out.println("Sending waypoint:" + loc + " (UTM: " + utm + ") for: " + this);
        //System.out.println("Sending waypoint:" + utm.getEasting() + " " + utm.getNorthing() + " " + utm.getZone() + " " + utm.getHemisphere());

        boolean ret = commander.setWaypoint(loc);
        if (ret) {
            // System.out.println("Waypoint set");
        } else {
            System.out.println("Setting waypoint failed");
        }

        currWaypoint = targetPos;
    }
}
