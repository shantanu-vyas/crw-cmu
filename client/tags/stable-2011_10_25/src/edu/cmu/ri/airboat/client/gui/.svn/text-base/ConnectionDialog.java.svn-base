/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
public class ConnectionDialog extends JDialog {

    public static final String LAST_URI_KEY = "edu.cmu.ri.airboat.client.gui.LastConnection";
    private final ScheduledExecutorService _scheduler = Executors.newScheduledThreadPool(1);

    private URI _rosCoreAddress = null;
    final JOptionPane _optionPane;
    final JTextField _rosCoreField;
    final AtomicBoolean _isScheduled = new AtomicBoolean(false);

    public ConnectionDialog(JFrame frame, boolean isModal) {
        super(frame, isModal);
        setTitle("Vehicle Connection");

        // Load the last URI that has been used
        Preferences p = Preferences.userRoot();
        _rosCoreField = new JTextField(p.get(LAST_URI_KEY, "http://localhost:11411"), 30);

        // Create an array of the text and components to be displayed.
        String message = "Enter the URI of a ROS Master";
        Object[] array = {message, _rosCoreField};

        // Create the JOptionPane.
        _optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null);

        // Make this dialog display the JOptionPane.
        setLocationRelativeTo(frame);
        setContentPane(_optionPane);
        pack();

        // Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                _optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        // Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent ce) {
                _rosCoreField.requestFocusInWindow();
            }
        });

        // Register an event handler that reacts to option pane state changes.
        _optionPane.addPropertyChangeListener(_propListener);

        // Register to listen to text change events
        _rosCoreField.getDocument().addDocumentListener(_docListener);
        _rosCoreField.addActionListener(_actionListener);
        _scheduler.scheduleWithFixedDelay(new AddressChecker(), 0, 2, TimeUnit.SECONDS);

        // Show the dialog box
        setVisible(true);
    }
    
    final PropertyChangeListener _propListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();

            // Was the option pane visible and the source of event?
            if (!isVisible() || (evt.getSource() != _optionPane)) {
                return;
            }

            // Did a property related to the input value change?
            if (!JOptionPane.VALUE_PROPERTY.equals(prop)
                    && !JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)) {
                return;
            }

            // Reset the JOptionPane's value.
            Object value = _optionPane.getValue();
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            } else {
                _optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            }

            // Did the user hit OK or cancel?
            if (value.equals(JOptionPane.OK_OPTION)) {
                try {
                    _rosCoreAddress = new URI(_rosCoreField.getText().toString());
                    
                    Preferences p = Preferences.userRoot();
                    p.put(LAST_URI_KEY, _rosCoreAddress.toString());
                    try {
                        p.flush();
                    } catch (BackingStoreException ex) {
                        Logger.getLogger(ConnectionDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (URISyntaxException ex) {
                    _rosCoreAddress = null;
                }
            }

            // Hide the dialog box
            _rosCoreField.setText(null);
            _scheduler.shutdownNow();
            dispose();
        }
    };

    final ActionListener _actionListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            _optionPane.setValue(JOptionPane.OK_OPTION);
        }
    };

    final DocumentListener _docListener = new DocumentListener() {

        public void insertUpdate(DocumentEvent de) {
            updateAddress();
        }

        public void removeUpdate(DocumentEvent de) {
            updateAddress();
        }

        public void changedUpdate(DocumentEvent de) {
            updateAddress();
        }
    };

    public enum Condition {

        VALID(new Color(0x00BBEEBB)),
        MAYBE(new Color(0x00EEEE99)),
        INVALID(new Color(0x00EEBBBB));
        public final Color color;

        Condition(Color c) {
            color = c;
        }
    }

    public void updateAddress() {
        if (!_isScheduled.getAndSet(true)) {
            _scheduler.schedule(new AddressChecker(), 0, TimeUnit.SECONDS);
        }
    }

    class AddressChecker implements Runnable {

        public void run() {
            _rosCoreField.setBackground(checkAddress().color);
        }
    }

    Condition checkAddress() {

        // Start by assuming the addresss is bogus
        Condition result = Condition.INVALID;
        _isScheduled.set(false);

        try {
            // Try to open the URI in the text box, if it succeeds, make
            // the box change color accordingly
            URL url = new URL(_rosCoreField.getText().toString());
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

    public URI getAddress() {
        return _rosCoreAddress;
    }

    /**
     * Simple test that opens a connection dialog and returns the result.
     *
     * @param args
     */
    public static void main(String[] args) {

        // Start a local ros core
        // (Not a big deal if this fails)
        RosCore core = null;
        try {
            core = RosCore.newPublic(11411);
            NodeRunner.newDefault().run(core, NodeConfiguration.newPrivate());
            core.awaitStart();
        } catch (RosRuntimeException e) {
            System.err.println("Failed to start ROS core: " + e.getMessage());
        }
        
        // Show the connection dialog
        ConnectionDialog d = new ConnectionDialog(null, true);
        System.out.println(d.getAddress());

        // Terminate the sample RosCore
        if (core != null)
            core.shutdown();
    }
}
