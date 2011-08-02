/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CameraPanel.java
 *
 * Created on Apr 6, 2011, 8:34:21 PM
 */

package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.crw.VehicleImageListener;
import edu.cmu.ri.crw.VehicleServer;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.ros.message.sensor_msgs.CompressedImage;

/**
 *
 * @author pkv
 */
public class CameraPanel extends AbstractAirboatPanel {

    /** Creates new form CameraPanel */
    public CameraPanel() {
        initComponents();
    }

    @Override
    public void setVehicle(VehicleServer vehicle) {
        super.setVehicle(vehicle);
        vehicle.addImageListener(new VehicleImageListener() {

            public void receivedImage(CompressedImage ci) {
                // Take a picture, and put the resulting image into the panel
                try {
                    BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(ci.data));
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(pictureLabel.getWidth(), pictureLabel.getHeight(), Image.SCALE_DEFAULT);
                        pictureLabel.setIcon(new ImageIcon(scaledImage));
                        CameraPanel.this.repaint();
                    } else {
                        System.err.println("Failed to decode image.");
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to decode image: " + ex);
                }

            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        savePictureButton = new javax.swing.JButton();
        takePictureButton = new javax.swing.JButton();
        pictureLabel = new javax.swing.JLabel();

        savePictureButton.setText("Save Picture");
        savePictureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePictureButtonActionPerformed(evt);
            }
        });

        takePictureButton.setText("Take Picture");
        takePictureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                takePictureButtonActionPerformed(evt);
            }
        });

        pictureLabel.setBackground(java.awt.Color.lightGray);
        pictureLabel.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pictureLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(savePictureButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(takePictureButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pictureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(takePictureButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(savePictureButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void takePictureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_takePictureButtonActionPerformed
        
        takePictureButton.setEnabled(false);
        takePictureButton.setSelected(true);

        // TODO: rename this button
        _vehicle.startCamera(10, 0.5, 640, 480, null);

        takePictureButton.setEnabled(true);
        takePictureButton.setSelected(false);
        
    }//GEN-LAST:event_takePictureButtonActionPerformed

    private void savePictureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePictureButtonActionPerformed

        savePictureButton.setEnabled(false);
        savePictureButton.setSelected(true);

        // TODO: rename this button
        _vehicle.stopCamera();

        savePictureButton.setEnabled(true);
        savePictureButton.setSelected(false);
    }//GEN-LAST:event_savePictureButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel pictureLabel;
    private javax.swing.JButton savePictureButton;
    private javax.swing.JButton takePictureButton;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void update() {
        // No updates required here!
    }

}
