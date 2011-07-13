package edu.cmu.ri.crw;

import org.ros.Node;
import org.ros.actionlib.ActionSpec;
import edu.cmu.ri.crw.RosVehicleActionServer;
import edu.cmu.ri.crw.RosVehicleServerCallbacks;
import edu.cmu.ri.crw.RosVehicleActionClient;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.exception.RosInitException;
import org.ros.message.crwlib_msgs.*;

public class RosVehicleActionSpec 
extends ActionSpec<VehicleNavigationAction, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult>{

	public RosVehicleActionSpec()
			throws RosException {
		super(VehicleNavigationAction.class, 
				"crwlib_msgs/VehicleNavigationAction",
		        "crwlib_msgs/VehicleNavigationActionFeedback",
		        "crwlib_msgs/VehicleNavigationActionGoal",
		        "crwlib_msgs/VehicleNavigationActionResult",
		        "crwlib_msgs/VehicleNavigationFeedback",
		        "crwlib_msgs/VehicleNavigationGoal",
		        "crwlib_msgs/VehicleNavigationResult"
		        ); 
	}
	  @Override
	  public
	      RosVehicleActionServer
	      buildSimpleActionServer(
	          String nameSpace,
	          SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> callbacks,
	          boolean useBlockingGoalCallback) throws RosInitException {

	    return new RosVehicleActionServer(nameSpace, this, (RosVehicleServerCallbacks) callbacks, useBlockingGoalCallback);

	  }

	  @Override
	  public
	      RosVehicleActionServer
	      buildSimpleActionServer(
	          Node node,
	          String nameSpace,
	          SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult>callbacks,
	          boolean useBlockingGoalCallback) throws RosInitException {

	    return new RosVehicleActionServer(node, nameSpace, this, (RosVehicleServerCallbacks) callbacks, useBlockingGoalCallback);

	  }
	  
	  @Override
	  public RosVehicleActionClient buildSimpleActionClient(String nameSpace) {

	    RosVehicleActionClient sac = null;
	    try {
	      return new RosVehicleActionClient(nameSpace, this);
	    } catch (RosException e) {
	      e.printStackTrace();
	    }
	    return sac;

	  }

	  @Override
	  public RosVehicleActionClient buildSimpleActionClient(Node node, String nameSpace) {

	    RosVehicleActionClient sac = null;
	    try {
	      sac = new RosVehicleActionClient(node, nameSpace, this);
	    } catch (RosException e) {
	      e.printStackTrace();
	    }
	    return sac;

	  }

}
