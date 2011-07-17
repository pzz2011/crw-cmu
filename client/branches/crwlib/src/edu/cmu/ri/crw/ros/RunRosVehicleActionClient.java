package edu.cmu.ri.crw.ros;

import java.net.URI;
import java.net.URISyntaxException;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.actionlib.client.SimpleActionClientCallbacks;

import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.exception.RosException;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.message.Duration;
import org.ros.message.crwlib_msgs.*;

/**
 * Starts a Simple Actionlib client for a RosVehicleActionServer.
 * 
 * @author kshaurya
 *
 */
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
		System.out.println("\nSo long!");
	}

	private static void run(NodeRunner runner, NodeConfiguration configuration) {

		try{

			String nodeName = "airboat_client";
			RosVehicleActionSpec spec = new RosVehicleActionSpec();
			RosVehicleActionClient sac = spec.buildSimpleActionClient(nodeName);

			runner.run(sac, configuration);
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
			sac.waitForServer(); // will wait for infinite time
			System.out.println("[Test] Action server started, sending goal");

			VehicleNavigationGoal goal = spec.createGoalMessage();

			goal.target_pose.position.x=1;
			goal.target_pose.position.y=1;
			goal.target_pose.position.z=1;

			goal.target_pose.orientation.w=1;
			goal.target_pose.orientation.x=1;
			goal.target_pose.orientation.y=1;
			goal.target_pose.orientation.z=1;

			sac.sendGoal(goal, new SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult>() {
				@Override
				public void feedbackCallback(VehicleNavigationFeedback feedback) {
					
					System.out.print("Client feedback\n\t");
					System.out.println("[Time] : "+feedback.stamp.toString()+" [Status] : "+feedback.pose_status);
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

			// wait for the action to return
			System.out.println("[Test] Waiting for result.");
			boolean finished_before_timeout = sac.waitForResult(new Duration(100000));//20 seconds

			if (finished_before_timeout) {
				SimpleClientGoalState state = sac.getState();
				System.out.println("[Test] Action finished: " + state.toString());

				VehicleNavigationResult res = sac.getResult();
				System.out.print("[Test] Final pose : " + res.final_pose);

				System.out.println();
			} else {
				System.out.println("[Test] Action did not finish before the time out, and state is "+sac.getState());
			}

		}
		catch(RosException e)
		{
			e.printStackTrace();
			
		}

	}

}
