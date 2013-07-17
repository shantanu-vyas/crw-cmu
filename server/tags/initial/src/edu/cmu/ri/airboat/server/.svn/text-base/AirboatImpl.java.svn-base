package edu.cmu.ri.airboat.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.code.microlog4android.LoggerFactory;

import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import edu.cmu.ri.airboat.interfaces.AirboatFilter;
import edu.cmu.ri.airboat.interfaces.AirboatSensor;

import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

/**
 * Contains the actual implementation of vehicle functionality, accessible as a 
 * singleton that is updated and maintained by a background service. 
 * 
 * @author pkv
 *
 */
public class AirboatImpl implements AirboatCommand, AirboatControl, AirboatSensor {
	private static final com.google.code.microlog4android.Logger logger = LoggerFactory.getLogger();
	private static final String logTag = AirboatImpl.class.getName();
	
	/**
	 * Defines the PID gains that will be returned if there is an error.
	 */
	public static final double[] NAN_GAINS = new double[] { Double.NaN, Double.NaN, Double.NaN };
	
	// Define Amarino function control codes
	public static final char GET_GYRO_FN = 'g';
	public static final char GET_RUDDER_FN = 'r';
	public static final char GET_THRUST_FN = 't';
	public static final char GET_TE_FN = 's';
	public static final char SET_VELOCITY_FN = 'v';
	public static final char GET_GAINS_FN = 'l';
	public static final char SET_GAINS_FN = 'k';
	
	// Set timeout for asynchronous Amarino calls
	public static final int RESPONSE_TIMEOUT_MS = 250;       //was 200 earlier

	// Status information
	boolean _isConnected = true;
	boolean _isAutonomous = false;
	
	// High-level autonomy
	AirboatControllerLibrary _controller = AirboatControllerLibrary.POINT_AND_SHOOT;
	double[] _waypoint = new double[6];	
	
	// UTM zone information
	boolean _utmHemiNorth = true;
	int _utmZone = 17;
	
	// Internal data structures for Amarino callbacks
	final Context _context;
	final String _arduinoAddr; 
	final List<String> _partialCommand = new ArrayList<String>(10);
	final Object _velocityGainLock = new Object();
	double[] _velocityGain = new double[3];
	double _velocityGainAxis = -1;
	
	/**
	 * Inertial state vector, currently containing a 6D pose estimate: 
	 * [x,y,z,roll,pitch,yaw]
	 */
	double[] _pose = new double[6];
	
	/**
	 * Filter used internally to update the current pose estimate
	 */
	AirboatFilter filter = new SimpleFilter();
	
	/**
	 * Inertial velocity vector, containing a 6D angular velocity estimate: 
	 * [rx, ry, rz, rPhi, rPsi, rOmega]
	 */
	double[] _velocities = new double[6];
	
	/**
	 * Raw gyroscopic readings, as reported from the Arduino.
	 */
	double[] _gyroReadings = new double[3];

	/**
	 * Creates a new instance of the vehicle implementation.  This function 
	 * should only be used internally when the corresponding vehicle service
	 * is started and stopped.
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
		logger.info("POSE: " + Arrays.toString(_pose));
		
		// If the vehicle is currently autonomous and has a valid navigation
		// controller, update the controller to get new commanded velocities
		if (_isAutonomous) {
			if (_controller != null) {
				_controller.controller.update(this, this, dt);
			} else {
				Log.w(logTag, "No controller update.");
			}
		}
		
		// Call Amarino with new velocities here
		Amarino.sendDataToArduino(_context, _arduinoAddr, SET_VELOCITY_FN, new float[] {
				(float)_velocities[0], (float)_velocities[1], (float)_velocities[2], 
				(float)_velocities[3], (float)_velocities[4], (float)_velocities[5]
			}); // Yes, I know this looks silly, but Amarino doesn't handle doubles
	}
	
	/**
	 * Returns the current state estimate of vehicle pose in 6D.
	 * 
	 * @return a 6D pose estimate: [x,y,z,roll,pitch,yaw]
	 */
	public double[] getPose() {
		return _pose;
	}
	
	/**
	 * Takes a 6D vehicle pose, does appropriate internal computation to
	 * change the current estimate of vehicle state to match the specified
	 * pose.  Used for user- or multirobot- pose corrections.
	 * 
	 * @param pose the corrected 6D pose of the vehicle: [x,y,z,roll,pitch,yaw]
	 */
	public boolean setPose(double[] pose) {
		if (pose.length != 6)
			throw new IllegalArgumentException("State should have length 6");
		
		// Change the offset of this vehicle by modifying filter
		filter.reset(pose, System.currentTimeMillis());
		logger.info("POSE: " + Arrays.toString(pose));
		return true;
	}
	
	/**
	 * Returns the current estimated 6D velocity of the vehicle.
	 * 
	 * @see AirboatControl#getVelocity()
	 */
	public double[] getVelocity() {
		return _velocities;
	}

	/**
	 * Sets a desired 6D velocity for the vehicle.
	 * 
	 * @see AirboatControl#setVelocity(double[])
	 */
	public boolean setVelocity(double[] vel) {
		if (vel.length != 6)
			throw new IllegalArgumentException("Velocity should have length 6");
	
		_velocities = vel;
		logger.info("SETVEL: " + Arrays.toString(_velocities));
		return true;
	}

	/**
	 * Retrieves the current waypoint being executed, as a 6D global pose:
	 * [ northing, easting, down, roll, pitch, yaw ]
	 * 
	 * @return the current waypoint, or null if there is no current waypoint.
	 */
	public double[] getWaypoint() {

		// Make copy of array to protect our waypoint from modification
		double[] waypoint = new double[_waypoint.length];
		System.arraycopy(_waypoint, 0, waypoint, 0, waypoint.length);
		return waypoint;
	}

	/**
	 * Sets the current waypoint for the vehicle to reach.  If the vehicle is
	 * set to autonomous mode, it will use the currently specified controller 
	 * to try and reach this waypoint.
	 * 
	 * @see AirboatCommand#setWaypoint(double[])
	 * @see AirboatCommand#setController(String)
	 */
	public boolean setWaypoint(double[] loc) {
		
		if (loc.length != 6)
			throw new IllegalArgumentException("Location should have length 6");
	
		// Copy new waypoint into existing data structure
		System.arraycopy(loc, 0, _waypoint, 0, _waypoint.length);
		logger.info("WPT: " + Arrays.toString(loc));
		return true;
	}
	
	public boolean isWaypointComplete() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isUTMHemisphereNorth() {
		return _utmHemiNorth;
	}
	
	public int getUTMZone() {
		return _utmZone;
	}
	
	public boolean setUTMZone(int zone, boolean isNorth) {
		_utmZone = zone;
		_utmHemiNorth = isNorth;
		logger.info("UTM: " + zone + " " + (isNorth ? "North" : "South"));
		return true;
	}
	
	/**
	 * @see AirboatCommand#isAutonomous()
	 */
	public boolean isAutonomous() {
		return _isAutonomous;
	}
	
	/**
	 * @see AirboatControl#setAutonomous(boolean)
	 */
	public boolean setAutonomous(boolean isAutonomous) {
		_isAutonomous = isAutonomous;
		logger.info("AUTO: "+ _isAutonomous);
		return true;
	}

	/**
	 * @see AirboatCommand#getController()
	 */
	public String getController() {
		return _controller.name();
	}

	/**
	 * @see AirboatCommand#setController()
	 */
	public boolean setController(final String controlName) {
		try {
			// Attempt to use a controller with the specified name
			_controller = AirboatControllerLibrary.valueOf(controlName);
			logger.info("CTRL: " + _controller.name());
			return true;
		} catch (IllegalArgumentException ex) {
			
			// If it does not exist, report failure
			return false;
		}
	}
	
	/**
	 * @see AirboatCommand#getControllers()
	 */
	public String[] getControllers() {
		AirboatControllerLibrary[] controllers = AirboatControllerLibrary.values();
		String[] controllerNames = new String[controllers.length];
		
		for (int i = 0; i < controllers.length; ++i) {
			controllerNames[i] = controllers[i].name();
		}
		
		return controllerNames;
	}
	
	/**
	 * @see AirboatControl#getVelocityGain(double)
	 */
	public double[] getVelocityGain(double axis) {
		
		// Call Amarino here
		Amarino.sendDataToArduino(_context, _arduinoAddr, GET_GAINS_FN, axis); 
		
		// Wait for response here (only if connected)
		if (_isConnected) {
			synchronized(_velocityGainLock) {
				
				// Clear any old return value
				_velocityGainAxis = -1;
				
				try {
					// Wait for the correct axis to be filled in, 
					// but if we start getting too backed up, drop calls
					for (int i = 0; i < 3 && _velocityGainAxis != axis; ++i)
						_velocityGainLock.wait(RESPONSE_TIMEOUT_MS);
				} catch(InterruptedException ex) {
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
	 * @see AirboatControl#setVelocityGain(double, double, double, double)
	 */
	public boolean setVelocityGain(double axis, double kp, double ki, double kd) {
		// Call Amarino here
		Amarino.sendDataToArduino(_context, _arduinoAddr, SET_GAINS_FN, new float[] {(float)axis, (float)kp, (float)ki, (float)kd} );
		logger.info("SETGAINS: " + axis + " " + kp + ", " + ki + ", " + kd);
		return true;
	}

	/**
	 * @see AirboatSensor#getGyro()
	 */
	public double[] getGyro() {
		return _gyroReadings;
	}
	
	/**
	 * @see AirboatCommand#isConnected()
	 */
    public boolean isConnected() {
		return _isConnected;
	}
	
	/**
	 * Internal function used to set the connection status of this object 
	 * (indicating whether currently in contact with vehicle controller).
	 */
	protected void setConnected(boolean isConnected) {
		_isConnected = isConnected;
	}

	/**
	 * Handles complete Arduino commands, once they are reassembled.
	 * 
	 * @param cmd the list of arguments composing a command
	 */
	protected void onCommand(List<String> cmd) {
		
		// Just like Amarino, we use a flag to differentiate channels
		switch(cmd.get(0).charAt(0)) {
		case GET_GAINS_FN:
			// Check size of function
			if (cmd.size() != 6) {
				Log.w(logTag, "Received corrupt gain function: " + cmd);
				return;
			}

			// Return a set of PID values
			try {
				synchronized(_velocityGainLock) {
					// Read in the axis that was filled in
					_velocityGainAxis = Double.parseDouble(cmd.get(1));

					// Cast the parameters to double-valued gains
					for (int i = 0; i < 3; i++)
						_velocityGain[i] = Double.parseDouble(cmd.get(i+2));
					
					// Notify the calling function (this is a synchronous call)
					_velocityGainLock.notifyAll();
				}
			} catch (NumberFormatException e) {
				Log.w(logTag, "Received corrupt gain function: " + cmd);
			}
			break;
		case GET_GYRO_FN:
			// Check size of function
			if (cmd.size() != 5) {
				Log.w(logTag, "Received corrupt gyro function: " + cmd);
				return;
			}
			
			// Update the gyro reading
			try {
				for (int i = 0; i < 3; i++)
					_gyroReadings[i] = Double.parseDouble(cmd.get(i+1));
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
			logger.info("TE: " + cmd);
			break;
		default:
			Log.w(logTag, "Received unknown function type: " + cmd);
			break;
		}
	}
	
	/**
	 * Waits for incoming Amarino data from our device, assembles it into
	 * a list of strings, then takes that and calls onCommand with it.
	 */
	public BroadcastReceiver dataCallback = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			// The device address from which the data was sent
			final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			
			// Ignore data from other devices
			if (!address.equalsIgnoreCase(_arduinoAddr)) return;
			
			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			
			// Read in data as string and add to queue for processing
			if (dataType == AmarinoIntent.STRING_EXTRA){
				String newCmd = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				_partialCommand.add(newCmd);
				
				// If a command is completed, attempt to execute it
				if (newCmd.indexOf('\r') >= 0 || newCmd.indexOf('\n') >= 0) {
					try{
						onCommand(_partialCommand);
					} catch(Throwable t){
						Log.e(logTag, "Command failed:", t);
					}
					_partialCommand.clear();
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
				final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
				if (!address.equalsIgnoreCase(_arduinoAddr)) 
					return;
				
				Log.i(logTag, "Connected to " + _arduinoAddr);
				setConnected(true);
			} else if (intent.getAction().equals(AmarinoIntent.ACTION_DISCONNECTED)) {
				final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
				if (!address.equalsIgnoreCase(_arduinoAddr)) 
					return;
				
				Log.i(logTag, "Disconnected from " + _arduinoAddr);
				setConnected(false);
			} else if (intent.getAction().equals(AmarinoIntent.ACTION_CONNECTED_DEVICES)) {
				final String[] devices = intent.getStringArrayExtra(AmarinoIntent.EXTRA_CONNECTED_DEVICE_ADDRESSES);
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
	
	public synchronized byte[] getImage() {
		
		byte[] bytes =  AirboatCameraActivity.takePhoto(_context);
		Log.i(logTag, "Sending image [" + bytes.length + "]");
		return bytes;
	}
	
	public synchronized boolean saveImage() {
		
		AirboatCameraActivity.savePhoto(_context);
		Log.i(logTag, "Saving image.");
		return true;
	}

}
