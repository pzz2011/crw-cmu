package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.util.logging.Logger;

import org.ros.internal.time.WallclockProvider;
import org.ros.message.geometry_msgs.Pose;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.ros.RosVehicleNavigation.*;

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
	
	public RosVehicleServer(VehicleServer server) {
		this(DEFAULT_MASTER_URI, DEFAULT_NODE_NAME, server);
	}
	
	public RosVehicleServer(String masterURI, String nodeName, VehicleServer server) {
		
		_masterURI = masterURI;
		_nodeName = nodeName;
		_server = server;
		
		RosVehicleActionServer sas = spec.buildSimpleActionServer(nodeName, impl, true);
		RosVehicleActionServer sas = spec.buildSimpleActionServer(nodeName, impl, true);
		NodeConfiguration configuration = NodeConfiguration.createDefault();
		String host = InetAddressFactory.createNonLoopback().getHostAddress();	//To avoid the node referring to localhost, which is unresolvable for external methods
		configuration.setHost(host);
		configuration.setMasterUri(new URI(_masterURI));
		NodeRunner runner = NodeRunner.createDefault();

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
	public class RosVehicleServerCallbacks implements SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

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

	}
}
