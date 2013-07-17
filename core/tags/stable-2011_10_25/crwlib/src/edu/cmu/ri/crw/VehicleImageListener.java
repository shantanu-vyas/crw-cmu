package edu.cmu.ri.crw;

import java.util.EventListener;

import org.ros.message.sensor_msgs.CompressedImage;

public interface VehicleImageListener extends EventListener {
	public void receivedImage(CompressedImage image);
}
