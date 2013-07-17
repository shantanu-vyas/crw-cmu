package edu.cmu.ri.crw;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

public interface VehicleServer {
	
	public enum SensorType { ANALOG, DIGITAL, TE, UNKNOWN };
	public enum WaypointState { GOING, DONE, CANCELLED, OFF, UNKNOWN };
	public enum CameraState { CAPTURING, DONE, CANCELLED, OFF, UNKNOWN };

	public void addStateListener(VehicleStateListener l);
	public void removeStateListener(VehicleStateListener l);
	public void setState(UtmPose state);
	public UtmPoseWithCovarianceStamped getState();
	
	public void addImageListener(VehicleImageListener l);
	public void removeImageListener(VehicleImageListener l);
	public void startCamera(long numFrames, double interval, int width, int height, ImagingObserver obs);
	public void stopCamera();
	public CompressedImage captureImage(int width, int height);
	public CameraState getCameraStatus();
	
	public void addSensorListener(int channel, VehicleSensorListener l);
	public void removeSensorListener(int channel, VehicleSensorListener l);
	public void setSensorType(int channel, SensorType type);
	public SensorType getSensorType(int channel);
	public int getNumSensors();
	
	public void addVelocityListener(VehicleVelocityListener l);
	public void removeVelocityListener(VehicleVelocityListener l);
	public void setVelocity(Twist velocity);
	public TwistWithCovarianceStamped getVelocity();
	
	public boolean isAutonomous();
	public void setAutonomous(boolean auto);
	
	public void startWaypoint(UtmPose waypoint, String controller, WaypointObserver obs);
	public void stopWaypoint();
	public UtmPose getWaypoint();
	public WaypointState getWaypointStatus();
	
	public void setGains(int axis, double[] gains);
	public double[] getGains(int axis);
}
