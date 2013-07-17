package edu.cmu.ri.crw;


/**
 * Defines an interface for a control object that has access to the various 
 * control and state functions of the vehicle boat.  This object will be called
 * periodically to provide an updated command to the vehicle.
 * 
 * @author pkv
 *
 */
public interface VehicleController {

    /**
     * Update function which is given a reference to the calling vehicle
     * and the time since the last control update was called. Returns whether
     * the controller believes the current waypoint has been reached.
     * 
     * @param server reference to vehicle
     * @param dt elapsed time since last controller call in seconds
     * @return 
     */
    public void update(VehicleServer server, double dt);
}
