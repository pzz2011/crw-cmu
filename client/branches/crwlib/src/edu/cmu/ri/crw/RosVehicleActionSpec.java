package edu.cmu.ri.crw;

import org.ros.Node;
import org.ros.actionlib.ActionSpec;
import edu.cmu.ri.crw.RosVehicleActionServer;
import edu.cmu.ri.crw.RosVehicleServerCallbacks;
import edu.cmu.ri.crw.RosVehicleActionClient;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.exception.RosInitException;
import org.ros.message.actionlib_tutorials.AirboatNavigationAction;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationActionResult;
import org.ros.message.actionlib_tutorials.AirboatNavigationFeedback;
import org.ros.message.actionlib_tutorials.AirboatNavigationGoal;
import org.ros.message.actionlib_tutorials.AirboatNavigationResult;


public class RosVehicleActionSpec 
extends ActionSpec<AirboatNavigationAction, AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult>{

	public RosVehicleActionSpec()
			throws RosException {
		super(AirboatNavigationAction.class, 
				"actionlib_tutorials/AirboatNavigationAction",
		        "actionlib_tutorials/AirboatNavigationActionFeedback",
		        "actionlib_tutorials/AirboatNavigationActionGoal",
		        "actionlib_tutorials/AirboatNavigationActionResult",
		        "actionlib_tutorials/AirboatNavigationFeedback",
		        "actionlib_tutorials/AirboatNavigationGoal",
		        "actionlib_tutorials/AirboatNavigationResult"
		        );
		// TODO Use own package instead of actionlib_tutorials
	}
	  @Override
	  public
	      RosVehicleActionServer
	      buildSimpleActionServer(
	          String nameSpace,
	          SimpleActionServerCallbacks<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult> callbacks,
	          boolean useBlockingGoalCallback) throws RosInitException {

	    return new RosVehicleActionServer(nameSpace, this, (RosVehicleServerCallbacks) callbacks, useBlockingGoalCallback);

	  }

	  @Override
	  public
	      RosVehicleActionServer
	      buildSimpleActionServer(
	          Node node,
	          String nameSpace,
	          SimpleActionServerCallbacks<AirboatNavigationActionFeedback, AirboatNavigationActionGoal, AirboatNavigationActionResult, AirboatNavigationFeedback, AirboatNavigationGoal, AirboatNavigationResult>callbacks,
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
