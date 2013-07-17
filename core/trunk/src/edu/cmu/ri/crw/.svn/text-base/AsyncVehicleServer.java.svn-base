package edu.cmu.ri.crw;

import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A variant of VehicleServer in which methods are asynchronous, and allows the
 * registration of observers that represent their outcomes.
 *
 * @see VehicleServer
 * 
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public interface AsyncVehicleServer {
	
	public void addPoseListener(PoseListener l, FunctionObserver<Void> obs);
	public void removePoseListener(PoseListener l, FunctionObserver<Void> obs);
	public void setPose(UtmPose pose, FunctionObserver<Void> obs);
	public void getPose(FunctionObserver<UtmPose> obs);
	
	public void addImageListener(ImageListener l, FunctionObserver<Void> obs);
	public void removeImageListener(ImageListener l, FunctionObserver<Void> obs);
        public void captureImage(int width, int height, FunctionObserver<byte[]> obs);
        
        public void addCameraListener(CameraListener l, FunctionObserver<Void> obs);
	public void removeCameraListener(CameraListener l, FunctionObserver<Void> obs);
	public void startCamera(int numFrames, double interval, int width, int height, FunctionObserver<Void> obs);
	public void stopCamera(FunctionObserver<Void> obs);
	public void getCameraStatus(FunctionObserver<CameraState> obs);
	
	public void addSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs);
	public void removeSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs);
	public void setSensorType(int channel, SensorType type, FunctionObserver<Void> obs);
	public void getSensorType(int channel, FunctionObserver<SensorType> obs);
	public void getNumSensors(FunctionObserver<Integer> obs);
	
	public void addVelocityListener(VelocityListener l, FunctionObserver<Void> obs);
	public void removeVelocityListener(VelocityListener l, FunctionObserver<Void> obs);
	public void setVelocity(Twist velocity, FunctionObserver<Void> obs);
	public void getVelocity(FunctionObserver<Twist> obs);
	
        public void addWaypointListener(WaypointListener l, FunctionObserver<Void> obs);
	public void removeWaypointListener(WaypointListener l, FunctionObserver<Void> obs);
	public void startWaypoints(UtmPose[] waypoint, String controller, FunctionObserver<Void> obs);
	public void stopWaypoints(FunctionObserver<Void> obs);
	public void getWaypoints(FunctionObserver<UtmPose[]> obs);
	public void getWaypointStatus(FunctionObserver<WaypointState> obs);
	
        public void isConnected(FunctionObserver<Boolean> obs);
        public void isAutonomous(FunctionObserver<Boolean> obs);
	public void setAutonomous(boolean auto, FunctionObserver<Void> obs);
        
	public void setGains(int axis, double[] gains, FunctionObserver<Void> obs);
	public void getGains(int axis, FunctionObserver<double[]> obs);

        /**
         * Utility class for handling AsyncVehicleServer objects.
         */
        public static class Util {
            /**
             * Converts VehicleServer implementation into asynchronous implementation.
             *
             * @param server the synchronous vehicle server implementation that will be wrapped
             * @return an asynchronous vehicle server using the specified implementation
             */
            public static AsyncVehicleServer toAsync(final VehicleServer server) {
                return new AsyncVehicleServer() {
                    
                    ExecutorService executor = Executors.newCachedThreadPool();

                    @Override
                    public void addPoseListener(final PoseListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.addPoseListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void removePoseListener(final PoseListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.removePoseListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void setPose(final UtmPose state, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.setPose(state);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void getPose(final FunctionObserver<UtmPose> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getPose());
                            }
                        });
                    }

                    @Override
                    public void addImageListener(final ImageListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.addImageListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void removeImageListener(final ImageListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.removeImageListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void captureImage(final int width, final int height, final FunctionObserver<byte[]> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.captureImage(width, height));
                            }
                        });
                    }

                    @Override
                    public void addCameraListener(final CameraListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.addCameraListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void removeCameraListener(final CameraListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.removeCameraListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void startCamera(final int numFrames, final double interval, final int width, final int height, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.startCamera(numFrames, interval, width, height);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void stopCamera(final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.stopCamera();
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void getCameraStatus(final FunctionObserver<CameraState> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getCameraStatus());
                            }
                        });
                    }

                    @Override
                    public void addSensorListener(final int channel, final SensorListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.addSensorListener(channel, l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void removeSensorListener(final int channel, final SensorListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.removeSensorListener(channel, l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void setSensorType(final int channel, final SensorType type, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.setSensorType(channel, type);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void getSensorType(final int channel, final FunctionObserver<SensorType> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getSensorType(channel));
                            }
                        });
                    }

                    @Override
                    public void getNumSensors(final FunctionObserver<Integer> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getNumSensors());
                            }
                        });
                    }

                    @Override
                    public void addVelocityListener(final VelocityListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.addVelocityListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void removeVelocityListener(final VelocityListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.removeVelocityListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void setVelocity(final Twist velocity, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.setVelocity(velocity);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void getVelocity(final FunctionObserver<Twist> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getVelocity());
                            }
                        });
                    }

                    @Override
                    public void addWaypointListener(final WaypointListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.addWaypointListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void removeWaypointListener(final WaypointListener l, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.removeWaypointListener(l);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void startWaypoints(final UtmPose[] waypoint, final String controller, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.startWaypoints(waypoint, controller);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void stopWaypoints(final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.stopWaypoints();
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void getWaypoints(final FunctionObserver<UtmPose[]> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getWaypoints());
                            }
                        });
                    }

                    @Override
                    public void getWaypointStatus(final FunctionObserver<WaypointState> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getWaypointStatus());
                            }
                        });
                    }

                    @Override
                    public void isConnected(final FunctionObserver<Boolean> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.isConnected());
                            }
                        });
                    }

                    @Override
                    public void isAutonomous(final FunctionObserver<Boolean> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.isAutonomous());
                            }
                        });
                    }

                    @Override
                    public void setAutonomous(final boolean auto, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.setAutonomous(auto);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void setGains(final int axis, final double[] gains, final FunctionObserver<Void> obs) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                server.setGains(axis, gains);
                                if (obs != null) obs.completed(null);
                            }
                        });
                    }

                    @Override
                    public void getGains(final int axis, final FunctionObserver<double[]> obs) {
                        if (obs == null) return;
                        
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                obs.completed(server.getGains(axis));
                            }
                        });
                    }
                };
            }

            /**
             * Converts AsyncVehicleServer implementation into synchronous implementation.
             *
             * @param server the asynchronous vehicle server implementation that will be wrapped
             * @return a synchronous vehicle server using the specified implementation
             */
            public static VehicleServer toSync(final AsyncVehicleServer server) {
                return new VehicleServer() {

                    /**
                     * Simple delay class that blocks a synchronous function
                     * call until the backing asynchronous one completes.
                     */
                    class Delayer<V> implements FunctionObserver<V> {
                        final CountDownLatch _latch = new CountDownLatch(1);
                        private V _result = null;
                        
                        public V awaitResult() {
                            try { _latch.await(); } catch (InterruptedException e) {}
                            return _result;
                        }
                        
                        @Override
                        public void completed(V result) {
                            _latch.countDown();
                            _result = result;
                        }

                        @Override
                        public void failed(FunctionError cause) {
                            _latch.countDown();
                        }                        
                    }
                    
                    @Override
                    public void addPoseListener(PoseListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addPoseListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removePoseListener(PoseListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removePoseListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setPose(UtmPose state) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setPose(state, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public UtmPose getPose() {
                        final Delayer<UtmPose> delayer = new Delayer<UtmPose>();
                        server.getPose(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addImageListener(ImageListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addImageListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeImageListener(ImageListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeImageListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public byte[] captureImage(int width, int height) {
                        final Delayer<byte[]> delayer = new Delayer<byte[]>();
                        server.captureImage(width, height, delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addCameraListener(CameraListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addCameraListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeCameraListener(CameraListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeCameraListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void startCamera(int numFrames, double interval, int width, int height) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.startCamera(numFrames, interval, width, height, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void stopCamera() {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.stopCamera(delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public CameraState getCameraStatus() {
                        final Delayer<CameraState> delayer = new Delayer<CameraState>();
                        server.getCameraStatus(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addSensorListener(int channel, SensorListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addSensorListener(channel, l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeSensorListener(int channel, SensorListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeSensorListener(channel, l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setSensorType(int channel, SensorType type) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setSensorType(channel, type, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public SensorType getSensorType(int channel) {
                        final Delayer<SensorType> delayer = new Delayer<SensorType>();
                        server.getSensorType(channel, delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public int getNumSensors() {
                        final Delayer<Integer> delayer = new Delayer<Integer>();
                        server.getNumSensors(delayer);
                        Integer nSensors = delayer.awaitResult();
                        return (nSensors != null) ? nSensors : -1;
                    }

                    @Override
                    public void addVelocityListener(VelocityListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addVelocityListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeVelocityListener(VelocityListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeVelocityListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setVelocity(Twist velocity) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setVelocity(velocity, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public Twist getVelocity() {
                        final Delayer<Twist> delayer = new Delayer<Twist>();
                        server.getVelocity(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addWaypointListener(WaypointListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addWaypointListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeWaypointListener(WaypointListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeWaypointListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void startWaypoints(UtmPose[] waypoint, String controller) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.startWaypoints(waypoint, controller, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void stopWaypoints() {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.stopWaypoints(delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public UtmPose[] getWaypoints() {
                        final Delayer<UtmPose[]> delayer = new Delayer<UtmPose[]>();
                        server.getWaypoints(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public WaypointState getWaypointStatus() {
                        final Delayer<WaypointState> delayer = new Delayer<WaypointState>();
                        server.getWaypointStatus(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public boolean isConnected() {
                        final Delayer<Boolean> delayer = new Delayer<Boolean>();
                        server.isConnected(delayer);
                        Boolean b = delayer.awaitResult();
                        return (b != null) ? b : false;
                    }

                    @Override
                    public boolean isAutonomous() {
                        final Delayer<Boolean> delayer = new Delayer<Boolean>();
                        server.isAutonomous(delayer);
                        Boolean b = delayer.awaitResult();
                        return (b != null) ? b : false;
                    }

                    @Override
                    public void setAutonomous(boolean auto) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setAutonomous(auto, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setGains(int axis, double[] gains) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setGains(axis, gains, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public double[] getGains(int axis) {
                        final Delayer<double[]> delayer = new Delayer<double[]>();
                        server.getGains(axis, delayer);
                        return delayer.awaitResult();
                    }
                };
            }
        }
}
