/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.airboat.interfaces.AirboatCommand;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import edu.cmu.ri.airboat.interfaces.AirboatSensor;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

/**
 * Simple base class for constructing Airboat GUI elements
 *
 * @author pkv
 */
public abstract class AbstractAirboatPanel extends JPanel implements AirboatComponent {

    public static int DEFAULT_UPDATE_PERIOD = 1000;

    protected Timer _timer = new Timer();
    protected AirboatControl _control = null;
    protected AirboatCommand _command = null;
    protected AirboatSensor _sensor = null;

    public AbstractAirboatPanel() {
        _timer.scheduleAtFixedRate(new UpdateTask(), 0, DEFAULT_UPDATE_PERIOD);
    }

    private class UpdateTask extends TimerTask {
        @Override
        public void run() {
            update();
        }
    }

    public final void setControl(AirboatControl control) {
        _control = control;
    }

    public final void setCommand(AirboatCommand command) {
        _command = command;
    }

    public final void setSensor(AirboatSensor sensor) {
        _sensor = sensor;
    }

    public final void setUpdateRate(long period_ms) {
        _timer.cancel();
        _timer = new Timer();
        _timer.scheduleAtFixedRate(new UpdateTask(), 0, period_ms);
    }

    protected abstract void update();
}
