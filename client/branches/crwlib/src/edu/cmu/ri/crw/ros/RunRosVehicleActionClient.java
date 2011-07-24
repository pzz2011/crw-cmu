package edu.cmu.ri.crw.ros;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import edu.cmu.ri.crw.ros.RosVehicleConfig;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.actionlib.client.SimpleActionClientCallbacks;
import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.exception.RosException;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.message.Duration;
import org.ros.message.crwlib_msgs.VehicleNavigationFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationResult;
import org.ros.namespace.NameResolver;

/**
 * Starts a Simple Actionlib client for a RosVehicleActionServer.
 * 
 * @author kshaurya
 *
 */
public class RunRosVehicleActionClient {

	public static final Logger logger = Logger.getLogger(RunRosVehicleActionClient.class
			.getName());
	public static void main(String[] args) {

		
		run();
		logger.info("\nSo long!");
	}

	private static void run() {

		try{

			String _nodeName = "airboat_client";
			//RosVehicleServer spec = new RosVehicleActionSpec();
			NodeConfiguration navConfig = NodeConfiguration.createDefault();
			NodeRunner runner = NodeRunner.createDefault();

			String masterURI=RosVehicleConfig.DEFAULT_MASTER_URI;
			String host = InetAddressFactory.createNonLoopback().getHostAddress();
			System.out.println("in host "+host);
			navConfig.setHost(host);
			try {
				navConfig.setMasterUri(new URI(masterURI));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			NameResolver navResolver = NameResolver.createFromString("/NAV");
			navConfig.setParentResolver(navResolver);
			final RosVehicleNavigation.Client navClient = new RosVehicleNavigation.Spec().buildSimpleActionClient(_nodeName);

			runner.run(navClient, navConfig);
			int i=5;
			try {
				do{
				logger.info(i+"..");
				Thread.sleep(1000);
				}while(i-->0);
			} catch (InterruptedException e) {
				// Don't care
			}

			logger.info("[Test] Waiting for action server to start");
			// wait for the action server to start
			navClient.waitForServer(); // will wait for infinite time
			logger.info("[Test] Action server started, sending goal");

			VehicleNavigationGoal goal = new RosVehicleNavigation.Spec().createGoalMessage();

			goal.targetPose.pose.position.x=10;
			goal.targetPose.pose.position.y=10;
			

			
			navClient.sendGoal(goal, new SimpleActionClientCallbacks<VehicleNavigationFeedback, VehicleNavigationResult>() {
				@Override
				public void feedbackCallback(VehicleNavigationFeedback feedback) {
					
					logger.info("Client feedback\n\t");
					logger.info("[Time] : "+feedback.header.stamp.toString()+" [Status] : "+feedback.status);
				}

				@Override
				public void doneCallback(SimpleClientGoalState state, VehicleNavigationResult result) {
					logger.info("Client done " + state);
					VehicleNavigationResult res = null;
					try {
						res = navClient.getResult();
					} catch (RosException e) {
						e.printStackTrace();
					}
					logger.info(res.finalPose.pose.pose.position.x+", y = "+res.finalPose.pose.pose.position.y+", z= "+res.finalPose.pose.pose.position.z);
				}

				@Override
				public void activeCallback() {
					logger.info("Client active");
				}
			});

			// wait for the action to return
			logger.info("[Test] Waiting for result.");
			boolean finished_before_timeout = navClient.waitForResult(new Duration(10000));//Forever!

			if (finished_before_timeout) {
				SimpleClientGoalState state = navClient.getState();
				logger.info("[Test] Action finished: " + state.toString());

				VehicleNavigationResult res = navClient.getResult();
				logger.info("[Test] Final status : " + res.status);

			} else {
				logger.info("[Test] Action did not finish before the time out, and state is "+navClient.getState());
			}
			
		}
		catch(RosException e)
		{
			e.printStackTrace();
			
		}

	}

}
