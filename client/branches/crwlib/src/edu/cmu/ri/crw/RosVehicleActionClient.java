package edu.cmu.ri.crw;

import org.ros.Node;
import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.client.SimpleActionClient;
import org.ros.exception.RosException;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionResult;
import org.ros.message.actionlib_tutorials.AirboatNavigationFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationResult;

public class RosVehicleActionClient
extends
SimpleActionClient<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult> {

	public RosVehicleActionClient(
			Node parentNode,
			String nameSpace,
			ActionSpec<?, AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult> spec)
	throws RosException {
		super(parentNode, nameSpace, spec);
	
	}
	public RosVehicleActionClient(String nameSpace, ActionSpec<?, AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult> spec)
	throws RosException {
		super(nameSpace, spec);
	}

}
