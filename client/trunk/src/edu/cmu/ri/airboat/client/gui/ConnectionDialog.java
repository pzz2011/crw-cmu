/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client.gui;

import java.awt.Color;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.ros.RosCore;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;

/**
 * Implements a connection dialog that auto-verifies that a connection is 
 * reaching a valid ROS core.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class ConnectionDialog extends JDialog implements DocumentListener {
    public static final String LAST_URI_KEY = "edu.cmu.ri.airboat.client.gui.LastConnection";
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    final JTextField rosCoreAddress;
    final AtomicBoolean _isScheduled = new AtomicBoolean(false);
    
    public ConnectionDialog() {
        super();
        setTitle("Vehicle Connection");

        // Load the last URI that has been used
        Preferences p = Preferences.userRoot();
        rosCoreAddress = new JTextField(p.get(LAST_URI_KEY, ""), 30);
        
        //Create an array of the text and components to be displayed.
        String message = "Enter the URI of a ROS Master";
        Object[] array = {message, rosCoreAddress};

        //Create the JOptionPane.
        JOptionPane optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.OK_CANCEL_OPTION,
                                    null);

        // Register to listen to text change events
        rosCoreAddress.getDocument().addDocumentListener(this);
        scheduler.scheduleWithFixedDelay(new AddressChecker(), 0, 2, TimeUnit.SECONDS);
        
        //Make this dialog display it.
        setContentPane(optionPane);
        pack();
    }
    
    public void insertUpdate(DocumentEvent de) {
        scheduleAddressCheck();
    }

    public void removeUpdate(DocumentEvent de) {
        scheduleAddressCheck();
    }

    public void changedUpdate(DocumentEvent de) {
        scheduleAddressCheck();
    }
    
    public enum Condition {
        VALID(Color.GREEN),
        MAYBE(Color.YELLOW),
        INVALID(Color.PINK);
        
        public final Color color;
        Condition(Color c) {
            color = c;
        }
    }
    
    public void scheduleAddressCheck() {
        if (!_isScheduled.getAndSet(true)) {
            scheduler.schedule(new AddressChecker(), 0, TimeUnit.SECONDS);
        }
    }
    
    class AddressChecker implements Runnable {
        public void run() {
            rosCoreAddress.setBackground(checkAddress().color);
        }
    }
            
    Condition checkAddress() {
        
        // Start by assuming the addresss is bogus
        Condition result = Condition.INVALID;
        _isScheduled.set(false);
        
        try {
            // Try to open the URI in the text box, if it succeeds, make 
            // the box change color accordingly
            URL url = new URL(rosCoreAddress.getText().toString());
            if (InetAddress.getByName(url.getHost()).isReachable(500)) {

                // Open a connection
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                if (urlConn != null) {
                    urlConn.setConnectTimeout(500);
                    urlConn.setReadTimeout(500);
                    urlConn.connect();
                    
                    // Check for a response code that matches ROS core
                    if (urlConn.getResponseCode() == HttpURLConnection.HTTP_NOT_IMPLEMENTED) {
                        result = Condition.VALID;
                    } else { // not sure, maybe still good?
                        result = Condition.MAYBE;
                    }
                    urlConn.disconnect();
                }
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Unexpected end of file")) {
                result = Condition.MAYBE;
            }
        }

        return result;
    }
    
    /**
     * Simple test that opens a connection dialog and returns the result.
     * 
     * @param args 
     */
    public static void main(String[] args) {
       
        // Start a local ros core
        // (Not a big deal if this fails)
        try {
            RosCore core = RosCore.newPublic(11411);
            NodeRunner.newDefault().run(core, NodeConfiguration.newPrivate());
            core.awaitStart();
        } catch (RosRuntimeException e) {
            System.err.println("Failed to start ROS core: " + e.getMessage());
        }
        
        // Show the connection dialog
        new ConnectionDialog().setVisible(true);
    }
}
