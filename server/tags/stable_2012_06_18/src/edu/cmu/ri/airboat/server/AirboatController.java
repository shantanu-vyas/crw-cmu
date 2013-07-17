package edu.cmu.ri.airboat.server;

import robotutils.Pose3D;
import edu.cmu.ri.crw.VehicleController;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;

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

		@Override
		public void update(VehicleServer server, double dt) {
			Twist twist = new Twist();

			// Get the position of the vehicle
			UtmPose state = server.getPose();
			Pose3D pose = state.pose;

			// Get the current waypoint, or return if there are none
			UtmPose[] waypoints = server.getWaypoints();
			if (waypoints == null || waypoints.length <= 0) {
				server.setVelocity(twist);
				return;
			}
			Pose3D waypoint = waypoints[0].pose;

			// TODO: handle different UTM zones!

			// Compute the distance and angle to the waypoint
			double distanceSq = planarDistanceSq(pose, waypoint);
			double angle = angleBetween(pose, waypoint)
					- pose.getRotation().toYaw();
			angle = normalizeAngle(angle);

			// Choose driving behavior depending on direction and where we are
			if (distanceSq <= 9.0) {
				
				// If we are "at" the destination, de-queue current waypoint
				UtmPose[] queuedWaypoints = new UtmPose[waypoints.length - 1];
				System.arraycopy(waypoints, 1, queuedWaypoints, 0,
						queuedWaypoints.length);
				server.startWaypoints(queuedWaypoints,
						AirboatController.POINT_AND_SHOOT.toString());
				
			} else if (Math.abs(angle) <= 0.78) { /*split into 45 deg increments */

				// If we are facing away, turn around first
				twist.dx(Math.min(distanceSq / 10.0, 3.0)); /* Min speed of 1 m/s and Max speed of 3 m/s and max speed of whatever its capped at*/
				twist.drz(Math.max(Math.min(angle/0.5, 0.78), -0.78)); //Ensures min turn rate (apart from 0) is increased by a factor of 2 capped off at 0.78
				
			}else if ((Math.abs(angle) > 0.78) && (Math.abs(angle)<= 1.56)) {/*45-90 deg; sharper turn slower thrust*/

				// If we are facing away, turn around first
				twist.dx(Math.min(distanceSq / 15.0, 1.5));/* Min speed of 0.67 m/s and Max speed of 1.5 m/s and max speed of whatever its capped at*/
				twist.drz(Math.max(Math.min(angle, 1.2), -1.2));
				
			}else if ((Math.abs(angle) > 1.56)) {/*90-180 deg; sharp turn slow thrust*/

				// If we are facing away, turn around first
				twist.dx(Math.min(distanceSq / 20.0, 1.0));/* Min speed of 0.5 m/s and Max speed of 1.5 m/s and max speed of whatever its capped at*/
				twist.drz(Math.max(Math.min(angle, 1.56), -1.56)); /*Max turn angle of 90 deg */	
			}  /*
			else {

				// If we are far away, drive forward and turn
				twist.dx(Math.min(distanceSq / 10.0, 2.0));
				twist.drz(Math.max(Math.min(angle / 0.5, 1.0), -1.0));
			} */

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
		UtmPose lastPose = new UtmPose();

		@Override
		public void update(VehicleServer server, double dt) {
			Twist twist = new Twist();

			// Get the position of the vehicle
			UtmPose state = server.getPose();
			Pose3D pose = state.pose;

			// Get the current waypoint, or return if there are none
			UtmPose[] waypoints = server.getWaypoints();
			if (waypoints == null || waypoints.length <= 0) {
				server.setVelocity(twist);
				return;
			}
			Pose3D waypoint = waypoints[0].pose;

			// TODO: handle different UTM zones!
			// Compute the distance and angle to the waypoint
			double distanceSq = planarDistanceSq(pose, waypoint);
			double angle = angleBetween(pose, waypoint)
					- pose.getRotation().toYaw();
			angle = normalizeAngle(angle);

			// Choose driving behavior depending on direction and where we are
			if (Math.abs(angle) > 1.0) {
				// If we are facing away, turn around first
				twist.dx(0.5);
				twist.drz(Math.max(Math.min(angle / 1.0, 1.0), -1.0));
			} else if (distanceSq >= 9.0) {
				// If we are far away, drive forward and turn
				twist.dx(Math.min(distanceSq / 10.0, 1.0));
				twist.drz(Math.max(Math.min(angle / 10.0, 1.0), -1.0));
			}

			// Set the desired velocity
			server.setVelocity(twist);

			System.out.println(isNovel(pose, waypoint, PlanningMethod.SIMPLE));
			// First check if we are actually set to capture
			if (lastPose == null
					|| isNovel(pose, waypoint, PlanningMethod.SIMPLE) > 0.8) {
				lastPose.pose = pose.clone();
				System.out.println("Should Capture now!!!!!");
				server.startCamera(1, 0.0, 640, 480);
			}

			// TODO: add next waypoint functionality back in!!
		}

		/**
		 * Takes a pose and determines whether the image to be taken is novel or
		 * not
		 * 
		 * @param pose
		 *            The current pose
		 * @param waypoint
		 *            The current waypoint
		 * @param method
		 *            The Planning Method to be implemented
		 * @return A weight of novelty between 0 and 1
		 */
		double isNovel(Pose3D pose, Pose3D waypoint, PlanningMethod method) {

			final double CAMERA_AOV = Math.PI / 180.0f * 30; // Assuming that
																// the angle of
																// view of the
																// camera is 30
																// Degrees
			final double OVERLAP_RATIO = 0.8f;
			final double EFFECTIVE_DISTANCE = 10.0; // The effective distance
													// till which the camera
													// resolution/detection is
													// trusted
			double novelty = 0.0;
			switch (method) {
			case SIMPLE:
				// To simply calculate if the new pose is different
				/*
				 * Capture if new pose is different as per a. Change in yaw b.
				 * Change in position
				 */

				// No need to worry about the waypoint, inconsequential
				double angle = Math.abs(pose.getRotation().toYaw()
						- lastPose.pose.getRotation().toYaw());
				double distance = Math
						.sqrt((lastPose.pose.getX() - pose.getX())
								* (lastPose.pose.getX() - pose.getX())
								+ (lastPose.pose.getY() - pose.getY())
								* (lastPose.pose.getY() - pose.getY()));

				// Assign half weight to yaw, and half to distance
				if (angle >= CAMERA_AOV * OVERLAP_RATIO) {
					// i.e. if the current yaw has changed more than the
					// previous orientation by greater than 30 degrees * overlap
					// factor

					// This is because ANY yaw greater than the angle of view
					// will have completely new info (Think sectors)
					novelty = 0.5 * angle / (CAMERA_AOV * OVERLAP_RATIO);

					// Assuming that the zone of overlap is not useful
					// information
				}

				novelty += (distance / EFFECTIVE_DISTANCE) * 0.5;

				break;
			case GEOMETRIC:
				throw new UnsupportedOperationException("Not implemented Yet!");

			}
			return novelty;
		}

	});

	public enum PlanningMethod {
		SIMPLE, GEOMETRIC
	};

	/**
	 * The controller implementation associated with this library name.
	 */
	public final VehicleController controller;

	/**
	 * Instantiates a library entry with the specified controller.
	 * 
	 * @param controller
	 *            the controller to be used by this entry.
	 */
	private AirboatController(VehicleController controller) {
		this.controller = controller;
	}

	/**
	 * Takes an angle and shifts it to be in the range -Pi to Pi.
	 * 
	 * @param angle
	 *            an angle in radians
	 * @return the same angle as given, normalized to the range -Pi to Pi.
	 */
	public static double normalizeAngle(double angle) {
		while (angle > Math.PI)
			angle -= 2 * Math.PI;
		while (angle < -Math.PI)
			angle += 2 * Math.PI;
		return angle;
	}

	/**
	 * Computes the squared XY-planar Euclidean distance between two points.
	 * Using the squared distance is cheaper (it avoid a sqrt), and for constant
	 * comparisons, it makes no difference (just square the constant).
	 * 
	 * @param a
	 *            the first pose
	 * @param b
	 *            the second pose
	 * @return the XY-planar Euclidean distance
	 */
	public static double planarDistanceSq(Pose3D a, Pose3D b) {
		double dx = a.getX() - b.getX();
		double dy = a.getY() - b.getY();
		return dx * dx + dy * dy;
	}

	/**
	 * Computes a direction vector from a source pose to a destination pose, as
	 * projected onto the XY-plane. Returns an angle representing the direction
	 * in the XY-plane to take if starting at the source pose to reach the
	 * destination pose.
	 * 
	 * @param a
	 *            the source (starting) pose
	 * @param b
	 *            the destination (final) pose
	 * @return an angle in the XY-plane (around +Z-axis) to get to destination
	 */
	public static double angleBetween(Pose3D src, Pose3D dest) {
		return Math.atan2((dest.getY() - src.getY()),
				(dest.getX() - src.getX()));
	}
}