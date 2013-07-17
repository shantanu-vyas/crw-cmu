package edu.cmu.ri.airboat.server;

import edu.cmu.ri.airboat.server.AirboatFailsafeService.ConnectionTest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.Window;

public class AirboatObstAvoidanceActivity extends Activity {
	private static final String TAG = AirboatObstAvoidanceActivity.class.getName();
	
	static AirboatObstAvoidanceView aoav;
	private ConnectionTest _connectionTest;
	private Intent mServiceIntent;
	
	  public AirboatObstAvoidanceActivity() {
	        Log.i(TAG, "Instantiated new " + this.getClass());
	    }
	  
	  /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        Log.i(TAG, "onCreate");
	        super.onCreate(savedInstanceState);
	        
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        //doBindService();
	        mServiceIntent = new Intent(this, AirboatService.class);
	        
	        bindService(mServiceIntent, _connection, Context.BIND_AUTO_CREATE);
	        aoav = new AirboatObstAvoidanceView(this);
	        setContentView(aoav);
	        
	    	
		    
	    } 
	    
	    
	    /**
		 * Takes a picture and immediately saves it to disk.
		 * 
		 * @param context the context of the calling Activity or Service
		 */
		public static synchronized int[] getObstacles(final Context context) {
			return aoav.obstacleLocations;
		}
		
		public AirboatService airboatService = null;
		private boolean _isBound = false;

	
		 /** 
	     * Listener that handles changes in connections to the airboat service 
	     */ 
	    private ServiceConnection _connection = new ServiceConnection() {
	        public void onServiceConnected(ComponentName className, IBinder service) {
	            // This is called when the connection with the service has been
	            // established, giving us the service object we can use to
	            // interact with the service.
	        	airboatService = ((AirboatService.AirboatBinder)service).getService();
	        }

	        public void onServiceDisconnected(ComponentName className) {
	            // This is called when the connection with the service has been
	            // unexpectedly disconnected -- that is, its process crashed.
	        	airboatService = null;
	        } 
	    };
	    
//	    private OrientationEventListener orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
//
//			@Override
//			public void onOrientationChanged(int orientation) {
//				// TODO Auto-generated method stub
//				int x=100;
//				x=x+10;
//			}
//	    };

	    
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
