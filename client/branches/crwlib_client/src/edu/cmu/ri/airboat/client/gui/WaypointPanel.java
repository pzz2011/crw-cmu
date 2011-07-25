/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WaypointPanel.java
 *
 * Created on Mar 2, 2011, 6:09:08 PM
 */

package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.airboat.client.UtmUtils;
import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.WaypointObserver;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.event.MouseAdapter;
import org.ros.message.crwlib_msgs.UtmPose;

/**
 *
 * @author pkv
 */
public class WaypointPanel extends AbstractAirboatPanel {

    public static final int DEFAULT_UPDATE_MS = 1000;
    private SimpleWorldPanel _worldPanel = null;
    private UtmPose waypoint = new UtmPose();

    /** Creates new form WaypointPanel */
    public WaypointPanel() {
        initComponents();
        setUpdateRate(DEFAULT_UPDATE_MS);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        currWaypointLabel = new javax.swing.JLabel();
        currWaypointText = new javax.swing.JTextField();
        selectedWaypointLabel = new javax.swing.JLabel();
        selectedWaypointText = new javax.swing.JTextField();
        completedBox = new ReadOnlyCheckBox();
        sendButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        currWaypointLabel.setText("Current:");

        currWaypointText.setEditable(false);
        currWaypointText.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        selectedWaypointLabel.setText("Selected on map:");

        selectedWaypointText.setEditable(false);

        completedBox.setForeground(new java.awt.Color(51, 51, 51));
        completedBox.setText("Completed");
        completedBox.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(currWaypointLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 75, Short.MAX_VALUE)
                .add(completedBox))
            .add(currWaypointText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(selectedWaypointLabel)
                .addContainerGap())
            .add(selectedWaypointText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
            .add(sendButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(currWaypointLabel)
                    .add(completedBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(currWaypointText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectedWaypointLabel)
                .add(5, 5, 5)
                .add(selectedWaypointText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(sendButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        completedBox.setSelected(false);
        _vehicle.startWaypoint(waypoint, new WaypointObserver() {

            public void waypointUpdate(WaypointState ws) {
                if (ws == WaypointState.DONE) {
                    completedBox.setSelected(true);
                }
            }
        });

    }//GEN-LAST:event_sendButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox completedBox;
    private javax.swing.JLabel currWaypointLabel;
    private javax.swing.JTextField currWaypointText;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel selectedWaypointLabel;
    private javax.swing.JTextField selectedWaypointText;
    private javax.swing.JButton sendButton;
    // End of variables declaration//GEN-END:variables

    public void setWorldPanel(SimpleWorldPanel worldPanel) {
        if (_worldPanel != null) {
            _worldPanel.removeMouseListener(_waypointListener);
        }

        _worldPanel = worldPanel;
        _worldPanel.addMouseListener(_waypointListener);
    }

    private final MouseAdapter _waypointListener = new MouseAdapter() {
        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            Position wpPos = _worldPanel.click.getPosition();
            UTMCoord wpUtm = UTMCoord.fromLatLon(wpPos.getLatitude(), wpPos.getLongitude());

            Position boatPos = _worldPanel.boat.getPosition();
            UTMCoord boatUtm = UTMCoord.fromLatLon(boatPos.getLatitude(), boatPos.getLongitude());

            // Convert out of zone for boat-local coordinates
            // TODO: figure out how to fix this!
            UtmUtils.UTM fakeUtm = UtmUtils.convertZone(boatUtm, wpUtm);
            waypoint.pose.position.x = fakeUtm.easting;
            waypoint.pose.position.y = fakeUtm.northing;

            waypoint.utm.zone = (byte)fakeUtm.zone;
            waypoint.utm.isNorth = fakeUtm.isNorth;

            waypoint.pose.position.z = wpPos.getAltitude();
            waypoint.pose.orientation = QuaternionUtils.fromEulerAngles(0.0, 0.0,
                    (_worldPanel.waypoint.getHeading() != null) ? _worldPanel.waypoint.getHeading().getRadians() : 0.0);

            selectedWaypointText.setText("[" + waypoint.pose.position.x + ", " + waypoint.pose.position.y + ", " + waypoint.pose.position.z + "] " + waypoint.utm.zone + " " + (waypoint.utm.isNorth ? "North" : "South"));
        }
    };

    @Override
    protected void update() {
        if (_vehicle != null) {
            UtmPose currWp = _vehicle.getWaypoint();
            if (currWp == null)
                return;

            currWaypointText.setText("[" + currWp.pose.position.x + ", " + currWp.pose.position.y + ", " + currWp.pose.position.z + "] " + currWp.utm.zone + " " + (currWp.utm.isNorth ? "North" : "South"));

            // Set marker position on globe map
            if (_worldPanel != null) {
                String wwHemi = (currWp.utm.isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South";
                try {
                    UTMCoord boatPos = UTMCoord.fromUTM(currWp.utm.zone, wwHemi, currWp.pose.position.x, currWp.pose.position.y);
                    _worldPanel.waypoint.getAttributes().setOpacity(1.0);
                    _worldPanel.waypoint.setPosition(new Position(boatPos.getLatitude(), boatPos.getLongitude(), 0.0));
                } catch (IllegalArgumentException ex) {
                    _worldPanel.waypoint.getAttributes().setOpacity(0.5);
                }
            }

            WaypointPanel.this.repaint();
        }
    }
}
