package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.SensorData;
import java.io.Serializable;
import java.util.EventListener;

public interface VehicleSensorListener extends EventListener {
	public void receivedSensor(SensorData sensor);
}
