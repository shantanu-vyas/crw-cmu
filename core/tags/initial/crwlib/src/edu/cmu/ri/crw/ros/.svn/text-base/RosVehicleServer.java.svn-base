package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.MessageListener;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.crwlib_msgs.VehicleImageCaptureActionFeedback;
import org.ros.message.crwlib_msgs.VehicleImageCaptureActionGoal;
import org.ros.message.crwlib_msgs.VehicleImageCaptureActionResult;
import org.ros.message.crwlib_msgs.VehicleImageCaptureFeedback;
import org.ros.message.crwlib_msgs.VehicleImageCaptureGoal;
import org.ros.message.crwlib_msgs.VehicleImageCaptureResult;
import org.ros.message.crwlib_msgs.VehicleNavigationActionFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationActionGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationActionResult;
import org.ros.message.crwlib_msgs.VehicleNavigationFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationResult;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CameraInfo;
import org.ros.message.sensor_msgs.CompressedImage;
import org.ros.namespace.NameResolver;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;
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

import edu.cmu.ri.crw.CrwNetworkUtils;
import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.VehicleImageListener;
import edu.cmu.ri.crw.VehicleSensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleStateListener;
import edu.cmu.ri.crw.VehicleVelocityListener;
import edu.cmu.ri.crw.WaypointObserver;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;

/**
 * Provides functionality for interfacing a vehicle server with ROS. This object
 * wraps an existing vehicle server and makes it available through a set of ros
 * actions, services, and topics.
 * 
 * @author pkv
 * @author kss
 * 
 */
public class RosVehicleServer {

	public static final Logger logger = Logger.getLogger(RosVehicleServer.class
			.getName());

	public static final String DEFAULT_NODE_NAME = "vehicle";

	protected VehicleServer _server;
	protected VehicleStateListener _stateListener;
	protected VehicleImageListener _imageListener;
	protected VehicleVelocityListener _velocityListener;
	protected VehicleSensorListener _sensorListeners[];

	protected Node _node;
	protected RosVehicleNavigation.Server _navServer;
	protected RosVehicleImaging.Server _imgServer;

	public RosVehicleServer(VehicleServer server) throws RosException {
		this(NodeConfiguration.DEFAULT_MASTER_URI, DEFAULT_NODE_NAME, server);
	}

	public RosVehicleServer(URI masterUri, String nodeName, VehicleServer server) {

		// TODO: Remove this logging setting -- it is a stopgap for a rosjava
		// bug
		Logger.getLogger("org.ros.internal.node.client").setLevel(Level.SEVERE);

		// Get a localhost address
		String host = CrwNetworkUtils.getLocalhost(masterUri.getHost());
		if (host == null || host.isEmpty())
			return;

		// Store the reference to VehicleServer implementation
		_server = server;

		// Create a node configuration and start the main node
		NodeConfiguration config = NodeConfiguration.newPublic(host, masterUri);
		_node = new DefaultNodeFactory().newNode(nodeName, config);

		// Create publisher for state data
		Publisher<UtmPoseWithCovarianceStamped> statePublisher = _node
				.newPublisher("state",
						"crwlib_msgs/UtmPoseWithCovarianceStamped");
		_stateListener = new StateHandler(statePublisher);
		_server.addStateListener(_stateListener);

		// Create publisher for image data and camera info
		Publisher<CompressedImage> imagePublisher = _node.newPublisher(
				"image/compressed", "sensor_msgs/CompressedImage");
		Publisher<CameraInfo> cameraInfoPublisher = _node.newPublisher(
				"camera_info", "sensor_msgs/CameraInfo");
		_imageListener = new ImageHandler(imagePublisher, cameraInfoPublisher);
		_server.addImageListener(_imageListener);

		// Create publisher for velocity data
		Publisher<TwistWithCovarianceStamped> velocityPublisher = _node
				.newPublisher("velocity",
						"geometry_msgs/TwistWithCovarianceStamped");
		_velocityListener = new VelocityHandler(velocityPublisher);
		_server.addVelocityListener(_velocityListener);

		// Query for vehicle sensors and create corresponding publishers
		int nSensors = server.getNumSensors();
		_sensorListeners = new VehicleSensorListener[nSensors];

		for (int iSensor = 0; iSensor < nSensors && iSensor < nSensors; ++iSensor) {
			Publisher<SensorData> sensorPublisher = _node.newPublisher(
					RosVehicleConfig.SENSOR_TOPIC_PREFIX + iSensor,
					"crwlib_msgs/SensorData");
			_sensorListeners[iSensor] = new SensorHandler(
					(Publisher<SensorData>) sensorPublisher);
			_server.addSensorListener(iSensor, _sensorListeners[iSensor]);
		}

		// Create a runner to start actionlib services
		NodeRunner runner = NodeRunner.newDefault();

		// Create an action server for vehicle navigation
		NodeConfiguration navConfig = NodeConfiguration.newPublic(host,
				masterUri);
		NameResolver navResolver = NameResolver.create("/nav");
		navConfig.setParentResolver(navResolver);

		// TODO: do we need to re-instantiate spec each time here?
		try {
			_navServer = new RosVehicleNavigation.Spec()
					.buildSimpleActionServer(_node, nodeName + "_nav",
							navigationHandler, false);
			runner.run(_navServer, navConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		// Create an action server for image capturing
		NodeConfiguration imgConfig = NodeConfiguration.newPublic(host,
				masterUri);
		NameResolver imgResolver = NameResolver.create("/img");
		imgConfig.setParentResolver(imgResolver);

		// TODO: do we need to re-instantiate spec each time here?
		try {
			_imgServer = new RosVehicleImaging.Spec().buildSimpleActionServer(
					_node, nodeName + "_img", imageCaptureHandler, false);
			runner.run(_imgServer, imgConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		// Create ROS subscriber for one-way velocity setter function
		_node.newSubscriber("cmd_vel", "geometry_msgs/Twist",
				new MessageListener<Twist>() {

					@Override
					public void onNewMessage(Twist velocity) {
						_server.setVelocity(velocity);
					}
				});
		// Create ROS service for resetting the log
		_node.newServiceServer("/cmd_reset_log", "crwlib_msgs/ResetLog", new ServiceResponseBuilder<ResetLog.Request, ResetLog.Response>() {
			@Override
			public ResetLog.Response build(ResetLog.Request request) {
				_server.resetLog();
				return new ResetLog.Response();
			}
		});

		// Create ROS services for accessor and setter functions
		// TODO: remove leading slash once rosjava is a little more stable
		_node.newServiceServer(
				"/set_state",
				"crwlib_msgs/SetState",
				new ServiceResponseBuilder<SetState.Request, SetState.Response>() {
					@Override
					public SetState.Response build(SetState.Request request) {
						_server.setState(request.pose);
						return new SetState.Response();
					}
				});

		_node.newServiceServer(
				"/get_state",
				"crwlib_msgs/GetState",
				new ServiceResponseBuilder<GetState.Request, GetState.Response>() {
					@Override
					public GetState.Response build(GetState.Request request) {
						GetState.Response response = new GetState.Response();
						response.pose = _server.getState();
						return response;
					}
				});

		_node.newServiceServer(
				"/capture_image",
				"crwlib_msgs/CaptureImage",
				new ServiceResponseBuilder<CaptureImage.Request, CaptureImage.Response>() {
					@Override
					public CaptureImage.Response build(
							CaptureImage.Request request) {
						CaptureImage.Response response = new CaptureImage.Response();
						_server.captureImage(request.width, request.height);
						// TODO: put the capture image result in the service
						// response
						return response;
					}
				});

		_node.newServiceServer(
				"/get_camera_status",
				"crwlib_msgs/GetCameraStatus",
				new ServiceResponseBuilder<GetCameraStatus.Request, GetCameraStatus.Response>() {
					@Override
					public GetCameraStatus.Response build(
							GetCameraStatus.Request request) {
						GetCameraStatus.Response response = new GetCameraStatus.Response();
						response.status = (byte) _server.getCameraStatus()
								.ordinal();
						return response;
					}
				});

		_node.newServiceServer(
				"/set_sensor_type",
				"crwlib_msgs/SetSensorType",
				new ServiceResponseBuilder<SetSensorType.Request, SetSensorType.Response>() {
					@Override
					public SetSensorType.Response build(
							SetSensorType.Request request) {
						_server.setSensorType(request.channel,
								SensorType.values()[request.type]);
						return new SetSensorType.Response();
					}
				});

		_node.newServiceServer(
				"/get_sensor_type",
				"crwlib_msgs/GetSensorType",
				new ServiceResponseBuilder<GetSensorType.Request, GetSensorType.Response>() {
					@Override
					public GetSensorType.Response build(
							GetSensorType.Request request) {
						GetSensorType.Response response = new GetSensorType.Response();
						response.type = (byte) _server.getSensorType(
								request.channel).ordinal();
						// TODO: it might make sense to also return the channel
						return response;
					}
				});

		_node.newServiceServer(
				"/get_num_sensors",
				"crwlib_msgs/GetNumSensors",
				new ServiceResponseBuilder<GetNumSensors.Request, GetNumSensors.Response>() {
					@Override
					public GetNumSensors.Response build(
							GetNumSensors.Request request) {
						GetNumSensors.Response response = new GetNumSensors.Response();
						response.numSensors = (byte) _server.getNumSensors();
						// TODO: this could probably be an int
						return response;
					}
				});

		_node.newServiceServer(
				"/get_velocity",
				"crwlib_msgs/GetVelocity",
				new ServiceResponseBuilder<GetVelocity.Request, GetVelocity.Response>() {
					@Override
					public GetVelocity.Response build(
							GetVelocity.Request request) {
						GetVelocity.Response response = new GetVelocity.Response();
						response.velocity = _server.getVelocity();
						return response;
					}
				});

		_node.newServiceServer(
				"/is_autonomous",
				"crwlib_msgs/IsAutonomous",
				new ServiceResponseBuilder<IsAutonomous.Request, IsAutonomous.Response>() {
					@Override
					public IsAutonomous.Response build(
							IsAutonomous.Request request) {
						IsAutonomous.Response response = new IsAutonomous.Response();
						response.isAutonomous = _server.isAutonomous();
						return response;
					}
				});

		_node.newServiceServer(
				"/set_autonomous",
				"crwlib_msgs/SetAutonomous",
				new ServiceResponseBuilder<SetAutonomous.Request, SetAutonomous.Response>() {
					@Override
					public SetAutonomous.Response build(
							SetAutonomous.Request request) {
						_server.setAutonomous(request.isAutonomous);
						return new SetAutonomous.Response();
					}
				});

		_node.newServiceServer(
				"/get_waypoint",
				"crwlib_msgs/GetWaypoint",
				new ServiceResponseBuilder<GetWaypoint.Request, GetWaypoint.Response>() {
					@Override
					public GetWaypoint.Response build(
							GetWaypoint.Request request) {
						GetWaypoint.Response response = new GetWaypoint.Response();
						UtmPose waypoint = _server.getWaypoint();
						if (waypoint != null)
							response.waypoint = waypoint;
						return response;
					}
				});

		_node.newServiceServer(
				"/get_waypoint_status",
				"crwlib_msgs/GetWaypointStatus",
				new ServiceResponseBuilder<GetWaypointStatus.Request, GetWaypointStatus.Response>() {
					@Override
					public GetWaypointStatus.Response build(
							GetWaypointStatus.Request request) {
						GetWaypointStatus.Response response = new GetWaypointStatus.Response();
						response.status = (byte) _server.getWaypointStatus()
								.ordinal();
						return response;
					}
				});

		_node.newServiceServer("/set_gains", "crwlib_msgs/SetGains",
				new ServiceResponseBuilder<SetGains.Request, SetGains.Response>() {
					@Override
					public SetGains.Response build(SetGains.Request request) {
						_server.setGains(request.axis, request.gains);
						return new SetGains.Response();
					}
				});

		_node.newServiceServer("/get_gains", "crwlib_msgs/GetGains",
				new ServiceResponseBuilder<GetGains.Request, GetGains.Response>() {
					@Override
					public GetGains.Response build(GetGains.Request request) {
						GetGains.Response response = new GetGains.Response();
						response.gains = _server.getGains(request.axis);
						return response;
					}
				});

		// TODO: we should probably use awaitPublisher here
		logger.info("Server initialized successfully.");
	}

	/**
	 * Terminates the ROS processes wrapping a VehicleServer.
	 */
	public void shutdown() {

		// Remove sensor handlers from wrapped vehicle server
		_server.removeStateListener(_stateListener);
		_server.removeImageListener(_imageListener);
		_server.removeVelocityListener(_velocityListener);
		for (int iSensor = 0; iSensor < _sensorListeners.length; ++iSensor)
			_server.removeSensorListener(iSensor, _sensorListeners[iSensor]);

		// Shutdown ROS objects
		_navServer.shutdown();
		_imgServer.shutdown();
		_node.shutdown();
	}

	/**
	 * This child class publishes state change information on the state topic.
	 */
	public class StateHandler implements VehicleStateListener {

		private final Publisher<UtmPoseWithCovarianceStamped> _publisher;

		public StateHandler(
				final Publisher<UtmPoseWithCovarianceStamped> publisher) {
			_publisher = publisher;
		}

		@Override
		public void receivedState(UtmPoseWithCovarianceStamped pose) {
			if (_publisher.hasSubscribers())
				_publisher.publish(pose);
		}
	};

	/**
	 * This child class publishes velocity information on the velocity topic.
	 */
	public class VelocityHandler implements VehicleVelocityListener {

		private final Publisher<TwistWithCovarianceStamped> _publisher;

		public VelocityHandler(
				final Publisher<TwistWithCovarianceStamped> publisher) {
			_publisher = publisher;
		}

		@Override
		public void receivedVelocity(TwistWithCovarianceStamped velocity) {
			if (_publisher.hasSubscribers())
				_publisher.publish(velocity);
		}
	};

	/**
	 * This child class publishes new captured images on the image topic.
	 */
	public class ImageHandler implements VehicleImageListener {

		private final Publisher<CompressedImage> _imgPublisher;
		private final Publisher<CameraInfo> _infoPublisher;

		public ImageHandler(final Publisher<CompressedImage> imgPublisher,
				final Publisher<CameraInfo> infoPublisher) {
			_imgPublisher = imgPublisher;
			_infoPublisher = infoPublisher;
		}

		@Override
		public void receivedImage(CompressedImage image) {
			if (_imgPublisher.hasSubscribers()) {
				_imgPublisher.publish(image);
			}
			if (_infoPublisher.hasSubscribers()) {
				// TODO: publish camera info
			}
		}
	}

	/**
	 * This child class publishes new sensor data images on a sensor topic.
	 */
	public class SensorHandler implements VehicleSensorListener {

		private final Publisher<SensorData> _publisher;

		public SensorHandler(final Publisher<SensorData> publisher) {
			_publisher = publisher;
		}

		@Override
		public void receivedSensor(SensorData sensor) {
			if (_publisher.hasSubscribers())
				_publisher.publish(sensor);
		}
	}

	/**
	 * This child class handles all of the logic associated with performing
	 * navigation as a preemptible task.
	 */
	public final SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> navigationHandler = new SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult>() {

		@Override
		public void goalCallback(
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {

			try {
				final VehicleNavigationGoal goal = _navServer.acceptNewGoal();
				logger.info("Starting navigation to: " + print(goal.targetPose));

				_server.stopWaypoint();
				_server.startWaypoint(goal.targetPose, goal.controller, new WaypointObserver() {

					@Override
					public void waypointUpdate(WaypointState status) {
						if (!_navServer.isActive())
							return;

						if (status == WaypointState.DONE) {
							VehicleNavigationResult result = new VehicleNavigationResult();
							result.header.stamp = new WallclockProvider()
									.getCurrentTime();
							result.status = (byte) status.ordinal();
							result.targetPose = goal.targetPose;
							_navServer.setSucceeded(result, "DONE");
						} else {
							VehicleNavigationFeedback feedback = new VehicleNavigationFeedback();
							feedback.header.stamp = new WallclockProvider()
									.getCurrentTime();
							feedback.status = (byte) status.ordinal();
							feedback.targetPose = goal.targetPose;
							_navServer.publishFeedback(feedback);
						}
					}
				});
			} catch (RosException e) {
				logger.warning("Unable to accept waypoint: " + e);
			}
		}

		@Override
		public void preemptCallback(
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Navigation cancelled.");
			_server.stopWaypoint();
		}

		@Override
		public void blockingGoalCallback(
				VehicleNavigationGoal goal,
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			// Blocking callback is not enabled
		}
	};

	/**
	 * This child class handles all of the logic associated with performing
	 * image capture as a preemptible task.
	 */
	public final SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> imageCaptureHandler = new SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult>() {

		@Override
		public void goalCallback(
				SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg0) {

			try {
				final VehicleImageCaptureGoal goal = _imgServer.acceptNewGoal();
				logger.info("Starting image capture: " + goal.frames + "@"
						+ goal.interval + ", " + goal.width + "x" + goal.height);

				_server.stopCamera();
				_server.startCamera(goal.frames, (double) goal.interval,
						goal.width, goal.height, new ImagingObserver() {

							@Override
							public void imagingUpdate(CameraState status) {
								if (!_navServer.isActive())
									return;

								if (status == CameraState.DONE) {
									VehicleImageCaptureResult result = new VehicleImageCaptureResult();
									result.header.stamp = new WallclockProvider()
											.getCurrentTime();
									result.status = (byte) status.ordinal();
									_imgServer.setSucceeded(result, "DONE");
								} else {
									VehicleImageCaptureFeedback feedback = new VehicleImageCaptureFeedback();
									feedback.header.stamp = new WallclockProvider()
											.getCurrentTime();
									feedback.status = (byte) status.ordinal();
									_imgServer.publishFeedback(feedback);
								}
							}
						});
			} catch (RosException e) {
				logger.warning("Unable to accept image capture: " + e);
			}
		}

		@Override
		public void preemptCallback(
				SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg0) {
			logger.info("Imaging cancelled.");
			_server.stopCamera();
		}

		@Override
		public void blockingGoalCallback(
				VehicleImageCaptureGoal goal,
				SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg1) {
			// Blocking callback is not enabled
		}
	};

	protected String print(UtmPose targetPose) {
		return "[" + targetPose.pose.position.x + ","
				+ targetPose.pose.position.y + "," + targetPose.pose.position.z
				+ "] @ " + targetPose.utm.zone
				+ (targetPose.utm.isNorth ? "North" : "South");
	}
}
