package edu.cmu.ri.crw.ros;
import java.net.URI;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.internal.node.address.InetAddressFactory;

import edu.cmu.ri.crw.VehicleServer;

/**
 * Starts a Simple Actionlib server - RosVehicleActionServer.
 * 
 * @author kshaurya
 *
 */
public class RunRosVehicleActionServer {

	protected static
	RosVehicleNavigation nav;
	String masterURI;
	String nodeName;
	VehicleServer server;

	public static void main(String[] args) {
		RunRosVehicleActionServer test = new RunRosVehicleActionServer();
		System.out.println("Starting server...");
		test.Launch();
	}
	public RunRosVehicleActionServer()
	{
		try{
			nav = new RosVehicleNavigation();
			masterURI = "http://syrah.cimds.ri.cmu.edu:11311";	//Address of roscore instance
			nodeName = "vehicle_server";
			//server = new SimpleBoatSimulator();
			//impl.setVehicle_server(server);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public RunRosVehicleActionServer(String masterURI, String nodeName, VehicleServer server) {
		try{
			nav = new RosVehicleNavigation();
			this.masterURI=masterURI;
			this.nodeName=nodeName;
			this.server=server;
		}catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public void Launch()
	{
		try{
			RosVehicleServer rosv = new RosVehicleServer(masterURI, nodeName, server);
			RosVehicleNavigation.Server sas = new RosVehicleNavigation.Spec().buildSimpleActionServer(nodeName, rosv.navigationHandler, true);
			NodeConfiguration configuration = NodeConfiguration.createDefault();
			String host = InetAddressFactory.createNonLoopback().getHostAddress();	//To avoid the node referring to localhost, which is unresolvable for external methods
			configuration.setHost(host);
			configuration.setMasterUri(new URI(masterURI));
			NodeRunner runner = NodeRunner.createDefault();
			runner.run(sas, configuration);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
