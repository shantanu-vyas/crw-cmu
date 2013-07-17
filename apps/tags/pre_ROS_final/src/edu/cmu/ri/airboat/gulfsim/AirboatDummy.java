/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim;

import com.flat502.rox.server.XmlRpcServer;
import edu.cmu.ri.airboat.gulfsim.tasking.pollution.CentralizedGAPollutionModel;
import edu.cmu.ri.airboat.gulfsim.tasking.pollution.PollutionModel;
import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dummy class used to test XML-RPC and interaction with AirboatController
 * proxy objects.
 *
 * @author pkv
 */
public class AirboatDummy implements AirboatControl, AirboatCommand {

    public static boolean POLLUTION_MODELING_ON = true;
    
    public static final int DEFAULT_RPC_PORT = 5000;
    private Random _rnd = new Random();
    double[] _waypoint = new double[6];
    double[] _state = new double[6];
    double[] _velocity = new double[6];
    int _utmLngZone = 17;
    String _utmLatZone = "N";
    double[][] _gain = new double[6][3];
    boolean _isAutonomous = false;
    boolean _isConnected = true;
    double simpleVelocityGain = 1.0e-4;
    //double simpleVelocityGain = 100.0;
        
    PollutionModel pollutionModel = null;
    CentralizedGAPollutionModel pModel = null;
    
    {
        if (POLLUTION_MODELING_ON) {
            pollutionModel = new PollutionModel(); 
            pModel = new CentralizedGAPollutionModel();
        }
    }
    
    long t = 0;
    
    public AirboatDummy() {
        this(22.0, -95.0);
    }

    public AirboatDummy(double initLat, double initLong) {

        UTMCoord utm = UTMCoord.fromLatLon(Angle.fromDegrees(initLat), Angle.fromDegrees(initLong));

        //System.out.println("Translated " + initLat + " " + initLong + " to " + utm);

        _state[0] = utm.getNorthing();
        _state[1] = utm.getEasting();

        _utmLngZone = utm.getZone();
        _utmLatZone = utm.getHemisphere().contains("North") ? "North" : "South";

        (new Thread()    {

            @Override
            public void run() {
                while (true) {

                    t++;
                    
                    try {
                        double rx = _waypoint[0] - _state[0];
                        if (Math.abs(rx) < Math.abs(_velocity[0])) {
                            _state[0] = _waypoint[0];
                        } else {
                            _state[0] += _velocity[0];
                        }

                        double ry = _waypoint[1] - _state[1];
                        if (Math.abs(ry) < Math.abs(_velocity[1])) {
                            _state[1] = _waypoint[1];
                        } else {
                            _state[1] += _velocity[1];
                        }

                        UTMCoord utmN = UTMCoord.fromUTM(_utmLngZone, "gov.nasa.worldwind.avkey.North", _state[1], _state[0]);
                        Angle lat = utmN.getLatitude();
                        Angle lon = utmN.getLongitude();
                        utmN = UTMCoord.fromLatLon(lat, lon);
                        if (_utmLngZone != utmN.getZone()) {
                            // System.out.println("================ CHANGED " + utmN.getZone() + " " + _utmLngZone);
                            _utmLngZone = utmN.getZone();
                            _state[0] = utmN.getNorthing();
                            _state[1] = utmN.getEasting();
                        }

                        _state[5] = Math.atan2(_velocity[1], _velocity[0]);
                        //System.out.println("Dist: " + rx + " " + ry);

                        if (POLLUTION_MODELING_ON) {
                            double value = pollutionModel.getValue(utmN, t);
                            System.out.println("Pollution reading? " + value);
                            pModel.addObservation(value, utmN, t);
                        }
                                                
                        try {
                            sleep(500);
                        } catch (Exception e) {
                        }

                    } catch (Exception e) {
                        System.out.println("EXCEPTION: " + e + " " + _state[1] + " " + _state[0]);
                    }
                }
            }
        }).start();
    }

    public double[] getVelocity() {
        double[] vel = Arrays.copyOf(_velocity, _velocity.length);
        for (int i = 0; i < vel.length; i++) {
            vel[i] += _rnd.nextGaussian() * 1.0e-5;
        }
        return vel;
    }

    public boolean setVelocity(double[] vel) {
        if (vel.length != _velocity.length) {
            throw new IllegalArgumentException("Velocity must be 6-D.");
        }

        System.arraycopy(vel, 0, _velocity, 0, _velocity.length);
        return true;
    }

    public double[] getVelocityGain(double axis) {
        int idx = (int) axis;
        if (idx < 0 || idx >= 6) {
            throw new IllegalArgumentException("Axis must be in [0,6).");
        }

        return _gain[idx];
    }

    public boolean setVelocityGain(double axis, double kp, double ki, double kd) {
        int idx = (int) axis;
        if (idx < 0 || idx >= 6) {
            throw new IllegalArgumentException("Axis must be in [0,6).");
        }

        _gain[idx][0] = kp;
        _gain[idx][1] = ki;
        _gain[idx][2] = kd;
        return true;
    }

    public boolean isConnected() {
        return _isConnected;
    }

    public boolean isAutonomous() {
        return _isAutonomous;
    }

    public boolean setAutonomous(boolean isAutonomous) {
        _isAutonomous = isAutonomous;
        return true;
    }

    public double[] getWaypoint() {
        return Arrays.copyOf(_waypoint, _waypoint.length);
    }

    public boolean setWaypoint(double[] loc) {
        if (loc.length != _waypoint.length) {
            throw new IllegalArgumentException("Location must be 6-D.");
        }

        /*
        // Translate from UTM to LatLon
        UTMCoord utm = UTMCoord.fromUTM(_utmLngZone, "" + _utmLatZone, loc[0], loc[1]);
        
        _velocity[0] = (utm.getLatitude().degrees - _state[0]);
        _velocity[1] = (utm.getLongitude().degrees - _state[1]);
         */

        _velocity[0] = (loc[0] - _state[0]);
        _velocity[1] = (loc[1] - _state[1]);

        double d = Math.sqrt(_velocity[0] * _velocity[0] + _velocity[1] * _velocity[1]) * simpleVelocityGain;

        //System.out.println("Waypoint now: " + loc[0] + " " + loc[1] + " from " + _state[0] + " " + _state[1] + " moving " + _velocity[0] + " " + _velocity[1]);

        _velocity[0] /= d;
        _velocity[1] /= d;

        //System.out.println("Waypoint now: " + loc[0] + " " + loc[1] + " from " + _state[0] + " " + _state[1] + " moving " + _velocity[0] + " " + _velocity[1]);

        System.arraycopy(loc, 0, _waypoint, 0, _waypoint.length);
        return true;
    }

    public double[] getPose() {

        double[] state = Arrays.copyOf(_state, _state.length);

        /*
        try {
        // UTMCoord utm = UTMCoord.fromUTM(_utmLngZone, "" + _utmLatZone, _state[0], _state[1]);
        UTMCoord utm = UTMCoord.fromLatLon(Angle.fromDegrees(_state[0]), Angle.fromDegrees(_state[1])); 
        // Translate to UTM
        _state[0] = utm.getEasting();
        _state[1] = utm.getNorthing();
        } catch (Exception e) {
        System.out.println("UTM in getPose failed: " + e);
        }
         */

        return state;
    }

    public boolean setPose(double[] state) {
        if (state.length != _state.length) {
            throw new IllegalArgumentException("Velocity must be 6-D.");
        }

        System.arraycopy(state, 0, _state, 0, _state.length);
        return true;
    }

    public double[] getGyro() {
        double[] angles = new double[]{_rnd.nextGaussian(), _rnd.nextGaussian(), _rnd.nextGaussian()};

        angles[0] = Math.min(Math.max(angles[0], -Math.PI), Math.PI);
        angles[1] = Math.min(Math.max(angles[0], -Math.PI), Math.PI);
        angles[2] = Math.min(Math.max(angles[0], -Math.PI), Math.PI);

        return angles;
    }

    public boolean isWaypointComplete() {
        return false;
    }

    public String getUTMLatZone() {
        return _utmLatZone;
    }

    // @deprecated
    public int getUTMLngZone() {
        return _utmLngZone;
    }

    public boolean setUTMZone(int lngZone, String latZone) {
        _utmLatZone = latZone;
        _utmLngZone = lngZone;
        return true;
    }

    public int getUTMZone() {
        return _utmLngZone;
    }

    public boolean isUTMHemisphereNorth() {
        return _utmLatZone.equalsIgnoreCase("N");
    }

    public boolean setUTMZone(int zone, boolean isNorth) {
        _utmLngZone = zone;
        
        if (isNorth) {
            _utmLatZone = "N";
        } else {
            _utmLatZone = "S";
        }
        
        return true;
    }

    
    
    public byte[] getImage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setController(String controlName) {
        // There are no other controllers for the dummy
        return false;
    }

    public String getController() {
        return "DUMMY";
    }

    public String[] getControllers() {
        return new String[]{"DUMMY"};
    }

    public static XmlRpcServer defaultRpcInstance() {
        return rpcInstance(DEFAULT_RPC_PORT);
    }

    public static XmlRpcServer rpcInstance(int port) {
        try {
            XmlRpcServer server = new XmlRpcServer(port);
            server.registerProxyingHandler(null, "^command\\.(.*)", new AirboatDummy());
            server.registerProxyingHandler(null, "^control\\.(.*)", new AirboatDummy());
            server.start();
            return server;
        } catch (IOException ex) {
            Logger.getLogger(AirboatDummy.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting an XML-RPC AirboatDummy on port 5000");
        AirboatDummy.defaultRpcInstance();
    }
}
