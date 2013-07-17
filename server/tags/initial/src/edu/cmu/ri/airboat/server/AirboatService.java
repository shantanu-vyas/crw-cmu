package edu.cmu.ri.airboat.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

import com.flat502.rox.server.XmlRpcServer;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.google.code.microlog4android.format.PatternFormatter;

import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.interfaces.AirboatControl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import at.abraxas.amarino.AmarinoIntent;

public class AirboatService extends Service {
	private static final String TAG = AirboatService.class.getName();
	private static final com.google.code.microlog4android.Logger logger = LoggerFactory.getLogger();
	
	// Default values for parameters
	private static final String DEFAULT_LOG_PREFIX = "airboat_";
	private static final int DEFAULT_UPDATE_RATE = 200;
	private static final int DEFAULT_RPC_PORT = 5000; 
	final int GPS_UPDATE_RATE = 200; //in milliseconds
	
	// Intent fields definitions
	public static final String BD_ADDR = "BD_ADDR";
	public static final String RPC_PORT = "RPC_PORT";
	public static final String UPDATE_RATE = "UPDATE_RATE";
	
	// Binder object that receives interactions from clients.
    private final IBinder _binder = new AirboatBinder();
    
	// Flag indicating run status (Android has no way to query if a service is running)
	public static boolean isRunning = false;

	// Member parameters 
	private int _updateRate;
	private int _rpcPort; 
	private String _arduinoAddr;
	
	// Objects implementing actual functionality
	private AirboatImpl _airboatImpl;
	private XmlRpcServer _rpcServer;
	
	// Timers for update function
	private Timer _timer;
	private TimerTask _updateTask;
	
	// Logger that pipes log information for airboat classes to file
	private FileAppender _fileAppender; 
	
	// Raw accelerometer values (pre-integration with magnetometer)
    private float[] accelerometerValues;

	/**
	 * Handles GPS updates by calling the appropriate update.
	 */
	private LocationListener locationListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
        
        public void onLocationChanged(Location location) {
        	
        	// Convert from lat/long to UTM coordinates
        	UTM utmLoc = UTM.latLongToUtm(
        				LatLong.valueOf(location.getLatitude(), location.getLongitude(), NonSI.DEGREE_ANGLE), 
        				ReferenceEllipsoid.WGS84
        			);
        	
        	// Apply update using filter object
        	if (_airboatImpl != null) {
        		_airboatImpl.setUTMZone(utmLoc.longitudeZone(), (utmLoc.latitudeZone() > 'M'));
        		_airboatImpl.filter.gpsUpdate(utmLoc.northingValue(SI.METER), utmLoc.eastingValue(SI.METER), location.getTime());
        		logger.info("GPS: " + utmLoc);
        	}
        }
      };
    
    /**
  	 * Handles accelerometer updates by calling the appropriate update.
  	 */
    private final SensorEventListener accelerometerListener = new SensorEventListener() {
		public void onAccuracyChanged(Sensor arg0, int arg1) {}
		
		public void onSensorChanged(SensorEvent event) {
			
			// Log these values, but do not use them directly in the filter
			accelerometerValues = event.values;
			logger.info("ACCELEROMETER: " + Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]));
		}
    };
    
    /**
  	 * Handles magnetometer updates by calling the appropriate update.
  	 */
    private final SensorEventListener magneticListener = new SensorEventListener() {
    	
    	float[] R = new float[9];
		float[] I = new float[9];
		float[] values = new float[3];
    	
    	public void onAccuracyChanged(Sensor arg0, int arg1) {}
		
		public void onSensorChanged(SensorEvent event) {
			if (accelerometerValues == null)
				return;
			
			// Combine magnetometer and accelerometer to get orientation
			SensorManager.getRotationMatrix(R, I, accelerometerValues, event.values);
			SensorManager.getOrientation(R, values);
			logger.info("ORIENTATION: " + Arrays.toString(values));

			// Extract heading from orientation and use in filter
			if (_airboatImpl != null) {
				double heading = values[0];
				_airboatImpl.filter.compassUpdate(heading, System.currentTimeMillis());
				logger.info("COMPASS: " +  heading);
			}
		}
    };

	
	/**
	 * Class that calls the periodic update function on the vehicle 
	 * implementation object.
	 */
	private static class UpdateTask extends TimerTask {
		AirboatService _service;
		long _lastUpdateMs = 0;
		
		public UpdateTask(AirboatService service) {
			_service = service;
		}
		
		@Override
		public void run() {
			if (_service._airboatImpl != null) {
				// Compute the number of milliseconds since last update
				// (or 0 if this is the first update)
				long currentUpdateMs = SystemClock.elapsedRealtime();
				long elapsedMs = (_lastUpdateMs > 0) ? currentUpdateMs - _lastUpdateMs : 0;			
				
				// Trigger the server update function for this interval
				_service._airboatImpl.update((elapsedMs / 1000.0));
				_lastUpdateMs = currentUpdateMs;
			}
		}
	}
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't deal with IPC.
     */
    public class AirboatBinder extends Binder {
        AirboatService getService() {
            return AirboatService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return _binder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Disable all DNS lookups (safer for private/ad-hoc networks)
		AirboatSecurityManager.load();
		
		_timer = new Timer();
		_updateTask = new UpdateTask(this);
		isRunning = true;
		
		// start tracing to "/sdcard/calc.trace"
		// Debug.startMethodTracing("trace_airboat");
		
		// TODO: optimize this to allocate resources up here and handle multiple start commands
	}

	/**
	 * Access method to get underlying implementation of server functionality.
	 * @return An interface allowing high-level control of the boat.
	 */
	public AirboatCommand getServer() {
		return _airboatImpl;
	}

	/**
	 * Access method to get underlying implementation of controller functionality.
	 * @return An interface allowing low-level control of the boat.
	 */
	public AirboatControl getController() {
		return _airboatImpl;
	}
	
	/** 
     * Constructs a default filename from the current date and time.
     * @return the default filename for the current time.
     */
    private static String defaultLogFilename() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
        return DEFAULT_LOG_PREFIX + sdf.format(d) + ".txt";
    }
    
    /**
     * Main service initialization: called whenever a request is
     * made to start the Airboat service.  
     * 
     * This is where the vehicle implementation is started, sensors are 
     * registered, and the update loop and RPC server are started.   
     */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		// Ignore startup requests that don't include a device name
		if (!intent.hasExtra(BD_ADDR))
			return Service.START_STICKY;
		
		// Ensure that we do not reinitialize if not necessary
		if (_airboatImpl != null || _rpcServer != null)
			return Service.START_STICKY;

		// Set up logging format to include time, tag, and value
        PropertyConfigurator.getConfigurator(this).configure();		    
		PatternFormatter formatter = new PatternFormatter(); 
		formatter.setPattern("%r %d %m %T");
		
		// Set up and register data logger to a date-stamped file
		String logFilename = defaultLogFilename();
		_fileAppender = new FileAppender();
		_fileAppender.setFileName (logFilename);
		_fileAppender.setAppend(true);
		_fileAppender.setFormatter (formatter);
		try {
			_fileAppender.open();
		} catch (IOException e) {
			Log.w(TAG, "Failed to open data log file: " + logFilename, e);
		}
	    logger.addAppender (_fileAppender);
		
		// Hook up to necessary Android sensors
        SensorManager sm; 
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor compass  = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(magneticListener, compass, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        
        // Hook up to the GPS system
        LocationManager gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setPowerRequirement(Criteria.NO_REQUIREMENT);
        String provider = gps.getBestProvider(c, false);
        gps.requestLocationUpdates(provider, GPS_UPDATE_RATE, 0, locationListener);
		
        // Get necessary connection parameters
		_arduinoAddr = intent.getStringExtra(BD_ADDR);
		_updateRate = intent.getIntExtra(UPDATE_RATE, DEFAULT_UPDATE_RATE);
		_rpcPort = intent.getIntExtra(RPC_PORT, DEFAULT_RPC_PORT);
		
        // Create a filter that listens to Amarino connection events
        IntentFilter amarinoFilter = new IntentFilter();
        amarinoFilter.addAction(AmarinoIntent.ACTION_CONNECTED_DEVICES);
        amarinoFilter.addAction(AmarinoIntent.ACTION_CONNECTED);
        amarinoFilter.addAction(AmarinoIntent.ACTION_DISCONNECTED);
        sendBroadcast(new Intent(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES));
		
		// Create the data object
		_airboatImpl = new AirboatImpl(this, _arduinoAddr);
		registerReceiver(_airboatImpl.dataCallback, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		registerReceiver(_airboatImpl.connectionCallback, amarinoFilter);
		
		// Create an XML-RPC service to expose the data object
		try {
			_rpcServer = new XmlRpcServer(_rpcPort);
			_rpcServer.registerProxyingHandler(null, "^command\\.(.*)", _airboatImpl);
			_rpcServer.registerProxyingHandler(null, "^control\\.(.*)", _airboatImpl);
			_rpcServer.start();
		} catch (IOException e) {
			Log.e(TAG, "XML-RPC failed", e);
		} catch (SecurityException e) {
			Log.e(TAG, "XML-RPC failed", e);
		}
		
		// Start a regular update function
		_timer.scheduleAtFixedRate(_updateTask, 0, _updateRate);
		Log.i(TAG,"AirboatService started.");
		
		// Log the velocity gains before starting the service
		// TODO: get initial velocity gains logged
		new Thread(new Runnable() {
		
			public void run() {
				try { Thread.sleep(5000); } catch (InterruptedException ex) { }
				
				double[] velGains;
				if (_airboatImpl != null) {
					velGains = _airboatImpl.getVelocityGain(0);
					logger.info("PIDGAINS: " + "0 " + velGains[0] + "," + velGains[1] + "," + velGains[2]);
					velGains = _airboatImpl.getVelocityGain(5);
					logger.info("PIDGAINS: " + "5 " + velGains[0] + "," + velGains[1] + "," + velGains[2]);
				}
			}
		}).start();
		
		// Indicate that the service should not be stopped arbitrarily
        return Service.START_STICKY;
	}
	
	/**
	 * Called when there are no longer any consumers of the service and 
	 * stopService has been called.
	 * 
	 * This is where the RPC and update loops are killed, the sensors 
	 * are unregistered, and the current vehicle implementation is unhooked 
	 * from all of its callbacks and discarded (allowing safe spawning of a new
	 * implementation when the service is restarted).
	 */
	@Override
	public void onDestroy() {
		//		Debug.stopMethodTracing();
		
		// Shutdown the regular update function
		_timer.cancel();
		
		// Shutdown the XML-RPC services
		if (_rpcServer != null) {
			try {
				_rpcServer.stop();
			} catch (Exception e) {
				Log.e(TAG, "XML-RPC shutdown error", e);
			}
			_rpcServer = null;
		}
		
		// Disconnect from the Android sensors
        SensorManager sm; 
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(magneticListener);
        sm.unregisterListener(accelerometerListener);
		
        // Disconnect from GPS updates
        LocationManager gps;
        gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gps.removeUpdates(locationListener);
        
		// Disconnect the data object from this service
		if (_airboatImpl != null) {
			unregisterReceiver(_airboatImpl.dataCallback);
			unregisterReceiver(_airboatImpl.connectionCallback);
			_airboatImpl.setConnected(false);
			_airboatImpl = null;
		}

		// Remove the data log (a new one will be created on restart)
		if (_fileAppender != null) {
			logger.removeAppender(_fileAppender);
			try {
				_fileAppender.close();
			} catch (IOException e) {
				Log.e(TAG, "Data log shutdown error", e);
			}
		}

		Log.i(TAG, "AirboatService stopped.");
		isRunning = false;
		super.onDestroy();
	}
}
