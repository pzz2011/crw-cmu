package edu.cmu.ri.crw;

import java.net.URI;
import java.net.URISyntaxException;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.actionlib.client.SimpleActionClientCallbacks;
import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.actionlib.state.SimpleClientGoalState.StateEnum;
import org.ros.exception.RosException;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.message.Duration;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationResult;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.PoseStamped;
import org.ros.message.rosgraph_msgs.Log;

/**
 * A simple simulation of an unmanned boat.
 * 
 * The vehicle is fixed on the ground (Z = 0.0), and can only turn along the 
 * Z-axis and move along the X-axis (a unicycle motion model).  Imagery and
 * sensor are simulated using simple artificial generator functions that produce
 * recognizable basic patterns.  
 * 
 * Implementation of RosVehicleActionServer and RosVehicleActionClient
 * 
 * @author pkv
 * @author kss
 *
 */
public class SimpleBoatSimulator extends AbstractVehicleServer {

	protected static
	RosVehicleServerCallbacks impl;
	RosVehicleActionSpec spec;
	RosVehicleActionClient sac;
	String masterURI;
	String serverNodeName;
	String clientNodeName;
	
	public SimpleBoatSimulator() {
		try{
			impl = new RosVehicleServerCallbacks();
			spec = new RosVehicleActionSpec();
			masterURI = "http://syrah.cimds.ri.cmu.edu:11311";	//Address of roscore instance
			serverNodeName = "sim_vehicle_server";
			clientNodeName = "sim_vehicle_client";
			current_pose = new PoseStamped();
			impl.setVehicle_server(this);//Todo: Try sending this as constant object ref?
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {

		SimpleBoatSimulator sim = new SimpleBoatSimulator();
		try{
			sim.LaunchServer();
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Couldn't kick up the server!");
		}
		try{
			sim.LaunchClient();
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Couldn't kick up the client!");
		}
		System.out.println("\nSo long!");
	}
	public void LaunchServer()
	{
		try{
			RosVehicleActionServer sas = spec.buildSimpleActionServer(serverNodeName, impl, true);
			NodeConfiguration configuration = NodeConfiguration.createDefault();
			String host = InetAddressFactory.createNonLoopback().getHostAddress();	//To avoid the node referring to localhost, which is unresolvable for external methods
			configuration.setHost(host);
			configuration.setMasterUri(new URI(masterURI));
			NodeRunner runner = NodeRunner.createDefault();

			runner.run(sas, configuration);
			System.out.println("Server initialised successfuly\n");
		}catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}

	public void LaunchClient()
	{
		try {
			NodeConfiguration configuration = NodeConfiguration.createDefault();
			NodeRunner runner = NodeRunner.createDefault();
			String host = InetAddressFactory.createNonLoopback().getHostAddress();
			configuration.setHost(host);
			configuration.setMasterUri(new URI(masterURI));

			run(runner, configuration);

		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void run(NodeRunner runner, NodeConfiguration configuration) {
		try{

			//RosVehicleActionSpec spec = new RosVehicleActionSpec();
			sac = spec.buildSimpleActionClient(clientNodeName);

			runner.run(sac, configuration);

			//Fancy countdown
			int i=5;
			try {
				do{
					System.out.print(i+"..");
					Thread.sleep(1000);
				}while(i-->0);
			} catch (InterruptedException e) {
				// Don't care
			}

			System.out.println("[Test] Waiting for action server to start");
			// wait for the action server to start
			sac.waitForServer(); // will wait for infinite time [apparently]
			System.out.println("[Test] Action server started, sending goal");

			//----End of minimum client implementation
			//----Start dummy stuff
			VehicleNavigationGoal goal = new VehicleNavigationGoal();
			
			goal.target_pose.position.x=1.0;
			goal.target_pose.position.y=1.0;
			goal.target_pose.position.z=1.0;

			goal.target_pose.orientation.w=0.0;
			goal.target_pose.orientation.x=0.0;
			goal.target_pose.orientation.y=0.0;
			goal.target_pose.orientation.z=0.0;
			
			startWaypoint(goal);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public Object captureImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPID(double axis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSensorType(int channel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getWaypoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPID(double axis, double[] gains) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSensorType(int channel, Object type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(Object p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCamera() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startWaypoint(VehicleNavigationGoal target) {
		/**
		 * Set goal pose for the boat
		 */
		try{
			
			VehicleNavigationGoal goal = spec.createGoalMessage();
			goal = target.clone();
			
			
//TODO Look at sendGoalAndWait()
			sac.sendGoal(goal, new SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult>() {
				@Override
				public void feedbackCallback(VehicleNavigationFeedback feedback) {

					System.out.print("Client feedback\n\t");
					System.out.println("[Time] : "+feedback.stamp.toString()+" [Status] : "+ WaypointStatus[feedback.pose_status]);
				}

				@Override
				public void doneCallback(SimpleClientGoalState state, VehicleNavigationResult result) {
					System.out.println("Client done " + state);

				}
				@Override
				public void activeCallback() {
					System.out.println("Client active");
				}
			});
/*
			// wait for the action to return
			System.out.println("[Test] Waiting for result.");
			boolean finished_before_timeout = sac.waitForResult(new Duration(10000));//God knows what.

			if (finished_before_timeout) {
				SimpleClientGoalState state = sac.getState();
				System.out.println("[Test] Action finished: " + state.toString());

				VehicleNavigationResult res = sac.getResult();
				System.out.print("[Test] Final pose : " + res.final_pose);

				System.out.println();
			} else {
				System.out.println("[Test] Action did not finish before the time out, and state is "+sac.getState());
			}*/

		}
		catch(RosException e)
		{
			e.printStackTrace();

		}


	}
	@Override
	public void stopWaypoint() {
		/**
		 * Send a command to sac to stop the current goal.
		 */
		try {
			sac.cancelGoal();
		} catch (RosException e) {
			e.printStackTrace();
		}

	}
	public void controllerUpdate()
	{
		Double vel = 0.100000000000000d;
		current_pose.pose.position.x += vel;
		current_pose.pose.position.y += vel;
		current_pose.pose.position.z += vel;
	}
	@Override
	public void stopCamera() {
		// TODO Auto-generated method stub

	}

	

	public byte getWaypointStatus()
	{
		boolean addCondition=true;
		byte i=5;
		if(sac.getState().equals(StateEnum.SUCCEEDED))	//A result message is available
		{
			i=0;
		}
		else if(sac.getState().equals(StateEnum.ACTIVE))	//A goal is present
		{

			/*if(addCondition)	//And special loiter mode is active
			{
				i=4;	//LOITER
			}
			else*/ if(addCondition)	//or current status is active
			{
				i=2;
			}
			else
			{
				i=1;	//then it is in the process of reading
			}
		}
		else
		{
			if(addCondition)	//The waypoint controller is alive
			{
				i=0;	//WAITING
			}
			else
			{
				i=5;	//ERROR
			}
		}
		return i;
	}


}
