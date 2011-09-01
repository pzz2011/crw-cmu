/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BoatPanel.java
 *
 * Created on Feb 15, 2011, 9:02:30 PM
 */
package edu.cmu.ri.airboat.floodtest;

import edu.cmu.ri.airboat.client.BoatDebugPanel;
import edu.cmu.ri.airboat.client.BoatDebugger;
import edu.cmu.ri.airboat.client.gui.TeleopFrame;
import gov.nasa.worldwind.geom.Position;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.border.MatteBorder;

/**
 *
 * @author pscerri
 */
public class BoatPanel extends javax.swing.JPanel {

    BoatSimpleProxy proxy = null;
    edu.cmu.ri.airboat.client.gui.TeleopFrame teleOpFrame = null;
    JFrame debugFrame = null;

    /** Creates new form BoatPanel */
    public BoatPanel() {
        initComponents();

        (new Thread() {

            public void run() {
                while (true) {
                    if (proxy != null) {
                        update();
                    }
                    try {
                        sleep(3000);
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    public BoatSimpleProxy getProxy() {
        return proxy;
    }

    private void update() {
        // proxy.getCurrWaypoint();
        modeL.setText(proxy.getMode().toString());
    }

    public void setProxy(BoatSimpleProxy proxy) {
        this.proxy = proxy;

        System.out.println("Setting proxy! " + proxy);

        if (proxy != null) {
            addressF.setText(proxy.toString());
            teleopB.setEnabled(true);
            assignAreaB.setEnabled(true);
            assignPathB.setEnabled(true);
            cancelB.setEnabled(true);
            debuggerB.setEnabled(true);
            disconnectB.setEnabled(true);
            
            setBorder(new MatteBorder(new Insets(5, 5, 5, 5), proxy.getColor()));

            update();

        } else {
            addressF.setText("None");
            teleopB.setEnabled(false);
            assignAreaB.setEnabled(false);
            assignPathB.setEnabled(false);
            cancelB.setEnabled(false);
            debuggerB.setEnabled(true);
            disconnectB.setEnabled(false);
            modeL.setText("Unknown");
            
            setBorder(null);
        }

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addressF = new javax.swing.JTextField();
        teleopB = new javax.swing.JButton();
        debuggerB = new javax.swing.JButton();
        assignAreaB = new javax.swing.JButton();
        assignPathB = new javax.swing.JButton();
        cancelB = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        noteTF = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        noteWriteB = new javax.swing.JButton();
        modeL = new javax.swing.JLabel();
        disconnectB = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 0, 0)));

        addressF.setText("None");
        addressF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressFActionPerformed(evt);
            }
        });

        teleopB.setText("Teleop");
        teleopB.setEnabled(false);
        teleopB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teleopBActionPerformed(evt);
            }
        });

        debuggerB.setText("Debug");
        debuggerB.setEnabled(false);
        debuggerB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debuggerBActionPerformed(evt);
            }
        });

        assignAreaB.setText("Assign Area");
        assignAreaB.setEnabled(false);
        assignAreaB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignAreaBActionPerformed(evt);
            }
        });

        assignPathB.setText("Assign Path");
        assignPathB.setEnabled(false);
        assignPathB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignPathBActionPerformed(evt);
            }
        });

        cancelB.setText("Cancel");
        cancelB.setEnabled(false);
        cancelB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBActionPerformed(evt);
            }
        });

        noteTF.setColumns(20);
        noteTF.setRows(5);
        jScrollPane1.setViewportView(noteTF);

        jLabel1.setText("Note:");

        noteWriteB.setText("Write");
        noteWriteB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noteWriteBActionPerformed(evt);
            }
        });

        modeL.setFont(new java.awt.Font("Lucida Grande", 1, 36)); // NOI18N
        modeL.setText("Mode");

        disconnectB.setText("Delete");
        disconnectB.setToolTipText("Use this if boat needs to reconnect");
        disconnectB.setEnabled(false);
        disconnectB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(layout.createSequentialGroup()
                                    .add(assignAreaB)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                    .add(assignPathB))
                                .add(cancelB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(addressF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 427, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(modeL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 406, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(62, 62, 62)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(137, 137, 137)
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(noteWriteB)
                                .add(1, 1, 1)))
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 268, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(debuggerB)
                    .add(teleopB)
                    .add(disconnectB))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(addressF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(teleopB))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(debuggerB))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cancelB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(disconnectB)
                        .add(9, 9, 9)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(16, 16, 16)
                                .add(noteWriteB)
                                .add(6, 6, 6))))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(assignAreaB)
                            .add(assignPathB))
                        .add(36, 36, 36)
                        .add(modeL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(21, 21, 21))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addressFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressFActionPerformed
    }//GEN-LAST:event_addressFActionPerformed

    private void teleopBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teleopBActionPerformed

        if (teleOpFrame != null && teleOpFrame.isVisible()) {
        } else {
            teleOpFrame = new TeleopFrame(proxy.getVehicleServer());
            teleOpFrame.setVisible(true);
            System.out.println("Created teleop frame");
        }

    }//GEN-LAST:event_teleopBActionPerformed

    private void debuggerBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debuggerBActionPerformed
        if (debugFrame != null && debugFrame.isVisible()) {
        } else {
            BoatDebugPanel boatPanel = new BoatDebugPanel();
            boatPanel.setServer(proxy.getVehicleServer());

            JFrame mainFrame = new JFrame();
            mainFrame.setTitle("Boat Debugging Panel");
            mainFrame.getContentPane().add(boatPanel);
            mainFrame.setLocation(100, 100);
            mainFrame.pack();
            mainFrame.setVisible(true);
            mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            debugFrame = mainFrame;

        }
    }//GEN-LAST:event_debuggerBActionPerformed

    private void assignAreaBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignAreaBActionPerformed

        // @todo Find a more elegant way of doing this
        OperatorConsole.assigningArea = true;
        update();
    }//GEN-LAST:event_assignAreaBActionPerformed

    private void cancelBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBActionPerformed
        proxy.stopBoat();
        update();
    }//GEN-LAST:event_cancelBActionPerformed

    private void assignPathBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignPathBActionPerformed

        // @todo Find a more elegant way of doing this
        OperatorConsole.assigningPath = true;
        update();
    }//GEN-LAST:event_assignPathBActionPerformed

    private void noteWriteBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noteWriteBActionPerformed
        System.out.println("Note: " + (proxy == null ? "" : proxy) + " " + noteTF.getText());
        noteTF.setText("");
    }//GEN-LAST:event_noteWriteBActionPerformed

    private void disconnectBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectBActionPerformed
        if (proxy != null) {
            ProxyManager.remove(proxy);
            proxy.remove();
            setProxy(null);
        }
    }//GEN-LAST:event_disconnectBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressF;
    private javax.swing.JButton assignAreaB;
    private javax.swing.JButton assignPathB;
    private javax.swing.JButton cancelB;
    private javax.swing.JButton debuggerB;
    private javax.swing.JButton disconnectB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel modeL;
    private javax.swing.JTextArea noteTF;
    private javax.swing.JButton noteWriteB;
    private javax.swing.JButton teleopB;
    // End of variables declaration//GEN-END:variables
}
