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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

/**
 * Implements a connection dialog that auto-verifies that a connection is 
 * reaching a valid ROS core.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class ConnectionDialog extends JDialog {

    public static final String LAST_URI_KEY = "edu.cmu.ri.airboat.client.gui.LastConnection";
    private final ScheduledExecutorService _scheduler = Executors.newScheduledThreadPool(1);

    private SocketAddress _vehicleAddress = null;
    final JOptionPane _optionPane;
    final JTextField _vehicleAddressField;
    final AtomicBoolean _isScheduled = new AtomicBoolean(false);

    public ConnectionDialog(JFrame frame, boolean isModal) {
        super(frame, isModal);
        setTitle("Vehicle Connection");

        // Load the last URI that has been used
        Preferences p = Preferences.userRoot();
        _vehicleAddressField = new JTextField(p.get(LAST_URI_KEY, "localhost:11411"), 30);

        // Create an array of the text and components to be displayed.
        String message = "Enter the URI of a ROS Master";
        Object[] array = {message, _vehicleAddressField};

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
                _vehicleAddressField.requestFocusInWindow();
            }
        });

        // Register an event handler that reacts to option pane state changes.
        _optionPane.addPropertyChangeListener(_propListener);

        // Register to listen to text change events
        _vehicleAddressField.getDocument().addDocumentListener(_docListener);
        _vehicleAddressField.addActionListener(_actionListener);
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
                    String[] addrParts = _vehicleAddressField.getText().toString().split(":");
                    _vehicleAddress = new InetSocketAddress(addrParts[0], Integer.parseInt(addrParts[1]));
                    
                    Preferences p = Preferences.userRoot();
                    p.put(LAST_URI_KEY, _vehicleAddress.toString());
                    try {
                        p.flush();
                    } catch (BackingStoreException ex) {
                        Logger.getLogger(ConnectionDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (NumberFormatException ex) {
                    _vehicleAddress = null;
                }
            }

            // Hide the dialog box
            _vehicleAddressField.setText(null);
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
            _vehicleAddressField.setBackground(checkAddress().color);
        }
    }

    Condition checkAddress() {

        // Start by assuming the addresss is bogus
        Condition result = Condition.INVALID;
        _isScheduled.set(false);

        try {
            // Try to open the URI in the text box, if it succeeds, make
            // the box change color accordingly
            URL url = new URL(_vehicleAddressField.getText().toString());
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

    public SocketAddress getAddress() {
        return _vehicleAddress;
    }
}
