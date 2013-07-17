package edu.cmu.ri.airboat.server;

import java.io.IOException;
import java.net.InetAddress;

import org.ros.message.crwlib_msgs.UtmPose;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import edu.cmu.ri.crw.VehicleServer;

/**
 * Runs a background process that verifies that the vehicle server is currently
 * connected to a particular IP address, and changes the current waypoint of
 * the vehicle to a predefined home position if that IP address becomes
 * unreachable.
 * 
 * @author pkv
 *
 */
public class AirboatFailsafeService extends Service {

	private static final String LOG_TAG = "AirboatFailsafeService";
	private static final int SERVICE_ID = 12311;
	
	private final IBinder _binder = new LocalBinder();
	
	// The host name that should be reachable under normal operation
	private String _hostname;
	
	// The "home" position that will be set as a waypoint if a failure occurs
	private UtmPose _homePosition = new UtmPose();
	
	// Contains a reference to the airboat service, or null if service is not running 
	private AirboatService _airboatService = null;
	
	// Indicates if we have a valid reference to the airboat service.
	private boolean _isBound = false;
	
	// Runnable class that performs connection tests
	private ConnectionTest _connectionTest;
	
	// Thread handler that schedules new connection tests
	private Handler _handler;
	
	// Number of successive failures so far
	private int _numFailures = 0;
	
	// Number of allowable successive failures
	private int _numAllowedFailures = 4;
	
	// Period between connection tests
	private int _connectionTestDelayMs = 2000;
	
	// Public field that indicates if service is started
	public static volatile boolean isRunning = false;
	
	/**
	 * Helper class that contains intent tag names.
	 */
	public static final class AirboatFailsafeIntent {
		public static final String HOSTNAME = "FAILSAFE_HOSTNAME";
		public static final String HOME_POSE = "FAILSAFE_POSE";
		public static final String HOME_ZONE = "FAILSAFE_ZONE";
		public static final String HOME_NORTH = "FAILSAFE_NORTH";
	}
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
		AirboatFailsafeService getService() {
            return AirboatFailsafeService.this;
        }
    }
	
	@Override
    public void onCreate() {
		super.onCreate();
		Log.i(LOG_TAG, "onCreate");
		
		_handler = new Handler();
		_connectionTest = new ConnectionTest();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
    	
		// Ignore startup requests that don't include an intent
		if (intent == null) {
			Log.e(LOG_TAG, "Started with null intent.");
			return Service.START_STICKY;
		}

    	Log.i(LOG_TAG, "onStart");
    	doBindService();
    	isRunning = true;
    	
    	 // Get necessary connection parameters
		_hostname = intent.getStringExtra(AirboatFailsafeIntent.HOSTNAME);
		double[] rawHomePose = intent.getDoubleArrayExtra(AirboatFailsafeIntent.HOME_POSE);
		int rawHomeZone = intent.getByteExtra(AirboatFailsafeIntent.HOME_ZONE, (byte)14);
		boolean rawHomeNorth = intent.getBooleanExtra(AirboatFailsafeIntent.HOME_NORTH, true);

		// Decode pose from intents
		_homePosition.pose.position.x = rawHomePose[0];
		_homePosition.pose.position.y = rawHomePose[1];
		_homePosition.pose.position.z = rawHomePose[2];
		_homePosition.utm.zone = (byte)rawHomeZone;
		_homePosition.utm.isNorth = rawHomeNorth;
		
		// Schedule the next connection test
		_handler.postDelayed(_connectionTest, _connectionTestDelayMs);
		
		// This is now a foreground service
		{
			// Set up the icon and ticker text
			int icon = R.drawable.icon; // TODO: change this to notification icon
			CharSequence tickerText = "Running normally.";
			long when = System.currentTimeMillis();
		
			// Set up the actual title and text
			Context context = getApplicationContext();
			CharSequence contentTitle = "Failsafe Server";
			CharSequence contentText = tickerText;
			Intent notificationIntent = new Intent(this, AirboatActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
			// Add a notification to the menu
			Notification notification = new Notification(icon, tickerText, when);
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		    startForeground(SERVICE_ID, notification);
		}
		
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.i(LOG_TAG, "onDestroy");
    	doUnbindService();
    	isRunning = false;
    	
        _handler.removeCallbacks(_connectionTest);
        _handler = null;
        
        // Remove service from foreground
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }
    
    /**
     * Runnable that tries to get an ICMP echo from a particular IP address,
     * and changes the current waypoint if the echo is not returned.
     */
    class ConnectionTest implements Runnable {

		@Override
		public void run() {
			
			// If there is no vehicle server, then there's nothing to do here
			if (_airboatService == null) return;
			VehicleServer server = _airboatService.getServer();
			
			// Test for connectivity to the specified server
			try {
				if (InetAddress.getByName(_hostname).isReachable(500)) {
					_numFailures = 0;
					return;
				}
			} catch (IOException e) {
				Log.i(LOG_TAG, "Connection failure: " + e.getMessage());
			}
			
			// If the connection failed, trigger the failsafe behavior
			_numFailures++;
			if (_numFailures > _numAllowedFailures) {
				Log.i(LOG_TAG, "Failsafe triggered: [" +
						_homePosition.pose.position.x + "," +
						_homePosition.pose.position.y + "," +
						_homePosition.pose.position.z + "] " +
						_homePosition.utm.zone + (_homePosition.utm.isNorth ? "North" : "South"));
				server.startWaypoint(_homePosition, null, null);
				_numFailures = 0;
			}
			
			// Schedule the next connection test
			if (isRunning) {
				_handler.postDelayed(_connectionTest, _connectionTestDelayMs);
			}
		}    	
    }
    
    /** 
     * Listener that handles changes in connections to the airboat service 
     */ 
    private ServiceConnection _connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.
            _airboatService = ((AirboatService.AirboatBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            _airboatService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation.
        if (!_isBound) {
        	bindService(new Intent(this, AirboatService.class), _connection, Context.BIND_AUTO_CREATE);
        	_isBound = true;
        }
    }

    void doUnbindService() {
        // Detach our existing connection.
    	if (_isBound) {
            unbindService(_connection);
            _isBound = false;
        }
    }

}
