/**
 * 
 */
package edu.cmu.ri.crw;

import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionResult;
import org.ros.message.actionlib_tutorials.AirboatNavigationFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationResult;

/**
 * @author kshaurya
 *
 */
public class RosVehicleServerCallbacks
		implements
		SimpleActionServerCallbacks<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult> {

	@Override
	public void goalCallback(
			SimpleActionServer<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult> 
actionServer) {
		// TODO Auto-generated method stub
		System.out.println("GOAL CALLBACK");
	}

	@Override
	public void preemptCallback(
			SimpleActionServer<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult>actionServer) {
		// TODO Auto-generated method stub
		System.out.println("PREEMPT CALLBACK");
		
	}

	@Override
	public void blockingGoalCallback(
			AirboatNavigationGoal goal,
			SimpleActionServer<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult>actionServer) {
		// TODO Auto-generated method stub
		
		System.out.println("BLOCKING GOAL CALLBACK");
		
	}

}
