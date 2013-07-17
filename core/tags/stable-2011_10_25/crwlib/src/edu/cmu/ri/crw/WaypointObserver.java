package edu.cmu.ri.crw;

import edu.cmu.ri.crw.VehicleServer.WaypointState;

public interface WaypointObserver {
	public void waypointUpdate(WaypointState status);
}
