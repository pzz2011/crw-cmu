package edu.cmu.ri.crw.ros;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.MessageListener;
import org.ros.message.crwlib_msgs.SensorData;
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
import org.ros.service.crwlib_msgs.GetPid;
import org.ros.service.crwlib_msgs.GetSensorType;
import org.ros.service.crwlib_msgs.GetState;
import org.ros.service.crwlib_msgs.GetVelocity;
import org.ros.service.crwlib_msgs.GetWaypoint;
import org.ros.service.crwlib_msgs.GetWaypointStatus;
import org.ros.service.crwlib_msgs.IsAutonomous;
import org.ros.service.crwlib_msgs.SetAutonomous;
import org.ros.service.crwlib_msgs.SetPid;
import org.ros.service.crwlib_msgs.SetSensorType;
import org.ros.service.crwlib_msgs.SetState;

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

	protected Node _node;
	protected RosVehicleNavigation.Server _navServer;
	protected RosVehicleImaging.Server _imgServer;
	
	public RosVehicleServer(VehicleServer server) {
		this(NodeConfiguration.DEFAULT_MASTER_URI, DEFAULT_NODE_NAME, server);
	}

	public RosVehicleServer(URI masterUri, String nodeName, VehicleServer server) {

		// Store the reference to VehicleServer implementation
		_server = server;
		
		// Create a node configuration and start the main node
		NodeConfiguration config = createNodeConfiguration(nodeName, masterUri);
	    _node = new DefaultNodeFactory().newNode(nodeName, config);

		// Create publisher for state data
	    Publisher<UtmPoseWithCovarianceStamped> statePublisher = _node.newPublisher("state", "crwlib_msgs/UtmPoseWithCovarianceStamped");
		_server.addStateListener(new StateHandler(statePublisher));

		// Create publisher for image data and camera info
		Publisher<CompressedImage> imagePublisher = _node.newPublisher("image/compressed", "sensor_msgs/CompressedImage");
		Publisher<CameraInfo> cameraInfoPublisher = _node.newPublisher("camera_info", "sensor_msgs/CameraInfo");
		_server.addImageListener(new ImageHandler(imagePublisher, cameraInfoPublisher));
		
		// Create publisher for velocity data
		Publisher<TwistWithCovarianceStamped> velocityPublisher = _node.newPublisher("velocity", "geometry_msgs/TwistWithCovarianceStamped");
		_server.addVelocityListener(new VelocityHandler(velocityPublisher));

		// Query for vehicle sensors and create corresponding publishers
		int nSensors = server.getNumSensors();
		for (int iSensor = 0; iSensor < nSensors && iSensor < nSensors; ++iSensor) {
			Publisher<SensorData> sensorPublisher = _node.newPublisher(
					RosVehicleConfig.SENSOR_TOPIC_PREFIX + iSensor,
					"crwlib_msgs/SensorData");
			_server.addSensorListener(iSensor, new SensorHandler(
					(Publisher<SensorData>) sensorPublisher));
		}

	    // Create a runner to start actionlib services
		NodeRunner runner = NodeRunner.newDefault();
		
		// Create an action server for vehicle navigation
		NodeConfiguration navConfig = createNodeConfiguration(nodeName, masterUri);
		NameResolver navResolver = NameResolver.create("/nav");
		navConfig.setParentResolver(navResolver);
		
		// TODO: do we need to re-instantiate spec each time here?
		try {
			_navServer = new RosVehicleNavigation.Spec()
			.buildSimpleActionServer(_node, nodeName + "/nav",
					navigationHandler, true);
			runner.run(_navServer, navConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		// Create an action server for image capturing
		NodeConfiguration imgConfig = createNodeConfiguration(nodeName, masterUri);
		NameResolver imgResolver = NameResolver.create("/img");
		imgConfig.setParentResolver(imgResolver);
		
		// TODO: do we need to re-instantiate spec each time here?
		try {
			_imgServer = new RosVehicleImaging.Spec().buildSimpleActionServer(
					_node, nodeName + "/img", imageCaptureHandler, true);
			runner.run(_imgServer, imgConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		// Create ROS subscriber for one-way velocity setter function
		_node.newSubscriber("cmd_vel", "geometry_msgs/Twist", new MessageListener<Twist>() {

			@Override
			public void onNewMessage(Twist velocity) {
				_server.setVelocity(velocity);
			}
		});
		
		// Create ROS services for accessor and setter functions
		// TODO: remove leading slash once rosjava is a little more stable
		_node.newServiceServer("/set_state", "crwlib_msgs/SetState",
				new ServiceResponseBuilder<SetState.Request, SetState.Response>() {
			@Override
			public SetState.Response build(SetState.Request request) {
				_server.setState(request.pose);
				return new SetState.Response();
			}
		});
		
		_node.newServiceServer("/get_state", "crwlib_msgs/GetState",
				new ServiceResponseBuilder<GetState.Request, GetState.Response>() {
			@Override
			public GetState.Response build(GetState.Request request) {
				GetState.Response response = new GetState.Response();
				response.pose = _server.getState();
				return response;
			}
		});
		
		_node.newServiceServer("/capture_image", "crwlib_msgs/CaptureImage",
				new ServiceResponseBuilder<CaptureImage.Request, CaptureImage.Response>() {
			@Override
			public CaptureImage.Response build(CaptureImage.Request request) {
				CaptureImage.Response response = new CaptureImage.Response();
				_server.captureImage(request.width, request.height);
				// TODO: put the capture image result in the service response
				return response;
			}
		});
		
		_node.newServiceServer("/get_camera_status", "crwlib_msgs/GetCameraStatus",
				new ServiceResponseBuilder<GetCameraStatus.Request, GetCameraStatus.Response>() {
			@Override
			public GetCameraStatus.Response build(GetCameraStatus.Request request) {
				GetCameraStatus.Response response = new GetCameraStatus.Response();
				response.status = (byte)_server.getCameraStatus().ordinal();
				return response;
			}
		});
		
		_node.newServiceServer("/set_sensor_type", "crwlib_msgs/SetSensorType",
				new ServiceResponseBuilder<SetSensorType.Request, SetSensorType.Response>() {
			@Override
			public SetSensorType.Response build(SetSensorType.Request request) {
				_server.setSensorType(request.channel, SensorType.values()[request.type]);
				return new SetSensorType.Response();
			}
		});
		
		_node.newServiceServer("/get_sensor_type", "crwlib_msgs/GetSensorType",
				new ServiceResponseBuilder<GetSensorType.Request, GetSensorType.Response>() {
			@Override
			public GetSensorType.Response build(GetSensorType.Request request) {
				GetSensorType.Response response = new GetSensorType.Response();
				response.type = (byte)_server.getSensorType(request.channel).ordinal();
				// TODO: it might make sense to also return the channel
				return response;
			}
		});
		
		_node.newServiceServer("/get_num_sensors", "crwlib_msgs/GetNumSensors",
				new ServiceResponseBuilder<GetNumSensors.Request, GetNumSensors.Response>() {
			@Override
			public GetNumSensors.Response build(GetNumSensors.Request request) {
				GetNumSensors.Response response = new GetNumSensors.Response();
				response.numSensors = (byte)_server.getNumSensors();
				// TODO: this could probably be an int
				return response;
			}
		});
		
		_node.newServiceServer("/get_velocity", "crwlib_msgs/GetVelocity",
				new ServiceResponseBuilder<GetVelocity.Request, GetVelocity.Response>() {
			@Override
			public GetVelocity.Response build(GetVelocity.Request request) {
				GetVelocity.Response response = new GetVelocity.Response();
				response.velocity = _server.getVelocity();
				return response;
			}
		});
		
		_node.newServiceServer("/is_autonomous", "crwlib_msgs/IsAutonomous",
				new ServiceResponseBuilder<IsAutonomous.Request, IsAutonomous.Response>() {
			@Override
			public IsAutonomous.Response build(IsAutonomous.Request request) {
				IsAutonomous.Response response = new IsAutonomous.Response();
				response.isAutonomous = _server.isAutonomous();
				return response;
			}
		});
	    
		_node.newServiceServer("/set_autonomous", "crwlib_msgs/SetAutonomous",
				new ServiceResponseBuilder<SetAutonomous.Request, SetAutonomous.Response>() {
			@Override
			public SetAutonomous.Response build(SetAutonomous.Request request) {
				_server.setAutonomous(request.isAutonomous);
				return new SetAutonomous.Response();
			}
		});
		
		_node.newServiceServer("/get_waypoint", "crwlib_msgs/GetWaypoint",
				new ServiceResponseBuilder<GetWaypoint.Request, GetWaypoint.Response>() {
			@Override
			public GetWaypoint.Response build(GetWaypoint.Request request) {
				GetWaypoint.Response response = new GetWaypoint.Response();
				response.waypoint = _server.getWaypoint();
				return response;
			}
		});
		
		_node.newServiceServer("/get_waypoint_status", "crwlib_msgs/GetWaypointStatus",
				new ServiceResponseBuilder<GetWaypointStatus.Request, GetWaypointStatus.Response>() {
			@Override
			public GetWaypointStatus.Response build(GetWaypointStatus.Request request) {
				GetWaypointStatus.Response response = new GetWaypointStatus.Response();
				response.status = (byte)_server.getWaypointStatus().ordinal();
				return response;
			}
		});
		
		_node.newServiceServer("/set_pid", "crwlib_msgs/SetPid",
				new ServiceResponseBuilder<SetPid.Request, SetPid.Response>() {
			@Override
			public SetPid.Response build(SetPid.Request request) {
				_server.setPID(request.axis, request.gains);
				return new SetPid.Response();
			}
		});
		
		_node.newServiceServer("/get_pid", "crwlib_msgs/GetPid",
				new ServiceResponseBuilder<GetPid.Request, GetPid.Response>() {
			@Override
			public GetPid.Response build(GetPid.Request request) {
				GetPid.Response response = new GetPid.Response();
				response.gains = _server.getPID(request.axis);
				return response;
			}
		});
		
	    // TODO: we should probably use awaitPublisher here

		logger.info("Server initialized successfully.");
	}

	/**
	 * Helper function that creates a public ROS node configuration if a 
	 * non-loopback hostname is available, and a private node configuration
	 * if the hostname cannot be resolved.
	 * 
	 * @param nodeName a ROS node name
	 * @param masterUri the desired ROS master URI 
	 * @return a ROS node configuration that is public when possible
	 */
	protected static NodeConfiguration createNodeConfiguration(String nodeName, URI masterUri) {
		NodeConfiguration config = null;
		try {
			String host = InetAddress.getLocalHost().getCanonicalHostName();
			config = NodeConfiguration.newPublic(host, masterUri);
		} catch (UnknownHostException ex) {
			logger.warning("Failed to get public hostname, using private hostname.");
			config = NodeConfiguration.newPrivate(masterUri);
		}
		return config;
	}
	
	/**
	 * Terminates the ROS processes wrapping a VehicleServer.
	 */
	public void shutdown() {
		_node.shutdown();
		_navServer.shutdown();
		_imgServer.shutdown();
		// TODO: remove publisher handlers as well
	}

	/**
	 * This child class publishes state change information on the state topic.
	 */
	public class StateHandler implements VehicleStateListener {

		private final Publisher<UtmPoseWithCovarianceStamped> _publisher;

		public StateHandler(final Publisher<UtmPoseWithCovarianceStamped> publisher) {
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

		public VelocityHandler(final Publisher<TwistWithCovarianceStamped> publisher) {
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
			logger.info("Starting navigation");
			// TODO: What is this callback for?
		}

		@Override
		public void preemptCallback(
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Navigation cancelled: ");
			_server.stopWaypoint();
			_navServer.setAborted(); // TODO: is this necessary, or automatic?
		}

		@Override
		public void blockingGoalCallback(
				VehicleNavigationGoal goal,
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Received waypoint: " + goal);

			// Ignore requests that have empty goals
			if (goal == null) return;
			
			// Tell vehicle server to start navigation, and register observer
			// to forward navigation status updates to ROS
			_server.startWaypoint(goal.targetPose, new WaypointObserver() {

				@Override
				public void waypointUpdate(VehicleServer server) {
					WaypointState status = _server.getWaypointStatus();
					
					switch (status) {
					case DONE:
						VehicleNavigationResult result = new VehicleNavigationResult();
						result.finalPose.pose.header.stamp = new WallclockProvider().getCurrentTime();
						result.finalPose.pose.pose = _server.getWaypoint().pose;  // TODO: Should this be vehicle pose or waypoint pose?
						_navServer.setSucceeded(result, "DONE");
						break;
					default:
						VehicleNavigationFeedback feedback = new VehicleNavigationFeedback();
						feedback.header.stamp = new WallclockProvider().getCurrentTime();
						feedback.status = (byte)status.ordinal();
						_navServer.publishFeedback(feedback);
						break;
					}
				}
			});
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
			logger.info("Starting imaging.");
			// TODO: What is this callback for?
		}

		@Override
		public void preemptCallback(
				SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg0) {
			logger.info("Imaging cancelled.");
			_server.stopCamera();
			_imgServer.setAborted(); // TODO: is this necessary, or automatic?
		}

		@Override
		public void blockingGoalCallback(
				VehicleImageCaptureGoal goal,
				SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg1) {

			// Ignore requests that have empty goals
			if (goal == null) return;
			
			// Tell vehicle server to start imaging, and register observer
			// to forward imaging status updates to ROS
			_server.startCamera(goal.frames, goal.interval, goal.width,
					goal.height, new ImagingObserver() {

				@Override
				public void imagingUpdate(VehicleServer server) {
					CameraState status = _server.getCameraStatus();
					
					switch (status) {
					case OFF:
						VehicleImageCaptureResult result = new VehicleImageCaptureResult();
						result.status = (byte)status.ordinal();
						_imgServer.setSucceeded(result, "OFF");
						break;
					default:
						VehicleImageCaptureFeedback feedback = new VehicleImageCaptureFeedback();
						feedback.header.stamp = new WallclockProvider().getCurrentTime();
						feedback.status = (byte)status.ordinal();
						_imgServer.publishFeedback(feedback);
						break;
					}
				}
			});
		}
	};
}
