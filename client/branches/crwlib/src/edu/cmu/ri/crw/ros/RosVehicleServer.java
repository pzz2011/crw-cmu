package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ros.DefaultNode;
import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.Publisher;
import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.internal.time.WallclockProvider;
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
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.sensor_msgs.CameraInfo;
import org.ros.message.sensor_msgs.CompressedImage;
import org.ros.namespace.NameResolver;

import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.VehicleImageListener;
import edu.cmu.ri.crw.VehicleSensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VehicleStateListener;
import edu.cmu.ri.crw.WaypointObserver;

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

	public static final String DEFAULT_MASTER_URI = "http://localhost:11311";
	public static final String DEFAULT_NODE_NAME = "vehicle";

	protected String _masterURI;
	protected String _nodeName;
	protected VehicleServer _server;

	protected RosVehicleNavigation.Server _navServer;
	protected RosVehicleImaging.Server _imgServer;

	protected DefaultNode _node;
	protected Publisher<UtmPoseWithCovarianceStamped> _statePublisher;
	protected Publisher<CompressedImage> _imagePublisher;
	protected Publisher<CameraInfo> _cameraInfoPublisher;
	protected List<Publisher<SensorData>> _sensorPublishers;

	public RosVehicleServer(VehicleServer server) {
		this(DEFAULT_MASTER_URI, DEFAULT_NODE_NAME, server);
	}

	public RosVehicleServer(String masterURI, String nodeName,
			VehicleServer server) {

		_masterURI = masterURI;
		_nodeName = nodeName;
		_server = server;

		_imgServer = null;

		// Create configuration for a ROS node
		NodeRunner runner = NodeRunner.createDefault();
		NodeConfiguration configuration = NodeConfiguration.createDefault();
		NodeConfiguration navConfig = NodeConfiguration.createDefault();
		NodeConfiguration imgConfig = NodeConfiguration.createDefault();
		String host = InetAddressFactory.createNonLoopback().getHostAddress(); // To
		// avoid
		// the
		// node
		// referring
		// to
		// localhost,
		// which
		// is
		// unresolvable
		// for
		// external
		// methods
		navConfig.setHost(host);
		imgConfig.setHost(host);
		configuration.setHost(host);
		NameResolver navResolver = NameResolver.createFromString("/NAV");
		navConfig.setParentResolver(navResolver);
		NameResolver imgResolver = NameResolver.createFromString("/IMG");
		imgConfig.setParentResolver(imgResolver);

		try {
			configuration.setMasterUri(new URI(_masterURI));
			navConfig.setMasterUri(new URI(_masterURI));
			imgConfig.setMasterUri(new URI(_masterURI));
		} catch (URISyntaxException ex) {
			logger.severe("Couldn't find master URI: " + _masterURI);
		}

		// Start up a ROS node
		_node = new DefaultNode(_nodeName + "/Broadcast", configuration);

		// Create publisher for state data
		_statePublisher = _node.createPublisher(configuration
				.getParentResolver().resolve("state"),
		"crwlib_msgs/UtmPoseWithCovarianceStamped");

		// Create publisher for image data and camera info
		_imagePublisher = _node.createPublisher(configuration
				.getParentResolver().resolve("image_raw/compressed"),
		"sensor_msgs/CompressedImage");
		_cameraInfoPublisher = _node.createPublisher(configuration
				.getParentResolver().resolve("camera_info"),
		"sensor_msgs/CameraInfo");

		// Query for vehicle capabilities and create corresponding publishers
		int nSensors = server.getNumSensors();
		_sensorPublishers = new ArrayList<Publisher<SensorData>>(nSensors);
		System.out.println(" nSensors = " + nSensors);
		for (int iSensor = 0; iSensor < nSensors && iSensor < nSensors; ++iSensor) {
			Publisher<SensorData> sensorPublisher = _node.createPublisher(
					RosVehicleConfig.SENSOR_TOPIC_PREFIX + iSensor,
			"crwlib_msgs/SensorData");
			_sensorPublishers.add(iSensor, sensorPublisher);
		}

		// Create an action server for vehicle navigation
		// TODO: do we need to re-instantiate spec each time here?
		try {

			_navServer = new RosVehicleNavigation.Spec()
			.buildSimpleActionServer(_node, _nodeName + "/NAV",
					navigationHandler, true);
			runner.run(_navServer, navConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		// Create an action server for image capturing
		// TODO: do we need to re-instantiate spec each time here?
		try {
			_imgServer = new RosVehicleImaging.Spec().buildSimpleActionServer(
					_node, _nodeName + "/IMAGE", imageCaptureHandler, true);
			runner.run(_imgServer, imgConfig);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		// Create ROS services for accessor and setter functions
		// TODO: wait until services are implemented here

		// Register handlers to publish state, image, and sensor data
		_server.addStateListener(stateHandler);
		_server.addImageListener(imageHandler);
		for (int iSensor = 0; iSensor < nSensors; ++iSensor) {
			_server.addSensorListener(iSensor, new SensorHandler(
					(Publisher<SensorData>) _sensorPublishers.get(iSensor)));
		}

		logger.info("Server initialized successfully.");
	}

	public void shutdown() {
		// TODO: shut down the ROS node and the action lib services here p
	}

	/**
	 * This child class publishes state change information on the state topic.
	 */
	public final VehicleStateListener stateHandler = new VehicleStateListener() {

		@Override
		public void receivedState(UtmPoseWithCovarianceStamped pose) {
			_statePublisher.publish(pose);
		}
	};

	/**
	 * This child class publishes new captured images on the image topic.
	 */
	public final VehicleImageListener imageHandler = new VehicleImageListener() {

		@Override
		public void receivedImage(CompressedImage image) {
			_imagePublisher.publish(image);
		}
	};

	public class SensorHandler implements VehicleSensorListener {

		private final Publisher<SensorData> _publisher;

		public SensorHandler(final Publisher<SensorData> publisher) {
			_publisher = publisher;
		}

		@Override
		public void receivedSensor(SensorData sensor) {
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
			logger.info("Received goal: ");
		}

		@Override
		public void preemptCallback(
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Goal cancelled: ");

		}

		@Override
		public void blockingGoalCallback(
				VehicleNavigationGoal goal,
				SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> actionServer) {
			logger.info("Received blocking goal: " + goal);

			System.out.println("BLOCKING GOAL CALLBACK");
			/*
			 * VehicleNavigationFeedback feedback = new
			 * VehicleNavigationFeedback(); do{ try{ feedback.stamp = new
			 * WallclockProvider().getCurrentTime(); feedback.pose_status =
			 * vehicle_server.getWaypointStatus();
			 * vehicle_server.controllerUpdate();
			 * vehicle_server.current_pose.header.stamp = new
			 * WallclockProvider().getCurrentTime();
			 * System.out.print("Server sends\n\t");
			 * //System.out.println("Time : "
			 * +feedback.stamp.toString()+" Status : "+feedback.pose_status);
			 * System
			 * .out.println("Current x = "+vehicle_server.current_pose.pose
			 * .position.x+"Target x = "+goal.target_pose.position.x);
			 * actionServer.publishFeedback(feedback);
			 * 
			 * snore();//Sleeps for one second. Optional, could be removed.
			 * }catch(Exception e) { System.out.println("\nWeebop!"); throw new
			 * RuntimeException(e); } }
			 * while(distToGoal(vehicle_server.current_pose.pose,
			 * goal.target_pose)>0.1); //Otherwise till goal is reached.
			 * System.out.println("Lalalla"); actionServer.setSucceeded();
			 * //Finish it off
			 */
			/*
			 * do{ try { Thread.sleep(1000); } catch (InterruptedException e) {
			 * e.printStackTrace(); }
			 * logger.info("Current x = "+_server.getState
			 * ().pose.pose.pose.position
			 * .x+"Current y = "+_server.getState().pose.pose.pose.position.y);
			 * }
			 * while(_server.getWaypointStatus()!=VehicleServer.WaypointState.DONE
			 * );
			 */

			if (goal != null)
				_server.startWaypoint(goal.targetPose, new WaypointObserver() {

					@Override
					public void waypointUpdate(VehicleServer server) {

						switch (_server.getWaypointStatus()) {
						case GOING:
							VehicleNavigationFeedback feedback = new VehicleNavigationFeedback();
							feedback.header.stamp = new WallclockProvider()
							.getCurrentTime();
							_navServer.publishFeedback(feedback);
							break;
						case DONE:
							VehicleNavigationResult result = new VehicleNavigationResult();
							result.finalPose.pose.header.stamp = new WallclockProvider()
							.getCurrentTime();
							result.finalPose.pose.pose = _server.getWaypoint().pose;
							_navServer.setSucceeded(result, "Tada!!!");

							break;
						default:
							logger.log(Level.SEVERE, "ABORTED!!!!");
						}
					}
				});

			System.out.println("\nLeaving town!"+_navServer.isActive());
		}
	};

	/**
	 * This child class handles all of the logic associated with performing
	 * image capture as a preemptible task.
	 */
	public final SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> imageCaptureHandler = new SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult>() {

		@Override
		public void blockingGoalCallback(
				VehicleImageCaptureGoal goal,
				SimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> arg1) {

			if (goal != null)
				_server.startCamera(goal.frames, goal.interval, goal.width,
						goal.height, new ImagingObserver() {

					@Override
					public void imagingUpdate(VehicleServer server) {

						// Do whatever you want to
						VehicleImageCaptureFeedback feedback = new VehicleImageCaptureFeedback();
						feedback.header.stamp = new WallclockProvider()
						.getCurrentTime();
						feedback.status = (byte) (_server
								.getCameraStatus().equals(
										CameraState.CAPTURING) == true ? 1
												: 0);
						_imgServer.publishFeedback(feedback);

					}
				});
			VehicleNavigationResult result = new VehicleNavigationResult();
			result.finalPose.pose.pose = _server.getState().pose.pose.pose;
			_navServer.setSucceeded(result, "Tada!!!");

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
