/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.buoytest;

import edu.cmu.ri.airboat.floodtest.BufferedImageWithPose;
import edu.cmu.ri.airboat.floodtest.ProxyManager;
import edu.cmu.ri.crw.data.UtmPose;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import robotutils.Pose3D;

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

    public void newImage(BufferedImage img, UtmPose pose) {
        BufferedImageWithPose pimg = new BufferedImageWithPose(img, pose);
        for (BuoyIDModel buoyIDModel : models) {
            buoyIDModel.offer(pimg);
        }
    }
    
    private ArrayList<LatLon> sent = new ArrayList<LatLon>();    
    public LatLon getNearestRequiring(LatLon curr) {
        LatLon ret = null;
        
        UTMCoord utm = UTMCoord.fromLatLon(curr.latitude, curr.longitude);
        Pose3D currP = new Pose3D(new double [] { utm.getEasting(), utm.getNorthing(), 0.0}, new double [] {0.0, 0.0, 0.0} );
         
        double minDist = Double.MAX_VALUE;
        
        for (BuoyIDModel buoyIDModel : models) {
            if (!buoyIDModel.isDone() && buoyIDModel.imgs.size() < 20 && !sent.contains(buoyIDModel.loc)) {
                double d = planarDistanceSq(buoyIDModel.loc3D, currP);
                if (d < minDist) {
                    minDist = d;
                    ret = buoyIDModel.getLoc();
                }
            }
        }
        
        sent.add(ret);
        if (sent.size() == models.size()) {
            // If someone has been sent to every buoy, reset and send again as required.
            sent.clear();
        }
        
        return ret;
    }

    public class BuoyIDModel {

        private boolean done = false;
        private double confidence = 0.5;
        private final LatLon loc;
        private final Pose3D loc3D;
        ArrayList<BufferedImageWithPose> imgs = new ArrayList<BufferedImageWithPose>();
        BasicMarker marker = null;
        BasicMarkerAttributes attr = null;

        public BuoyIDModel(LatLon loc) {
            this.loc = loc;
            UTMCoord utm = UTMCoord.fromLatLon(loc.latitude, loc.longitude);
            loc3D = new Pose3D(new double [] { utm.getEasting(), utm.getNorthing(), 0.0}, new double [] {0.0, 0.0, 0.0} );
            attr = new BasicMarkerAttributes();
            attr.setMaterial(chooseMaterial());

            marker = new BasicMarker(new Position(loc, 0.0), attr);
        }

        public Marker getMarker() {

            return marker;
        }

        public void offer(BufferedImageWithPose pimg) {

            if (done) {
                return;
            }

            double dist = pimg.getPose().pose.getEuclideanDistance(loc3D);
            double angle = normalizeAngle(angleBetween(pimg.getPose().pose, loc3D));
            
            double adjangle = angle - pimg.getPose().pose.getRotation().toYaw();
            
            // System.out.println("Dist = " + dist + ", raw angle = " + angle + " and adj. angle " + adjangle);                        
                        
            if (Math.abs(adjangle) < Math.PI/6.0 && dist < 100.0) {
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
            if (confidence > 0.8) {
                return Material.GREEN;
            } else if (confidence < 0.2) {
                return Material.RED;
            } else {
                return Material.GRAY;
            }
        }
    }

    public static double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    public static double planarDistanceSq(Pose3D a, Pose3D b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return dx * dx + dy * dy;
    }

    public static double angleBetween(Pose3D src, Pose3D dest) {
        return Math.atan2((dest.getY() - src.getY()), (dest.getX() - src.getX()));
    }
}
