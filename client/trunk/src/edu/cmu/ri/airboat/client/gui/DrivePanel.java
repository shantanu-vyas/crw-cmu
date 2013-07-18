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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author pkv
 */
public class DrivePanel extends AbstractAirboatPanel {

    public static final int DEFAULT_UPDATE_MS = 750;
    public static final int DEFAULT_COMMAND_MS = 200;
    // Ranges for thrust and rudder signals
    public static final double THRUST_MIN = 0.0;
    public static final double THRUST_MAX = 1.0;
    public static final double RUDDER_MIN = 1.0;
    public static final double RUDDER_MAX = -1.0;
    // Key codes for arrow keys
    Timer timer = new Timer();
    static Timer timekeep = new Timer();
    static final int UP = 38;
    static final int DOWN = 40;
    static final int LEFT = 37;
    static final int RIGHT = 39;
    static int keyCode;
    static boolean inBox;
    DrivePanel _DrivePanel = this; //non-static pointer 
    int[] keyArray;
    int tempThrustMax = 40;
    boolean keyboardMode = true;
    boolean controllerMode = false;
    static Controller Joystick;
    static boolean straight = false;
    static boolean left = true;
    // Sets up a flag limiting the rate of velocity command transmission
    public AtomicBoolean _sentVelCommand = new AtomicBoolean(false);
    public AtomicBoolean _queuedVelCommand = new AtomicBoolean(false);

    /**
     * Creates new form DrivePanel
     */
    public DrivePanel() {
        initComponents();
        setUpdateRate(DEFAULT_UPDATE_MS);
        jThrust.setMinorTickSpacing(10);
        jThrust.setMajorTickSpacing(50);
        jRudder.setMajorTickSpacing(50); //put a tick so you know when the rudder is straight
        jRudder.setPaintTicks(true);
        jThrust.setPaintTicks(true);


        controlSystem(); //keyboard control system 
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jRudder = new javax.swing.JSlider();
        jThrust = new javax.swing.JSlider();
        jRudderBar = new javax.swing.JProgressBar();
        jThrustBar = new javax.swing.JProgressBar();
        jAutonomyBox = new ReadOnlyCheckBox();
        jConnectedBox = new ReadOnlyCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

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

        jLabel1.setText("Thrust");

        jLabel2.setText("Steering");

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jAutonomyBox))
                .addComponent(jConnectedBox)))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(12, 12, 12))))
                .addComponent(jRudder, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addComponent(jRudderBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jThrustBar, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addComponent(jThrust, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jAutonomyBox)
                .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jConnectedBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRudder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRudderBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap()));
    }// </editor-fold>                        

    private void jThrustStateChanged(javax.swing.event.ChangeEvent evt) {
        updateVelocity();
    }

    private void jRudderStateChanged(javax.swing.event.ChangeEvent evt) {
        updateVelocity();
    }

    private void jAutonomyBoxActionPerformed(java.awt.event.ActionEvent evt) {
        if (_vehicle != null) {
            final boolean value = !jAutonomyBox.isSelected();
            _vehicle.setAutonomous(value, new FunctionObserver<Void>() {
                public void completed(Void v) {
                    jAutonomyBox.setSelected(value);
                }

                public void failed(FunctionError fe) {
                }
            });
        }
    }
    // Variables declaration - do not modify                     
    private javax.swing.JCheckBox jAutonomyBox;
    private javax.swing.JCheckBox jConnectedBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSlider jRudder;
    private javax.swing.JProgressBar jRudderBar;
    private javax.swing.JSlider jThrust;
    private javax.swing.JProgressBar jThrustBar;
    // End of variables declaration                   

    // Callback that handles GUI events that change velocity
    protected void updateVelocity() {
        // Check if there is already a command queued up, if not, queue one up
        if (!_sentVelCommand.getAndSet(true)) {

            // Send one command immediately
            sendVelocity();

            // Queue up a command at the end of the refresh timestep
            _timer.schedule(new DrivePanel.UpdateVelTask(), DEFAULT_COMMAND_MS);
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
                _timer.schedule(new DrivePanel.UpdateVelTask(), DEFAULT_COMMAND_MS);
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
    private double fromProgressToRange(int progress, double min, double max) {
        return (min + (max - min) * ((double) progress) / 100.0);
    }

    // Converts from progress bar value to linear scaling between min and max
    private int fromRangeToProgress(double value, double min, double max) {
        return (int) (100.0 * (value - min) / (max - min));
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

    public void controlSystem() { //possibly when mouse is in the field then apply the keys
//        goStraight1(true);
        controllerMode = true;
        _DrivePanel.requestFocus();
        jThrust.setFocusable(false);
        jRudder.setFocusable(false);
        keyArray = new int[4];
        keyArray[0] = 0; //up
        keyArray[1] = 0; //down
        keyArray[2] = 0; //left
        keyArray[3] = 0; //right

        _DrivePanel.addMouseListener(new MouseListener() { //mouse listener for making sure you click in the box before the key commands work
            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                output(e);
            }

            void output(MouseEvent e) {
                //System.out.println("this is the frame" + e.getSource()); 
                if (e.getSource() == _DrivePanel) {
                    _DrivePanel.requestFocus();
                    inBox = true;
                } else {
                    inBox = false;
                }
            }
        });

        _DrivePanel.addKeyListener(new KeyListener() //key listener for updating thrust and rudder 
        {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                keyCode = e.getKeyCode();  //when you press a key set its value to 1, the later if that value is 1 do something
                if (keyCode == UP) {
                    keyArray[0] = 1;
                }
                if (keyCode == DOWN) {
                    keyArray[1] = 1;
                }
                if (keyCode == LEFT) {
                    keyArray[2] = 1;
                }
                if (keyCode == RIGHT) {
                    keyArray[3] = 1;
                }
                displayInfo(e);
            }

            public void keyReleased(KeyEvent e) {
                keyCode = e.getKeyCode();
                if (keyCode == UP) {
                    keyArray[0] = 0;
                } //if a key is lifted set that value to 0
                if (keyCode == DOWN) {
                    keyArray[1] = 0;
                }
                if (keyCode == LEFT) {
                    keyArray[2] = 0;
                }
                if (keyCode == RIGHT) {
                    keyArray[3] = 0;
                }
            }

            public void displayInfo(KeyEvent e) {
                if (inBox == true) {  //makes sure you are in the box
                    if (keyArray[0] == 1 && keyArray[1] != 1) {  //up
                        jThrust.setValue(jThrust.getValue() + 2);
                        controllerMode = false;
                    }
                    if (keyArray[1] == 1 && keyArray[0] != 1) { //down
                        jThrust.setValue(jThrust.getValue() - 2);
                        controllerMode = false;
                    }
                    if (keyArray[2] == 1 && keyArray[3] != 1) { //left
                        jRudder.setValue(jRudder.getValue() - 10);
                        controllerMode = false;
                    }
                    if (keyArray[3] == 1 && keyArray[2] != 1) { //right
                        jRudder.setValue(jRudder.getValue() + 10);
                        controllerMode = false;
                    }
                }
            }
        });
        if (controllerConnected() == true) {
            Controllers.init();
            System.out.println("is connected");
            new javax.swing.Timer(35, new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    Controllers.loop();
                    if (Controllers.isLeftBumperPressed()) {
                        straight = true;
                        if (left == true) {

                            if (jRudder.getValue() != 25) {
                                if (jRudder.getValue() > 25) {
                                    jRudder.setValue(jRudder.getValue() - 1);

                                    if (jRudder.getValue() == 25) {
                                        left = false;
                                    }
                                }
                            }
                        } else if (left == false) {
                            if (jRudder.getValue() != 75) {
                                if (jRudder.getValue() != 75) {
                                    if (jRudder.getValue() < 75) {
                                        jRudder.setValue(jRudder.getValue() + 1);
                                        if (jRudder.getValue() == 75) {
                                            left = true;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        straight = false;
                    }

                    if (!Controllers.isLeftTriggerPressed()) {

                        if (Controllers.isRightTriggerPressed()) {
                            if (jThrust.getValue() != 80) {
                                if (jThrust.getValue() <= 80) {
                                    jThrust.setValue(jThrust.getValue() + 2);
                                }
                            }
                        }
                        if (jThrust.getValue() <= tempThrustMax) {
                            if (Controllers.returnJ1Y() < -.3) { //y ais is flippsed
                                jThrust.setValue(jThrust.getValue() + 1);
                                controllerMode = true;
                            }
                        }
                        if (Controllers.returnJ1Y() > .3) { //y ais is flippsedÏ
                            jThrust.setValue(jThrust.getValue() - 1);
                            controllerMode = true;
                        }
                    }
                    if (straight == false) {
                        if (Controllers.returnJ1X() < -.7 || Controllers.returnJ2X() < -.7) { //y ais is flippsed
                            jRudder.setValue(jRudder.getValue() - 5);
                        }
                        if (Controllers.returnJ1X() > .7 || Controllers.returnJ2X() > .7) { //y ais is flippsed
                            jRudder.setValue(jRudder.getValue() + 5);
                        }
                    }
                    //////////////////////////////////////////////////////////////
                    if (controllerMode) {
                        if (!Controllers.isLeftTriggerPressed()) {
                            if ((Controllers.returnJ1Y() < .3) && (Controllers.returnJ1Y() > -.3)) {
                                if (jThrust.getValue() != 0) {
                                    jThrust.setValue(jThrust.getValue() - 1);
                                }
                            }
                        }
                        if ((Controllers.returnJ1X() < .3) && (Controllers.returnJ1X() > -.3) || (Controllers.returnJ2X() < .3) && (Controllers.returnJ2X() > -.3)) {
                            if (straight == false) {
                                if (jRudder.getValue() != 50) {
                                    if (jRudder.getValue() > 50) {
                                        jRudder.setValue(jRudder.getValue() - 1);
                                    }
                                    if (jRudder.getValue() < 50) {
                                        jRudder.setValue(jRudder.getValue() + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }).start();
        }
    }

    public static boolean controllerConnected() {

        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            if (c.getType() == Controller.Type.STICK) {
                Joystick = c;
                System.out.println(Joystick.getName());
//                System.out.println(Joystick.getName() + "is connected");//System.out.println(Joystick.getName())
            }
        }
        try {
            return Joystick.poll();
        } catch (Exception e) {
//                System.out.println("not connected");
            return false;
        }
    }
}