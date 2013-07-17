package edu.cmu.ri.airboat.interfaces;

/**
 * Represents a 6D inertial state estimator that can handle updates from
 * all the different available sensors that yield information about pose.
 * 
 * This filter can be called from multiple sensor threads, so its behavior 
 * must be thread-safe.
 * 
 * @author pkv
 *
 */
public interface AirboatFilter {
	
	/**
	 * Resets the vehicle pose to the specified initial condition at the 
	 * specified initial time.
	 * 
	 * @param pose the specified pose of the vehicle
	 * @param time the current time in milliseconds
	 */
	public void reset(double[] pose, long time);
	
	/**
	 * Gets the current estimate for the pose of the vehicle, given the current
	 * time.  The behavior of this function is not defined if a time is given 
	 * that occurred before the most recent update. 
	 * 
	 * @param time the current time in milliseconds
	 * @return the estimated pose of the vehicle 
	 */
	public double[] pose(long time);
	
	public void gpsUpdate(double northing, double easting, long time);
	public void compassUpdate(double heading, long time);
	public void gyroUpdate(double headingVel, long time);
}
