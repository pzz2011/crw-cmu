package edu.cmu.ri.crw;

import java.util.EventListener;

public interface VehicleImageListener extends EventListener {
	public void receivedImage(Object image);
}
