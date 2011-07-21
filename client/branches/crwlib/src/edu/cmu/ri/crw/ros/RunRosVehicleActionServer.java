package edu.cmu.ri.crw.ros;

import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer;

/**
 * Starts a Simple Actionlib server - RosVehicleActionServer.
 * 
 * @author kshaurya
 * 
 */
public class RunRosVehicleActionServer {

	protected static RosVehicleNavigation nav;
	String masterURI;
	String nodeName;
	VehicleServer server;

	public static void main(String[] args) {
		RunRosVehicleActionServer test = new RunRosVehicleActionServer();
		System.out.println("Starting server...");
		test.Launch();
	}

	public RunRosVehicleActionServer() {
		try {
			nav = new RosVehicleNavigation();
			masterURI = "http://syrah.cimds.ri.cmu.edu:11311"; // Address of
																// roscore
																// instance
			nodeName = "vehicle";
			server = new SimpleBoatSimulator();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public RunRosVehicleActionServer(String masterURI, String nodeName,
			VehicleServer server) {
		try {
			nav = new RosVehicleNavigation();
			this.masterURI = masterURI;
			this.nodeName = nodeName;
			this.server = server;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void Launch() {
		try {
			new RosVehicleServer(masterURI, nodeName, server);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
