package edu.cmu.ri.crw.data;

import edu.cmu.ri.crw.VehicleServer.SensorType;
import java.io.Serializable;

/**
 * Structure for holding sensor data.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class SensorData implements Cloneable, Serializable  {
    public int channel;
    public double[] data;
    public SensorType type;
}