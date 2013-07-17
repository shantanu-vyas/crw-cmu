package edu.cmu.ri.crw.vbs;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.vbs.ImageServerLink.ImageEvent;
import edu.cmu.ri.crw.vbs.ImageServerLink.ImageEventListener;
import robotutils.Pose3D;

/**
 * Implements a simulated vehicle in a VBS2 server.
 *
 * @see VehicleServer
 *
 * @author pkv
 */
public class VbsBoatSimulator extends AbstractVehicleServer {

    private static final Logger logger = Logger.getLogger(VbsBoatSimulator.class.getName());
    public static final int DEFAULT_RPC_PORT = 5000;
    protected final Vbs2Unit _vbsServer;
    protected final ImageServerLink _imageServer;
    UtmPose[] _waypoints = new UtmPose[0];
    UtmPose _offset = new UtmPose();
    
    public VbsBoatSimulator(String vbsServerName, double[] position) {

        // Spawn the new vehicle
        _vbsServer = new Vbs2Unit(Vbs2Constants.Object.DOUBLEEAGLE_ROV, position);
        _vbsServer.connect(vbsServerName);

        // Connect an imageserver to the same address
        _imageServer = new ImageServerLink(vbsServerName, 5003);
        _imageServer.connect();
        _imageServer.addImageEventListener(new ImageEventListener() {

            @Override
            public void receivedImage(ImageEvent evt) {
                sendImage(_imageServer.getDirectImage());
            }
        });

        // Load up the map origin immediately after spawning
        Vbs2Unit.Origin origin = _vbsServer.origin();
        _offset.pose = new Pose3D(origin.easting, origin.northing, 0.0, 0.0, 0.0, 0.0);
        _offset.origin = new Utm(origin.zone, (origin.hemisphere == 'N' || origin.hemisphere == 'n'));

        // Add initial waypoint to stay in same spot
        _vbsServer.waypoints().add(_vbsServer.position());
    }

    @Override
    public byte[] captureImage(int width, int height) {
        logger.log(Level.INFO, "Took image @ ({0}x{1})", new Object[]{width, height});
        _imageServer.takePicture();
        return _imageServer.getDirectImage();
    }

    @Override
    public int getNumSensors() {
        return 0;
    }

    @Override
    public SensorType getSensorType(int channel) {
        return null;
    }

    @Override
    public void setSensorType(int channel, SensorType type) {
        // Do nothing
    }

    @Override
    public UtmPose getPose() {
        UtmPose poseMsg = new UtmPose();
        double[] pos = _vbsServer.position();
        double[] rot = _vbsServer.rotation();

        poseMsg.pose = new Pose3D(pos[0], pos[1], pos[2], rot[0], rot[1], rot[2]);
        return poseMsg;
    }

    @Override
    public void setPose(UtmPose state) {
        logger.log(Level.INFO, "Ignored setState: {0}", state);
    }

    @Override
    public Twist getVelocity() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void setVelocity(Twist velocity) {
        logger.log(Level.INFO, "Ignored setVelocity: {0}", velocity);
    }

    @Override
    public void startCamera(final int numFrames, final double interval, final int width, final int height) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void stopCamera() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void startWaypoints(final UtmPose[] waypoint, final String controller) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void stopWaypoints() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WaypointState getWaypointStatus() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UtmPose[] getWaypoints() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CameraState getCameraStatus() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isAutonomous() {
        return true;
    }

    @Override
    public void setAutonomous(boolean auto) {
        // This implementation does not support non-autonomy!
    }

    @Override
    public boolean isConnected() {
        return (_vbsServer.isConnected() && _imageServer.isConnected());
    }
}