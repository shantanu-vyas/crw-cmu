package edu.cmu.ri.airboat.server;

import edu.cmu.ri.airboat.interfaces.AirboatFilter;

/**
 * A basic filter that uses weighted averages and a first-order approximate
 * motion model to predict and update state.
 * 
 * @author pkv
 *
 */
public class SimpleFilter implements AirboatFilter {
	
	// The largest allowed numerical integration timestep
	// (larger intervals are integrated using multiple steps of this length)
	public static final long MAX_STEP_MS = 200;
	
	// Tuning factors for compass and GPS
	// 0.0 means no confidence, 1.0 means perfect accuracy.  Nominal is ~0.1.
	public static final double ALPHA_COMPASS = 0.0;
	public static final double ALPHA_GPS = 0.9;
	
	// Indicator variables used to mark whether absolute heading and position were measured
	boolean _isInitializedGps = false;
	boolean _isInitializedCompass = false;
	
	// State represented by 6D pose (x,y,z,roll,pitch,yaw), and 6D vel (dx,dy,dz,rx,ry,rz)
	double[] _pose = new double[6];
	double[] _vels = new double[6];
	
	// The current time in milliseconds, used to measure filter update intervals
	long _time = System.currentTimeMillis();
	
	protected void predict(long time) {
		while(_time < time) {
			long step = Math.min(time - _time, MAX_STEP_MS);
			double dt = step / 1000.0;
			
			_pose[0] += dt * (_vels[0] * Math.cos(_pose[5]) - _vels[1] * Math.sin(_pose[5]));
			_pose[1] += dt * (_vels[0] * Math.sin(_pose[5]) + _vels[1] * Math.cos(_pose[5]));
			_pose[5] += dt * (_vels[5]);
			
			_time += step;
		}
	}
	
	@Override
	public synchronized void compassUpdate(double heading, long time) {
		predict(time);
		
		// On the first compass update, simply take on the initial heading
		if (_isInitializedCompass) {
			_pose[5] = angleAverage(ALPHA_COMPASS, _pose[5], heading);
		} else {
			_pose[5] = heading;
			_isInitializedCompass = true;
		}
	}

	@Override
	public synchronized void gpsUpdate(double northing, double easting, long time) {
		predict(time);
		
		// On the first GPS update, simply take on the initial readings
		if (_isInitializedGps) {
			_pose[0] = ALPHA_GPS * northing + (1 - ALPHA_GPS) * _pose[0];
			_pose[1] = ALPHA_GPS * easting + (1 - ALPHA_GPS) * _pose[1];
		} else {
			_pose[0] = northing;
			_pose[1] = easting;
			_isInitializedGps = true;
		}
	}

	@Override
	public synchronized void gyroUpdate(double headingVel, long time) {
		predict(time);
		_vels[5] = headingVel;
	}

	@Override
	public synchronized double[] pose(long time) {
		return _pose;
	}

	@Override
	public synchronized void reset(double[] pose, long time) {
		_time = time;
		System.arraycopy(pose, 0, _pose, 0, _pose.length);
		
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
