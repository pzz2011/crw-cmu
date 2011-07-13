package edu.cmu.ri.crw;

import org.ros.Node;
import org.ros.actionlib.server.DefaultSimpleActionServer;
import org.ros.exception.RosInitException;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionResult;
import org.ros.message.actionlib_tutorials.AirboatNavigationFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationResult;

public class RosVehicleActionServer
		extends
		DefaultSimpleActionServer<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult>  {

	public RosVehicleActionServer(
		      String nameSpace,
		      RosVehicleActionSpec spec,
		      RosVehicleServerCallbacks callbacks,
		      boolean useBlockingGoalCallback) throws RosInitException {
		    super(nameSpace, spec, callbacks, useBlockingGoalCallback);
		  }

		  public RosVehicleActionServer(
		      Node parent,
		      String nameSpace,
		      RosVehicleActionSpec spec,
		      RosVehicleServerCallbacks callbacks,
		      boolean useBlockingGoalCallback) throws RosInitException {
		    super(parent, nameSpace, spec, callbacks, useBlockingGoalCallback);
		  }

}
