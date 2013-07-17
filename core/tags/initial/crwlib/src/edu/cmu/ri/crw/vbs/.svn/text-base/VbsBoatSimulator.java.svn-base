package edu.cmu.ri.crw.vbs;

import java.util.logging.Logger;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.WaypointObserver;
import edu.cmu.ri.crw.vbs.ImageServerLink.ImageEvent;
import edu.cmu.ri.crw.vbs.ImageServerLink.ImageEventListener;

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
    UtmPose _waypoint = null;
    UtmPose _offset = new UtmPose();
    
    private volatile boolean _isCapturing = false;
	private volatile boolean _isNavigating = false;
	
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
				sendImage(toCompressedImage(640, 480, _imageServer.getDirectImage()));
			}
		});

        // Load up the map origin immediately after spawning
        Vbs2Unit.Origin origin = _vbsServer.origin();
        _offset.pose.position.y = origin.northing;
        _offset.pose.position.x = origin.easting;
        _offset.utm.zone = (byte)origin.zone;
        _offset.utm.isNorth = (origin.hemisphere == 'N' || origin.hemisphere == 'n');

        // Add initial waypoint to stay in same spot
        _vbsServer.waypoints().add(_vbsServer.position());
    }

	@Override
	public CompressedImage captureImage(int width, int height) {
		logger.info("Took image @ (" + width + " x " + height + ")");
		_imageServer.takePicture();
		return toCompressedImage(640, 480, _imageServer.getDirectImage());  // TODO: fix image size guessing here
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
	public UtmPoseWithCovarianceStamped getState() {
		UtmPoseWithCovarianceStamped poseMsg = new UtmPoseWithCovarianceStamped();
		double[] pos = _vbsServer.position();
		double[] rot = _vbsServer.rotation();
		
		poseMsg.pose.pose.pose.position.x = pos[0];
		poseMsg.pose.pose.pose.position.y = pos[1];
		poseMsg.pose.pose.pose.position.z = pos[2];
		poseMsg.pose.pose.pose.orientation = QuaternionUtils.fromEulerAngles(rot[0], rot[1], rot[2]);
		
		return poseMsg;
	}

	@Override
	public void setState(UtmPose state) {
		logger.info("Ignored setState: " + state);
	}
	
	@Override
	public TwistWithCovarianceStamped getVelocity() {
		return new TwistWithCovarianceStamped();
	}
	
	@Override
	public void setVelocity(Twist velocity) {
		logger.info("Ignored setVelocity: " + velocity);
	}

	@Override
	public void startCamera(final long numFrames, final double interval, final int width,
			final int height, final ImagingObserver obs) {
		
		final long captureInterval = (long)(interval * 1000.0);
		_isCapturing = true;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int iFrame = 0;
				
				while (_isCapturing && (iFrame < numFrames)) {

					// Every so often, take a picture 
					_imageServer.takePicture();
					iFrame++;
					
					// Alert observer that status might have changed
					if (obs != null)
						obs.imagingUpdate(CameraState.CAPTURING);
					
					// Wait for a while
					try { 
						Thread.sleep(captureInterval); 
					} catch (InterruptedException ex) {
						obs.imagingUpdate(CameraState.CANCELLED);
						_isCapturing = false;
						return;
					}
				}
				
				if (_isCapturing) {
					_isCapturing = false;
					obs.imagingUpdate(CameraState.DONE);
				} else {
					obs.imagingUpdate(CameraState.CANCELLED);
				}
			}
		}).start();
		
	}

	@Override
	public void stopCamera() {
		_isCapturing = false;
	}
	
	@Override
	public void startWaypoint(final UtmPose waypoint, final String controller, final WaypointObserver obs) {
		
		// Tell VBS boat to go to specified waypoint
		double[] dest = new double[3];
		dest[0] = waypoint.pose.position.x;
		dest[1] = waypoint.pose.position.y;
		dest[2] = waypoint.pose.position.z;
		
		// TODO: handle different UTM zones
		_vbsServer.waypoints().set(0, dest);
        _vbsServer.gotoWaypoint(0);
        
		// Spawn monitoring thread
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// Loop until stopped or waypoint is reached
				while (_isNavigating && getWaypointStatus() != WaypointState.DONE) {
					
					// Alert that there might be a status change
					if (obs != null)
						obs.waypointUpdate(WaypointState.GOING);
					
					// Wait for a while
					try { 
						Thread.sleep(500); 
					} catch (InterruptedException ex) {
						obs.waypointUpdate(WaypointState.CANCELLED);
						_isNavigating = false;
						return;
					}
				}
				
				if (_isNavigating) {
					obs.waypointUpdate(WaypointState.DONE);
					_isNavigating = false;
				} else {
					obs.waypointUpdate(WaypointState.CANCELLED);
				}
				
			}
		}).start();		
	}

	@Override
	public void stopWaypoint() {

		// Tell VBS2 to stop by setting waypoint to current position
        _vbsServer.waypoints().set(0, _vbsServer.position());
        _vbsServer.gotoWaypoint(0);
		
		_isNavigating = false;
	}
	
	@Override
	public WaypointState getWaypointStatus() {
		
		// Copy position of current waypoint
        double[] wpt = _vbsServer.waypoints().get(0);
        double[] pos = _vbsServer.position();
        
        // Measure distance to waypoint 
        double dx = wpt[0] - pos[0];
        double dy = wpt[1] - pos[1];
        double dz = wpt[2] - pos[2];
        
        double d = dx*dx + dy*dy + dz*dz;

        // Use distance threshold to determine if waypoint is complete
        if (d > 0.5) {
        	return WaypointState.GOING;
        } else {
        	return WaypointState.DONE;
        }       		
	}

	@Override
	public UtmPose getWaypoint() {
		
		// Get current waypoint from list
		double[] dest = _vbsServer.waypoints().get(0);
		
		// Pack as ROS data structure
		UtmPose waypoint = new UtmPose();
		waypoint.utm = _offset.utm.clone();
		waypoint.pose.position.x = dest[0];
		waypoint.pose.position.y = dest[1];
		waypoint.pose.position.z = dest[2];
		
		// TODO Auto-generated method stub
		return waypoint;
	}

	@Override
	public CameraState getCameraStatus() {
		if(this._isCapturing)
			return CameraState.CAPTURING;
		else
			return CameraState.OFF;
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
	public void resetLog() {
		// No Sensor data required...yet.
		
	}
}