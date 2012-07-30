/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DrivePanel.java
 *
 * Created on Jan 13, 2011, 3:11:53 PM
 */

package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.VelocityListener;
import edu.cmu.ri.crw.data.Twist;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author pkv
 */
public class DrivePanel extends AbstractAirboatPanel {

    public static final int DEFAULT_UPDATE_MS = 750;
    public static final int DEFAULT_COMMAND_MS = 200;

    // Ranges for thrust and rudder signals
    public static final double THRUST_MIN = 0.0;
    public static final double THRUST_MAX = 2.0;
    public static final double RUDDER_MIN = 2.5;
    public static final double RUDDER_MAX = -2.5;
    
    // Resolution for keyboard controls
    public static final double RESOLUTION = 50;

    // Sets up a flag limiting the rate of velocity command transmission
    public AtomicBoolean _sentVelCommand = new AtomicBoolean(false);
    public AtomicBoolean _queuedVelCommand = new AtomicBoolean(false);

    /** Creates new form DrivePanel */
    public DrivePanel() {
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

        jRudder = new javax.swing.JSlider();
        jThrust = new javax.swing.JSlider();
        jRudderBar = new javax.swing.JProgressBar();
        jThrustBar = new javax.swing.JProgressBar();
        jAutonomyBox = new ReadOnlyCheckBox();
        jConnectedBox = new ReadOnlyCheckBox();
        jControlBox = new javax.swing.JCheckBox();
        messageBox = new javax.swing.JTextField();

        jRudder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRudderStateChanged(evt);
            }
        });

        jThrust.setOrientation(javax.swing.JSlider.VERTICAL);
        jThrust.setValue(0);
        jThrust.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jThrustStateChanged(evt);
            }
        });

        jRudderBar.setValue(50);

        jThrustBar.setOrientation(1);

        jAutonomyBox.setText("Autonomous");
        jAutonomyBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAutonomyBoxActionPerformed(evt);
            }
        });

        jConnectedBox.setForeground(new java.awt.Color(51, 51, 51));
        jConnectedBox.setText("Connected");

        jControlBox.setText("Controls");
        jControlBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jControlBoxActionPerformed(evt);
            }
        });
        jControlBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jControlBoxFocusLost(evt);
            }
        });

        messageBox.setBackground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.paletteBackground"));
        messageBox.setEditable(false);
        messageBox.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jThrust, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jThrustBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jAutonomyBox)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jConnectedBox)
                                            .addComponent(jControlBox))
                                        .addGap(13, 13, 13))))
                            .addComponent(messageBox)))
                    .addComponent(jRudder, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                    .addComponent(jRudderBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jThrustBar, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .addComponent(jThrust, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jAutonomyBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jConnectedBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jControlBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(messageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRudder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRudderBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jThrustStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jThrustStateChanged
        updateVelocity();
    }//GEN-LAST:event_jThrustStateChanged

    private void jRudderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRudderStateChanged
        updateVelocity();
    }//GEN-LAST:event_jRudderStateChanged

    private void jAutonomyBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAutonomyBoxActionPerformed
        if (_vehicle != null) {
            final boolean value = !jAutonomyBox.isSelected();
            _vehicle.setAutonomous(value, new FunctionObserver<Void>() {

                public void completed(Void v) {
                    jAutonomyBox.setSelected(value);
                }

                public void failed(FunctionError fe) {}
            });
        }
    }//GEN-LAST:event_jAutonomyBoxActionPerformed

    private void jControlBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jControlBoxActionPerformed
        //first, ensure that vehicle is connected
        KeyListener kl = new KeyListener() {
            public void keyTyped(KeyEvent ke) {}
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_UP) {
                    messageBox.setBackground(Color.YELLOW);
                    messageBox.setText("Faster");
                    int new_thrust = (int) (jThrust.getValue() + (jThrust.getMaximum() - jThrust.getMinimum())/RESOLUTION);
                    jThrust.setValue(new_thrust);
                }
                else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                    messageBox.setBackground(Color.BLUE);
                    messageBox.setText("Slower");
                    int new_thrust = (int) (jThrust.getValue() - (jThrust.getMaximum() - jThrust.getMinimum())/RESOLUTION);
                    jThrust.setValue(new_thrust);
                }
                else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                    messageBox.setBackground(Color.GREEN);
                    messageBox.setText("Turn Left");
                    int new_rudder = (int) (jRudder.getValue() - (jRudder.getMaximum() - jRudder.getMinimum())/RESOLUTION);
                    jRudder.setValue(new_rudder);
                }
                else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                    messageBox.setBackground(Color.RED);
                    messageBox.setText("Turn Right");
                    int new_rudder = (int) (jRudder.getValue() + (jRudder.getMaximum() - jRudder.getMinimum())/RESOLUTION);
                    jRudder.setValue(new_rudder);
                }
            }
            public void keyReleased(KeyEvent ke) {}
        };
        if (jControlBox.isSelected()) {
            if (_vehicle != null) {
                // if autonomous, change it
                if (jAutonomyBox.isSelected()) {
                    jAutonomyBoxActionPerformed(evt);
                }
                if (jControlBox.isFocusOwner()) {
                    messageBox.setBackground(Color.green);
                }
                messageBox.setText("Keep Focus on Control Box to Allow Keyboard Controls");
                jControlBox.addKeyListener(kl);
            }
        }
        else {
            jControlBox.removeKeyListener(kl);
        }
    }//GEN-LAST:event_jControlBoxActionPerformed

    private void jControlBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jControlBoxFocusLost
        // TODO add your handling code here:
        if (jControlBox.isSelected()) {
            messageBox.setText("Reselect focus on Control Box to Allow Keyboard Controls");   
            jControlBox.doClick();
        }
        
    }//GEN-LAST:event_jControlBoxFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAutonomyBox;
    private javax.swing.JCheckBox jConnectedBox;
    private javax.swing.JCheckBox jControlBox;
    protected javax.swing.JSlider jRudder;
    protected javax.swing.JProgressBar jRudderBar;
    protected javax.swing.JSlider jThrust;
    protected javax.swing.JProgressBar jThrustBar;
    private javax.swing.JTextField messageBox;
    // End of variables declaration//GEN-END:variables
   
    

    
    
    // Callback that handles GUI events that change velocity
    protected void updateVelocity() {
        // Check if there is already a command queued up, if not, queue one up
        if (!_sentVelCommand.getAndSet(true)) {

            // Send one command immediately
            sendVelocity();

            // Queue up a command at the end of the refresh timestep
            _timer.schedule(new UpdateVelTask(), DEFAULT_COMMAND_MS);
        } else {
            _queuedVelCommand.set(true);
        }
    }

    // Simple update task that periodically checks whether velocity needs updating
    class UpdateVelTask extends TimerTask {
        @Override
        public void run() {
            if (_queuedVelCommand.getAndSet(false)) {
                sendVelocity();
                _timer.schedule(new UpdateVelTask(), DEFAULT_COMMAND_MS);
            } else {
                _sentVelCommand.set(false);
            }
        }
    }

    // Sets velocities from sliders to control proxy
    protected void sendVelocity() {
        if (_vehicle != null) {
            Twist twist = new Twist();
            twist.dx(fromProgressToRange(jThrust.getValue(), THRUST_MIN, THRUST_MAX));
            twist.drz(fromProgressToRange(jRudder.getValue(), RUDDER_MIN, RUDDER_MAX));
            _vehicle.setVelocity(twist, null);
        }
    }

    // Converts from progress bar value to linear scaling between min and max
    protected double fromProgressToRange(int progress, double min, double max) {
            return (min + (max - min) * ((double)progress)/100.0);
    }

    // Converts from progress bar value to linear scaling between min and max
    protected int fromRangeToProgress(double value, double min, double max) {
            return (int)(100.0 * (value - min)/(max - min));
    }

    @Override
    public void setVehicle(AsyncVehicleServer vehicle) {
        super.setVehicle(vehicle);
        vehicle.addVelocityListener(new VelocityListener() {

            public void receivedVelocity(Twist twist) {
                jThrustBar.setValue(fromRangeToProgress(twist.dx(), THRUST_MIN, THRUST_MAX));
                jRudderBar.setValue(fromRangeToProgress(twist.drz(), RUDDER_MIN, RUDDER_MAX));
                DrivePanel.this.repaint();
            }
        }, null);
    }

    /**
     * Performs periodic update of GUI elements
     */
    public void update() {
        if (_vehicle != null) {
            _vehicle.isAutonomous(new FunctionObserver<Boolean>() {

                public void completed(Boolean v) {
                    jAutonomyBox.setEnabled(true);
                    jAutonomyBox.setSelected(v);
                }

                public void failed(FunctionError fe) {
                    jAutonomyBox.setEnabled(false);
                }
            });
            
            _vehicle.isConnected(new FunctionObserver<Boolean>() {

                public void completed(Boolean v) {
                    jConnectedBox.setEnabled(true);
                    jConnectedBox.setSelected(v);
                }

                public void failed(FunctionError fe) {
                    jConnectedBox.setEnabled(false);
                }
            });
        } else {
            if (jAutonomyBox != null) {
                jAutonomyBox.setEnabled(false);
                jAutonomyBox.setSelected(false);
            }
            
            if (jConnectedBox != null) {
                jConnectedBox.setEnabled(false);
                jConnectedBox.setSelected(false);
            }
        }
    }
}
