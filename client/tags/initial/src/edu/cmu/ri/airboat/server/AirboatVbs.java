/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.server;

import com.flat502.rox.server.XmlRpcServer;
import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.vbs.Vbs2Constants;
import edu.cmu.ri.airboat.vbs.Vbs2Unit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Porto, setPose to set base
 * WGS84
 * 
 * Implements a simulated airboat in a VBS2 server.
 *
 * @see AirboatServer
 *
 * @author pkv
 */
public class AirboatVbs implements AirboatCommand {

    public static final int DEFAULT_RPC_PORT = 5000;

    protected Vbs2Unit _server;

    double[] _waypoint = new double[6];
    double[] _offset = new double[6];

    int _utmZone = 17;
    boolean _utmHemisphereNorth = true;
    
    boolean _isAutonomous = true;
    
    public AirboatVbs(String hostname, double[] position) {

        // Spawn the new vehicle
        _server = new Vbs2Unit(Vbs2Constants.Object.DOUBLEEAGLE_ROV, position);
        _server.connect(hostname);

        // Load up the map origin immediately after spawning
        Vbs2Unit.Origin origin = _server.origin();
        _offset[0] = origin.northing;
        _offset[1] = origin.easting;
        _utmZone = origin.zone;
        _utmHemisphereNorth = (origin.hemisphere == 'N' || origin.hemisphere == 'n');

        // Add initial waypoint to stay in same spot
        _server.waypoints().add(_server.position());
    }

    public double[] getWaypoint() {

        // Copy position of current waypoint to 6D pose structure
        double[] pos = _server.waypoints().get(0);
        double[] rot = new double[3];

        // Convert 6D pose to global frame
        return globalFromLocal(localFromVbs(pos, rot));
    }

    public boolean setWaypoint(double[] loc) {

        // Convert from 6D global pose to 3D local pose
        _server.waypoints().set(0, vbsFromLocal(localFromGlobal(loc)));

        // Begin moving to waypoint
        _server.gotoWaypoint(0);
        return true;
    }

    public boolean isConnected() {
        return _server.isConnected();
    }

    public boolean isAutonomous() {
        return _isAutonomous;
    }

    public double[] getPose() {
        
        double[] pos = _server.position();
        double[] rot = _server.rotation();

        return globalFromLocal(localFromVbs(pos,rot));
    }

    public boolean setPose(double[] pose) {
        if (pose.length != 6)
            throw new IllegalArgumentException("Velocity must be 6-D.");
        
        // Add the inverse of this difference to the offset, to make current
        // pose correspond to specified pose
        double[] oldPose = getPose();
        for (int i = 0; i < _offset.length; ++i) {
            _offset[i] += (pose[i] - oldPose[i]);
        }

        return true;
    }

    public double[] getGyro() {
        // TODO: return fake heading velocity
        return new double[] { 0.0, 0.0, 0.0 };
    }

    public boolean isWaypointComplete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getUTMZone() {
        return _utmZone;
    }

    public boolean isUTMHemisphereNorth() {
        return _utmHemisphereNorth;
    }

    public boolean setUTMZone(int zone, boolean isNorth) {
        _utmZone = zone;
        _utmHemisphereNorth = isNorth;
        return true;
    }

    public byte[] getImage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean saveImage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Converts from local frame to VBS2 coordinate frame (position only)
    protected double[] vbsFromLocal(double[] local) {
        double[] vbsPos = new double[3];

        vbsPos[0] = local[1];
        vbsPos[1] = local[0];
        vbsPos[2] = -local[2];

        return vbsPos;
    }

    // Converts from VBS2 coordinate frame to local frame
    protected double[] localFromVbs(double[] pos, double[] rot) {
        double[] local = new double[6];

        local[0] = pos[1];
        local[1] = pos[0];
        local[2] = -pos[2];

        System.arraycopy(rot, 0, local, 3, rot.length);

        return local;
    }

    // Converts from global frame (UTM) to local frame
    protected double[] localFromGlobal(double[] global) {
        double[] local = new double[global.length];

        for (int i = 0; i < local.length; ++i) {
            local[i] = global[i] - _offset[i];
        }

        return local;
    }

    // Converts from local frame to global (UTM frame)
    protected double[] globalFromLocal(double[] local) {
        double[] global = new double[local.length];
        
        for (int i = 0; i < global.length; ++i) {
            global[i] = local[i] + _offset[i];
        }

        return global;
    }

    public boolean setController(String controlName) {
        // TODO: implement different waypoint behaviors here
        return false;
    }

    public String getController() {
        return "NAVIGATE";
    }

    public String[] getControllers() {
        return new String[] {"NAVIGATE"};
    }

    /**
     * Create an XML-RPC server running an instance of a VBS2 boat.  All boat
     * commands are encapsulated within this server, just like a physical boat.
     *
     * If running multiple boats on the same machine, make sure not to use
     * unique port numbers for each of them.
     *
     * @param vbsServer the host name of the VBS2 server
     * @param port the desired <b>XML-RPC</b> port to use
     * @param startLocation the starting location for the boats in the VBS2 map
     * @return a running XML-RPC server instance
     */
    public static XmlRpcServer startRpcInstance(String vbsServer, int port, double[] startLocation) {

        // Create an AirboatServer instance using VBS2
        AirboatVbs boat = new AirboatVbs(vbsServer, startLocation);
        System.out.println("Name: " + boat._server.name());

        try {
            // Bind this instance to an XML-RPC server, which is returned
            XmlRpcServer _server = new XmlRpcServer(port);
            _server.registerProxyingHandler(null, "^command\\.(.*)", boat);
            _server.start();
            return _server;
        } catch (IOException ex) {
            Logger.getLogger(AirboatVbs.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
