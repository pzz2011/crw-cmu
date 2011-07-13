package edu.cmu.ri.crw;

import java.net.URI;
import java.net.URISyntaxException;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.actionlib.client.SimpleActionClientCallbacks;

import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.exception.RosException;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.message.Duration;
import org.ros.message.actionlib_tutorials.AirboatNavigationFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationResult;


public class RunRosVehicleActionClient {

	public static void main(String[] args) {

		NodeConfiguration configuration = NodeConfiguration.createDefault();
		NodeRunner runner = NodeRunner.createDefault();

		String masterURI = "http://syrah.cimds.ri.cmu.edu:11311";
		String host = InetAddressFactory.createNonLoopback().getHostAddress();

		configuration.setHost(host);
		try {
			configuration.setMasterUri(new URI(masterURI));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		run(runner, configuration);

	}

	private static void run(NodeRunner runner, NodeConfiguration configuration) {

		try{

			String nodeName = "airboat_client";
			RosVehicleActionSpec spec = new RosVehicleActionSpec();
			RosVehicleActionClient sac = spec.buildSimpleActionClient(nodeName);

			runner.run(sac, configuration);
			 try {
			        Thread.sleep(10000);
			      } catch (InterruptedException e) {
			        // Don't care
			      }

			System.out.println("[Test] Waiting for action server to start");
			// wait for the action server to start
			sac.waitForServer(); // will wait for infinite time
			System.out.println("[Test] Action server started, sending goal");
			
			AirboatNavigationGoal goal = spec.createGoalMessage();
		
			goal.target_pose.position.x=1;
			goal.target_pose.position.y=1;
			goal.target_pose.position.z=1;
			
			goal.target_pose.orientation.w=1;
			goal.target_pose.orientation.x=1;
			goal.target_pose.orientation.y=1;
			goal.target_pose.orientation.z=1;
			
			sac.sendGoal(goal, new SimpleActionClientCallbacks<AirboatNavigationFeedback, AirboatNavigationResult>() {
		        @Override
		        public void feedbackCallback(AirboatNavigationFeedback feedback) {
		          System.out.print("Client feedback\n\t");
		          
		        }

		        @Override
		        public void doneCallback(SimpleClientGoalState state, AirboatNavigationResult result) {
		          System.out.println("Client done " + state);
		          
		        }

		        @Override
		        public void activeCallback() {
		          System.out.println("Client active");
		        }
		      });

			// wait for the action to return
		      System.out.println("[Test] Waiting for result.");
		      boolean finished_before_timeout = sac.waitForResult(new Duration(100.0));

		      if (finished_before_timeout) {
		        SimpleClientGoalState state = sac.getState();
		        System.out.println("[Test] Action finished: " + state.toString());

		        AirboatNavigationResult res = sac.getResult();
		        System.out.print("[Test] Final pose : " + res.final_pose);
		       
		        System.out.println();
		      } else {
		        System.out.println("[Test] Action did not finish before the time out");
		      }
			

		}
		catch(RosException e)
		{
			e.printStackTrace();
		}

	}

}
