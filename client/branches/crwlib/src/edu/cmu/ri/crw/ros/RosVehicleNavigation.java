package edu.cmu.ri.crw.ros;

import org.ros.Node;
import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.client.SimpleActionClient;
import org.ros.actionlib.server.DefaultSimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.exception.RosInitException;
import org.ros.message.crwlib_msgs.*;

/**
 * Contains boilerplate template classes that are necessary for ROS actionlib
 * implementation of vehicle navigation actions.
 * 
 * @author pkv
 *
 */
public class RosVehicleNavigation {

	/**
	 * Wrapper for utilizing a SimpleActionClient
	 * 
	 * @author kshaurya
	 * 
	 */
	public class Client
			extends
			SimpleActionClient<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

		public RosVehicleActionClient(
				Node parentNode,
				String nameSpace,
				ActionSpec<?, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> spec)
				throws RosException {
			super(parentNode, nameSpace, spec);

		}

		public RosVehicleActionClient(
				String nameSpace,
				ActionSpec<?, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> spec)
				throws RosException {
			super(nameSpace, spec);
		}

	}

	/**
	 * Wrapper for utilizing a DefaultSimpleActionServer
	 * 
	 * @author kshaurya
	 * 
	 */
	public class Server
			extends
			DefaultSimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

		public RosVehicleActionServer(String nameSpace,
				RosVehicleActionSpec spec, RosVehicleServerCallbacks callbacks,
				boolean useBlockingGoalCallback) throws RosInitException {
			super(nameSpace, spec, callbacks, useBlockingGoalCallback);
		}

		public RosVehicleActionServer(Node parent, String nameSpace,
				RosVehicleActionSpec spec, RosVehicleServerCallbacks callbacks,
				boolean useBlockingGoalCallback) throws RosInitException {
			super(parent, nameSpace, spec, callbacks, useBlockingGoalCallback);
		}

	}

	/**
	 * Provides Actionlib specifications for the particular .action file. in
	 * this case VehicleNavigation.action
	 * 
	 * @author kshaurya
	 * 
	 */
	public class RosVehicleActionSpec
			extends
			ActionSpec<VehicleNavigationAction, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

		public RosVehicleActionSpec() throws RosException {
			super(VehicleNavigationAction.class,
					"crwlib_msgs/VehicleNavigationAction",
					"crwlib_msgs/VehicleNavigationActionFeedback",
					"crwlib_msgs/VehicleNavigationActionGoal",
					"crwlib_msgs/VehicleNavigationActionResult",
					"crwlib_msgs/VehicleNavigationFeedback",
					"crwlib_msgs/VehicleNavigationGoal",
					"crwlib_msgs/VehicleNavigationResult");
		}

		@Override
		public RosVehicleActionServer buildSimpleActionServer(
				String nameSpace,
				SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> callbacks,
				boolean useBlockingGoalCallback) throws RosInitException {

			return new RosVehicleActionServer(nameSpace, this,
					(RosVehicleServerCallbacks) callbacks,
					useBlockingGoalCallback);

		}

		@Override
		public RosVehicleActionServer buildSimpleActionServer(
				Node node,
				String nameSpace,
				SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> callbacks,
				boolean useBlockingGoalCallback) throws RosInitException {

			return new RosVehicleActionServer(node, nameSpace, this,
					(RosVehicleServerCallbacks) callbacks,
					useBlockingGoalCallback);

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
		public RosVehicleActionClient buildSimpleActionClient(Node node,
				String nameSpace) {

			RosVehicleActionClient sac = null;
			try {
				sac = new RosVehicleActionClient(node, nameSpace, this);
			} catch (RosException e) {
				e.printStackTrace();
			}
			return sac;

		}

	}
}
