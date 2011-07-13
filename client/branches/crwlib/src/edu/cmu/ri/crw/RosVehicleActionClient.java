package edu.cmu.ri.crw;

import org.ros.Node;
import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.client.SimpleActionClient;
import org.ros.exception.RosException;
import org.ros.message.crwlib_msgs.*;

public class RosVehicleActionClient
extends
SimpleActionClient<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

	public RosVehicleActionClient(
			Node parentNode,
			String nameSpace,
			ActionSpec<?, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> spec)
	throws RosException {
		super(parentNode, nameSpace, spec);
	
	}
	public RosVehicleActionClient(String nameSpace, ActionSpec<?, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> spec)
	throws RosException {
		super(nameSpace, spec);
	}

}
