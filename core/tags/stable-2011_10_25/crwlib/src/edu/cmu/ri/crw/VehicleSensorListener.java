package edu.cmu.ri.crw;

import java.util.EventListener;

import org.ros.message.crwlib_msgs.SensorData;

public interface VehicleSensorListener extends EventListener {
	public void receivedSensor(SensorData sensor);
}
