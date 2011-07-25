package edu.cmu.ri.crw.test;

import java.net.URI;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ros.RosCore;
import org.ros.exception.RosRuntimeException;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;

import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.ros.RosVehicleProxy;
import edu.cmu.ri.crw.ros.RosVehicleServer;

public class TestRosServer {
	public static void main(String args[]) throws Exception {
		
		// TODO: Remove this logging setting -- it is a stopgap for a rosjava bug
		Logger.getLogger("org.ros.internal.node.client").setLevel(Level.SEVERE);
		
		// Start a local ros core
		RosCore core = RosCore.newPublic();
		NodeRunner.newDefault().run(core, NodeConfiguration.newPrivate());
		core.awaitStart();
		
		// Select if we want to use the local ros core or a remote one
		//URI masterUri = core.getUri();
		URI masterUri = new URI("http://localhost:11311");
		System.out.println("Master URI: " + masterUri);
		
		// Create a simulated boat and run a ROS server around it
		VehicleServer server = new SimpleBoatSimulator();
		RosVehicleServer rosServer = new RosVehicleServer(masterUri, "vehicle", server);
		
		// Create a ROS proxy server that accesses the same object
		RosVehicleProxy proxyServer = new RosVehicleProxy(masterUri, "vehicle_client");
		
		// Wait for someone to hit Enter
		{
			System.out.println("Press [ENTER] to begin.");
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
		}
		
		// TODO: put some system tests in here
		UtmPose p = new UtmPose();
		p.pose.position.x = 100.0;
		p.utm.isNorth = true;
		proxyServer.setState(p);
		
		// Wait for someone to hit Enter
		{
			System.out.println("Press [ENTER] to continue.");
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
		}
		
		// Shut down everything
		try {
			proxyServer.shutdown();
		} catch (RosRuntimeException ex) {
			System.err.println("Proxy server was uncleanly shutdown.");
		}
		
		try {
			rosServer.shutdown();
		} catch (RosRuntimeException ex) {
			System.err.println("Ros VehicleServer was uncleanly shutdown.");
		}
		
		try {
			core.shutdown();
		} catch (RosRuntimeException ex) {
			System.err.println("Core was uncleanly shutdown.");
		}
		System.exit(0);
	}
}
 