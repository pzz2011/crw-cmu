package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.actionlib.client.SimpleActionClientCallbacks;
import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.exception.RosException;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.message.crwlib_msgs.Utm;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.crwlib_msgs.VehicleImageCaptureFeedback;
import org.ros.message.crwlib_msgs.VehicleImageCaptureResult;
import org.ros.message.crwlib_msgs.VehicleNavigationFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationResult;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

import edu.cmu.ri.crw.AbstractVehicleServer;

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

	public static final Logger logger = Logger.getLogger(RosVehicleProxy.class
			.getName());

	public static final String DEFAULT_MASTER_URI = "http://localhost:11311";
	public static final String DEFAULT_NODE_NAME = "vehicle";

	protected String _masterURI;
	protected String _nodeName;

	public RosVehicleProxy() {
		this(DEFAULT_MASTER_URI, DEFAULT_NODE_NAME);
	}

	public RosVehicleProxy(String nodeName) {
		this(DEFAULT_MASTER_URI, nodeName);
	}

	public RosVehicleProxy(String masterURI, String nodeName) {

		_masterURI = masterURI;
		_nodeName = nodeName;

		// Create a node configuration and start a node runner
		// (To avoid the node referring to localhost, we use createNonLoopback)
		NodeRunner runner = NodeRunner.createDefault();
		NodeConfiguration configuration = NodeConfiguration.createDefault();
		String host = InetAddressFactory.createNonLoopback().getHostAddress();
		configuration.setHost(host);

		try {
			configuration.setMasterUri(new URI(_masterURI));
		} catch (URISyntaxException ex) {
			logger.severe("Couldn't find master URI: " + _masterURI);
		}

		try {
			RosVehicleNavigation.Client navClient = new RosVehicleNavigation.Spec()
					.buildSimpleActionClient(_nodeName);
			runner.run(navClient, configuration);
		} catch (RosException ex) {
			logger.severe("Unable to start navigation action client: " + ex);
		}

		try {
			RosVehicleImaging.Client imageCaptureClient = new RosVehicleImaging.Spec()
					.buildSimpleActionClient(_nodeName);
			runner.run(imageCaptureClient, configuration);
		} catch (RosException ex) {
			logger.severe("Unable to start image action client: " + ex);
		}

		logger.info("Proxy initialized successfully.");
	}

	@Override
	public CompressedImage captureImage(int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getPID(int axis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SensorType getSensorType(int channel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UtmPoseWithCovarianceStamped getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UtmPose getWaypoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WaypointState getWaypointStatus() {
		// TODO Auto-generated method stub
		return WaypointState.DONE;
	}

	@Override
	public void setPID(int axis, double[] gains) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSensorType(int channel, SensorType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(UtmPose p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCamera(int numFrames, double interval, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startWaypoint(Utm waypoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopCamera() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopWaypoint() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumSensors() {
		// TODO Auto-generated method stub
		return 0;
	}

	SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult> navigationHandler = new SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult>() {

		@Override
		public void feedbackCallback(VehicleNavigationFeedback feedback) {
			logger.info("Vehicle feedback");
		}

		@Override
		public void doneCallback(SimpleClientGoalState state,
				VehicleNavigationResult result) {
			logger.info("Vehicle finished");
		}

		@Override
		public void activeCallback() {
			logger.info("Vehicle active");
		}
	};
	
	SimpleActionClientCallbacks<VehicleImageCaptureFeedback, VehicleImageCaptureResult> imageCaptureHandler = new SimpleActionClientCallbacks<VehicleImageCaptureFeedback, VehicleImageCaptureResult>() {

		@Override
		public void feedbackCallback(VehicleImageCaptureFeedback feedback) {
			logger.info("Capture feedback");
		}
		
		@Override
		public void doneCallback(SimpleClientGoalState state, VehicleImageCaptureResult result) {
			logger.info("Capture finished");
		}

		@Override
		public void activeCallback() {
			logger.info("Capture active");
		}

	};

	@Override
	public TwistWithCovarianceStamped getVelocity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVelocity(Twist velocity) {
		// TODO Auto-generated method stub
		
	}
}
