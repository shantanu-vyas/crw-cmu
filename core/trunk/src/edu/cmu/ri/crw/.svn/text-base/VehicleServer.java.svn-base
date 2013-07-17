package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;


/**
 * Standard interface for controlling a vehicle.  Methods in this interface
 * are assumed to block until completion, or throw a runtime exception upon
 * failure.
 *
 * For asynchronous operation, see AsyncVehicleServer.
 *
 * @see AsyncVehicleServer
 *
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public interface VehicleServer {
	
	public enum SensorType { ANALOG, DIGITAL, TE, DEPTH, WATERCANARY, BATTERY, UNKNOWN };
	public enum WaypointState { GOING, PAUSED, DONE, CANCELLED, OFF, UNKNOWN };
	public enum CameraState { CAPTURING, DONE, CANCELLED, OFF, UNKNOWN };

	public void addPoseListener(PoseListener l);
	public void removePoseListener(PoseListener l);
	public void setPose(UtmPose pose);
	public UtmPose getPose();
	
	public void addImageListener(ImageListener l);
	public void removeImageListener(ImageListener l);
        public byte[] captureImage(int width, int height);
        
        public void addCameraListener(CameraListener l);
	public void removeCameraListener(CameraListener l);
	public void startCamera(int numFrames, double interval, int width, int height);
	public void stopCamera();
	public CameraState getCameraStatus();
	
	public void addSensorListener(int channel, SensorListener l);
	public void removeSensorListener(int channel, SensorListener l);
	public void setSensorType(int channel, SensorType type);
	public SensorType getSensorType(int channel);
	public int getNumSensors();
	
	public void addVelocityListener(VelocityListener l);
	public void removeVelocityListener(VelocityListener l);
	public void setVelocity(Twist velocity);
	public Twist getVelocity();
	
        public void addWaypointListener(WaypointListener l);
	public void removeWaypointListener(WaypointListener l);
	public void startWaypoints(UtmPose[] waypoint, String controller);
	public void stopWaypoints();
	public UtmPose[] getWaypoints();
	public WaypointState getWaypointStatus();
	
        public boolean isConnected();
        public boolean isAutonomous();
	public void setAutonomous(boolean auto);
        
	public void setGains(int axis, double[] gains);
	public double[] getGains(int axis);
}
