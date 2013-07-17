package edu.cmu.ri.airboat.interfaces;

/**
 * Defines an interface for a control object that has access to the low-level
 * control and sensor interfaces to the boat.  This object will be called
 * periodically to provide an updated command to the vehicle.
 * 
 * @author pkv
 *
 */
public interface AirboatController {

	/**
	 * Update function containing the control and sensor interfaces to the boats
	 * and the time since the last update was called.
	 * 
	 * @param control low-level vehicle control interface
	 * @param sensor low-level vehicle sensor interface
	 * @param dt elapsed time since last controller call (in seconds)
	 */
	public void update(AirboatControl control, AirboatSensor sensor, double dt);
}
