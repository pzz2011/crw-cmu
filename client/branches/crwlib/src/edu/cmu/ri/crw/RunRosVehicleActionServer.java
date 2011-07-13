package edu.cmu.ri.crw;

import java.net.URI;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;
import org.ros.internal.node.address.InetAddressFactory;

/**
 * Provides functionality for interfacing a vehicle server with ROS.
 * 
 * @author pkv
 *
 */
public class RunRosVehicleActionServer {

	protected static
		RosVehicleServerCallbacks impl;
		RosVehicleActionSpec spec;
		String masterURI;
		String nodeName;
		VehicleServer server;
		
	public static void main(String[] args) {
		RunRosVehicleActionServer test = new RunRosVehicleActionServer();
		test.Launch();
	}
	public RunRosVehicleActionServer()
	{
		try{
		impl = new RosVehicleServerCallbacks();
		spec = new RosVehicleActionSpec();
		masterURI = "http://syrah.cimds.ri.cmu.edu:11311";
		nodeName = "airboat_server";
		
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public RunRosVehicleActionServer(String masterURI, String nodeName, VehicleServer server) {

		this.masterURI=masterURI;
		this.nodeName=nodeName;
		this.server=server;
		
		
	}
	public void Launch()
	{
		try{
			RosVehicleActionServer sas = spec.buildSimpleActionServer(nodeName, impl, true);
			NodeConfiguration configuration = NodeConfiguration.createDefault();
			String host = InetAddressFactory.createNonLoopback().getHostAddress();
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
	
	public void shutdown() {

	}

	public static VehicleServer connect(String nodeName) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
