package edu.cmu.ri.crw;

import org.ros.Node;
import org.ros.actionlib.server.DefaultSimpleActionServer;
import org.ros.exception.RosInitException;
import org.ros.message.crwlib_msgs.*;

public class RosVehicleActionServer
		extends
		DefaultSimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult>  {

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
