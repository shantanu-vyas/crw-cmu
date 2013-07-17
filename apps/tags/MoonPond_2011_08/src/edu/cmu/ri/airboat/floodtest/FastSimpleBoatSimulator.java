/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.WaypointObserver;
import java.util.concurrent.atomic.AtomicBoolean;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;

/**
 * @todo Allow some of these things to be configured
 * @author pscerri
 */
public class FastSimpleBoatSimulator extends SimpleBoatSimulator {

    protected long LOCAL_UPDATE_INTERVAL = 1000L;

    @Override
    public void startWaypoint(final UtmPose waypoint, String controller, final WaypointObserver obs) {

        // Keep a reference to the navigation flag for THIS waypoint
        final AtomicBoolean isNavigating = new AtomicBoolean(true);
        System.out.println("\nStart Waypoint to " + waypoint.pose.position.x);
        // Set this to be the current navigation flag
               
        synchronized (_navigationLock) {
            if (_isNavigating != null) {
                _isNavigating.set(false);
            }
            _isNavigating = isNavigating;
            _waypoint = waypoint.clone();
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (isNavigating.get()) {

                    // If we are not set in autonomous mode, don't try to drive!
                    // if (_isAutonomous.get()) {

                    // Get the position of the vehicle and the waypoint
                    UtmPoseWithCovarianceStamped state = getState();
                    Pose pose = state.pose.pose.pose;
                    Pose wpPose = waypoint.pose;

                    // TODO: handle different UTM zones!

                    // Compute the distance and angle to the waypoint
                    // TODO: compute distance more efficiently
                    double distance = Math.sqrt(Math.pow(
                            (wpPose.position.x - pose.position.x), 2)
                            + Math.pow(
                            (wpPose.position.y - pose.position.y),
                            2));
                    double angle = Math.atan2(
                            (wpPose.position.y - pose.position.y),
                            (wpPose.position.x - pose.position.x))
                            - QuaternionUtils.toYaw(pose.orientation);
                    angle = normalizeAngle(angle);

                    // Choose driving behavior depending on direction and
                    // where we are
                    if (Math.abs(angle) > 1.0) {

                        // If we are facing away, turn around first
                        _velocity.linear.x = 1.0;
                        _velocity.angular.z = Math.max(Math.min(angle / 1.0, 5.0), -5.0);
                    } else if (distance >= 3.0) {

                        // If we are far away, drive forward and turn
                        _velocity.linear.x = Math.min(distance / 5.0, 400.0);
                        _velocity.angular.z = Math.max(Math.min(angle / 10.0, 5.0), -5.0);
                    } else /* (distance < 3.0) */ {
                        break;
                    }
                    //}

                    // Pause for a while
                    try {
                        Thread.sleep(LOCAL_UPDATE_INTERVAL);
                        if (obs != null) {
                            obs.waypointUpdate(WaypointState.GOING);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Stop the vehicle
                _velocity.linear.x = 0.0;
                _velocity.angular.z = 0.0;

                // Upon completion, report status
                // (if isNavigating is still true, we completed on our own)
                if (isNavigating.getAndSet(false)) {
                    if (obs != null) {
                        obs.waypointUpdate(WaypointState.DONE);
                    }
                } else {
                    if (obs != null) {
                        obs.waypointUpdate(WaypointState.CANCELLED);
                    }
                }
            }
        }).start();
    }
}
