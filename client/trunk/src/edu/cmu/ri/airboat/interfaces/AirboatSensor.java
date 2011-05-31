package edu.cmu.ri.airboat.interfaces;

/**
 * This interface defines direct access to the sensors available on the airboat.
 * It can be used by external modules to log and process various environmental
 * data.
 * 
 * @author pkv
 * 
 */
public interface AirboatSensor {

	// Get 3D raw gyro readings
	public double[] getGyro();
        
        public String[] getTypes();
        public String getType(int port);
        public boolean setType(int port, String type);
        public double[] read(int port);
}
