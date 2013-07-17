/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.FunctionObserver;
import java.util.HashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Specialized hybrid data structure that holds continuations for functions until either
 * they are called, or a timeout occurs.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class TimeoutMap {

    final HashMap<Long, FunctionObserver> _tickets = new HashMap<Long, FunctionObserver>();
    final DelayQueue<Timeout> _ticketTimeouts = new DelayQueue<Timeout>();
    final AtomicBoolean isRunning = new AtomicBoolean(true);
    final Thread timeoutThread = new Thread(new TimeoutQueuer());
    {
        timeoutThread.start();
    }
    
    /**
     * Simple queuing thread that times out old functions in the delay queue.
     */
    private class TimeoutQueuer implements Runnable {
        @Override
        public void run() {
            while (isRunning.get()) {
                try {
                    timeout(_ticketTimeouts.take().ticket);
                } catch (InterruptedException e) {
                    // We'll check if we need to terminate in the while loop
                }
            }
        }

    }
    
    /**
     * Simple delay class to manage function timeouts.
     */
    private class Timeout implements Delayed {
        private final long _timeout_ns = System.nanoTime() + UdpConstants.TIMEOUT_NS;
        public final long ticket;
        
        public Timeout(long t) {
            ticket = t;
        }
        
        @Override
        public long getDelay(TimeUnit tu) {
            return tu.convert(_timeout_ns - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed t) {
            if (t instanceof Timeout) {
                return Long.signum(_timeout_ns - ((Timeout)t)._timeout_ns);
            } else {
                return Long.signum(this.getDelay(TimeUnit.NANOSECONDS) - t.getDelay(TimeUnit.NANOSECONDS));
            }
        }
    }
    
    public synchronized void put(long ticket, FunctionObserver obs) {
        _tickets.put(ticket, obs);
        _ticketTimeouts.put(new Timeout(ticket));
    }
    
    public synchronized FunctionObserver remove(long ticket) {
        // TODO: Optimize removal to disable timeouts
        return _tickets.remove(ticket);
    }
    
    /**
     * Allows the synchronized removal of tickets that have timed out from the
     * internal delay queue.
     * 
     * @param ticket the ticket that should be removed
     */
    private synchronized void timeout(long ticket) {
        FunctionObserver obs = _tickets.remove(ticket);
        if (obs != null)
            obs.failed(FunctionObserver.FunctionError.TIMEOUT);
    }
    
    public void shutdown() {
        isRunning.set(false);
        timeoutThread.interrupt();
    }
}
