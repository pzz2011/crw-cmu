package edu.cmu.ri.crw;

import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.message.crwlib_msgs.*;
import edu.cmu.ri.crw.AbstractVehicleServer;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.PoseStamped;

/**
 * Defines callback methods
 * 
 * @author kshaurya
 *
 */
public class RosVehicleServerCallbacks
implements
SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

	AbstractVehicleServer vehicle_server;

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
		VehicleNavigationFeedback feedback = new VehicleNavigationFeedback();
		do{
			try{
				feedback.stamp = new WallclockProvider().getCurrentTime();
				feedback.pose_status = vehicle_server.getWaypointStatus();
				vehicle_server.controllerUpdate();
				vehicle_server.current_pose.header.stamp = new WallclockProvider().getCurrentTime();
				System.out.print("Server sends\n\t");
				//System.out.println("Time : "+feedback.stamp.toString()+" Status : "+feedback.pose_status);
				System.out.println("Current x = "+vehicle_server.current_pose.pose.position.x+"Target x = "+goal.target_pose.position.x);
				actionServer.publishFeedback(feedback);
				
				snore();//Sleeps for one second. Optional, could be removed.
			}catch(Exception e)
			{
				System.out.println("\nWeebop!");
				throw new RuntimeException(e);
			}
		}while(distToGoal(vehicle_server.current_pose.pose, goal.target_pose)>0.1);	//Otherwise till goal is reached.
		System.out.println("Lalalla");
		actionServer.setSucceeded(); //Finish it off
	}

	private void snore() {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

	}

	public void setVehicle_server(AbstractVehicleServer server) {
		this.vehicle_server = server;
	}

	double distToGoal(Pose x, Pose y)
	{
		double x1=x.position.x, x2=x.position.y, x3=x.position.z;
		double y1=y.position.x, y2=y.position.y, y3=y.position.z;
		
		return Math.sqrt(Math.pow(x1-y1, 2)+Math.pow(x2-y2,2)+Math.pow(x3-y3, 2));
	}


}
