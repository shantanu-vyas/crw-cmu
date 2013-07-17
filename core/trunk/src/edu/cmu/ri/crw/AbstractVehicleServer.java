package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public abstract class AbstractVehicleServer implements VehicleServer {

    protected double[][] _gains = new double[6][3];
    protected final Map<Integer, List<SensorListener>> _sensorListeners = new TreeMap<Integer, List<SensorListener>>();
    protected final List<ImageListener> _imageListeners = new ArrayList<ImageListener>();
    protected final List<VelocityListener> _velocityListeners = new ArrayList<VelocityListener>();
    protected final List<PoseListener> _stateListeners = new ArrayList<PoseListener>();
    protected final List<CameraListener> _cameraListeners = new ArrayList<CameraListener>();
    protected final List<WaypointListener> _waypointListeners = new ArrayList<WaypointListener>();

    @Override
    public double[] getGains(int axis) {
        if (axis < 0 || axis >= _gains.length) {
            return new double[0];
        }

        // Make a copy of the current state (for immutability) and return it
        double[] gains = new double[_gains[axis].length];
        System.arraycopy(_gains[axis], 0, gains, 0, _gains[axis].length);
        return gains;
    }

    @Override
    public void setGains(int axis, double[] gains) {
        if (axis < 0 || axis >= _gains.length) {
            return;
        }

        // Make a copy of the provided state (for immutability)
        System.arraycopy(gains, 0, _gains[axis], 0, Math.min(gains.length, _gains[axis].length));
    }

    @Override
    public void addPoseListener(PoseListener l) {
        synchronized (_stateListeners) {
            _stateListeners.add(l);
        }
    }

    @Override
    public void removePoseListener(PoseListener l) {
        synchronized (_stateListeners) {
            _stateListeners.remove(l);
        }
    }

    protected void sendState(UtmPose pose) {
        // Process the listeners last to first, notifying
        // those that are interested in this event
        synchronized (_stateListeners) {
            for (PoseListener l : _stateListeners) {
                l.receivedPose(pose);
            }
        }
    }

    @Override
    public void addImageListener(ImageListener l) {
        synchronized (_imageListeners) {
            _imageListeners.add(l);
        }
    }

    @Override
    public void removeImageListener(ImageListener l) {
        synchronized (_imageListeners) {
            _imageListeners.remove(l);
        }
    }

    protected static byte[] toCompressedImage(RenderedImage image) {
        // This might be inefficient, but it is far more inefficient to
        // uncompress hardware-compressed JPEG images on Android.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpeg", buffer);
            return buffer.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }

    protected void sendImage(byte[] image) {
        synchronized (_imageListeners) {
            for (ImageListener l : _imageListeners) {
                l.receivedImage(image);
            }
        }
    }

    @Override
    public void addSensorListener(int channel, SensorListener l) {
        synchronized (_sensorListeners) {
            // If there were no previous listeners for the channel, create a list
            if (!_sensorListeners.containsKey(channel)) {
                _sensorListeners.put(channel, new ArrayList<SensorListener>());
            }

            // Add the listener to the appropriate list
            _sensorListeners.get(channel).add(l);
        }
    }

    @Override
    public void removeSensorListener(int channel, SensorListener l) {
        synchronized (_sensorListeners) {
            // If there is no list of listeners, there is nothing to remove
            if (!_sensorListeners.containsKey(channel)) {
                return;
            }

            // Remove the listener from the appropriate list
            _sensorListeners.get(channel).remove(l);

            // If there are no more listeners for the channel, delete the list
            if (_sensorListeners.get(channel).isEmpty()) {
                _sensorListeners.remove(channel);
            }
        }
    }

    protected void sendSensor(int channel, SensorData reading) {
        synchronized (_sensorListeners) {
            // If there is no list of listeners, there is nothing to notify
            if (!_sensorListeners.containsKey(channel)) {
                return;
            }

            // Notify each listener in the appropriate list
            for (SensorListener l : _sensorListeners.get(channel)) {
                l.receivedSensor(reading);
            }
        }
    }

    @Override
    public void addVelocityListener(VelocityListener l) {
        synchronized (_velocityListeners) {
            _velocityListeners.add(l);
        }
    }

    @Override
    public void removeVelocityListener(VelocityListener l) {
        synchronized (_velocityListeners) {
            _velocityListeners.remove(l);
        }
    }

    protected void sendVelocity(Twist velocity) {
        synchronized (_velocityListeners) {
            for (VelocityListener l : _velocityListeners) {
                l.receivedVelocity(velocity);
            }
        }
    }

    @Override
    public void addCameraListener(CameraListener l) {
        synchronized (_cameraListeners) {
            _cameraListeners.add(l);
        }
    }

    @Override
    public void removeCameraListener(CameraListener l) {
        synchronized (_cameraListeners) {
            _cameraListeners.remove(l);
        }
    }
    
    protected void sendCameraUpdate(CameraState status) {
        // Process the listeners last to first, notifying
        // those that are interested in this event
        synchronized (_cameraListeners) {
            for (CameraListener l : _cameraListeners) {
                l.imagingUpdate(status);
            }
        }
    }

    @Override
    public void addWaypointListener(WaypointListener l) {
        synchronized (_waypointListeners) {
            _waypointListeners.add(l);
        }
    }

    @Override
    public void removeWaypointListener(WaypointListener l) {
        synchronized (_waypointListeners) {
            _waypointListeners.remove(l);
        }
    }
    
    protected void sendWaypointUpdate(WaypointState status) {
        // Process the listeners last to first, notifying
        // those that are interested in this event
        synchronized (_waypointListeners) {
            for (WaypointListener l : _waypointListeners) {
                l.waypointUpdate(status);
            }
        }
    }
}
