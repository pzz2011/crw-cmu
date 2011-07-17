package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.util.logging.Logger;

import org.ros.DefaultNode;
import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.Publisher;
import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.internal.node.Node;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.crwlib_msgs.VehicleNavigationActionFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationActionGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationActionResult;
import org.ros.message.crwlib_msgs.VehicleNavigationFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationResult;

import edu.cmu.ri.crw.VehicleServer;

/**
 * Provides functionality for interfacing a vehicle server with ROS.  This 
 * object wraps an existing vehicle server and makes it available through a set
 * of ros actions, services, and topics.
 * 
 * @author pkv
 *
 */
public class RosVehicleServer {
	
	public Logger logger = Logger.getLogger(RosVehicleServer.class.getName());
	
	public static final String DEFAULT_MASTER_URI = "http://localhost:11311";
	public static final String DEFAULT_NODE_NAME = "vehicle";
	
	protected String _masterURI;
	protected String _nodeName;
	protected VehicleServer _server;
	
	protected DefaultNode _node;
	protected Publisher<MessageType> _statePublisher;
	protected Publisher<MessageType> _imagePublisher;
	protected Publisher<MessageType> _sensorPublisher[];
	
	public RosVehicleServer(VehicleServer server) {
		this(DEFAULT_MASTER_URI, DEFAULT_NODE_NAME, server);
	}
	
	public RosVehicleServer(String masterURI, String nodeName, VehicleServer server) {
		
		_masterURI = masterURI;
		_nodeName = nodeName;
		_server = server;
		
		
		// Create configuration for ROS node
		NodeConfiguration configuration = NodeConfiguration.createDefault(); // ???
		
		// Create a ROS node for publishing data streams
		_node = new DefaultNode(_nodeName, configuration);
	    _statePublisher = _node.createPublisher("chatter", "std_msgs/String");
	    _imagePublisher = _node.createPublisher("chatter", "std_msgs/String");
	    
	    // Query for vehicle capabilites and create corresponding publishers
	    int nSensors = server.getNumSensors();
		_sensorPublisher = new Publisher[nSensors];
		for (int iSensors = 0; iSensors < nSensors; ++iSensors) {
			_sensorPublisher[iSensors] = 
				_node.createPublisher(RosVehicleConfig.SENSOR_TOPIC_PREFIX + iSensors, "std_msgs/String");
		}

		// Create an action server for vehicle navigation
		// TODO: do we need to reinstantiate spec each time here?
		RosVehicleNavigation.Server sas = new RosVehicleNavigation.Spec().buildSimpleActionServer(_nodeName, navigationHandler, true);
		
		String host = InetAddressFactory.createNonLoopback().getHostAddress();	//To avoid the node referring to localhost, which is unresolvable for external methods
		configuration.setHost(host);
		configuration.setMasterUri(new URI(_masterURI));
		NodeRunner runner = NodeRunner.createDefault();
		
		// Create an action server for image capturing

		// Create ROS services for accessor and setter functions
		// TODO: wait until services are implemented here
		
		runner.run(sas, configuration);
		logger.info("Server initialized successfully.");
	}
	
	public void shutdown() {
		// TODO: shut down the ROS node and the action lib services here 
	}
	
	/**
	 * This child class handles all of the logic associated with performing
	 * navigation as a preemptible task.  
	 * 
	 * @author pkv
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
			}while(distToGoal(vehicle_server.current_pose.pose, goal.target_pose)>0.1);	//Otherwise till goal is reached.
			System.out.println("Lalalla");
			actionServer.setSucceeded(); //Finish it off
		}
	};
}
