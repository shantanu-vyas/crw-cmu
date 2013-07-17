package edu.cmu.ri.crw;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.ros.message.Time;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CameraInfo;
import org.ros.message.sensor_msgs.CompressedImage;


public abstract class AbstractVehicleServer implements VehicleServer {

	protected double[][] _gains = new double[6][3];

	protected List<VehicleSensorListener> _sensorListeners = new ArrayList<VehicleSensorListener>();
	protected List<VehicleImageListener> _imageListeners = new ArrayList<VehicleImageListener>();
	protected List<VehicleVelocityListener> _velocityListeners = new ArrayList<VehicleVelocityListener>();
	protected List<VehicleStateListener> _stateListeners = new ArrayList<VehicleStateListener>();
	
	public double[] getGains(int axis) {
		if (axis < 0 || axis >= _gains.length) 
			return new double[0];
		
		// Make a copy of the current state (for immutability) and return it
		double[] gains = new double[_gains[axis].length];
		System.arraycopy(_gains[axis], 0, gains, 0, _gains[axis].length);
		return gains;
	}
	
	public void setGains(int axis, double[] gains) {
		if (axis < 0 || axis >= _gains.length) 
			return;
		
		// Make a copy of the provided state (for immutability)
		System.arraycopy(gains, 0, _gains[axis], 0, Math.min(gains.length, _gains[axis].length));
	}
	
	public void addStateListener(VehicleStateListener l) {
		_stateListeners.add(l);
	}

	public void removeStateListener(VehicleStateListener l) {
		_stateListeners.remove(l);
	}

	protected void sendState(UtmPoseWithCovarianceStamped pose) {
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (VehicleStateListener l : _stateListeners) {
			l.receivedState(pose);
		}
	}

	public void addImageListener(VehicleImageListener l) {
		_imageListeners.add(l);
	}

	public void removeImageListener(VehicleImageListener l) {
		_imageListeners.remove(l);
	}

	protected static CompressedImage toCompressedImage(RenderedImage image) {
		// This might be inefficient, but it is far more inefficient to
		// uncompress hardware-compressed JPEG images on Android.
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try { 
			ImageIO.write(image, "jpeg", buffer);
			return toCompressedImage(image.getWidth(), image.getHeight(), buffer.toByteArray());
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	protected static CompressedImage toCompressedImage(int width, int height, byte[] data) {
		CompressedImage cImage = new CompressedImage();
		CameraInfo cInfo = new CameraInfo();
		
		cImage.data = new byte[data.length];
		System.arraycopy(data, 0, cImage.data, 0, data.length);
		
		cImage.format = "jpeg";
		cImage.header.stamp = Time.fromMillis(System.currentTimeMillis());
		cImage.header.frame_id = "camera";
		
		cInfo.header.stamp = cImage.header.stamp;
		cInfo.header.frame_id = cImage.header.frame_id;
		cInfo.width = width;
		cInfo.height = height;
		// TODO: figure out what to do with cInfo?
		
		return cImage;
	}
	
	protected void sendImage(CompressedImage image) {
		for (VehicleImageListener l : _imageListeners) {
			l.receivedImage(image);
		}
	}

	public void addSensorListener(int channel, VehicleSensorListener l) {
		// TODO: add support for separate channels
		_sensorListeners.add(l);
	}

	public void removeSensorListener(int channel, VehicleSensorListener l) {
		// TODO: add support for separate channels
		_sensorListeners.remove(l);
	}

	protected void sendSensor(int channel, SensorData reading) {
		for (VehicleSensorListener l : _sensorListeners) {
			l.receivedSensor(reading);
		}
	}

	public void addVelocityListener(VehicleVelocityListener l) {
		_velocityListeners.add(l);
	}

	public void removeVelocityListener(VehicleVelocityListener l) {
		_velocityListeners.remove(l);
	}

	protected void sendVelocity(TwistWithCovarianceStamped velocity) {
		for (VehicleVelocityListener l : _velocityListeners) {
			l.receivedVelocity(velocity);
		}
	}
}
