package edu.cmu.ri.crw;

import java.util.EventListener;

public interface VehicleStateListener extends EventListener {
	public void receivedState(Object state);
}
