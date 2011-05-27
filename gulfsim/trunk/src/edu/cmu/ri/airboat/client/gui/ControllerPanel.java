/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ControllerPanel.java
 *
 * Created on Mar 2, 2011, 6:09:18 PM
 */

package edu.cmu.ri.airboat.client.gui;

import java.util.Arrays;

/**
 *
 * @author pkv
 */
public class ControllerPanel extends AbstractAirboatPanel {

    public static final int DEFAULT_UPDATE_MS = 1500;

    /** Creates new form ControllerPanel */
    public ControllerPanel() {
        initComponents();
        setUpdateRate(1500);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controllerText = new javax.swing.JTextField();
        controllerCombo = new javax.swing.JComboBox();
        controllerLabel = new javax.swing.JLabel();
        setControllerLabel = new javax.swing.JLabel();
        controllerSend = new javax.swing.JButton();

        controllerText.setEditable(false);
        controllerText.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        controllerCombo.setEditable(true);

        controllerLabel.setText("Current controller:");

        setControllerLabel.setText("Set controller:");

        controllerSend.setText("Send");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(controllerLabel)
                .addContainerGap(154, Short.MAX_VALUE))
            .add(controllerText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(setControllerLabel)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(controllerCombo, 0, 189, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controllerSend))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(controllerLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controllerText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setControllerLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(controllerCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(controllerSend)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox controllerCombo;
    private javax.swing.JLabel controllerLabel;
    private javax.swing.JButton controllerSend;
    private javax.swing.JTextField controllerText;
    private javax.swing.JLabel setControllerLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Performs periodic updates on GUI elements
     */
    protected void update() {
        if (_command != null) {
            try {
                // Get all the controllers and sort them alphabetically
                String[] controllers = _command.getControllers();
                Arrays.sort(controllers);

                // Now, do a concurrent in-order traverse
                int i = 0;
                int j = 0;

                // We go through both lists, getting the alphabetic next element
                while (i < controllers.length && j < controllerCombo.getItemCount()) {
                    int order = controllers[i].compareToIgnoreCase((String)controllerCombo.getItemAt(j));

                    if (order < 0) {
                        // If the controller is not found, add it
                        controllerCombo.insertItemAt(controllers[i], j);
                        ++i;
                    } else if (order > 0) {
                        // If a combo box entry is not found, remove it
                        controllerCombo.removeItemAt(j);
                    } else {
                        // If we get a match, just move along
                        ++i;
                        ++j;
                    }
                }

                // Remove all the remaining items from the combo box
                for (int k = j; k < controllerCombo.getItemCount(); ++k) {
                    controllerCombo.removeItemAt(j);
                }

                // Add any controllers that were never found
                for (; i < controllers.length; ++i) {
                    controllerCombo.addItem(controllers[i]);
                }

                controllerText.setText(_command.getController());

            } catch (java.lang.reflect.UndeclaredThrowableException ex) {
                controllerText.setText("");
                controllerCombo.removeAllItems();
            }

            ControllerPanel.this.repaint();
        }
    }
}
