/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import java.awt.BorderLayout;

/**
 *
 * @author pscerri
 */
public class AdvancedDataDisplay extends javax.swing.JFrame {
        
    /**
     * Creates new form AdvancedDataDisplay
     */
    public AdvancedDataDisplay(DataManager dm) {
        initComponents();
        
        dataP.setLayout(new BorderLayout());
        dataP.add(dm, BorderLayout.CENTER);        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataP = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        dataP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.jdesktop.layout.GroupLayout dataPLayout = new org.jdesktop.layout.GroupLayout(dataP);
        dataP.setLayout(dataPLayout);
        dataPLayout.setHorizontalGroup(
            dataPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 876, Short.MAX_VALUE)
        );
        dataPLayout.setVerticalGroup(
            dataPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 406, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dataP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(dataP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 118, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dataP;
    // End of variables declaration//GEN-END:variables
}
