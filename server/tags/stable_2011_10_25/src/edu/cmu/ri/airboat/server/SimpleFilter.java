package edu.cmu.ri.airboat.server;

import org.ros.message.Time;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Twist;

import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.VehicleFilter;

/**
 * A basic filter that uses weighted averages and a first-order approximate
 * motion model to predict and update state.
 * 
 * @author pkv
 *
 */
public class SimpleFilter implements VehicleFilter {
	
	// The largest allowed numerical integration timestep
	// (larger intervals are integrated using multiple steps of this length)
	public static final long MAX_STEP_MS = 200;
	
	// Tuning factors for compass and GPS
	// 0.0 means no confidence, 1.0 means perfect accuracy.  Nominal is ~0.1.
	public static final double ALPHA_COMPASS = 0.1;
	public static final double ALPHA_GPS = 0.9;
	
	// Indicator variables used to mark whether absolute heading and position were measured
	boolean _isInitializedGps = false;
	boolean _isInitializedCompass = false;
	
	// State represented by 6D pose
	UtmPose _pose = new UtmPose();
	Twist _vels = new Twist();
	
	// The current time in milliseconds, used to measure filter update intervals
	long _time = System.currentTimeMillis();
	
	protected void predict(long time) {
		while(_time < time) {
			long step = Math.min(time - _time, MAX_STEP_MS);
			double dt = step / 1000.0;
			double yaw = QuaternionUtils.toYaw(_pose.pose.orientation);
			
			_pose.pose.position.x += dt * (_vels.linear.x * Math.cos(yaw) - _vels.linear.y * Math.sin(yaw));
			_pose.pose.position.y += dt * (_vels.linear.x * Math.sin(yaw) + _vels.linear.y * Math.cos(yaw));
			_pose.pose.orientation = QuaternionUtils.fromEulerAngles(0, 0, yaw + dt * _vels.angular.z);
			
			_time += step;
		}
	}
	
	@Override
	public synchronized void compassUpdate(double yaw, long time) {
		predict(time);
		
		// On the first compass update, simply take on the initial heading
		// (invert the heading because yaw is negative heading) 
		if (_isInitializedCompass) {
			double oldYaw = QuaternionUtils.toYaw(_pose.pose.orientation);
			_pose.pose.orientation = QuaternionUtils.fromEulerAngles(0, 0, angleAverage(ALPHA_COMPASS, oldYaw, yaw));
		} else {
			_pose.pose.orientation = QuaternionUtils.fromEulerAngles(0, 0, yaw);
			_isInitializedCompass = true;
		}
	}

	@Override
	public synchronized void gpsUpdate(UtmPose utm, long time) {
		predict(time);
		
		// If we are in the wrong zone or are uninitialized, use the GPS position
		if (utm.utm.zone != _pose.utm.zone || utm.utm.isNorth != _pose.utm.isNorth || !_isInitializedGps) {
			_pose.utm = utm.utm.clone();
			_pose.pose = utm.pose.clone();
			_isInitializedGps = true;
		} else {
			// On other update, average together the readings
			_pose.pose.position.x = ALPHA_GPS * utm.pose.position.x + (1 - ALPHA_GPS) * _pose.pose.position.x;
			_pose.pose.position.y = ALPHA_GPS * utm.pose.position.y + (1 - ALPHA_GPS) * _pose.pose.position.y;
			
			// If we have altitude, use it (0.0 if not filled in)
			if (utm.pose.position.z != 0.0) {
				_pose.pose.position.z = utm.pose.position.z;
			}
			
			// If we have a bearing, use it as well (w != 0 in most valid quaternions)
			if (utm.pose.orientation.w != 0.0) {
				double oldYaw = QuaternionUtils.toYaw(_pose.pose.orientation);
				double yaw = QuaternionUtils.toYaw(utm.pose.orientation);
				_pose.pose.orientation = QuaternionUtils.fromEulerAngles(0, 0, angleAverage(ALPHA_GPS, oldYaw, yaw));
			}
		}
	}

	@Override
	public synchronized void gyroUpdate(double yawVel, long time) {
		predict(time);
		_vels.angular.z = yawVel;
	}

	@Override
	public synchronized UtmPoseWithCovarianceStamped pose(long time) {
		
		UtmPoseWithCovarianceStamped poseMsg = new UtmPoseWithCovarianceStamped();
		
		poseMsg.utm = _pose.utm.clone();
		poseMsg.pose.pose.pose = _pose.pose.clone();
		poseMsg.pose.header.frame_id = "/base_link";
		poseMsg.pose.header.stamp = Time.fromMillis(System.currentTimeMillis());
		
		return poseMsg;
	}

	@Override
	public synchronized void reset(UtmPose pose, long time) {
		_time = time;
		_pose = pose.clone();
		
		_isInitializedGps = true;
		_isInitializedCompass = true;
	}

	/**
	 * Helper function that reprojects any angle into the range of (-pi, pi]
	 * 
	 * @param angle the angle to be reprojected
	 * @return the reprojected angle, between -pi and pi
	 */
	protected double normalizeAngle(double angle) {
		while (angle > Math.PI)
			angle -= 2*Math.PI;
		while (angle <= -Math.PI)
			angle += 2*Math.PI;
		return angle;
	}
	
	/**
	 * Computes a weighted average of two angles, with correct ring math.  It takes a tuning 
	 * constant which weights between the two parameters. A weight of 0.0 will simply return
	 * the first angle, while a weight of 1.0 will simply return the second angle.  A weight
	 * of 0.5 corresponds to the arithmetic mean of the two angles.
	 * 
	 * @param weight tuning constant between the two angles. 
	 * @param angle1 the first angle
	 * @param angle2 the second angle
	 * @return the weighted average of the two angles
	 */
	protected double angleAverage(double weight, double angle1, double angle2) {
		
		// Find the difference between the two angles (should always be less than pi/2) 
		double diff = normalizeAngle(angle2 - angle1);
		
		// Use the weight to foreshorten this angular difference, then add to original angle
		return angle1 + (weight*diff);
	}
}
