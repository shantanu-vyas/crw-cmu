package edu.cmu.ri.crw.test;

import java.net.URI;
import java.util.Scanner;

import org.ros.exception.RosRuntimeException;

import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.ros.RosVehicleProxy;
import edu.cmu.ri.crw.ros.RosVehicleServer;

public class TestRosServer {
	public static void main(String args[]) throws Exception {
		
		// Start a local ros core
		/*RosCore core = RosCore.newPublic(11411);
		NodeRunner.newDefault().run(core, NodeConfiguration.newPrivate());
		core.awaitStart();*/
		
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
		/*
		// TODO: put some system tests in here
		proxyServer.setAutonomous(true);
		
		UtmPose p = new UtmPose();
		p.pose.position.x = 10.0;
		p.pose.position.y = 60.0;
		proxyServer.startWaypoint(p, new WaypointObserver() {
			@Override
			public void waypointUpdate(WaypointState status) {
				System.out.println("STATUS: " + status);
			}
		});
		
		p.pose.position.x = 22.0;
		proxyServer.startWaypoint(p, null, new WaypointObserver() {
			@Override
			public void waypointUpdate(WaypointState status) {
				System.out.println("STATUS: " + status);
			}
		});
		p.pose.position.x = 33.0;
		proxyServer.startWaypoint(p, null, new WaypointObserver() {
			@Override
			public void waypointUpdate(WaypointState status) {
				System.out.println("STATUS: " + status);
			}
		});
		
		p.pose.position.x = 11.0;
		proxyServer.startWaypoint(p, null, new WaypointObserver() {
			@Override
			public void waypointUpdate(WaypointState status) {
				System.out.println("STATUS: " + status);
			}
		});
		Thread.sleep(10000000);
		
		
		proxyServer.stopWaypoint();
		
		
		
		proxyServer.startCamera(0, 1.0, 640, 480, new ImagingObserver() {
			@Override
			public void imagingUpdate(CameraState status) {
				System.err.println("IMAGES: " + status);
			}
		});
		*/
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
			ex.printStackTrace();
			System.err.println("Ros VehicleServer was uncleanly shutdown.");
		}
		
		/*try {
			core.shutdown();
		} catch (RosRuntimeException ex) {
			System.err.println("Core was uncleanly shutdown.");
		}*/
		System.exit(0);
	}
}
 