package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import robotutils.Pose3D;

/**
 * A library of available navigation controllers that are accessible through the
 * high-level API.
 * 
 * @author pkv
 */
public enum SimpleBoatController {

    /**
     * This controller turns the boat around until it is facing the waypoint,
     * then drives roughly in an arc towards the waypoint. When it gets within a
     * certain range, it will cut power to the boat entirely.
     */
    POINT_AND_SHOOT(new VehicleController() {

        @Override
        public void update(VehicleServer server, double dt) {
            Twist twist = new Twist();

            // Get the position of the vehicle and the current waypoint
            UtmPose state = server.getPose();
            Pose3D pose = state.pose;

            UtmPose[] waypoints = server.getWaypoints();
            if (waypoints.length < 0) {
                server.setVelocity(twist);
                return;
            }
            Pose3D waypoint = waypoints[0].pose;

            // TODO: handle different UTM zones!

            // Compute the distance and angle to the waypoint
            // TODO: compute distance more efficiently
            double distance = waypoint.getEuclideanDistance(pose);
            double angle = Math.atan2((waypoint.getY() - pose.getY()),
                    (waypoint.getX() - pose.getX()))
                    - pose.getRotation().toYaw();
            angle = normalizeAngle(angle);

            // Choose driving behavior depending on direction and and where we
            // are
            if (Math.abs(angle) > 1.0) {

                // If we are facing away, turn around first
                twist.dx(0.5);
                twist.drz(Math.max(Math.min(angle / 1.0, 1.0), -1.0));
            } else if (distance >= 3.0) {

                // If we are far away, drive forward and turn
                twist.dx(Math.min(distance / 10.0, 1.0));
                twist.drz(Math.max(Math.min(angle / 10.0, 1.0), -1.0));
            }

            // Set the desired velocity
            server.setVelocity(twist);
        }
    }),
    
    /**
     * This controller simply cuts all power to the boat, letting it drift
     * freely. It will not attempt to hold position or steer the boat in any
     * way, and completely ignores the waypoint.
     */
    STOP(new VehicleController() {
        @Override
        public void update(VehicleServer server, double dt) {
            server.setVelocity(new Twist());

        }
    }),
    
    /**
     * This controller shoots a picture only if it moves to a significantly
     * different pose
     */
    SHOOT_ON_MOVE(new VehicleController() {
        UtmPose lastPose = new UtmPose();

        @Override
        public void update(VehicleServer server, double dt) {
            Twist twist = new Twist();
            // Get the position of the vehicle and the waypoint
            UtmPose state = server.getPose();
            Pose3D pose = state.pose;

            UtmPose[] waypoints = server.getWaypoints();
            if (waypoints.length < 0) {
                server.setVelocity(twist);
                return;
            }
            Pose3D waypoint = waypoints[0].pose;

            // TODO: handle different UTM zones!
            // Compute the distance and angle to the waypoint
            // TODO: compute distance more efficiently
            double distance = waypoint.getEuclideanDistance(pose);
            double angle = Math.atan2((waypoint.getY() - pose.getY()),
                    (waypoint.getX() - pose.getX())
                    - pose.getRotation().toYaw());
            angle = normalizeAngle(angle);

            // Choose driving behavior depending on direction and and where we
            // are
            if (Math.abs(angle) > 1.0) {
                // If we are facing away, turn around first
                twist.dx(0.5);
                twist.drz(Math.max(Math.min(angle / 1.0, 1.0), -1.0));
            } else if (distance >= 3.0) {
                // If we are far away, drive forward and turn
                twist.dx(Math.min(distance / 10.0, 1.0));
                twist.drz(Math.max(Math.min(angle / 10.0, 1.0), -1.0));
            }

            // Set the desired velocity
            server.setVelocity(twist);

            System.out.println(isNovel(pose, waypoint, PlanningMethod.SIMPLE));
            // First check if we are actually set to capture
            if (lastPose == null || isNovel(pose, waypoint, PlanningMethod.SIMPLE) > 0.8) {
                lastPose.pose = pose.clone();
                System.out.println("Should Capture now!!!!!");
                server.startCamera(1, 0.0, 640, 480);
            }
            
            // TODO: add next waypoint functionality back in!!
        }
        
        /**
         * Takes a pose and determines whether the image to be taken is novel or not
         * 
         * @param pose The current pose
         * @param waypoint The current waypoint
         * @param method The Planning Method to be implemented
         * @return A weight of novelty between 0 and 1
         */
        double isNovel(Pose3D pose, Pose3D waypoint, PlanningMethod method) {

            final double CAMERA_AOV = Math.PI / 180.0f * 30;	//Assuming that the angle of view of the camera is 30 Degrees
            final double OVERLAP_RATIO = 0.8f;
            final double EFFECTIVE_DISTANCE = 10.0;		//The effective distance till which the camera resolution/detection is trusted
            double novelty = 0.0;
            switch (method) {
                case SIMPLE:
                    //To simply calculate if the new pose is different
                                    /*
                     * Capture if new pose is different as per 
                     * a. Change in yaw 
                     * b. Change in position
                     */

                    //No need to worry about the waypoint, inconsequential
                    double angle = Math.abs(pose.getRotation().toYaw() - lastPose.pose.getRotation().toYaw());
                    double distance = Math.sqrt(
                            (lastPose.pose.getX() - pose.getX()) * (lastPose.pose.getX() - pose.getX())
                            + (lastPose.pose.getY() - pose.getY()) * (lastPose.pose.getY() - pose.getY()));

                    //Assign half weight to yaw, and half to distance

                    if (angle >= CAMERA_AOV * OVERLAP_RATIO) {
                        //i.e. if the current yaw has changed more than the previous orientation by greater than 30 degrees * overlap factor

                        novelty = 0.5 * angle / (CAMERA_AOV * OVERLAP_RATIO);	//This is because ANY yaw greater than the angle of view will have completely new info (Think sectors)
                        //Assuming that the zone of overlap is not useful information
                    }


                    novelty += (distance / EFFECTIVE_DISTANCE) * 0.5;


                    break;
                case GEOMETRIC:
                    throw new UnsupportedOperationException("Not implemented Yet!");

            }
            return novelty;
        }
        
    });
    
    /**
     * The controller implementation associated with this library name.
     */
    public final VehicleController controller;
    

    /**
     * Instantiates a library entry with the specified controller.
     * 
     * @param controller
     *            the controller to be used by this entry.
     */
    private SimpleBoatController(VehicleController controller) {
        this.controller = controller;
    }

    /**
     * Takes an angle and shifts it to be in the range -Pi to Pi.
     * 
     * @param angle
     *            an angle in radians
     * @return the same angle as given, normalized to the range -Pi to Pi.
     */
    public static double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    public enum PlanningMethod {
        SIMPLE, GEOMETRIC
    };    
}
