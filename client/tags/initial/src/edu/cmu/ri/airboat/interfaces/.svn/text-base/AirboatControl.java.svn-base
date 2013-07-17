package edu.cmu.ri.airboat.interfaces;

/**
 * This is the interface that represents the low-level functionality of the 
 * vehicle onboard controller, used for controller testing and PID tuning.
 * 
 * This interface should be used only for debugging purposes.
 * 
 * @author pkv
 *
 */
public interface AirboatControl {
	
	/**** Re-look these to see if this is the right level of abstraction ****/
	/*** Need to re-think this: getvel is used by the controller, so should be an internal function. On the 
	 * other hand, setVel is used by AirboatServer ... Def not convinced that they should both by public
	 */
	public double[] getVelocity();
	public boolean setVelocity(double[] vel);
	public double[] getVelocityGain(double axis);
	public boolean setVelocityGain(double axis, double kp, double ki, double kd);
	
	// Current waypoint information
	public double[] getPose();
	public double[] getWaypoint();
	
	// Low-level status control
	public boolean isConnected();
	public boolean isAutonomous();
	public boolean setAutonomous(boolean isAutonomous);
}
