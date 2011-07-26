package edu.cmu.ri.crw.ros;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.ros.actionlib.client.SimpleActionClientCallbacks;
import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.address.InetAddressFactory;
import org.ros.exception.RemoteException;
import org.ros.exception.RosException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.message.MessageListener;
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

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.WaypointObserver;

/**
 * Takes the node name of an existing RosVehicleServer and connects through ROS,
 * wrapping the functionality of a VehicleServer transparently. Once connected,
 * this object can be used as a vehicle server, but all commands will be
 * forwarded to the underlying ROS node.
 * 
 * @author pkv
 * @author kss
 * 
 */
public class RosVehicleProxy extends AbstractVehicleServer {

	public static final Logger logger = 
		Logger.getLogger(RosVehicleProxy.class.getName());

	public static final String DEFAULT_NODE_NAME = "vehicle_client";

	protected Node _node;
	protected Publisher<Twist> _velocityPublisher;
	protected RosVehicleNavigation.Client _navClient; 
	protected RosVehicleImaging.Client _imgClient;
	
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
	protected ServiceClient<SetPid.Request, SetPid.Response> _setPidClient;    
	protected ServiceClient<GetPid.Request, GetPid.Response> _getPidClient;  
	
	public RosVehicleProxy() {
		this(NodeConfiguration.DEFAULT_MASTER_URI, DEFAULT_NODE_NAME);
	}

	public RosVehicleProxy(String nodeName) {
		this(NodeConfiguration.DEFAULT_MASTER_URI, nodeName);
	}

	public RosVehicleProxy(URI masterUri, String nodeName) {
		
		// Get a legitimate hostname
		String host = InetAddressFactory.newNonLoopback().getHostAddress();
		
		// Create a node configuration and start a node
		NodeConfiguration config = NodeConfiguration.newPublic(host, masterUri);
	    _node = new DefaultNodeFactory().newNode(nodeName, config);		
		
	    // Start up action clients to run navigation and imaging
	    NodeRunner runner = NodeRunner.newDefault();
	    
	    // Create an action server for vehicle navigation
	    NodeConfiguration navConfig = NodeConfiguration.newPublic(host, masterUri);
		NameResolver navResolver = NameResolver.create("/nav");
		navConfig.setParentResolver(navResolver);
		
		try {
			_navClient = new RosVehicleNavigation.Spec()
					.buildSimpleActionClient(nodeName + "/nav");
			runner.run(_navClient, navConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		// Create an action server for image capturing
		NodeConfiguration imgConfig = NodeConfiguration.newPublic(host, masterUri);
		NameResolver imgResolver = NameResolver.create("/img");
		imgConfig.setParentResolver(imgResolver);
		
		try {
			_imgClient = new RosVehicleImaging.Spec()
					.buildSimpleActionClient(nodeName + "/img");
			runner.run(_imgClient, imgConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start image action client: " + ex);
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
		
		// Register publisher for one-way setters
		_velocityPublisher = _node.newPublisher("cmd_vel", "geometry_msgs/Twist");
		
		// Register services for two-way setters and accessor functions
		try {
		    _setStateClient = _node.newServiceClient("/set_state", "crwlib_msgs/SetState");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for SetState.");
		}
		
		try { 
			_getStateClient = _node.newServiceClient("/get_state", "crwlib_msgs/GetState");
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetState.");
		}
		
		try {
		    _captureImageClient = _node.newServiceClient("/capture_image", "crwlib_msgs/CaptureImage");
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for CaptureImage.");
		}
		
		try {
		    _getCameraStatusClient = _node.newServiceClient("/get_camera_status", "crwlib_msgs/GetCameraStatus");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetCameraStatus.");
		}
		
		try {
		    _setSensorTypeClient = _node.newServiceClient("/set_sensor_type", "crwlib_msgs/SetSensorType");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for SetSensorType.");
		}
		
		try {
			_getSensorTypeClient = _node.newServiceClient("/get_sensor_type", "crwlib_msgs/GetSensorType");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetSensorType.");
		}
		
		try {
			_getNumSensorsClient = _node.newServiceClient("/get_num_sensors", "crwlib_msgs/GetNumSensors");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetNumSensors.");
		}
		
		try {
			_getVelocityClient = _node.newServiceClient("/get_velocity", "crwlib_msgs/GetVelocity");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetVelocity.");
		}
		
		try {
			_isAutonomousClient = _node.newServiceClient("/is_autonomous", "crwlib_msgs/IsAutonomous");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for IsAutonomous.");
		}
		
		try {
			_setAutonomousClient = _node.newServiceClient("/set_autonomous", "crwlib_msgs/SetAutonomous");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for SetAutonomous.");
		}
	    
		try {
			_getWaypointClient = _node.newServiceClient("/get_waypoint", "crwlib_msgs/GetWaypoint"); 
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetWaypoint.");
		}
		
		try {
			_getWaypointStatusClient = _node.newServiceClient("/get_waypoint_status", "crwlib_msgs/GetWaypointStatus");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetWaypointStatus.");
		}
		
		try {
			_setPidClient = _node.newServiceClient("/set_pid", "crwlib_msgs/SetPid");    
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for SetPid.");
		}
	
		try {
			_getPidClient = _node.newServiceClient("/get_pid", "crwlib_msgs/GetPid");  
		} catch (ServiceNotFoundException ex) {
			logger.warning("Failed to find service for GetPid.");
		}
		
		logger.info("Proxy initialized successfully.");
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
				_complete.await();
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
		_navClient.shutdown();
		_imgClient.shutdown();
		_node.shutdown();
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
			if (_obs != null)
				_obs.waypointUpdate(WaypointState.values()[result.status]);
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
			if (_obs != null)
				_obs.imagingUpdate(CameraState.values()[result.status]);
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
		
		BlockingListener<CaptureImage.Response> listener = new BlockingListener<CaptureImage.Response>();
		_captureImageClient.call(request, listener);
		CaptureImage.Response response = listener.waitForCompletion();
		
		// TODO: return actual taken image
		return (response != null) ? new CompressedImage() : null;
	}

	@Override
	public CameraState getCameraStatus() {
		BlockingListener<GetCameraStatus.Response> listener = new BlockingListener<GetCameraStatus.Response>();
		_getCameraStatusClient.call(new GetCameraStatus.Request(), listener);
		GetCameraStatus.Response response = listener.waitForCompletion();
		return (response != null) ? CameraState.values()[response.status] : CameraState.UNKNOWN;
	}

	@Override
	public int getNumSensors() {
		BlockingListener<GetNumSensors.Response> listener = new BlockingListener<GetNumSensors.Response>();
		_getNumSensorsClient.call(new GetNumSensors.Request(), listener);
		GetNumSensors.Response response = listener.waitForCompletion();
		return (response != null) ? response.numSensors : -1;
	}

	@Override
	public double[] getPID(int axis) {
		GetPid.Request request = new GetPid.Request();
		request.axis = (byte)axis;
		
		BlockingListener<GetPid.Response> listener = new BlockingListener<GetPid.Response>();
		_getPidClient.call(request, listener);
		GetPid.Response response = listener.waitForCompletion();
		return (response != null) ? response.gains : new double[0];
	}
	
	@Override
	public SensorType getSensorType(int channel) {
		GetSensorType.Request request = new GetSensorType.Request();
		request.channel = (byte)channel;
		
		BlockingListener<GetSensorType.Response> listener = new BlockingListener<GetSensorType.Response>();
		_getSensorTypeClient.call(request, listener);
		GetSensorType.Response response = listener.waitForCompletion();
		return (response != null) ? SensorType.values()[response.type] : SensorType.UNKNOWN;
	}

	@Override
	public UtmPoseWithCovarianceStamped getState() {
		BlockingListener<GetState.Response> listener = new BlockingListener<GetState.Response>();
		_getStateClient.call(new GetState.Request(), listener);
		GetState.Response response = listener.waitForCompletion();
		return (response != null) ? response.pose : null;	
	}

	@Override
	public TwistWithCovarianceStamped getVelocity() {
		BlockingListener<GetVelocity.Response> listener = new BlockingListener<GetVelocity.Response>();
		_getVelocityClient.call(new GetVelocity.Request(), listener);
		GetVelocity.Response response = listener.waitForCompletion();
		return (response != null) ? response.velocity : null;
	}

	@Override
	public UtmPose getWaypoint() {
		BlockingListener<GetWaypoint.Response> listener = new BlockingListener<GetWaypoint.Response>();
		_getWaypointClient.call(new GetWaypoint.Request(), listener);
		GetWaypoint.Response response = listener.waitForCompletion();
		return (response != null) ? response.waypoint : null;
	}

	@Override
	public WaypointState getWaypointStatus() {
		BlockingListener<GetWaypointStatus.Response> listener = new BlockingListener<GetWaypointStatus.Response>();
		_getWaypointStatusClient.call(new GetWaypointStatus.Request(), listener);
		GetWaypointStatus.Response response = listener.waitForCompletion();
		return (response != null) ? WaypointState.values()[response.status] : WaypointState.UNKNOWN;	
	}

	@Override
	public boolean isAutonomous() {
		BlockingListener<IsAutonomous.Response> listener = new BlockingListener<IsAutonomous.Response>();
		_isAutonomousClient.call(new IsAutonomous.Request(), listener);
		IsAutonomous.Response response = listener.waitForCompletion();
		return (response != null) ? response.isAutonomous : false;
	}

	@Override
	public void setAutonomous(boolean auto) {
		SetAutonomous.Request request = new SetAutonomous.Request();
		request.isAutonomous = auto;
		
		BlockingListener<SetAutonomous.Response> listener = new BlockingListener<SetAutonomous.Response>();
		_setAutonomousClient.call(request, listener);
		listener.waitForCompletion();
	}

	@Override
	public void setPID(int axis, double[] gains) {
		SetPid.Request request = new SetPid.Request();
		request.axis = (byte)axis;
		request.gains = gains;
		
		BlockingListener<SetPid.Response> listener = new BlockingListener<SetPid.Response>();
		_setPidClient.call(request, listener);
		listener.waitForCompletion();
	}
	
	@Override
	public void setSensorType(int channel, SensorType type) {
		SetSensorType.Request request = new SetSensorType.Request();
		request.channel = (byte)channel;
		request.type = (byte)type.ordinal();
		
		BlockingListener<SetSensorType.Response> listener = new BlockingListener<SetSensorType.Response>();
		_setSensorTypeClient.call(request, listener);
		listener.waitForCompletion();
	}

	@Override
	public void setState(UtmPose state) {
		SetState.Request request = new SetState.Request();
		request.pose = state;
		
		BlockingListener<SetState.Response> listener = new BlockingListener<SetState.Response>();
		_setStateClient.call(request, listener);
		listener.waitForCompletion();
	}

	@Override
	public void setVelocity(Twist velocity) {
		if (_velocityPublisher.hasSubscribers())
			_velocityPublisher.publish(velocity);
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
	public void startWaypoint(UtmPose waypoint, WaypointObserver obs) {
		VehicleNavigationGoal goal = new VehicleNavigationGoal();
		goal.targetPose = waypoint;
	
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
			logger.warning("Unable to cancel navigation: " + e);
		}
	}

}
