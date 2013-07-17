package edu.cmu.ri.airboat.server;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.Twist;


import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.VehicleController;
import edu.cmu.ri.crw.VehicleServer;

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
	
	POINT_AND_SHOOT(new VehicleController() {
		
		@Override
		public void update(VehicleServer server, double dt) {
			Twist twist = new Twist();
			
			// Get the position of the vehicle and the waypoint 
			UtmPoseWithCovarianceStamped state = server.getState();
			Pose pose = state.pose.pose.pose;
			
			UtmPose waypointState = server.getWaypoint();
			Pose waypoint = waypointState.pose;
			
			// TODO: handle different UTM zones!
			
			// Compute the distance and angle to the waypoint
			// TODO: compute distance more efficiently
			double distance = Math.sqrt( Math.pow((waypoint.position.x - pose.position.x),2)
										+ Math.pow((waypoint.position.y - pose.position.y),2));
			double angle = Math.atan2( (waypoint.position.y - pose.position.y),
										(waypoint.position.x - pose.position.x) )
							- QuaternionUtils.toYaw(pose.orientation);
			angle = normalizeAngle(angle);

			// Choose driving behavior depending on direction and and where we are 
			if (Math.abs(angle) > 1.0) {
				
				// If we are facing away, turn around first
				twist.linear.x = 0.5;
				twist.angular.z = Math.max(Math.min( angle / 1.0, 1.0 ), -1.0);
			} else if (distance >= 3.0) {
				
				// If we are far away, drive forward and turn
				twist.linear.x = Math.min( distance / 10.0, 1.0 );
				twist.angular.z = Math.max(Math.min( angle / 10.0, 1.0 ), -1.0);
			}
	
			// Set the desired velocity
			server.setVelocity(twist);
		}

	}),

	/**
	 * This controller simply cuts all power to the boat, letting it drift 
	 * freely.  It will not attempt to hold position or steer the boat in 
	 * any way, and completely ignores the waypoint.
	 */
	STOP(new VehicleController() {

		@Override
		public void update(VehicleServer server, double dt) {
			server.setVelocity(new Twist());
			
		}
	});
	
	/**
	 * The controller implementation associated with this library name.
	 */
	public final VehicleController controller;
	
	/**
	 * Instantiates a library entry with the specified controller.
	 * @param controller the controller to be used by this entry.
	 */
	private AirboatControllerLibrary(VehicleController controller) {
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
