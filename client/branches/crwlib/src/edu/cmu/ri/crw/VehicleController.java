package edu.cmu.ri.crw;


/**
 * Defines an interface for a control object that has access to the low-level
 * control and sensor interfaces to the boat.  This object will be called
 * periodically to provide an updated command to the vehicle.
 * 
 * @author pkv
 *
 */
public interface VehicleController {

        /**
         * Update function containing the control and sensor interfaces to the boats
         * and the time since the last update was called.
         * 
         * @param control low-level vehicle control interface
         * @param sensor low-level vehicle sensor interface
         * @param dt elapsed time since last controller call (in seconds)
         */
        public void update(VehicleServer server, double dt);
}
