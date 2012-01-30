/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConfigureBoatsFrame.java
 *
 * Created on Feb 24, 2011, 9:25:34 PM
 */
package edu.cmu.ri.airboat.general;

import edu.cmu.ri.airboat.floodtest.ImagePanel;
import edu.cmu.ri.airboat.generalAlmost.FastSimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpVehicleService;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.Color;
import java.net.InetSocketAddress;
import java.security.AccessControlException;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import robotutils.Pose3D;

/**
 *
 * @author pscerri
 */
public class ConfigureBoatsFrame extends javax.swing.JFrame {

    ProxyManager proxyManager = new ProxyManager();
    final String LAST_URI_KEY = "LAST_URI_KEY";
    final String LAST_IMG_DIR_KEY = "LAST_IMG_DIR_KEY";

    /** Creates new form ConfigureBoatsFrame */
    public ConfigureBoatsFrame() {
        initComponents();

        Color color = randomColor();
        colorB.setBackground(color);
        colorB.setForeground(color);

        try {
            physicalServer.setText(Preferences.userRoot().get(LAST_URI_KEY, "http://168.192.1.X:11411"));
            imagesF.setText(Preferences.userRoot().get(LAST_IMG_DIR_KEY, "/tmp"));
        } catch (AccessControlException e) {
            System.out.println("Failed to access preferences");
            physicalServer.setText("http://168.192.1.X:11411");
            imagesF.setText("/tmp");
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

        jPanel1 = new javax.swing.JPanel();
        physicalServer = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        createPhysicalB = new javax.swing.JButton();
        eBoxT = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        boatNoS = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        phoneT = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        simNoS = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        simPortNo = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        latSim = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        lonSim = new javax.swing.JTextField();
        createSimB = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        nameF = new javax.swing.JTextField();
        colorB = new javax.swing.JButton();
        imagesF = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        browseFB = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Physical"));

        physicalServer.setText("http://192.168.1.149:11411");

        jLabel6.setText("Server");

        createPhysicalB.setText("Create");
        createPhysicalB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPhysicalBActionPerformed(evt);
            }
        });

        eBoxT.setText("0");

        jLabel12.setText("Ebox No.");

        boatNoS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                boatNoSStateChanged(evt);
            }
        });

        jLabel13.setText("Phone");

        phoneT.setText("0");
        phoneT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneTActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(boatNoS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(55, 55, 55)
                        .add(jLabel6))
                    .add(jLabel12))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(eBoxT)
                    .add(physicalServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                .add(27, 27, 27)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel13)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(phoneT, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 233, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(createPhysicalB))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(physicalServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6)
                            .add(jLabel13)
                            .add(phoneT, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(eBoxT, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel12)
                            .add(createPhysicalB)))
                    .add(boatNoS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(63, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Simulated"));

        jLabel7.setText("Number");

        simNoS.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel8.setText("First port:");

        simPortNo.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(11411), null, null, Integer.valueOf(1)));

        jLabel9.setText("Lat");

        latSim.setText("40.441");
        latSim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latSimActionPerformed(evt);
            }
        });

        jLabel10.setText("Lon");

        lonSim.setText("-80.014");

        createSimB.setText("Create");
        createSimB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createSimBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(105, 105, 105)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel7)
                        .add(34, 34, 34)
                        .add(simNoS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel8)
                        .add(18, 18, 18)
                        .add(simPortNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(111, 111, 111)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel9)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lonSim, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                    .add(latSim, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE))
                .add(189, 189, 189)
                .add(createSimB)
                .add(112, 112, 112))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(latSim, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel9))
                    .add(simNoS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(lonSim, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(simPortNo)
                            .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                        .add(92, 92, 92))))
            .add(jPanel3Layout.createSequentialGroup()
                .add(createSimB)
                .addContainerGap(153, Short.MAX_VALUE))
        );

        jLabel11.setText("Name");

        nameF.setText("Boat");
        nameF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFActionPerformed(evt);
            }
        });

        colorB.setBackground(new java.awt.Color(0, 0, 0));
        colorB.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        colorB.setText("Color");
        colorB.setOpaque(true);
        colorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorBActionPerformed(evt);
            }
        });

        imagesF.setText("jTextField1");

        jLabel14.setText("Images Directory");

        browseFB.setText("Browse");
        browseFB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseFBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(colorB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 181, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jLabel11)
                        .add(18, 18, 18)
                        .add(nameF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 192, Short.MAX_VALUE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jLabel14)
                        .add(51, 51, 51)
                        .add(imagesF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 223, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(browseFB))
                .add(8, 8, 8))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(nameF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(colorB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(9, 9, 9)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel14)
                            .add(imagesF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseFB)))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 139, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private Color randomColor() {
        Random rand = new Random();

        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        return new Color(r, g, b);
    }

    private void createPhysicalBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPhysicalBActionPerformed
        String server = physicalServer.getText();
        // @todo connection to physical boat

        // These are done in BoatSimpleProxy
        // URI masterUri = new URI(server);
        // RosVehicleProxy rosServer = new RosVehicleProxy(masterUri, "vehicle_client" + new Random().nextInt(1000000));

        proxyManager.createPhysicalBoatProxy(nameF.getText(), server, colorB.getBackground());

        ImagePanel.setImagesDirectory(imagesF.getText());

        try {
            Preferences p = Preferences.userRoot();
            p.put(LAST_URI_KEY, server);
            p.put(LAST_IMG_DIR_KEY, imagesF.getText());
        } catch (AccessControlException e) {
            System.out.println("Failed to save preferences");
        }

    }//GEN-LAST:event_createPhysicalBActionPerformed

    private void latSimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latSimActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_latSimActionPerformed

    private void createSimBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createSimBActionPerformed
        double lat = Double.parseDouble(latSim.getText());
        double lon = Double.parseDouble(lonSim.getText());

        int port = (Integer) simPortNo.getValue();
        int count = (Integer) simNoS.getValue();

        for (int i = 0; i < count; i++) {
            
            // Create a simulated boat and run a ROS server around it
            VehicleServer server = new FastSimpleBoatSimulator();

            // @todo Can specify port?
            UdpVehicleService rosServer = new UdpVehicleService(port + i, server);

            // Create a ROS proxy server that accesses the same object
            // RosVehicleProxy proxyServer = new RosVehicleProxy(masterUri, "vehicle_client");

            System.out.println("Initialization of vehicle server complete");

            UTMCoord utm = UTMCoord.fromLatLon(Angle.fromDegrees(lat), Angle.fromDegrees(lon));

            UtmPose p1 = new UtmPose(new Pose3D(utm.getEasting(), utm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(utm.getZone(), utm.getHemisphere().contains("North")));            

            server.setPose(p1);

            proxyManager.createSimulatedBoatProxy(nameF.getText(), new InetSocketAddress("localhost", port + i), colorB.getBackground());
        }

        simPortNo.setValue(port + count);
    }//GEN-LAST:event_createSimBActionPerformed

    private void phoneTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_phoneTActionPerformed

    private void nameFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameFActionPerformed

    private void boatNoSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_boatNoSStateChanged
        eBoxT.setText(boatNoS.getValue().toString());
        phoneT.setText(boatNoS.getValue().toString());
    }//GEN-LAST:event_boatNoSStateChanged

    private void colorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorBActionPerformed
        Color color = JColorChooser.showDialog(this, "Choose color", colorB.getBackground());
        colorB.setBackground(color);
        colorB.setForeground(color);
    }//GEN-LAST:event_colorBActionPerformed

    private void browseFBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFBActionPerformed
        JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        //In response to a button click:
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            imagesF.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseFBActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ConfigureBoatsFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner boatNoS;
    private javax.swing.JButton browseFB;
    private javax.swing.JButton colorB;
    private javax.swing.JButton createPhysicalB;
    private javax.swing.JButton createSimB;
    private javax.swing.JTextField eBoxT;
    private javax.swing.JTextField imagesF;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField latSim;
    private javax.swing.JTextField lonSim;
    private javax.swing.JTextField nameF;
    private javax.swing.JTextField phoneT;
    private javax.swing.JTextField physicalServer;
    private javax.swing.JSpinner simNoS;
    private javax.swing.JSpinner simPortNo;
    // End of variables declaration//GEN-END:variables
}
