package edu.cmu.ri.airboat.interfaces;

/**
 * This is the interface that represents all of the public functionality for the
 * high-level (waypoint) command of the vehicle.  This can be used by a task
 * executor or other higher level module to implement navigation behaviors.
 * 
 * @author pkv
 *
 */
public interface AirboatCommand {

    /**
     * Returns the current waypoint that the vehicle is attempting to reach as
     * a 6D pose.  The format of this pose is [x,y,z,roll,pitch,yaw] in the
     * world coordinate frame.
     *
     * @return a 6D pose describing the current waypoint
     */
    public double[] getWaypoint();

    /**
     * Sets the current waypoint that should be used by the vehicle controller.
     * This change should take effect immediately, overriding any previous
     * waypoint.  The result of changing this waypoint depends on the controller
     * that is currently in use.
     * 
     * @param loc a 6D pose describing the new current waypoint
     * @return true if the waypoint was set successfully
     */
    public boolean setWaypoint(double[] loc);

    /**
     * Reports the completion of a waypoint. Intuitively, this represents a 
     * condition such as getting within a certain raduis of the current 
     * waypoint, but its behavior is ultimately defined by the current vehicle 
     * controller.
     * 
     * @return true if the waypoint has been completed according to the controller
     */
    public boolean isWaypointComplete();

    /**
     * Indicates whether this vehicle server is currently connected to the
     * low-level hardware system.
     *
     * @return true if the vehicle server is connected to low-level hardware
     */
    public boolean isConnected();

    /**
     * Indicates whether the vehicle controller is enabled, allowing for
     * autonomous waypoint navigation.  If this is disabled, the vehicle
     * controller will not be able to actuate the boat.
     * 
     * This state is controlled from the AirboatControl interface, and is 
     * intended for use with emergency stop and teleoperation interfaces.
     * 
     * @return true if the vehicle controller is enabled
     */
    public boolean isAutonomous();

    /**
     * Sets the current vehicle controller, selected from the list specified
     * by getControllers.
     *
     * The vehicle can have a number of controllers which encode different
     * behaviors, such as circling a point or trying to hold a position.
     *
     * @param controlName the name of the controller to apply
     * @return true if the controller was loaded successfully
     */
    public boolean setController(String controlName);

    /**
     * Returns the controller that is currently in use, as selected from the 
     * list specified by getControllers.
     * 
     * @return the name of the currently active controller.
     */
    public String getController();

    /**
     * Lists the controllers that are currently available in this vehicle 
     * server.  This can vary depending on the available hardware and vehicle
     * type.
     * 
     * @return a list of available controllers
     */
    public String[] getControllers();

    /**
     * Returns the current pose of the vehicle, in a UTM registered world frame.
     * The pose is returned in a 6D [x,y,z,roll,pitch,yaw] array.
     *
     * @return the current pose of the vehicle
     */
    public double[] getPose();

    /**
     * Sets the current pose of the vehicle by recomputing the origin of the
     * vehicle frame to match the current pose estimate with the specified
     * pose.  This may affect other poses that are also registered to the
     * vehicle's world frame.
     * 
     * This function can be used to orient the boat in a joint world frame with
     * other boats, or to correct pose offsets that may occur over time.
     * 
     * @param state the current pose of the vehicle
     * @return true if the pose was successfully updated
     */
    public boolean setPose(double[] state);

    /**
     * Indicated whether the world frame used by the vehicle is a UTM zone in
     * the northern or southern hemisphere.
     *
     * @return true if the UTM zone of the world frame is in the northern hemisphere
     */
    public boolean isUTMHemisphereNorth();

    /**
     * Indicates the longitudinal UTM zone that the vehicle world frame is
     * using as an origin.
     *
     * @return the UTM longitude zone code of the current world frame
     */
    public int getUTMZone();

    /**
     * Changes the current UTM zone of the vehicle.  Can be used in conjunction
     * with the setPose function to reorient the vehicle in a joint or
     * virtual world frame.
     *
     * @param zone the UTM longitude zone code
     * @param isNorth true if the UTM zone is in the northern hemisphere
     * @return true if the world frame was successfully updated
     */
    public boolean setUTMZone(int zone, boolean isNorth);
}
