package edu.cmu.ri.crw;

import java.util.EventListener;

public interface VehicleSensorListener extends EventListener {
	public void receivedSensor(Object sensor);
}
