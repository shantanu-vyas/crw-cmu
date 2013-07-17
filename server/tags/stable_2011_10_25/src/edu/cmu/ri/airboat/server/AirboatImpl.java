package edu.cmu.ri.airboat.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ros.internal.time.WallclockProvider;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

import com.google.code.microlog4android.LoggerFactory;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.VehicleFilter;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.WaypointObserver;

/**
 * Contains the actual implementation of vehicle functionality, accessible as a
 * singleton that is updated and maintained by a background service.
 * 
 * @author pkv
 * @author kss
 * 
 */
public class AirboatImpl extends AbstractVehicleServer {

	private static final com.google.code.microlog4android.Logger logger = 
		LoggerFactory.getLogger();

	private static final String logTag = AirboatImpl.class.getName();
	public static final int UPDATE_INTERVAL_MS = 100;
	public static final int NUM_SENSORS = 3;
	public static final AirboatController DEFAULT_CONTROLLER = AirboatController.POINT_AND_SHOOT;
	
	protected final SensorType[] _sensorTypes = new SensorType[NUM_SENSORS];
	protected UtmPose _waypoint = null;

	protected final Object _captureLock = new Object();
	protected AtomicBoolean _isCapturing = null;
	
	protected final Object _navigationLock = new Object();
	protected AtomicBoolean _isNavigating = null;

	/**
	 * Defines the PID gains that will be returned if there is an error.
	 */
	public static final double[] NAN_GAINS = 
		new double[] { Double.NaN, Double.NaN, Double.NaN };

	// Define Amarino function control codes
	public static final char GET_GYRO_FN = 'g';
	public static final char GET_RUDDER_FN = 'r';
	public static final char GET_THRUST_FN = 't';
	public static final char GET_TE_FN = 's';
	public static final char SET_VELOCITY_FN = 'v';
	public static final char GET_GAINS_FN = 'l';
	public static final char SET_GAINS_FN = 'k';

	// Set timeout for asynchronous Amarino calls
	public static final int RESPONSE_TIMEOUT_MS = 250; // was 200 earlier

	// Status information
	AtomicBoolean _isConnected = new AtomicBoolean(true) ;
	AtomicBoolean _isAutonomous = new AtomicBoolean(false);

	// Internal data structures for Amarino callbacks
	final Context _context;
	final String _arduinoAddr;
	final List<String> _partialCommand = new ArrayList<String>(10);
	final Object _velocityGainLock = new Object();
	double[] _velocityGain = new double[3];
	double _velocityGainAxis = -1;

	/**
	 * Keeps track of time
	 */
	WallclockProvider _wallclock = new WallclockProvider();
	
	/**
	 * Inertial state vector, currently containing a 6D pose estimate:
	 * [x,y,z,roll,pitch,yaw]
	 */
	public UtmPoseWithCovarianceStamped _pose = new UtmPoseWithCovarianceStamped();

	/**
	 * Filter used internally to update the current pose estimate
	 */
	VehicleFilter filter = new SimpleFilter();

	/**
	 * Inertial velocity vector, containing a 6D angular velocity estimate: [rx,
	 * ry, rz, rPhi, rPsi, rOmega]
	 */
	Twist _velocities = new Twist();

	/**
	 * Raw gyroscopic readings, as reported from the Arduino.
	 */
	double[] _gyroReadings = new double[3];

	/**
	 * Creates a new instance of the vehicle implementation. This function
	 * should only be used internally when the corresponding vehicle service is
	 * started and stopped.
	 * 
	 * @param context the application context to use
	 * @param addr the bluetooth address of the vehicle controller
	 */

	protected AirboatImpl(Context context, String addr) {
		_context = context;
		_arduinoAddr = addr;
	}

	/**
	 * Internal update function called at regular intervals to process command
	 * and control events.
	 * 
	 * @param dt the elapsed time since the last update call (in seconds)
	 */
	protected void update(double dt) {

		// Do an intelligent state prediction update here
		_pose = filter.pose(System.currentTimeMillis());
		logger.info("POSE: " + "[" +
				_pose.pose.pose.pose.position.x + "," +
				_pose.pose.pose.pose.position.y + "," +
				_pose.pose.pose.pose.position.z + "] " +
				Arrays.toString(QuaternionUtils.toEulerAngles(_pose.pose.pose.pose.orientation)));
		sendState(_pose.clone());
		
		// Call Amarino with new velocities here
		Amarino.sendDataToArduino(_context, _arduinoAddr, SET_VELOCITY_FN,
				new float[] { 
					(float) _velocities.linear.x, 
					(float) _velocities.linear.y,
					(float) _velocities.linear.z, 
					(float) _velocities.angular.x,
					(float) _velocities.angular.y, 
					(float) _velocities.angular.z
				});
		// Yes, I know this looks silly, but Amarino doesn't handle doubles
		
		// Log velocities
		logger.info("VEL: " + "[" +
				_velocities.linear.x + "," +
				_velocities.linear.y + "," +
				_velocities.linear.z + "," +
				_velocities.angular.x + "," +
				_velocities.angular.y + "," +
				_velocities.angular.z + "]");
		
		// Send velocities 
		TwistWithCovarianceStamped vel = new TwistWithCovarianceStamped();
		vel.header.stamp = _wallclock.getCurrentTime();
		vel.twist.twist = _velocities;
		sendVelocity(vel);		
	}
	
	/**
	 * @see VehicleServer#getGains(int)
	 */
	@Override
	public double[] getGains(int axis) {

		// Call Amarino here
		Amarino.sendDataToArduino(_context, _arduinoAddr, GET_GAINS_FN, axis);

		// Wait for response here (only if connected)
		if (_isConnected.get()) {
			synchronized (_velocityGainLock) {

				// Clear any old return value
				_velocityGainAxis = -1;

				try {
					// Wait for the correct axis to be filled in,
					// but if we start getting too backed up, drop calls
					for (int i = 0; i < 3 && _velocityGainAxis != axis; ++i)
						_velocityGainLock.wait(RESPONSE_TIMEOUT_MS);
				} catch (InterruptedException ex) {
					Log.w(logTag, "Interrupted function: " + GET_GAINS_FN, ex);
				}

				// If got the appropriate axis, make a copy immediately
				if (_velocityGainAxis == axis) {
					double[] output = new double[3];
					System.arraycopy(_velocityGain, 0, output, 0, output.length);
					return output;
				} else {
					Log.w(logTag, "No response for: " + GET_GAINS_FN);
					return NAN_GAINS;
				}
			}
		} else {
			Log.w(logTag, "Not connected, can't perform: " + GET_GAINS_FN);
			return NAN_GAINS;
		}
	}

	/**
	 * @see VehicleServer#setGains(int, double[])
	 */
	@Override
	public void setGains(int axis, double[] k) {
		
		// Call Amarino here
		Amarino.sendDataToArduino(
				_context,
				_arduinoAddr,
				SET_GAINS_FN,
				new float[] { (float) axis, (float) k[0], (float) k[1], (float) k[2] });
		logger.info("SETGAINS: " + axis + " " + Arrays.toString(k));
	}

	/**
	 * @see AirboatCommand#isConnected()
	 */
	public boolean isConnected() {
		return _isConnected.get();
	}

	/**
	 * Internal function used to set the connection status of this object
	 * (indicating whether currently in contact with vehicle controller).
	 */
	protected void setConnected(boolean isConnected) {
		_isConnected.set(isConnected);
	}

	/**
	 * Handles complete Arduino commands, once they are reassembled.
	 * 
	 * @param cmd
	 *            the list of arguments composing a command
	 */
	protected void onCommand(List<String> cmd) {

		// Just like Amarino, we use a flag to differentiate channels
		switch (cmd.get(0).charAt(0)) {
		case GET_GAINS_FN:
			// Check size of function
			if (cmd.size() != 5) {
				Log.w(logTag, "Received corrupt gain function: " + cmd);
				return;
			}

			// Return a set of PID values
			try {
				synchronized (_velocityGainLock) {
					// Read in the axis that was filled in
					_velocityGainAxis = Double.parseDouble(cmd.get(1));

					// Cast the parameters to double-valued gains
					for (int i = 0; i < 3; i++)
						_velocityGain[i] = Double.parseDouble(cmd.get(i + 2));

					// Notify the calling function (this is a synchronous call)
					_velocityGainLock.notifyAll();
				}
			} catch (NumberFormatException e) {
				Log.w(logTag, "Received corrupt gain function: " + cmd);
			}
			break;
		case GET_GYRO_FN:
			// Check size of function
			if (cmd.size() != 4) {
				Log.w(logTag, "Received corrupt gyro function: " + cmd);
				return;
			}

			// Update the gyro reading
			try {
				for (int i = 0; i < 3; i++)
					_gyroReadings[i] = Double.parseDouble(cmd.get(i + 1));
				filter.gyroUpdate(_gyroReadings[2], System.currentTimeMillis());
				logger.info("GYRO: " + cmd);
			} catch (NumberFormatException e) {
				for (int i = 0; i < 3; i++)
					_gyroReadings[i] = Double.NaN;
				Log.w(logTag, "Received corrupt gyro reading: " + cmd);
			}

			break;
		case GET_RUDDER_FN:
			logger.info("RUDDER: " + cmd);
			break;
		case GET_THRUST_FN:
			logger.info("THRUST: " + cmd);
			break;
		case GET_TE_FN:
			// Check size of function
			if (cmd.size() != 4) {
				Log.w(logTag, "Received corrupt sensor function: " + cmd);
				return;
			}
			
			// Broadcast the sensor reading
			try {
				SensorData reading = new SensorData();
				reading.data = new double[3];
				reading.type = (byte)SensorType.TE.ordinal();
				for (int i = 0; i < 3; i++)
					reading.data[i] = Double.parseDouble(cmd.get(i + 1));
				sendSensor(0, reading);
				logger.info("TE: " + cmd);
			} catch (NumberFormatException e) {
				Log.w(logTag, "Received corrupt sensor reading: " + cmd);
			}
			
			break;
		default:
			Log.w(logTag, "Received unknown function type: " + cmd);
			break;
		}
	}

	/**
	 * Waits for incoming Amarino data from our device, assembles it into a list
	 * of strings, then takes that and calls onCommand with it.
	 */
	public BroadcastReceiver dataCallback = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// The device address from which the data was sent
			final String address = intent
					.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);

			// Ignore data from other devices
			if (!address.equalsIgnoreCase(_arduinoAddr))
				return;

			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(
					AmarinoIntent.EXTRA_DATA_TYPE, -1);

			// Read in data as string and add to queue for processing
			if (dataType == AmarinoIntent.STRING_EXTRA) {
				String newCmd = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				
				// If a command is completed, attempt to execute it
				if (newCmd.indexOf('\r') >= 0 || newCmd.indexOf('\n') >= 0) {
					try {
						onCommand(_partialCommand);
					} catch (Throwable t) {
						Log.e(logTag, "Command failed:", t);
					}
					_partialCommand.clear();
				} else {
					// Otherwise, just add this command to the list
					_partialCommand.add(newCmd);
				}
			}
		}
	};

	/**
	 * Listens for connection and disconnection of the vehicle controller.
	 */
	public BroadcastReceiver connectionCallback = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// Check for events indicating connection or disconnection
			if (intent.getAction().equals(AmarinoIntent.ACTION_CONNECTED)) {
				final String address = intent
						.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
				if (!address.equalsIgnoreCase(_arduinoAddr))
					return;

				Log.i(logTag, "Connected to " + _arduinoAddr);
				setConnected(true);
			} else if (intent.getAction().equals(
					AmarinoIntent.ACTION_DISCONNECTED)) {
				final String address = intent
						.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
				if (!address.equalsIgnoreCase(_arduinoAddr))
					return;

				Log.i(logTag, "Disconnected from " + _arduinoAddr);
				setConnected(false);
			} else if (intent.getAction().equals(
					AmarinoIntent.ACTION_CONNECTED_DEVICES)) {
				final String[] devices = intent
						.getStringArrayExtra(AmarinoIntent.EXTRA_CONNECTED_DEVICE_ADDRESSES);
				if (devices != null)
					for (String device : devices)
						if (device.equalsIgnoreCase(_arduinoAddr)) {
							Log.i(logTag, "Connected to " + _arduinoAddr);
							setConnected(true);
							return;
						}
				Log.i(logTag, "Disconnected from " + _arduinoAddr);
				setConnected(false);
			}
		}
	};

	public synchronized CompressedImage captureImage(int width, int height) {

		byte[] bytes = AirboatCameraActivity.takePhoto(_context, width, height);
		Log.i(logTag, "Sending image [" + bytes.length + "]");
		return toCompressedImage(width, height, bytes);
	}

	public synchronized boolean saveImage() {
		
		AirboatCameraActivity.savePhoto(_context);
		Log.i(logTag, "Saving image.");
		return true;
	}

	@Override
	public SensorType getSensorType(int channel) {
		return _sensorTypes[channel];
	}

	@Override
	public UtmPose getWaypoint() {
		return _waypoint;
	}

	@Override
	public void setSensorType(int channel, SensorType type) {
		_sensorTypes[channel] = type;
	}

	@Override
	public void startCamera(final long numFrames, final double interval,
			final int width, final int height, final ImagingObserver obs) {
		
		// Precompute the timing interval in ms
		final long int_ms = (long)(interval * 1000.0);
		
		// Keep a reference to the capture flag for THIS capture process
		final AtomicBoolean isCapturing = new AtomicBoolean(true);
		
		// Set this to be the current capture flag
		synchronized(_captureLock) {
			if (_isCapturing != null)
				_isCapturing.set(false);		
			_isCapturing = isCapturing;
		}

		// Start a capturing thread
		new Thread(new Runnable() {

 			@Override
			public void run() {
 				int iFrame = 0;

 				// Report the new imaging job in the log file
				logger.info("IMG: " + numFrames + " @ " + interval + "s, " +
						width + " x " + height);
 				
				while (isCapturing.get()
						&& (numFrames <= 0 || iFrame < numFrames)) {

					// Every so often, send out a random picture
					sendImage(captureImage(width, height));
					iFrame++;
					
					// Report status
					if (obs != null)
						obs.imagingUpdate(CameraState.CAPTURING);

					// Wait for a while
					try {
						Thread.sleep(int_ms);
					} catch (InterruptedException ex) {
						return;
					}
				}
				
				// Upon completion, report status 
				// (if isCapturing is still true, we completed on our own)
				if (isCapturing.getAndSet(false)) {
					if (obs != null)
						obs.imagingUpdate(CameraState.DONE);
				} else {
					if (obs != null)
						obs.imagingUpdate(CameraState.CANCELLED);
				}
			}
		}).start();
	}

	@Override
	public void stopCamera() {
		// Stop the thread that sends out images by terminating its
		// navigation flag and then removing the reference to the old flag.
		synchronized(_captureLock) {
			if (_isCapturing != null) {
				_isCapturing.set(false);
				_isCapturing = null;
			}
		}
	}

	double distToGoal(Pose x, Pose y) {
		double dx = x.position.x - y.position.x;
		double dy = x.position.y - y.position.y;
		double dz = 0;  // The z on worldwind messes zs up. Anyway, we use UTM. x.position.z - y.position.z;
		
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	@Override
	public int getNumSensors() {
		return NUM_SENSORS;
	}

	@Override
	public UtmPoseWithCovarianceStamped getState() {
		return _pose;
	}

	/**
	 * Takes a 6D vehicle pose, does appropriate internal computation to change
	 * the current estimate of vehicle state to match the specified pose. Used
	 * for user- or multirobot- pose corrections.
	 * 
	 * @param pose the corrected 6D pose of the vehicle: [x,y,z,roll,pitch,yaw]
	 */
	public void setState(UtmPose pose) {
		
		// Change the offset of this vehicle by modifying filter
		filter.reset(pose, System.currentTimeMillis());
		
		// Copy this pose over the existing value
		_pose.pose.pose.pose = pose.pose.clone();
		_pose.utm = pose.utm.clone();
		
		// Report the new pose in the log file
		logger.info("POSE: " + "[" +
				_pose.pose.pose.pose.position.x + "," +
				_pose.pose.pose.pose.position.y + "," +
				_pose.pose.pose.pose.position.z + "] " +
				Arrays.toString(QuaternionUtils.toEulerAngles(_pose.pose.pose.pose.orientation)));
	}

	public WaypointState getWaypointStatus() {
		synchronized(_navigationLock) {
			if (_isNavigating == null) return WaypointState.OFF;
			return (_isNavigating.get() ? WaypointState.GOING : WaypointState.OFF);
		}
		 
	}
	
	@Override
	public void startWaypoint(final UtmPose waypoint, final String controller, final WaypointObserver obs) {
		
		// Precompute timing interval in ms
		// TODO: use actual time to compute timesteps on the fly for navigation
		final double dt = (double)UPDATE_INTERVAL_MS / 1000.0;
		
		// Keep a reference to the navigation flag for THIS waypoint
		final AtomicBoolean isNavigating = new AtomicBoolean(true);
		
		// Set this to be the current navigation flag
		synchronized(_navigationLock) {
			if (_isNavigating != null)
				_isNavigating.set(false);
			_isNavigating = isNavigating;
			_waypoint = waypoint.clone();
		}
		
		// Start a navigation thread
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				AirboatController vehicleController;
				try {
					vehicleController = AirboatController.valueOf(controller);
				} catch(Exception e) {
					vehicleController = DEFAULT_CONTROLLER;
				}
				
				// Report the new waypoint in the log file
				logger.info("NAV: " + vehicleController + "[" +
						_pose.pose.pose.pose.position.x + "," +
						_pose.pose.pose.pose.position.y + "," +
						_pose.pose.pose.pose.position.z + "] " +
						Arrays.toString(QuaternionUtils.toEulerAngles(_pose.pose.pose.pose.orientation)));
				
				while (isNavigating.get()) {
					
					// If we are not set in autonomous mode, don't try to drive!
					if (!_isAutonomous.get()) {
						// TODO: probably should add a "paused" state
						if (obs != null) 
							obs.waypointUpdate(WaypointState.OFF);
					} else {
						// Report our status as moving toward target
						if (obs != null)
							obs.waypointUpdate(WaypointState.GOING);
						
						// Get the position of the vehicle and the waypoint
						// TODO: fix threading issue (fast stop/start)
						Pose pose = _pose.pose.pose.pose;
						Pose wpPose = waypoint.pose;
	
						// TODO: handle different UTM zones!
	
						// Figure out how to drive to waypoint
						vehicleController.controller.update(AirboatImpl.this, dt);
						
						// TODO: measure dt directly instead of approximating
						
						// Check for termination condition
						// TODO: termination conditions tested by controller
						double dist = distToGoal(pose, wpPose);
						logger.debug("Distance to goal is "+dist);
						if (dist <= 6.0) 
							{
								logger.info("Should reach a waypoint now.");
								break;
							}
					}
					
					// Pause for a while
					try { 
						Thread.sleep(UPDATE_INTERVAL_MS); 
					} catch (InterruptedException e) {}
				}
				
				// Stop the vehicle
				_velocities.linear.x = 0.0;
				_velocities.linear.y = 0.0;
				_velocities.linear.z = 0.0;
				_velocities.angular.x = 0.0;
				_velocities.angular.y = 0.0;
				_velocities.angular.z = 0.0;
				
				// Upon completion, report status 
				// (if isNavigating is still true, we completed on our own)
				if (isNavigating.getAndSet(false)) {
					if (obs != null)
					{
						obs.waypointUpdate(WaypointState.DONE);
						logger.info("Should report reaching a waypoint now.");
						
					}
				} else {
					if (obs != null){
						obs.waypointUpdate(WaypointState.CANCELLED);
						logger.info("Should report cancelling a waypoint now.");
					}
				}
			}
		}).start();
	}
	
	@Override
	public void stopWaypoint() {
		
		// Stop the thread that is doing the "navigation" by terminating its
		// navigation flag and then removing the reference to the old flag.
		synchronized(_navigationLock) {
			if (_isNavigating != null) {
				_isNavigating.set(false);
				_isNavigating = null;
			}
			_waypoint = null;
		}
	}

	/**
	 * Returns the current estimated 6D velocity of the vehicle.
	 */
	public TwistWithCovarianceStamped getVelocity() {
		TwistWithCovarianceStamped twistMsg = new TwistWithCovarianceStamped();
		twistMsg.header.stamp = _wallclock.getCurrentTime();
		twistMsg.twist.twist = _velocities.clone();
		return twistMsg;
	}

	/**
	 * Sets a desired 6D velocity for the vehicle.
	 */
	public void setVelocity(Twist vel) {
		_velocities = vel.clone();
	}

	@Override
	public CameraState getCameraStatus() {
		synchronized(_captureLock) {
			if (_isCapturing == null) return CameraState.OFF;
			return (_isCapturing.get() ? CameraState.CAPTURING : CameraState.OFF);
		}
	}

	@Override
	public boolean isAutonomous() {
		return _isAutonomous.get();
	}

	@Override
	public void setAutonomous(boolean isAutonomous) {
		_isAutonomous.set(isAutonomous);
		
		// Set velocities to zero to allow for safer transitions
		_velocities.linear.x = 0.0;
		_velocities.linear.y = 0.0;
		_velocities.linear.z = 0.0;
		_velocities.angular.x = 0.0;
		_velocities.angular.y = 0.0;
		_velocities.angular.z = 0.0;
	}

	/**
	 * Performs cleanup functions in preparation for stopping the server.
	 */
	public void shutdown() {
		stopWaypoint();
		stopCamera();
		
		_isAutonomous.set(false);
		_isConnected.set(false);
	}
}
