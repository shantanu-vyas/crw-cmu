package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ros.actionlib.client.SimpleActionClientCallbacks;
import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.exception.RemoteException;
import org.ros.exception.RosException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.message.MessageListener;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.crwlib_msgs.VehicleImageCaptureFeedback;
import org.ros.message.crwlib_msgs.VehicleImageCaptureGoal;
import org.ros.message.crwlib_msgs.VehicleImageCaptureResult;
import org.ros.message.crwlib_msgs.VehicleNavigationFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationResult;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;
import org.ros.namespace.NameResolver;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;
import org.ros.service.crwlib_msgs.CaptureImage;
import org.ros.service.crwlib_msgs.GetCameraStatus;
import org.ros.service.crwlib_msgs.GetNumSensors;
import org.ros.service.crwlib_msgs.GetGains;
import org.ros.service.crwlib_msgs.GetSensorType;
import org.ros.service.crwlib_msgs.GetState;
import org.ros.service.crwlib_msgs.GetVelocity;
import org.ros.service.crwlib_msgs.GetWaypoint;
import org.ros.service.crwlib_msgs.GetWaypointStatus;
import org.ros.service.crwlib_msgs.IsAutonomous;
import org.ros.service.crwlib_msgs.ResetLog;
import org.ros.service.crwlib_msgs.SetAutonomous;
import org.ros.service.crwlib_msgs.SetGains;
import org.ros.service.crwlib_msgs.SetSensorType;
import org.ros.service.crwlib_msgs.SetState;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.CrwNetworkUtils;
import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.WaypointObserver;

/**
 * Takes the node name of an existing RosVehicleServer and connects through ROS,
 * wrapping the functionality of a VehicleServer transparently. Once connected,
 * this object can be used as a vehicle server, but all commands will be
 * forwarded to the underlying ROS node.
 * 
 * Note: To deal with ROS's occasional intolerance of null values, functions
 * that receive object references may sanitize null references into empty 
 * objects.  For example, a null String may be replaced by "". 
 * 
 * @author pkv
 * @author kss
 * 
 */
public class RosVehicleProxy extends AbstractVehicleServer {

	public static final Logger logger = 
		Logger.getLogger(RosVehicleProxy.class.getName());

	public static final String DEFAULT_NODE_NAME = "vehicle_client";
	public static final int SERVICE_TIMEOUT_MS = 1000;
	
	protected final URI _masterUri;
	protected final String _nodeName;
	
	protected Node _node;
	
	protected RosVehicleNavigation.Client _navClient; 
	protected RosVehicleImaging.Client _imgClient;
	
	protected Publisher<Twist> _velocityPublisher;
	
	protected ServiceClient<ResetLog.Request, ResetLog.Response> _resetLog;	
	protected ServiceClient<SetState.Request, SetState.Response> _setStateClient;
	protected ServiceClient<GetState.Request, GetState.Response> _getStateClient;
	protected ServiceClient<CaptureImage.Request, CaptureImage.Response> _captureImageClient;
	protected ServiceClient<GetCameraStatus.Request, GetCameraStatus.Response> _getCameraStatusClient;    
	protected ServiceClient<SetSensorType.Request, SetSensorType.Response> _setSensorTypeClient;    
	protected ServiceClient<GetSensorType.Request, GetSensorType.Response> _getSensorTypeClient;    
	protected ServiceClient<GetNumSensors.Request, GetNumSensors.Response> _getNumSensorsClient;    
	protected ServiceClient<GetVelocity.Request, GetVelocity.Response> _getVelocityClient;    
	protected ServiceClient<IsAutonomous.Request, IsAutonomous.Response> _isAutonomousClient;    
	protected ServiceClient<SetAutonomous.Request, SetAutonomous.Response> _setAutonomousClient;    
	protected ServiceClient<GetWaypoint.Request, GetWaypoint.Response> _getWaypointClient; 
	protected ServiceClient<GetWaypointStatus.Request, GetWaypointStatus.Response> _getWaypointStatusClient;    
	protected ServiceClient<SetGains.Request, SetGains.Response> _setGainsClient;    
	protected ServiceClient<GetGains.Request, GetGains.Response> _getGainsClient;  
	
	public RosVehicleProxy() {
		this(NodeConfiguration.DEFAULT_MASTER_URI, DEFAULT_NODE_NAME);
	}

	public RosVehicleProxy(String nodeName) {
		this(NodeConfiguration.DEFAULT_MASTER_URI, nodeName);
	}

	public RosVehicleProxy(URI masterUri, String nodeName) {
		
		// TODO: Remove this logging setting -- it is a stopgap for a rosjava bug
		Logger.getLogger("org.ros.internal.node.client").setLevel(Level.SEVERE);
		
		_masterUri = masterUri;
		_nodeName = nodeName;
		
		connect();
	}
	
	/**
	 * Performs a single attempt at connecting to a ROS core.  On failure, a
	 * disconnect() should be called to perform any residual cleanup.
	 * 
	 * @return true if the connection was successful, false if it failed
	 */
	protected synchronized boolean connect() {

		// Get a localhost address 
		String host = CrwNetworkUtils.getLocalhost(_masterUri.getHost());
		if (host == null || host.isEmpty()) return false;
		
		// Create a node configuration and start a node
		NodeConfiguration config = NodeConfiguration.newPublic(host, _masterUri);
	    _node = new DefaultNodeFactory().newNode(_nodeName, config);		
		
	    // Start up action clients to run navigation and imaging
	    NodeRunner runner = NodeRunner.newDefault();
	    
	    // Create an action server for vehicle navigation
	    NodeConfiguration navConfig = NodeConfiguration.newPublic(host, _masterUri);
		NameResolver navResolver = NameResolver.create("/nav");
		navConfig.setParentResolver(navResolver);
		
		try {
			_navClient = new RosVehicleNavigation.Spec()
					.buildSimpleActionClient(_nodeName + "_nav");
			runner.run(_navClient, navConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
			return false;
		}

		// Create an action server for image capturing
		NodeConfiguration imgConfig = NodeConfiguration.newPublic(host, _masterUri);
		NameResolver imgResolver = NameResolver.create("/img");
		imgConfig.setParentResolver(imgResolver);
		
		try {
			_imgClient = new RosVehicleImaging.Spec()
					.buildSimpleActionClient(_nodeName + "_img");
			runner.run(_imgClient, imgConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start image action client: " + ex);
			return false;
		}

		// Register subscriber for state
		_node.newSubscriber("state", "crwlib_msgs/UtmPoseWithCovarianceStamped", 
				new MessageListener<UtmPoseWithCovarianceStamped>() {

			@Override
			public void onNewMessage(UtmPoseWithCovarianceStamped pose) {
				sendState(pose);
			}
		});
		
		// Register subscriber for imaging
		_node.newSubscriber("image/compressed", "sensor_msgs/CompressedImage", 
				new MessageListener<CompressedImage>() {

			@Override
			public void onNewMessage(CompressedImage image) {
				sendImage(image);
			}
		});
		
		// Register subscriber for velocity
		_node.newSubscriber("velocity", "geometry_msgs/TwistWithCovarianceStamped", 
				new MessageListener<TwistWithCovarianceStamped>() {

			@Override
			public void onNewMessage(TwistWithCovarianceStamped velocity) {
				sendVelocity(velocity);
			}
		});
		
		// Register subscriber for sensor
		_node.newSubscriber("sensor0", "crwlib_msgs/SensorData", 
				new MessageListener<SensorData>() {

			@Override
			public void onNewMessage(SensorData sensor) {
				if(sensor==null){
					logger.info("Die!");
					return;}
				sendSensor(0, sensor);
			}
		});
		
		// Register publisher for one-way setters
		_velocityPublisher = _node.newPublisher("cmd_vel", "geometry_msgs/Twist");
		
		// Register services for two-way setters and accessor functions
		_setStateClient = registerService("/set_state", "crwlib_msgs/SetState");
		if (_setStateClient == null) return false;
		
		_getStateClient = registerService("/get_state", "crwlib_msgs/GetState");
		if (_getStateClient == null) return false;
		
		_captureImageClient = registerService("/capture_image", "crwlib_msgs/CaptureImage");
		if (_captureImageClient == null) return false;
		
		_getCameraStatusClient = registerService("/get_camera_status", "crwlib_msgs/GetCameraStatus");
		if (_getCameraStatusClient == null) return false;
		
		_setSensorTypeClient = registerService("/set_sensor_type", "crwlib_msgs/SetSensorType");
		if (_setSensorTypeClient == null) return false;
		
		_getSensorTypeClient = registerService("/get_sensor_type", "crwlib_msgs/GetSensorType");
		if (_getSensorTypeClient == null) return false;
		
		_getNumSensorsClient = registerService("/get_num_sensors", "crwlib_msgs/GetNumSensors");
		if (_getNumSensorsClient == null) return false;
		
		_getVelocityClient = registerService("/get_velocity", "crwlib_msgs/GetVelocity");
		if (_getVelocityClient == null) return false;
		
		_isAutonomousClient = registerService("/is_autonomous", "crwlib_msgs/IsAutonomous");
		if (_isAutonomousClient == null) return false;
		
		_setAutonomousClient = registerService("/set_autonomous", "crwlib_msgs/SetAutonomous");
		if (_setAutonomousClient == null) return false;
		
		_getWaypointClient = registerService("/get_waypoint", "crwlib_msgs/GetWaypoint");
		if (_getWaypointClient == null) return false;
		
		_getWaypointStatusClient = registerService("/get_waypoint_status", "crwlib_msgs/GetWaypointStatus");
		if (_getWaypointStatusClient == null) return false;
		
		_setGainsClient = registerService("/set_gains", "crwlib_msgs/SetGains");
		if (_setGainsClient == null) return false;
		
		_getGainsClient = registerService("/get_gains", "crwlib_msgs/GetGains");
		if (_getGainsClient == null) return false;
		
		_resetLog = registerService("/reset_log", "std_msgs/Empty");
		if (_resetLog == null) return false;
		
		logger.info("Proxy initialized successfully.");
		return true;
	}
	
	protected synchronized void disconnect() {
		try {
			if (_navClient != null)
				_navClient.shutdown();
			
			if (_imgClient != null)
				_imgClient.shutdown();
			
			if (_node != null)
				_node.shutdown();
		} catch (Exception e) {
			logger.warning("Unclean disconnection: " + e);
		}
	}

	/**
	 * Helper function to register services for two-way setters and accessor 
	 * functions.
	 * 
	 * @param topic
	 * @param msgType
	 * @return
	 */
	protected <Request, Response> ServiceClient<Request, Response> registerService(String topic, String msgType) {
		try {
			ServiceClient<Request, Response> serviceClient = _node.newServiceClient(topic, msgType);
			return serviceClient;
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for: " + topic);
			return null;
		} catch (RosRuntimeException e) {
			logger.warning("Failed to register service " + topic + ":" + e);
			return null;
		}
	}
	
	/**
	 * Helper function to safely call services, and reconnect if the service
	 * fails to respond correctly.
	 * 
	 * @param <Request>
	 * @param <Response>
	 * @param client
	 * @param request
	 * @return
	 */
	protected <Request, Response> Response safeCall(ServiceClient<Request, Response> client, Request request) {
		if (client == null)
			return null;
		
		try {
			BlockingListener<Response> listener = new BlockingListener<Response>();
			client.call(request, listener);
			Response response = listener.waitForCompletion();
			return response;
		} catch (RosRuntimeException e) {
			logger.warning("Failed to call " + client + ":" + e);
			return null;
		}
	}
	
	protected class BlockingListener<MessageType> implements ServiceResponseListener<MessageType> {
		private final CountDownLatch _complete = new CountDownLatch(1);
		private MessageType _result = null;

		@Override
    	public void onSuccess(MessageType message) {
			_result = message;
			_complete.countDown();
    	}

    	@Override
    	public void onFailure(RemoteException e) {
    		logger.warning("Failed to complete service call: " + e);
    		_result = null;
    		_complete.countDown();
    	}
    	
    	public MessageType waitForCompletion() {
			try {
				_complete.await(SERVICE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
				return _result;
			} catch (InterruptedException e) {
				return null;
			}
    	}
	}
	
	/**
	 * Terminates the ROS processes wrapping a VehicleServer.
	 */
	public void shutdown() {
		disconnect();
	}

	protected class NavigationHandler implements SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult> {
		final WaypointObserver _obs;
		
		public NavigationHandler(WaypointObserver obs) {
			_obs = obs;
		}

		@Override
		public void feedbackCallback(VehicleNavigationFeedback feedback) {
			logger.finer("Navigation feedback");
			if (_obs != null)
				_obs.waypointUpdate(WaypointState.values()[feedback.status]);
		}

		@Override
		public void doneCallback(SimpleClientGoalState state,
				VehicleNavigationResult result) {
			logger.fine("Navigation finished");
			if (_obs != null) {
				if (result == null) {
					_obs.waypointUpdate(WaypointState.UNKNOWN);
				} else {
					_obs.waypointUpdate(WaypointState.values()[result.status]);
				}
			}
		}

		@Override
		public void activeCallback() {
			logger.fine("Navigation active");
		}
	};
	
	protected class ImageCaptureHandler implements SimpleActionClientCallbacks<VehicleImageCaptureFeedback, VehicleImageCaptureResult> {
		final ImagingObserver _obs;
		
		public ImageCaptureHandler(ImagingObserver obs) {
			_obs = obs;
		}
		
		@Override
		public void feedbackCallback(VehicleImageCaptureFeedback feedback) {
			logger.finer("Capture feedback");
			if (_obs != null)
				_obs.imagingUpdate(CameraState.values()[feedback.status]);
		}
		
		@Override
		public void doneCallback(SimpleClientGoalState state, VehicleImageCaptureResult result) {
			logger.fine("Capture finished");
			if (_obs != null) {
				if (result == null) {
					_obs.imagingUpdate(CameraState.UNKNOWN);
				} else {
					_obs.imagingUpdate(CameraState.values()[result.status]);
				}
			}
			
		}

		@Override
		public void activeCallback() {
			logger.fine("Capture active");
		}

	};

	@Override
	public CompressedImage captureImage(int width, int height) {
		CaptureImage.Request request = new CaptureImage.Request();
		request.width = width;
		request.height = height;
		
		CaptureImage.Response response = safeCall(_captureImageClient, request);
		// TODO: return actual taken image
		return (response != null) ? new CompressedImage() : null;
	}

	@Override
	public CameraState getCameraStatus() {
		GetCameraStatus.Response response = safeCall(_getCameraStatusClient, new GetCameraStatus.Request());
		return (response != null) ? CameraState.values()[response.status] : CameraState.UNKNOWN;
	}

	@Override
	public int getNumSensors() {
		GetNumSensors.Response response = safeCall(_getNumSensorsClient, new GetNumSensors.Request());
		return (response != null) ? response.numSensors : -1;
	}

	@Override
	public double[] getGains(int axis) {
		GetGains.Request request = new GetGains.Request();
		request.axis = (byte)axis;
		
		GetGains.Response response = safeCall(_getGainsClient, request); 
		return (response != null) ? response.gains : new double[0];
	}
	
	@Override
	public SensorType getSensorType(int channel) {
		GetSensorType.Request request = new GetSensorType.Request();
		request.channel = (byte)channel;
			
		GetSensorType.Response response = safeCall(_getSensorTypeClient, request);
		return (response != null) ? SensorType.values()[response.type] : SensorType.UNKNOWN;
	}

	@Override
	public UtmPoseWithCovarianceStamped getState() {
		GetState.Response response = safeCall(_getStateClient, new GetState.Request());
		return (response != null) ? response.pose : null;
	}

	@Override
	public TwistWithCovarianceStamped getVelocity() {
		GetVelocity.Response response = safeCall(_getVelocityClient, new GetVelocity.Request());
		return (response != null) ? response.velocity : null;
	}

	@Override
	public UtmPose getWaypoint() {
		GetWaypoint.Response response = safeCall(_getWaypointClient, new GetWaypoint.Request());
		return (response != null) ? response.waypoint : null;
	}

	@Override
	public WaypointState getWaypointStatus() {
		GetWaypointStatus.Response response = safeCall(_getWaypointStatusClient, new GetWaypointStatus.Request());
		return (response != null) ? WaypointState.values()[response.status] : WaypointState.UNKNOWN;
	}

	@Override
	public boolean isAutonomous() {
		IsAutonomous.Response response = safeCall(_isAutonomousClient, new IsAutonomous.Request());
		return (response != null) ? response.isAutonomous : false;
	}

	@Override
	public void setAutonomous(boolean auto) {
		SetAutonomous.Request request = new SetAutonomous.Request();
		request.isAutonomous = auto;
				
		safeCall(_setAutonomousClient, request);
	}

	@Override
	public void setGains(int axis, double[] gains) {
		SetGains.Request request = new SetGains.Request();
		request.axis = (byte)axis;
		request.gains = gains;
				
		safeCall(_setGainsClient, request);
	}
	
	@Override
	public void setSensorType(int channel, SensorType type) {
		SetSensorType.Request request = new SetSensorType.Request();
		request.channel = (byte)channel;
		request.type = (byte)type.ordinal();
		
		safeCall(_setSensorTypeClient, request);
	}

	@Override
	public void setState(UtmPose state) {
		SetState.Request request = new SetState.Request();
		request.pose = state;

		safeCall(_setStateClient, request);
	}

	

	@Override
	public void startCamera(long numFrames, double interval, int width,
			int height, ImagingObserver obs) {
		VehicleImageCaptureGoal goal = new VehicleImageCaptureGoal();
		goal.frames = numFrames;
		goal.interval = (float)interval;
		goal.width = width;
		goal.height = height;
		
		try {
			_imgClient.sendGoal(goal, new ImageCaptureHandler(obs));
		} catch (RosException e) {
			logger.warning("Unable to start waypoint: " + e);
		}
	}

	@Override
	public void startWaypoint(UtmPose waypoint, String controller, WaypointObserver obs) {
		VehicleNavigationGoal goal = new VehicleNavigationGoal();
		
		goal.targetPose = (waypoint != null) ? waypoint : new UtmPose();
		goal.controller = (controller != null) ? controller : "";
	
		try {
			_navClient.sendGoal(goal, new NavigationHandler(obs));
		} catch (RosException e) {
			logger.warning("Unable to start waypoint: " + e);
		}
	}

	@Override
	public void stopCamera() {
		try {
			_imgClient.cancelGoal();
		} catch (RosException e) {
			logger.warning("Unable to cancel imaging: " + e);
		}
	}

	@Override
	public void stopWaypoint() {
		try {
			_navClient.cancelGoal();
		} catch (RosException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void setVelocity(Twist velocity) {
		if (_velocityPublisher.hasSubscribers())
			_velocityPublisher.publish(velocity);
	}
	@Override
	public void resetLog() {
		ResetLog.Request request = new ResetLog.Request();
		request.reset = null;
		safeCall(_resetLog, request);
	}

}
