/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import edu.cmu.ri.crw.SimpleBoatController;
import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleController;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @todo Allow some of these things to be configured
 * @author pscerri
 */
public class FastSimpleBoatSimulator extends SimpleBoatSimulator {

    protected long LOCAL_UPDATE_INTERVAL = 1000L;
    private static final Logger logger = Logger.getLogger(SimpleBoatSimulator.class.getName());
    
     @Override
    public void startWaypoints(final UtmPose[] waypoints, final String controller) {
        logger.log(Level.INFO, "Starting waypoints: {0}", Arrays.toString(waypoints));
        
        // Create a waypoint navigation task
        TimerTask newNavigationTask = new TimerTask() {
            final double dt = (double) UPDATE_INTERVAL_MS / 1000.0;

            // Retrieve the appropriate controller in initializer
            VehicleController vc = SimpleBoatController.STOP.controller;
            {
                try {
                    vc = SimpleBoatController.valueOf(controller).controller;
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "Unknown controller specified (using {0} instead): {1}", new Object[]{vc, controller});
                    // System.out.println("Unknown controller specified (using {0} instead): {1}");
                }
            }

            @Override
            public void run() {
                synchronized (_navigationLock) {
                    if (!_isAutonomous.get()) {
                        // If we are not autonomous, do nothing
                        sendWaypointUpdate(WaypointState.PAUSED);
                        return;
                    } else if (_waypoints.length == 0) {
                        // If we are finished with waypoints, stop in place
                        sendWaypointUpdate(WaypointState.DONE);
                        setVelocity(new Twist());
                        this.cancel();
                        _navigationTask = null;
                    } else {
                        // If we are still executing waypoints, use a 
                        // controller to figure out how to get to waypoint
                        // TODO: measure dt directly instead of approximating
                        vc.update(FastSimpleBoatSimulator.this, dt);
                        sendWaypointUpdate(WaypointState.GOING);
                    }
                }
            }
        };
        
        synchronized (_navigationLock) {
            // Change waypoints to new set of waypoints
            _waypoints = Arrays.copyOf(waypoints, waypoints.length);

            // Cancel any previous navigation tasks
            if (_navigationTask != null)
                _navigationTask.cancel();            
            
            // Schedule this task for execution
            _navigationTask = newNavigationTask;
            _timer.scheduleAtFixedRate(_navigationTask, 0, UPDATE_INTERVAL_MS);
        }
    }
}
