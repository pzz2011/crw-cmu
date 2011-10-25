package edu.cmu.ri.crw.data;

import java.io.Serializable;
import robotutils.Pose3D;

/**
 * Represents a location in 6D pose and UTM origin.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UtmPose implements Serializable, Cloneable {
 
    // TODO: make this externalizable and cloneable
    public Pose3D pose;
    public Utm origin;
    
    public UtmPose() {};
    
    public UtmPose(Pose3D pose, Utm origin) {
        this.pose = pose;
        this.origin = origin;
    }
    
    @Override
    public UtmPose clone() {
        return new UtmPose(pose, origin);
    }
}
