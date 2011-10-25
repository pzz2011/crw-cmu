package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.Twist;
import java.util.EventListener;

public interface VehicleVelocityListener extends EventListener {
	public void receivedVelocity(Twist velocity);
}