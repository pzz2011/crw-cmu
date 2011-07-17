package edu.cmu.ri.crw;

/**
 * Simple data class that holds a UTM coordinate and provides some conversion
 * functions.
 * 
 * @author pkv
 *
 */
public class UTM {

	public final double northing;
	public final double easting;
	public final int zone;
	public final boolean hemisphereNorth;
	
	public UTM(double northing, double easting, int zone, boolean hemisphereNorth) {
		this.northing = northing;
		this.easting = easting;
		this.zone = zone;
		this.hemisphereNorth = hemisphereNorth;
	}
}
