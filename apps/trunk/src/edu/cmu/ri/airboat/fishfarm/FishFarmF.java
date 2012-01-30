/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FishFarmF.java
 *
 * Created on Jan 19, 2012, 9:12:58 PM
 */
package edu.cmu.ri.airboat.fishfarm;

import edu.cmu.ri.airboat.client.gui.TeleopFrame;
import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.airboat.general.ConfigureBoatsFrame;
import edu.cmu.ri.airboat.general.ProxyManager;
import edu.cmu.ri.airboat.general.ProxyManagerListener;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CrwSecurityManager;
import gov.nasa.worldwind.geom.LatLon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;

/**
 *
 * @author pscerri
 */
public class FishFarmF extends javax.swing.JFrame {

    private final String MIN_LAT_S = "MIN_LAT_S";
    private final String MAX_LAT_S = "MAX_LAT_S";
    private final String MIN_LON_S = "MIN_LON_S";
    private final String MAX_LON_S = "MAX_LON_S";
    private LatLon mins = LatLon.ZERO;
    private LatLon maxs = LatLon.ZERO;
    private ArrayList<FishFarmBoatProxy> proxies = new ArrayList<FishFarmBoatProxy>();
    private DataManager dm = new DataManager(proxies);
    private DecimalFormat df = new DecimalFormat("#.###");

    edu.cmu.ri.airboat.client.gui.TeleopFrame teleOpFrame = null;
    
    /** Creates new form FishFarmF */
    public FishFarmF() {
        initComponents();

        imgTypeCombo.setModel(new DefaultComboBoxModel(DataRepository.ImageType.values()));

        try {

            longMinTF.setText(Preferences.userRoot().get(MIN_LON_S, "?"));
            longMaxTF.setText(Preferences.userRoot().get(MAX_LON_S, "?"));
            latMinTF.setText(Preferences.userRoot().get(MIN_LAT_S, "?"));
            latMaxTF.setText(Preferences.userRoot().get(MAX_LAT_S, "?"));

            mins = LatLon.fromDegrees(Double.parseDouble(latMinTF.getText()), Double.parseDouble(longMinTF.getText()));
            maxs = LatLon.fromDegrees(Double.parseDouble(latMaxTF.getText()), Double.parseDouble(longMaxTF.getText()));

        } catch (AccessControlException e) {
            System.out.println("Failed to access preferences");
        }


        ProxyManagerListener listener = new ProxyManagerListener() {

            public void proxyAdded(BoatProxy bp) {
                FishFarmBoatProxy fb = new FishFarmBoatProxy(bp, dm);
                fb.setRepo(dm.repo);
                proxies.add(fb);

                ((DefaultComboBoxModel) proxyC.getModel()).addElement(fb);
            }
        };
        (new ProxyManager()).addListener(listener);

        dm.setExtent(mins, maxs);
        dataViewP.setLayout(new BorderLayout());
        dataViewP.add(dm, BorderLayout.CENTER);
        
        proxyC.setRenderer(new ProxyComboRenderer());
        proxyC.setEditable(false);
        
        algC.setSelectedItem(dm.repo.getAlg());
        
        contourValueTF.setText("" + dm.repo.getContourValue());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        latMaxTF = new javax.swing.JTextField();
        latMinTF = new javax.swing.JTextField();
        longMinTF = new javax.swing.JTextField();
        dataViewP = new javax.swing.JPanel();
        longMaxTF = new javax.swing.JTextField();
        imgTypeCombo = new javax.swing.JComboBox();
        proxyP = new javax.swing.JPanel();
        proxyC = new javax.swing.JComboBox();
        teleopB = new javax.swing.JButton();
        autoCB = new javax.swing.JCheckBox();
        algC = new javax.swing.JComboBox();
        contourP = new javax.swing.JPanel();
        contourTF = new javax.swing.JTextField();
        contourS = new javax.swing.JSlider();
        contourValueTF = new javax.swing.JTextField();
        indexC = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        latMaxTF.setText("jTextField1");
        latMaxTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latMaxTFActionPerformed(evt);
            }
        });

        latMinTF.setText("jTextField2");
        latMinTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latMinTFActionPerformed(evt);
            }
        });

        longMinTF.setText("jTextField3");
        longMinTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longMinTFActionPerformed(evt);
            }
        });

        dataViewP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.jdesktop.layout.GroupLayout dataViewPLayout = new org.jdesktop.layout.GroupLayout(dataViewP);
        dataViewP.setLayout(dataViewPLayout);
        dataViewPLayout.setHorizontalGroup(
            dataViewPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 971, Short.MAX_VALUE)
        );
        dataViewPLayout.setVerticalGroup(
            dataViewPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 609, Short.MAX_VALUE)
        );

        longMaxTF.setText("jTextField4");
        longMaxTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longMaxTFActionPerformed(evt);
            }
        });

        imgTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        imgTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgTypeComboActionPerformed(evt);
            }
        });

        proxyP.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        proxyC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxyCActionPerformed(evt);
            }
        });

        teleopB.setText("TeleOp");
        teleopB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teleopBActionPerformed(evt);
            }
        });

        autoCB.setText("Auto");
        autoCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout proxyPLayout = new org.jdesktop.layout.GroupLayout(proxyP);
        proxyP.setLayout(proxyPLayout);
        proxyPLayout.setHorizontalGroup(
            proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(proxyPLayout.createSequentialGroup()
                .addContainerGap()
                .add(proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(proxyC, 0, 177, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, proxyPLayout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(autoCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(teleopB)))
                .addContainerGap())
        );
        proxyPLayout.setVerticalGroup(
            proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(proxyPLayout.createSequentialGroup()
                .add(8, 8, 8)
                .add(proxyC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(teleopB)
                    .add(autoCB))
                .addContainerGap(98, Short.MAX_VALUE))
        );

        algC.setModel(new DefaultComboBoxModel(DataRepository.AutonomyAlgorithm.values()));
        algC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algCActionPerformed(evt);
            }
        });

        contourP.setBorder(javax.swing.BorderFactory.createTitledBorder("Contour Precentile"));

        contourTF.setText("0.0");
        contourTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contourTFActionPerformed(evt);
            }
        });

        contourS.setPaintLabels(true);
        contourS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                contourSStateChanged(evt);
            }
        });

        contourValueTF.setText("0.0");
        contourValueTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contourValueTFActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout contourPLayout = new org.jdesktop.layout.GroupLayout(contourP);
        contourP.setLayout(contourPLayout);
        contourPLayout.setHorizontalGroup(
            contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contourPLayout.createSequentialGroup()
                .add(contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(contourPLayout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(contourS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(contourPLayout.createSequentialGroup()
                        .add(contourTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(contourValueTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contourPLayout.setVerticalGroup(
            contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contourPLayout.createSequentialGroup()
                .add(contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contourTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(contourValueTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contourS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        indexC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0" }));
        indexC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexCActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(latMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(longMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(longMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(78, 78, 78)
                                .add(dataViewP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(imgTypeCombo, 0, 215, Short.MAX_VALUE)
                            .add(proxyP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(indexC, 0, 215, Short.MAX_VALUE)
                            .add(algC, 0, 215, Short.MAX_VALUE)))
                    .add(latMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(32, 32, 32))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(1059, Short.MAX_VALUE)
                .add(contourP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 235, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(latMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(imgTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(algC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(indexC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(30, 30, 30)
                        .add(contourP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 248, Short.MAX_VALUE)
                        .add(proxyP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(11, 11, 11))
                    .add(layout.createSequentialGroup()
                        .add(dataViewP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(latMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(longMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(longMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void longMaxTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longMaxTFActionPerformed

        maxs = LatLon.fromDegrees(maxs.latitude.degrees, Double.parseDouble(longMaxTF.getText()));
        dm.setExtent(mins, maxs);
        savePref(MAX_LON_S, longMaxTF.getText());
    }//GEN-LAST:event_longMaxTFActionPerformed

    private void latMinTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latMinTFActionPerformed

        mins = LatLon.fromDegrees(Double.parseDouble(latMinTF.getText()), mins.longitude.degrees);
        dm.setExtent(mins, maxs);
        savePref(MIN_LAT_S, latMinTF.getText());
    }//GEN-LAST:event_latMinTFActionPerformed

    private void longMinTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longMinTFActionPerformed

        mins = LatLon.fromDegrees(mins.latitude.degrees, Double.parseDouble(longMinTF.getText()));
        dm.setExtent(mins, maxs);
        savePref(MIN_LON_S, longMinTF.getText());
    }//GEN-LAST:event_longMinTFActionPerformed

    private void latMaxTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latMaxTFActionPerformed

        maxs = LatLon.fromDegrees(Double.parseDouble(latMaxTF.getText()), maxs.longitude.degrees);
        dm.setExtent(mins, maxs);
        savePref(MAX_LAT_S, latMaxTF.getText());
    }//GEN-LAST:event_latMaxTFActionPerformed

    private void imgTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgTypeComboActionPerformed
        dm.setImgType((DataRepository.ImageType) imgTypeCombo.getSelectedItem());
    }//GEN-LAST:event_imgTypeComboActionPerformed

    private void teleopBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teleopBActionPerformed
        if ((teleOpFrame != null && teleOpFrame.isVisible()) ||  proxyC.getSelectedItem() == null) {
        } else {
            teleOpFrame = new TeleopFrame(AsyncVehicleServer.Util.toSync(((FishFarmBoatProxy)proxyC.getSelectedItem()).getProxy().getVehicleServer()));
            teleOpFrame.setVisible(true);
            // System.out.println("Created teleop frame");
        }
    }//GEN-LAST:event_teleopBActionPerformed

    private void autoCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoCBActionPerformed
        
        if (proxyC.getSelectedItem() == null) {
            return;
        } else {
            FishFarmBoatProxy proxy = (FishFarmBoatProxy)proxyC.getSelectedItem();
            proxy.setAutonomous(autoCB.isSelected());
        }
        
    }//GEN-LAST:event_autoCBActionPerformed

    private void algCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algCActionPerformed
        dm.repo.setAlg((DataRepository.AutonomyAlgorithm)algC.getSelectedItem());
    }//GEN-LAST:event_algCActionPerformed

    private void proxyCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyCActionPerformed
        FishFarmBoatProxy p = (FishFarmBoatProxy)proxyC.getSelectedItem();
        autoCB.setSelected(p.isIsAutonomous());
        proxyP.setBorder(new EtchedBorder(p.getColor(), p.getColor().brighter()));
    }//GEN-LAST:event_proxyCActionPerformed

    private void contourTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contourTFActionPerformed
        try {
            int i = Integer.parseInt(contourTF.getText());
            contourS.setValue(i);
        } catch (NumberFormatException e) {}
    }//GEN-LAST:event_contourTFActionPerformed

    private void indexCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexCActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_indexCActionPerformed

    private void contourSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_contourSStateChanged
        contourTF.setText("" + contourS.getValue());
        double v = dm.repo.setContourPercentOfMax(contourS.getValue()/100.0);
        contourValueTF.setText(df.format(v));
    }//GEN-LAST:event_contourSStateChanged

    private void contourValueTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contourValueTFActionPerformed
        try {
            dm.repo.setContourValue(Double.parseDouble(contourValueTF.getText()));
        } catch (NumberFormatException e) {}
    }//GEN-LAST:event_contourValueTFActionPerformed

    private void savePref(String key, String value) {
        try {
            Preferences p = Preferences.userRoot();
            p.put(key, value);
        } catch (AccessControlException e) {
            System.out.println("Failed to save preferences");
        }
    }

    private class ProxyComboRenderer extends JLabel implements ListCellRenderer {

        public ProxyComboRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            // System.out.println("Called with index " + index + " object " + value);
            
            if (value == null) return new JLabel("None");
            
            FishFarmBoatProxy proxy = (FishFarmBoatProxy) value;
            JLabel label = new JLabel(proxy.toString());

            label.setForeground(proxy.getColor());            
            
            return label;
        }
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                CrwSecurityManager.loadIfDNSIsSlow();

                // End Cut and paste from BoatDebugger

                ConfigureBoatsFrame config = new ConfigureBoatsFrame();
                config.setVisible(true);

                new FishFarmF().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algC;
    private javax.swing.JCheckBox autoCB;
    private javax.swing.JPanel contourP;
    private javax.swing.JSlider contourS;
    private javax.swing.JTextField contourTF;
    private javax.swing.JTextField contourValueTF;
    private javax.swing.JPanel dataViewP;
    private javax.swing.JComboBox imgTypeCombo;
    private javax.swing.JComboBox indexC;
    private javax.swing.JTextField latMaxTF;
    private javax.swing.JTextField latMinTF;
    private javax.swing.JTextField longMaxTF;
    private javax.swing.JTextField longMinTF;
    private javax.swing.JComboBox proxyC;
    private javax.swing.JPanel proxyP;
    private javax.swing.JButton teleopB;
    // End of variables declaration//GEN-END:variables
}
