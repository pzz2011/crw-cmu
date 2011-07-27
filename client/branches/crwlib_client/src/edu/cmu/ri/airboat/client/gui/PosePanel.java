/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PosePanel.java
 *
 * Created on Mar 2, 2011, 8:05:56 PM
 */

package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleStateListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.text.DecimalFormat;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;

/**
 *
 * @author pkv
 */
public class PosePanel extends AbstractAirboatPanel {

    private static final DecimalFormat UTM_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat ANGLE_FORMAT = new DecimalFormat("0.000");
    private SimpleWorldPanel _worldPanel = null;

    /** Creates new form PosePanel */
    public PosePanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        positionLabel = new javax.swing.JLabel();
        positionText = new javax.swing.JTextField();
        orientationLabel = new javax.swing.JLabel();
        rollBar = new javax.swing.JProgressBar();
        rollTitleLabel = new javax.swing.JLabel();
        pitchTitleLabel = new javax.swing.JLabel();
        yawTitleLabel = new javax.swing.JLabel();
        pitchBar = new javax.swing.JProgressBar();
        yawBar = new javax.swing.JProgressBar();
        rollLabel = new javax.swing.JLabel();
        pitchLabel = new javax.swing.JLabel();
        yawLabel = new javax.swing.JLabel();
        setPoseButton = new javax.swing.JButton();

        positionLabel.setText("Position:");

        positionText.setEditable(false);

        orientationLabel.setText("Orientation:");

        rollTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rollTitleLabel.setText("Roll");

        pitchTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pitchTitleLabel.setText("Pitch");

        yawTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        yawTitleLabel.setText("Yaw");

        rollLabel.setText("0.000 rads");

        pitchLabel.setText("0.000 rads");

        yawLabel.setText("0.000 rads");

        setPoseButton.setText("Set pose from map");
        setPoseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setPoseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(positionLabel)
                .addContainerGap(175, Short.MAX_VALUE))
            .add(positionText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(orientationLabel)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(pitchTitleLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(rollTitleLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(yawTitleLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(yawBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                    .add(pitchBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                    .add(rollBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(yawLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(pitchLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(rollLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, setPoseButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(positionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(positionText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(orientationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 20, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(rollBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(rollLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, rollTitleLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(pitchLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pitchTitleLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(pitchBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, yawTitleLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(yawBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(yawLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setPoseButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setPoseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setPoseButtonActionPerformed
        if (_worldPanel != null) {
            Position wpPos = _worldPanel.click.getPosition();
            UTMCoord wpUtm = UTMCoord.fromLatLon(wpPos.getLatitude(), wpPos.getLongitude());

            UtmPose pose = new UtmPose();
            pose.utm.isNorth = wpUtm.getHemisphere().contains("North");
            pose.utm.zone = (byte)wpUtm.getZone();
            pose.pose.position.x = wpUtm.getEasting();
            pose.pose.position.y = wpUtm.getNorthing();
            pose.pose.position.z = wpPos.getAltitude();

            _vehicle.setState(pose);
        }
    }//GEN-LAST:event_setPoseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel orientationLabel;
    private javax.swing.JProgressBar pitchBar;
    private javax.swing.JLabel pitchLabel;
    private javax.swing.JLabel pitchTitleLabel;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JTextField positionText;
    private javax.swing.JProgressBar rollBar;
    private javax.swing.JLabel rollLabel;
    private javax.swing.JLabel rollTitleLabel;
    private javax.swing.JButton setPoseButton;
    private javax.swing.JProgressBar yawBar;
    private javax.swing.JLabel yawLabel;
    private javax.swing.JLabel yawTitleLabel;
    // End of variables declaration//GEN-END:variables

    // Converts from progress bar value to linear scaling between min and max
    private int fromRangeToProgress(double value, double min, double max) {
            return (int)(100.0 * (value - min)/(max - min));
    }

    // Convert angle to betwee +/- PI
    private double normalizeAngle(double angle) {
        while (angle >= Math.PI)
            angle -= 2*Math.PI;
        while (angle <= -Math.PI)
            angle += 2*Math.PI;
        return angle;
    }

    // Update the reference to the world map
    public void setWorldPanel(SimpleWorldPanel worldPanel) {
        _worldPanel = worldPanel;
    }

    @Override
    public void setVehicle(VehicleServer vehicle) {
        super.setVehicle(vehicle);
        vehicle.addStateListener(new VehicleStateListener() {

            public void receivedState(UtmPoseWithCovarianceStamped upwcs) {
                int longZone = upwcs.utm.zone;
                String latZone = (upwcs.utm.isNorth ? "North" : "South");
                Pose pose = upwcs.pose.pose.pose;
                positionText.setText("[" + 
                        UTM_FORMAT.format(pose.position.x) + ", " +
                        UTM_FORMAT.format(pose.position.y) + ", " +
                        UTM_FORMAT.format(pose.position.z) + "] " +
                        longZone + " " + latZone);

                double[] rpy = QuaternionUtils.toEulerAngles(pose.orientation);
                
                rollBar.setValue(fromRangeToProgress(rpy[0], -Math.PI, Math.PI));
                pitchBar.setValue(fromRangeToProgress(rpy[1], -Math.PI, Math.PI));
                yawBar.setValue(fromRangeToProgress(rpy[2], -Math.PI, Math.PI));

                rollLabel.setText(ANGLE_FORMAT.format(rpy[0]) + " rads");
                pitchLabel.setText(ANGLE_FORMAT.format(rpy[1]) + " rads");
                yawLabel.setText(ANGLE_FORMAT.format(rpy[2]) + " rads");

                // Set marker position on globe map
                if (_worldPanel != null) {
                    try {
                        String wwHemi = (upwcs.utm.isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South";
                        UTMCoord boatPos = UTMCoord.fromUTM(longZone, wwHemi, pose.position.x, pose.position.y);
                        _worldPanel.boat.getAttributes().setOpacity(1.0);
                        _worldPanel.boat.setPosition(new Position(boatPos.getLatitude(), boatPos.getLongitude(), rpy[2]));
                        _worldPanel.boat.setHeading(Angle.fromRadians(Math.PI/2.0-rpy[2]));
                    } catch (Exception e) {
                        _worldPanel.boat.getAttributes().setOpacity(0.0);
                    }
                    _worldPanel.repaint();
                }

                PosePanel.this.repaint();
            }
        });
    }

    /**
     * Performs periodic updates of the GUI elements
     */
    public void update() {
       // Nothing to do here
    }
}
