package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.UtmPose;
import java.util.EventListener;

public interface VehicleStateListener extends EventListener {
	public void receivedState(UtmPose state);
}
