package edu.cmu.ri.airboat.server;

import edu.cmu.ri.airboat.interfaces.AirboatControl;
import edu.cmu.ri.airboat.interfaces.AirboatController;
import edu.cmu.ri.airboat.interfaces.AirboatSensor;

/**
 * A library of available navigation controllers that are accessible through
 * the high-level API.
 * 
 * @author pkv
 *
 */
public enum AirboatControllerLibrary {
	
	/**
	 * This controller turns the boat around until it is facing the waypoint, 
	 * then drives roughly in an arc towards the waypoint.  When it gets within
	 * a certain range, it will cut power to the boat entirely.
	 */
	POINT_AND_SHOOT(new AirboatController() {
		
		@Override
		public void update(AirboatControl control, AirboatSensor sensor, double dt) {
			
			// Get the position of the vehicle and the waypoint 
			double[] pose = control.getPose();
			double[] waypoint = control.getWaypoint();
			
			// Compute the distance and angle to the waypoint
			double distance = Math.sqrt( (waypoint[0] - pose[0]) * (waypoint[0] - pose[0])
										+ (waypoint[1] - pose[1]) * (waypoint[1] - pose[1]) );
			double angle = Math.atan2( (waypoint[1] - pose[1]), (waypoint[0] - pose[0]) ) - pose[5];
			angle = normalizeAngle(angle);

			// Choose driving behavior depending on direction and and where we are 
			if (Math.abs(angle) > 1.0) {
				
				// If we are facing away, turn around first
				double yawVel = Math.max(Math.min( angle / 1.0, 1.0 ), -1.0);
				control.setVelocity(new double[] {0.5, 0.0, 0.0, 0.0, 0.0, yawVel});
				
			} else if (distance >= 3.0) {
				
				// If we are far away, drive forward and turn
				double forwardVel = Math.min( distance / 10.0, 1.0 );
				double yawVel = Math.max(Math.min( angle / 10.0, 1.0 ), -1.0);
				control.setVelocity(new double[] {forwardVel, 0.0, 0.0, 0.0, 0.0, yawVel});
				
			} else {
				
				// If we are at the waypoint, stop moving
				control.setVelocity(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
			}
	
		}
	}),

	/**
	 * This controller simply cuts all power to the boat, letting it drift 
	 * freely.  It will not attempt to hold position or steer the boat in 
	 * any way, and completely ignores the waypoint.
	 */
	STOP(new AirboatController() {
		
		@Override
		public void update(AirboatControl control, AirboatSensor sensor, double dt) {
			control.setVelocity(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
		}
	});
	
	/**
	 * The controller implementation associated with this library name.
	 */
	public final AirboatController controller;
	
	/**
	 * Instantiates a library entry with the specified controller.
	 * @param controller the controller to be used by this entry.
	 */
	private AirboatControllerLibrary(AirboatController controller) {
		this.controller = controller;
	}
	
	/**
	 * Takes an angle and shifts it to be in the range -Pi to Pi.
	 * 
	 * @param angle an angle in radians
	 * @return the same angle as given, normalized to the range -Pi to Pi.
	 */
	public static double normalizeAngle(double angle) {
		while (angle > Math.PI) 
			angle -= 2*Math.PI;
		while (angle < -Math.PI)
			angle += 2*Math.PI;
		return angle;
	}
		
}
