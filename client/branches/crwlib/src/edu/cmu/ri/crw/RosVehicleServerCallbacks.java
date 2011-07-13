/**
 * 
 */
package edu.cmu.ri.crw;

import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.message.crwlib_msgs.*;

/**
 * @author kshaurya
 *
 */
public class RosVehicleServerCallbacks
		implements
		SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

	@Override
	public void goalCallback(
			SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> 
actionServer) {
		// TODO Auto-generated method stub
		System.out.println("GOAL CALLBACK");
	}

	@Override
	public void preemptCallback(
			SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult>actionServer) {
		// TODO Auto-generated method stub
		System.out.println("PREEMPT CALLBACK");
		
	}

	@Override
	public void blockingGoalCallback(
			VehicleNavigationGoal goal,
			SimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult>actionServer) {
		// TODO Auto-generated method stub
		
		System.out.println("BLOCKING GOAL CALLBACK");
		
	}

}
