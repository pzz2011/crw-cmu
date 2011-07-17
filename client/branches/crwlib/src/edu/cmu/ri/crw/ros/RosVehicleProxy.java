package edu.cmu.ri.crw.ros;

import SimpleActionClientCallbacks;
import VehicleNavigationFeedback;
import VehicleNavigationResult;

import java.net.URI;
import java.util.logging.Logger;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.UTM;
import edu.cmu.ri.crw.VehicleServer;

/**
 * Takes the node name of an existing RosVehicleServer and connects through ROS,
 * wrapping the functionality of a VehicleServer transparently.  Once connected,
 * this object can be used as a vehicle server, but all commands will be 
 * forwarded to the underlying ROS node.
 * 
 * @author pkv
 *
 */
public class RosVehicleProxy extends AbstractVehicleServer {

	public Logger logger = Logger.getLogger(RosVehicleProxy.class.getName());
	
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
		
		//RosVehicleActionServer sas = spec.buildSimpleActionServer(serverNodeName, impl, true);
		NodeConfiguration configuration = NodeConfiguration.createDefault();
		String host = InetAddressFactory.createNonLoopback().getHostAddress();	//To avoid the node referring to localhost, which is unresolvable for external methods
		configuration.setHost(host);
		configuration.setMasterUri(new URI(_masterURI));
		NodeRunner runner = NodeRunner.createDefault();

		runner.run(sas, configuration);
		logger.info("Server initialized successfully.");
	}

	@Override
	public Image captureImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UTM getOrigin() {
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
	public double[] getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UTM getWaypoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WaypointState getWaypointStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOrigin(UTM utm) {
		// TODO Auto-generated method stub
		
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
	public void setState(double[] p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startCamera(double interval, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startWaypoint(UTM waypoint) {
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
	
	SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult> navigationHandler = new SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult>() {
		
		@Override
		public void feedbackCallback(VehicleNavigationFeedback feedback) {
			logger.info("Vehicle feedback");
		}

		@Override
		public void doneCallback(SimpleClientGoalState state, VehicleNavigationResult result) {
			logger.info("Vehicle finished");
		}
		
		@Override
		public void activeCallback() {
			logger.info("Vehicle active");
		}
	};
	
}
