package edu.cmu.ri.crw.data;

import java.io.Serializable;

/**
 * Represents a UTM frame, but NOT a location in that frame.
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class Utm implements Serializable, Cloneable {
    // TODO: make this externalizable
    public final int zone;
    public final boolean isNorth;
    
    public Utm(int zone, boolean isNorth) {
        this.zone = zone;
        this.isNorth = isNorth;
    }
    
    @Override
    public Utm clone() {
        return new Utm(zone, isNorth);
    }
    
    @Override
    public String toString() {
        return zone + (isNorth ? "North" : "South");
    }
}
