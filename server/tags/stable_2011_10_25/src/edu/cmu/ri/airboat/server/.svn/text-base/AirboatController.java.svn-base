package edu.cmu.ri.airboat.server;

import android.util.Log;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.Twist;

import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.VehicleController;
import edu.cmu.ri.crw.VehicleServer;

/**
 * A library of available navigation controllers that are accessible through the
 * high-level API.
 * 
 * @author pkv
 * @author kss
 * 
 */
public enum AirboatController {
	
	/**
	 * This controller turns the boat around until it is facing the waypoint,
	 * then drives roughly in an arc towards the waypoint. When it gets within a
	 * certain range, it will cut power to the boat entirely.
	 */

	POINT_AND_SHOOT(new VehicleController() {
		private final String logTag = AirboatController.class.getName();

		@Override
		public void update(VehicleServer server, double dt) {
			Twist twist = new Twist();

			// Get the position of the vehicle and the waypoint
			UtmPoseWithCovarianceStamped state = server.getState();
			UtmPose waypointState = server.getWaypoint();
			if (state == null || waypointState == null) {
				Log.w(logTag, "State or waypoint was null, not updating.");
				return;
			}
			
			Pose pose = state.pose.pose.pose;
			Pose waypoint = waypointState.pose;

			// TODO: handle different UTM zones!

			// Compute the distance and angle to the waypoint
			// TODO: compute distance more efficiently
			double distance = Math.sqrt(Math.pow(
					(waypoint.position.x - pose.position.x), 2)
					+ Math.pow((waypoint.position.y - pose.position.y), 2));
			double angle = Math.atan2((waypoint.position.y - pose.position.y),
					(waypoint.position.x - pose.position.x))
					- QuaternionUtils.toYaw(pose.orientation);
			angle = normalizeAngle(angle);

			// Choose driving behavior depending on direction and and where we
			// are
			if (Math.abs(angle) > 1.0) {

				// If we are facing away, turn around first
				twist.linear.x = 0.5;
				twist.angular.z = Math.max(Math.min(angle / 1.0, 1.0), -1.0);
			} else if (distance >= 3.0) {

				// If we are far away, drive forward and turn
				twist.linear.x = Math.min(distance / 10.0, 1.0);
				twist.angular.z = Math.max(Math.min(angle / 10.0, 1.0), -1.0);
			}

			// Set the desired velocity
			server.setVelocity(twist);
		}

	}),

	/**
	 * This controller simply cuts all power to the boat, letting it drift
	 * freely. It will not attempt to hold position or steer the boat in any
	 * way, and completely ignores the waypoint.
	 */
	STOP(new VehicleController() {

		@Override
		public void update(VehicleServer server, double dt) {
			server.setVelocity(new Twist());

		}
	}),

	/**
	 * This controller shoots a picture only if it moves to a significantly
	 * different pose
	 */

	SHOOT_ON_MOVE(new VehicleController() {
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
			double distance = Math.sqrt(Math.pow(
					(waypoint.position.x - pose.position.x), 2)
					+ Math.pow((waypoint.position.y - pose.position.y), 2));
			double angle = Math.atan2((waypoint.position.y - pose.position.y),
					(waypoint.position.x - pose.position.x))
					- QuaternionUtils.toYaw(pose.orientation);
			angle = normalizeAngle(angle);

			// Choose driving behavior depending on direction and and where we
			// are
			if (Math.abs(angle) > 1.0) {
				// If we are facing away, turn around first
				twist.linear.x = 0.5;
				twist.angular.z = Math.max(Math.min(angle / 1.0, 1.0), -1.0);
			} else if (distance >= 3.0) {
				// If we are far away, drive forward and turn
				twist.linear.x = Math.min(distance / 10.0, 1.0);
				twist.angular.z = Math.max(Math.min(angle / 10.0, 1.0), -1.0);
			}

			// Set the desired velocity
			server.setVelocity(twist);

			System.out.println(isNovel(pose, waypoint, PlanningMethod.SIMPLE));
			// First check if we are actually set to capture
			if (lastPose == null
					|| isNovel(pose, waypoint, PlanningMethod.SIMPLE) > 0.8) {
				lastPose.pose = pose.clone();
				System.out.println("Should Capture now!!!!!");
				server.startCamera(1, 0, 640, 480, null);
			}
		}
	});

	/**
	 * The controller implementation associated with this library name.
	 */
	public final VehicleController controller;
	static UtmPose lastPose = new UtmPose();

	/**
	 * Instantiates a library entry with the specified controller.
	 * 
	 * @param controller the controller to be used by this entry.
	 */
	private AirboatController(VehicleController controller) {
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
			angle -= 2 * Math.PI;
		while (angle < -Math.PI)
			angle += 2 * Math.PI;
		return angle;
	}

	public enum PlanningMethod {
		SIMPLE, GEOMETRIC
	};

	/**
	 * Takes a pose and determines whether the image to be taken is novel or not
	 * 
	 * @param pose
	 *            The current pose
	 * @param waypoint
	 *            The current waypoint
	 * @param method
	 *            The Planning Method to be implemented
	 * @return A weight of novelty between 0 and 1
	 */
	static double isNovel(Pose pose, Pose waypoint, PlanningMethod method) {

		// Assuming that the angle of view of the camera is 30 Degrees
		final double CAMERA_AOV = Math.PI / 180.0f * 30;
		final double OVERLAP_RATIO = 0.8f;
		// The effective distance till which the camera resolution/detection is
		// trusted
		final double EFFECTIVE_DISTANCE = 10.0;
		double novelty = 0.0;
		switch (method) {
		case SIMPLE:
			// To simply calculate if the new pose is different
			/*
			 * Capture if new pose is different as per a. Change in yaw b.
			 * Change in position
			 */

			// No need to worry about the waypoint, inconsequential
			double angle = Math.abs(QuaternionUtils.toYaw(pose.orientation)
					- QuaternionUtils.toYaw(lastPose.pose.orientation));
			double distance = Math
					.sqrt((lastPose.pose.position.x - pose.position.x)
							* (lastPose.pose.position.x - pose.position.x)
							+ (lastPose.pose.position.y - pose.position.y)
							* (lastPose.pose.position.y - pose.position.y));

			// Assign roughly half initial weight to yaw, and half to distance

			if (angle >= CAMERA_AOV * OVERLAP_RATIO) {
				// i.e. if the current yaw has changed more than the previous
				// orientation by greater than 30 degrees * overlap factor
				novelty = 0.5 * angle / (CAMERA_AOV * OVERLAP_RATIO);
				// This is because ANY yaw greater than the angle of view will
				// have completely new info (Think sectors)
				// Assuming that the zone of overlap is not useful information
			}

			novelty += (distance / EFFECTIVE_DISTANCE) * 0.5;

			break;
		/**
		 * This planning method would involve a grid based image capture routine
		 */
		case GEOMETRIC:
			throw new UnsupportedOperationException("Not implemented Yet!");

		}
		return novelty;
	}

}