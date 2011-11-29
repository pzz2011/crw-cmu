/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConnectionPanel.java
 *
 * Created on Mar 2, 2011, 6:08:50 PM
 */

package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CrwNetworkUtils;
import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.udp.UdpVehicleServer;
import java.awt.Color;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

/**
 *
 * @author pkv
 */
public class ConnectionPanel extends javax.swing.JPanel {
    public static final String LAST_URI_KEY = "edu.cmu.ri.airboat.client.gui.LastConnection";

    public static int UPDATE_PERIOD_MS = 1000;

    private final Timer _timer = new Timer();
    private final UdpVehicleServer _vehicle = new UdpVehicleServer();
    private final HashMap<String, Integer> _cachedVehicles = new HashMap<String, Integer>();
    
    /** Creates new form ConnectionPanel */
    public ConnectionPanel() {
        initComponents();
        initUpdates();

        Preferences p = Preferences.userRoot();
        connectCombo.addItem(p.get(LAST_URI_KEY, "") + " - Last Used");

        // Insert a shutdown hook to cleanly close the vehicle down
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (_vehicle != null)
                    _vehicle.shutdown();
            }
        });
    }

    /**
     * Starts up a timer task that periodically checks to see if the proxy
     * command object is actually returning values.  Changes the color of the
     * button to reflect this status.
     */
    private void initUpdates() {
        // Use proxies to periodically update status, check for connection
        _timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (_vehicle == null) {
                    connectedBox.setSelected(false);
                    autonomousBox.setSelected(false);
                    connectCombo.setBackground(Color.PINK);
                } else {
                    _vehicle.isConnected(new FunctionObserver<Boolean>() {

                        public void completed(Boolean v) {
                            connectedBox.setSelected(v);
                            connectCombo.setBackground(Color.GREEN);
                        }

                        public void failed(FunctionError fe) {
                            connectCombo.setBackground(Color.PINK);
                        }
                    });

                    _vehicle.isAutonomous(new FunctionObserver<Boolean>() {

                        public void completed(Boolean v) {
                            connectCombo.setBackground(Color.GREEN);
                            autonomousBox.setSelected(v);
                        }

                        public void failed(FunctionError fe) {
                            connectCombo.setBackground(Color.PINK);
                        }
                    });
                    
                    _vehicle.getVehicleServices(new FunctionObserver<Map<SocketAddress, String>>() {

                        public void completed(Map<SocketAddress, String> v) {
                            registryCombo.setBackground(Color.GREEN);
                            
                            // Compile a list of all the recent vehicles
                            HashSet<String> recentVehicles = new HashSet<String>(v.size());
                            for (Map.Entry<SocketAddress, String> e : v.entrySet())
                                recentVehicles.add(
                                        ((InetSocketAddress)e.getKey()).getAddress().getHostAddress() +
                                        ":" + ((InetSocketAddress)e.getKey()).getPort() +
                                        " - " + e.getValue());
                            
                            synchronized(_cachedVehicles) {
                                
                                // Validate old vehicle entries 
                                for (String vehicle : _cachedVehicles.keySet()) {
                                    if (!recentVehicles.contains(vehicle)) {
                                        int idx = _cachedVehicles.remove(vehicle);
                                        connectCombo.removeItemAt(idx);
                                    }
                                }
                            
                                // Add new vehicle entries
                                for (String vehicle: recentVehicles) {
                                    if (!_cachedVehicles.containsKey(vehicle)) {
                                        _cachedVehicles.put(vehicle, connectCombo.getItemCount());
                                        connectCombo.addItem(vehicle);
                                    }
                                }
                            }
                        }

                        public void failed(FunctionError fe) {
                            registryCombo.setBackground(Color.PINK);
                        }
                    });
                }
            }
        }, 0, UPDATE_PERIOD_MS);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectCombo = new javax.swing.JComboBox();
        connectedBox = new ReadOnlyCheckBox();
        autonomousBox = new ReadOnlyCheckBox();
        registryCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        connectCombo.setEditable(true);
        connectCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Vehicle", "localhost:11411 - Simulator" }));
        connectCombo.setOpaque(true);
        connectCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectComboActionPerformed(evt);
            }
        });

        connectedBox.setForeground(new java.awt.Color(51, 51, 51));
        connectedBox.setText("Connected to Arduino");

        autonomousBox.setForeground(new java.awt.Color(51, 51, 51));
        autonomousBox.setText("Autonomous Mode");

        registryCombo.setEditable(true);
        registryCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Registry", "athiri.cimds.ri.cmu.edu:6077" }));
        registryCombo.setOpaque(true);
        registryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registryComboActionPerformed(evt);
            }
        });

        jLabel1.setText("  Server:");

        jLabel2.setText("  Registry:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, connectedBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
            .add(autonomousBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(connectCombo, 0, 155, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, registryCombo, 0, 155, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(2, 2, 2)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(registryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectedBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autonomousBox))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void registryComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registryComboActionPerformed
        synchronized(this) {
            String registryAddr = ((String)registryCombo.getSelectedItem()).trim();
            InetSocketAddress addr = CrwNetworkUtils.toInetSocketAddress(registryAddr);
            _vehicle.setRegistryService(addr);
            System.out.println("SET REGISTRY TO " + _vehicle.getRegistryService());

            // Remove old vehicle entries 
            synchronized(_cachedVehicles) {
                for (String vehicle : _cachedVehicles.keySet()) {
                    int idx = _cachedVehicles.remove(vehicle);
                    connectCombo.removeItemAt(idx);
                }
            }
        }
    }//GEN-LAST:event_registryComboActionPerformed

    private void connectComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectComboActionPerformed
        synchronized(this) {
            // Create a new proxy server that accesses the vehicle
            UdpVehicleServer vehicle = null;
            try {
                // Set vehicle to new address
                String addr = ((String)connectCombo.getSelectedItem()).split("-")[0].trim();
                _vehicle.setVehicleService(CrwNetworkUtils.toInetSocketAddress(addr));
                
                // Update listeners to new vehicle status
                fireConnectionListener(_vehicle);
                System.out.println("SET VEHICLE TO " + _vehicle.getVehicleService());
                
                // If vehicle is valid, store last used
                if (_vehicle.getVehicleService() != null) {
                    Preferences p = Preferences.userRoot();
                    p.put(LAST_URI_KEY, addr);
                }
            } catch (Exception ex) {
                System.err.println("Failed to open vehicle proxy: " + ex);
                return;
            }
        }
    }//GEN-LAST:event_connectComboActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autonomousBox;
    private javax.swing.JComboBox connectCombo;
    private javax.swing.JCheckBox connectedBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox registryCombo;
    // End of variables declaration//GEN-END:variables

    public static interface ConnectionListener {
        public void connectionChanged(VehicleServer vehicle);
    }

    private List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();

    public void addConnectionListener(ConnectionListener l) {
        listeners.add(l);
    }

    public void removeConnectionListener(ConnectionListener l) {
        listeners.remove(l);
    }

    protected void fireConnectionListener(AsyncVehicleServer vehicle){
        for(int i = 0; i < listeners.size(); i++)
            (listeners.get(i)).connectionChanged(AsyncVehicleServer.Util.toSync(vehicle)); // TODO: inefficient hack here
    }
}

