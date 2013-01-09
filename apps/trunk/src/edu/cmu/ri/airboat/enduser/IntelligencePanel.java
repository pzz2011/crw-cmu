/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.general.ProxyManager;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author pscerri
 */
public class IntelligencePanel extends javax.swing.JPanel {

    boolean running = false;
    DataManager dm = null;

    /**
     * Creates new form IntelligencePanel
     */
    public IntelligencePanel() {
        initComponents();

        algC.setModel(new DefaultComboBoxModel(IntelligenceAlgorithms.Algorithm.values()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        algC = new javax.swing.JComboBox();
        startB = new javax.swing.JButton();
        allCB = new javax.swing.JCheckBox();
        liveB = new javax.swing.JButton();
        modelB = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Intelligence"));

        algC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        algC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algCActionPerformed(evt);
            }
        });

        startB.setText("Start");
        startB.setEnabled(false);
        startB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBActionPerformed(evt);
            }
        });

        allCB.setText("All");
        allCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allCBActionPerformed(evt);
            }
        });

        liveB.setText("Live Data");
        liveB.setEnabled(false);
        liveB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                liveBActionPerformed(evt);
            }
        });

        modelB.setText("Model");
        modelB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(algC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 64, Short.MAX_VALUE)
                        .add(startB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(allCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(liveB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(modelB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(algC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(startB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(allCB)
                    .add(liveB)
                    .add(modelB))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void startBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBActionPerformed

        running = !running;
        if (running) {
            startB.setText("Stop");

            if (allCB.isSelected()) {
                (new IntelligenceAlgorithms()).startAutonomyAll();
            } else {
                (new IntelligenceAlgorithms()).startAutonomyProxy();
            }

        } else {
            startB.setText("Start");

            (new IntelligenceAlgorithms()).stop();
        }
    }//GEN-LAST:event_startBActionPerformed

    public void configureAdvanced() {

        // Set up the advanced display
        dm = new DataManager();
        IntelligenceAlgorithms algs = new IntelligenceAlgorithms();
        dm.setExtent(algs.getMinLatLon(), algs.getMaxLatLon());

    }

    private void allCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allCBActionPerformed
        System.out.println("All checked");
        if (running) {
            if (allCB.isSelected()) {
                (new IntelligenceAlgorithms()).startAutonomyAll();
            }
        } else {
            (new IntelligenceAlgorithms()).stop();
        }
    }//GEN-LAST:event_allCBActionPerformed

    private void liveBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_liveBActionPerformed
        (new IntelligenceAlgorithms()).showDataDisplay();
    }//GEN-LAST:event_liveBActionPerformed

    private void algCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algCActionPerformed
        (new IntelligenceAlgorithms()).setCurrAlg((IntelligenceAlgorithms.Algorithm) algC.getSelectedItem());
    }//GEN-LAST:event_algCActionPerformed

    private void modelBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelBActionPerformed
        System.out.println("Setting model visible");
        (new AdvancedDataDisplay(dm)).setVisible(true);
    }//GEN-LAST:event_modelBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algC;
    private javax.swing.JCheckBox allCB;
    public javax.swing.JButton liveB;
    private javax.swing.JButton modelB;
    public javax.swing.JButton startB;
    // End of variables declaration//GEN-END:variables
}
