/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.buoytest;

import edu.cmu.ri.airboat.floodtest.BufferedImageWithPose;
import edu.cmu.ri.airboat.floodtest.ProxyManager;
import edu.cmu.ri.crw.data.UtmPose;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class BuoyManager {

    ArrayList<BuoyIDModel> models = new ArrayList<BuoyIDModel>();
    
    public BuoyManager(ArrayList<LatLon> buoyLocations, Polygon pgon) {
        ProxyManager pm = new ProxyManager();
        
        for (LatLon latLon : buoyLocations) {
            // @todo work out which buoys are within poly
            BuoyIDModel bim = new BuoyIDModel(latLon);
            models.add(bim);
            pm.getMarkers().add(bim.getMarker());
        }
                
    }
    
    public void showProcessingFrame() {
        System.out.println("Showing processing frame");
        BuoyUserEvaluation frame = new BuoyUserEvaluation(models);
        frame.setVisible(true);
    }
    
    public void newImaage(BufferedImage img, UtmPose pose) {        
        BufferedImageWithPose pimg = new BufferedImageWithPose(img, pose);
        for (BuoyIDModel buoyIDModel : models) {
            buoyIDModel.offer(pimg);
        }
    }
    
    public class BuoyIDModel {
                
        private boolean done = false;
        private double confidence = 0.5;
        private final LatLon loc;
        ArrayList<BufferedImageWithPose> imgs = new ArrayList<BufferedImageWithPose>();
        BasicMarker marker = null;
        BasicMarkerAttributes attr = null;
        
        public BuoyIDModel(LatLon loc) {
            this.loc = loc;
            attr = new BasicMarkerAttributes();
            attr.setMaterial(chooseMaterial());            
            
            marker = new BasicMarker(new Position(loc, 0.0), attr);
        }
        
        public Marker getMarker() {
                        
            return marker;
        }
        
        public void offer(BufferedImageWithPose pimg) {
            
            if (done) return;
            
            // @todo determine the value of an image to the BuoyIDModel (currently random)
            if ((new Random()).nextBoolean()) {
                imgs.add(pimg);
                System.out.println("Useful image for " + this);
            }
        }

        public void setConfidence(double confidence) {
            System.out.println("Confidence set");
            attr.setMaterial(chooseMaterial());           
            marker.setAttributes(attr);
            this.confidence = confidence;
        }

        public void setDone(boolean done) {
            attr.setMaterial(chooseMaterial());            
            marker.setAttributes(attr);
            this.done = done;
        }

        public double getConfidence() {
            return confidence;
        }

        public boolean isDone() {
            return done;
        }

        public LatLon getLoc() {
            return loc;
        }               
        
        private Material chooseMaterial() {
            if (confidence > 0.8)
                return Material.GREEN;
            else if (confidence < 0.2) 
                return Material.RED;
            else 
                return Material.GRAY;
        }
    }
    
}
