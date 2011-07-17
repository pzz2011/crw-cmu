package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ros.DefaultNode;
import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.Publisher;
import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.internal.time.WallclockProvider;
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
import org.ros.message.sensor_msgs.CameraInfo;
import org.ros.message.sensor_msgs.CompressedImage;
import org.ros.namespace.NameResolver;

import edu.cmu.ri.crw.VehicleImageListener;
import edu.cmu.ri.crw.VehicleSensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleStateListener;

/**
 * Provides functionality for interfacing a vehicle server with ROS.  This 
 * object wraps an existing vehicle server and makes it available through a set
 * of ros actions, services, and topics.
 * 
 * @author pkv
 *
 */
public class RosVehicleServer {
	
	public static final Logger logger = Logger.getLogger(RosVehicleServer.class.getName());
	
	public static final String DEFAULT_MASTER_URI = "http://localhost:11311";
	public static final String DEFAULT_NODE_NAME = "vehicle";
	
	protected String _masterURI;
	protected String _nodeName;
	protected VehicleServer _server;
	
	protected DefaultNode _node;
	protected Publisher<UtmPoseWithCovarianceStamped> _statePublisher;
	protected Publisher<CompressedImage> _imagePublisher;
	protected Publisher<CameraInfo> _cameraInfoPublisher;
	protected List<Publisher<?>> _sensorPublishers;
	
	public RosVehicleServer(VehicleServer server) {
		this(DEFAULT_MASTER_URI, DEFAULT_NODE_NAME, server);
	}
	
	public RosVehicleServer(String masterURI, String nodeName, VehicleServer server) {
		
		_masterURI = masterURI;
		_nodeName = nodeName;
		_server = server;
		
		// Create configuration for a ROS node
		NodeRunner runner = NodeRunner.createDefault();
		NodeConfiguration configuration = NodeConfiguration.createDefault();
		String host = InetAddressFactory.createNonLoopback().getHostAddress();	//To avoid the node referring to localhost, which is unresolvable for external methods
		configuration.setHost(host);
		configuration.setMasterUri(new URI(_masterURI));
		
		// Start up a ROS node
		_node = new DefaultNode(_nodeName, configuration);
		NameResolver resolver = _node.getResolver().createResolver(new GraphName("vehicle"));
		
		// Create publisher for state data
		_statePublisher = _node.createPublisher(resolver.resolve("state"), "std_msgs/String");
	    
	    // Create publisher for image data and camera info
	    _imagePublisher =
	        _node.createPublisher(resolver.resolve("image_raw/compressed"), "sensor_msgs/CompressedImage");
	    _cameraInfoPublisher =
	        _node.createPublisher(resolver.resolve("camera_info"), "sensor_msgs/CameraInfo");
	    
	    // Query for vehicle capabilities and create corresponding publishers
	    int nSensors = server.getNumSensors();
		_sensorPublishers = new ArrayList<Publisher<?>>(nSensors);
		for (int iSensor = 0; iSensor < nSensors; ++iSensor) {
			_sensorPublishers.set(iSensor,
					_node.createPublisher(RosVehicleConfig.SENSOR_TOPIC_PREFIX + iSensor, "crwlib_msgs/String"));
		}

		// Create an action server for vehicle navigation
		// TODO: do we need to re-instantiate spec each time here?
		RosVehicleNavigation.Server navServer = new RosVehicleNavigation.Spec().buildSimpleActionServer(_nodeName, navigationHandler, true);
		runner.run(navServer, configuration);
		
		// Create an action server for image capturing
		// TODO: do we need to re-instantiate spec each time here?
		RosVehicleImaging.Server imageServer = new RosVehicleImaging.Spec().buildSimpleActionServer(_nodeName, imageCaptureHandler, true);
		runner.run(imageServer, configuration);

		// Create ROS services for accessor and setter functions
		// TODO: wait until services are implemented here
		
		
		// Register handlers to publish state, image, and sensor data
		_server.addStateListener(stateHandler);
		_server.addImageListener(imageHandler);
		for (int iSensor = 0; iSensor < nSensors; ++iSensor) {
			_server.addSensorListener(iSensor, new SensorHandler((Publisher<SensorData>)_sensorPublishers.get(iSensor)));
		}
		
		logger.info("Server initialized successfully.");
	}
	
	public void shutdown() {
		// TODO: shut down the ROS node and the action lib services here 
	}
	
	/**
	 * This child class publishes state change information on the state topic. 
	 */
	public final VehicleStateListener stateHandler = new VehicleStateListener() {
		
		@Override
		public void receivedState(Object state) {
			// TODO: fill this in
		}
	};
	
	/**
	 * This child class publishes new captured images on the image topic. 
	 */
	public final VehicleImageListener imageHandler = new VehicleImageListener() {
		
		@Override
		public void receivedImage(Object image) {
			// TODO: fill this in
		}
	};
	
	public class SensorHandler implements VehicleSensorListener {

		private final Publisher<SensorData> _publisher;
		
		public SensorHandler(final Publisher<SensorData> publisher) {
			_publisher = publisher;
		}
		
		@Override
		public void receivedSensor(Object sensor) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * This child class handles all of the logic associated with performing
	 * navigation as a preemptible task.  
	 */
	public final SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> 
		navigationHandler = new SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult>() {

		@Override
		public void goalCallback( SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Received goal: ");
		}

		@Override
		public void preemptCallback(
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Goal cancelled: ");

		}

		@Override
		public void blockingGoalCallback(VehicleNavigationGoal goal, SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Received blocking goal: " + goal);
			
			System.out.println("BLOCKING GOAL CALLBACK");
			VehicleNavigationFeedback feedback = new VehicleNavigationFeedback();
			do{
				try{
					feedback.stamp = new WallclockProvider().getCurrentTime();
					feedback.pose_status = vehicle_server.getWaypointStatus();
					vehicle_server.controllerUpdate();
					vehicle_server.current_pose.header.stamp = new WallclockProvider().getCurrentTime();
					System.out.print("Server sends\n\t");
					//System.out.println("Time : "+feedback.stamp.toString()+" Status : "+feedback.pose_status);
					System.out.println("Current x = "+vehicle_server.current_pose.pose.position.x+"Target x = "+goal.target_pose.position.x);
					actionServer.publishFeedback(feedback);
					
					snore();//Sleeps for one second. Optional, could be removed.
				}catch(Exception e)
				{
					System.out.println("\nWeebop!");
					throw new RuntimeException(e);
				}
			} while(distToGoal(vehicle_server.current_pose.pose, goal.target_pose)>0.1);	//Otherwise till goal is reached.
			System.out.println("Lalalla");
			actionServer.setSucceeded(); //Finish it off
		}
	};
	
	/**
	 * This child class handles all of the logic associated with performing
	 * image capture as a preemptible task.  
	 */
	public final SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> 
		imageCaptureHandler = new SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult>() {

			@Override
			public void blockingGoalCallback(
					VehicleImageCaptureGoal arg0,
					SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void goalCallback(
					SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void preemptCallback(
					SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg0) {
				// TODO Auto-generated method stub
				
			}
	};
}
