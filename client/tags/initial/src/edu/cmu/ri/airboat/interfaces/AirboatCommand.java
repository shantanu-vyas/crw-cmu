package edu.cmu.ri.airboat.interfaces;

/**
 * This is the interface that represents all of the public functionality for the
 * high-level (waypoint) command of the vehicle.  This can be used by a task
 * executor or other higher level module to implement navigation behaviors.
 * 
 * @author pkv
 *
 */
public interface AirboatCommand {
	
	// High-level Waypoint controller 
	/*** I would probably argue that this should not be a part of the server and should be at 
	 * 	a higher level .... In fact all amarino, arduino code should be @ driver level along with
	 * the lowest level PID gains. One step above should be base functionality: navigation etc.
	 * Its navigation level that should be exposed, whereas the PID, arduino, gyro should be abstracted
	 * So you have two services running: server/boat/arduino or what I tend to call robotCntl and
	 * waypoint, obstacle detection, navigation or what I call taskExecution. the innermost loop (robotcntl)
	 * can run as fast we think makes sense 10hz/100hz and so on, whereas the outer loop (taskExec) 
	 * can run slower and comms between the two would be through xml/rpc. Now, if we feel that would 
	 * have too heavy an overhead, wewe can run them on the same service and see if we can spawn off a 
	 * thread ... ---- comments are welcome 
	 */
	
	public double[] getWaypoint();
	public boolean setWaypoint(double[] loc);
	public boolean isWaypointComplete();
	/****************************************************************************************/
	
	// Basic vehicle status
	public boolean isConnected();
	public boolean isAutonomous();
	
	// Access vehicle controller type
	public boolean setController(String controlName);
	public String getController();
	public String[] getControllers();
	
	// Access filtered state information
	public double[] getPose();
	public boolean setPose(double[] state);
	
	// Store UTM zone information for position
	public boolean isUTMHemisphereNorth();
	public int getUTMZone();
	public boolean setUTMZone(int zone, boolean isNorth);
	
	// Camera control functions
	public byte[] getImage();
	public boolean saveImage();
}
