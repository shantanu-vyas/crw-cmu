/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim;

import com.flat502.rox.client.XmlRpcClient;
import edu.cmu.ri.airboat.gulfsim.UtmUtils.UTM;
import edu.cmu.ri.airboat.gulfsim.tasking.Role;
import edu.cmu.ri.airboat.gulfsim.tasking.RoleAgent;
import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
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
    private double atWaypointTolerance = 30.0;
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
    private UTMCoord currUtm = null;
    private RoleAgent currRole = null;
    private int lngZone = -1;
    // Simulated
    private double fuelLevel = 1.0;
    private double fuelUsageRate = 1.0e-2;

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

        lngZone = commander.getUTMZone();

        String latZone = "N"; // "" + commander.getUTMLatZone();

        // System.out.println("Got latZone: " + latZone);

        while (true) {

            try {
                double[] pose = null;

                pose = commander.getPose();
                int lz = commander.getUTMZone();

                //System.out.println("Pos: " + pose[0] + ", " + pose[1] + " " + lngZone + " " + latZone);
                //System.out.println("lz = " + lz);                

                UTMCoord c = UTMCoord.fromUTM(lz, "gov.nasa.worldwind.avkey.North", pose[1], pose[0]);
                Position p = Position.fromDegrees(c.getLatitude().degrees, c.getLongitude().degrees, 0.0);

                currLoc = p;
                currUtm = c;

                if (ownMarkers.size() > 0) {
                    ownMarkers.get(0).setPosition(p);
                } else {
                    Marker marker = new BoatMarker(this, p, new BasicMarkerAttributes(material, BasicMarkerShape.ORIENTED_CYLINDER_LINE, 0.9));
                    markers.add(marker);
                    ownMarkers.add(marker);
                }

                proxyManager.redraw();

                if ((currWaypoint != null && at(p, currWaypoint)) || lz != lngZone) {

                    if (lz != lngZone) {
                        System.out.println("XXXX Change of zone " + lngZone + " to " + lz);
                    }
                    lngZone = lz;
                    plan();
                }

            } catch (Exception e) {
                System.out.println("Failed to update position: " + e);
            }

            try {
                // @todo make this configurable
                sleep(3000);
            } catch (Exception e) {
            }

            // Simulating fuel use
            fuelLevel -= fuelUsageRate;
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

    public RoleAgent getCurrRole() {
        return currRole;
    }

    public Position getCurrWaypoint() {
        return currWaypoint;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public boolean offerRole(RoleAgent ra) {

        // @todo Consider rejecting ... 

        roles.add(ra);

        System.out.println("Proxy offered role: " + ra);

        plan();
        return true;
    }

    private void plan() {
        if (currUtm != null && roles.size() > 0) {
            RoleAgent ra = roles.get(0);
            System.out.println("Proxy dealing with role: " + ra);
            if (currLoc != null && at(ra.getRole().getLocation(), currLoc)) {
                if (ra.canComplete()) {
                    ra.done();
                    roles.remove(ra);
                    plan();
                    System.out.println("Role completed");
                }
            } else if (ra != currRole) {
                setWaypoint(ra.getRole().getLocation());
            }
        } else {
            currWaypoint = null;
            // @todo Do we need to tell the airboat not to worry about the waypoint any more?
        }
    }

    private boolean at(Position p1, Position p2) {
        UTMCoord utm1 = UTMCoord.fromLatLon(p1.latitude, p1.longitude);
        UTMCoord utm2 = UTMCoord.fromLatLon(p2.latitude, p2.longitude);

        // @todo This only really works for short distances
        double dx = utm1.getEasting() - utm2.getEasting();
        double dy = utm1.getNorthing() - utm2.getNorthing();

        double dist = Math.sqrt(dx * dx + dy * dy);

        //System.out.println("Dist to waypoint now: " + dist);

        if (utm1.getHemisphere().equalsIgnoreCase(utm2.getHemisphere()) && utm1.getZone() == utm2.getZone()) {
            return dist < atWaypointTolerance;
        } else {
            return false;
        }

    }

    void setWaypoint(Position targetPos) {
        double[] loc = new double[6];

        UTMCoord utm = UTMCoord.fromLatLon(targetPos.latitude, targetPos.longitude);

        if (utm.getZone() != currUtm.getZone()) {
            UTM adjUTM = UtmUtils.convertZone(currUtm, utm);
            loc[0] = adjUTM.northing;
            loc[1] = adjUTM.easting;
        } else {
            loc[0] = utm.getNorthing();
            loc[1] = utm.getEasting();
        }

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

    public ArrayList<Role> getRoles() {
        ArrayList<Role> rs = new ArrayList<Role>();

        for (RoleAgent ra : roles) {
            rs.add(ra.getRole());
        }

        return rs;
    }
}
