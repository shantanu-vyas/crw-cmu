package edu.cmu.ri.crw;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;

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
public interface VehicleFilter {

        /**
         * Resets the vehicle pose to the specified initial condition at the 
         * specified initial time.
         * 
         * @param pose the specified pose of the vehicle in UTM coordinates
         * @param time the current time in milliseconds
         */
        public void reset(UtmPose pose, long time);

        /**
         * Gets the current estimate for the pose of the vehicle, given the 
         * current time.  The behavior of this function is not defined if a time
         * is given that occurred before the most recent update.
         * 
         * This function should internally perform any necessary prediction 
         * steps to give the best possible current estimate of pose. 
         * 
         * @param time the current time in milliseconds
         * @return the estimated pose of the vehicle in UTM coordinates
         */
        public UtmPoseWithCovarianceStamped pose(long time);

        // TODO: change GPS update to include covariance of reading 
        /**
         * Update function that is called when the vehicle has received a new
         * GPS position estimate.  
         * 
         * @param position UTM position estimated by GPS
         * @param time the current time in milliseconds
         */
        public void gpsUpdate(UtmPose position, long time);
        
        /**
         * Update function that is called when the vehicle has received a new
         * compass heading.  
         * 
         * Note: the angle specified here is <b>not</b> the canonical 
         * <i>heading</i> of the vehicle.  It is the rotation around the +Z axis
         * (in this case, directly up, i.e. away from the center of the earth).
         * This means the following:
         * 
         * EAST: yaw = 0
         * NORTH: yaw = PI/2
         * WEST: yaw = PI or -PI
         * SOUTH: yaw = 3*PI/2 or -PI/2
         * 
         * @param yaw the estimated yaw of the vehicle in radians
         * @param time the current time in milliseconds
         */
        public void compassUpdate(double yaw, long time);
        
        /**
         * Update function that is called when the vehicle has received a new
         * gyro velocity update.
         * 
         * Note: the anglular velocity specified here is <b>not</b> relative to 
         * the canonical <i>heading</i> of the vehicle.  It is with respect to 
         * rotation around the +Z axis (in this case, directly up, i.e. away 
         * from the center of the earth).
         * This means the following:
         * 
         * Clockwise motion: yawVel < 0
         * Counterclockwise motion: yawVel > 0
         * 
         * @param yawVel the estimated yaw rate of the vehicle in radians/sec
         * @param time the current time in milliseconds
         */
        public void gyroUpdate(double yawVel, long time);
}
