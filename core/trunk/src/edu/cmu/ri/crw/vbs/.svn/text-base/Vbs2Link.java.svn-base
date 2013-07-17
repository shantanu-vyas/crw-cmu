/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.vbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

/**
 * A simple communications class that talks to VBS2 over a socket using the
 * TcpBridge plugin.
 * 
 * @author pkv
 */
public class Vbs2Link {
    private static final Logger logger = Logger.getLogger(Vbs2Link.class.getName());

    private static final Charset DEFAULT_CHARSET = Charset.forName("US-ASCII");
    private static final byte[] DEFAULT_LINE_ENDING = "\r\n".getBytes(DEFAULT_CHARSET);
    private static final String EVENT_REGEX = "^#.*";
    private static final int INVALID_TICKET = -1;

    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 6223;
    public static final int DEFAULT_TIMEOUT = 2000;

    private final LinkedHashMap<Integer, String> responses = new LinkedHashMap<Integer, String>();
    private final ConcurrentLinkedQueue<Integer> ignoreList = new ConcurrentLinkedQueue<Integer>();
    private final EventListenerList listenerList = new EventListenerList();
    private MessageEvent msgEvent = null;

    private Socket sock;
    private BufferedReader in;
    private OutputStream out;

    private int sendSeq = 0;
    private int recvSeq = 0;
    private final Object sendLock = new Object();
    private final Object receiveLock = new Object();

    public void connect() {
        connect(DEFAULT_HOSTNAME, DEFAULT_PORT);
    }

    public void connect(String hostname) {
        connect(hostname, DEFAULT_PORT);
    }

    public void connect(int port) {
        connect(DEFAULT_HOSTNAME, port);
    }

    public void connect(String hostname, int port) {
        if (isConnected()) return;

        try {
            sock = new Socket();
            sock.connect(new InetSocketAddress(hostname, port), DEFAULT_TIMEOUT);
            out = sock.getOutputStream();
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            new Thread(new Listener()).start();
        } catch (SocketTimeoutException e) {
            logger.info("Failed to connect to VBS2.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to open socket: {0}", e);
        }
    }

    public void disconnect() {
        if (!isConnected()) return;

        try { sock.close(); } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close socket: {0}", e);
        }

        try { out.close(); } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close output stream: {0}", e);
        }

        try { in.close(); } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close input stream: {0}", e);
        }
    }

    public boolean isConnected() {
        return (sock != null)
                && (sock.isConnected())
                && (!sock.isClosed());
    }

    protected int sendInternal(final String cmd) {
        if (!isConnected())
            throw new IllegalStateException("Not connected to server.");

        synchronized(sendLock) {
            byte[] b = cmd.getBytes(DEFAULT_CHARSET);

            try {
                out.write(b);
                out.write(DEFAULT_LINE_ENDING);
                out.flush();
                logger.log(Level.FINE, "SENT: {0}", cmd);
                return (++sendSeq);
            } catch (IOException e){
                logger.log(Level.SEVERE, "Failed to transmit string ''{0}'': {1}", new Object[]{cmd, e});
                return INVALID_TICKET;
            }
        }
    }

    protected String recvInternal(final int ticket) {
        if (ticket == -1)
            return "ERROR: NO TICKET";

        try {
            synchronized (receiveLock) {
                while (!responses.containsKey(ticket)) {
                    receiveLock.wait();
                }
                return responses.remove(ticket);
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "No response to: {0}", ticket);
            return "ERROR: NO RESPONSE";
        }
    }

    public void send(final String cmd) {
        if (!isConnected())
            throw new IllegalStateException("Not connected to server.");

        final int ticket = sendInternal(cmd);
        ignoreList.add(ticket);
    }

    public String evaluate(final String cmd) {
        if (!isConnected()) throw new IllegalStateException("Not connected to server.");

        final int ticket = sendInternal(cmd);
        return recvInternal(ticket);
    }
    
    public void addMessageListener(MessageListener l) {
        listenerList.add(MessageListener.class, l);
    }

    public void removeMessageListener(MessageListener l) {
        listenerList.remove(MessageListener.class, l);
    }

    protected void fireMessage(String msg) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MessageListener.class) {
                // Lazily create the event
                if (msgEvent == null) msgEvent = new MessageEvent(this, msg);
                ((MessageListener)listeners[i+1]).receivedMessage(msgEvent);
            }
        }

        // Release reference to object
        msgEvent = null;
    }

    public interface MessageListener extends EventListener {
        public void receivedMessage(MessageEvent evt);
    }

    public class MessageEvent extends EventObject {
        private static final long serialVersionUID = 1L;
		private final String message;

        public MessageEvent(Vbs2Link src, String msg) {
            super(src);
            message = msg;
        }

        public String getMessage() { return message; }

        @Override
        public String toString() { 
            return super.toString() + "[message=" + message + "]";
        }
    }


    private class Listener implements Runnable {
        public void run() {
            try {
                while (isConnected()) {
                    String line = in.readLine();
                    if (line.matches(EVENT_REGEX)) {
                        fireMessage(line);
                    } else {
                        synchronized (receiveLock) {
                            
                            // Insert new line into receiving queue
                            logger.log(Level.FINE, "RECV: {0}", line);
                            responses.put((++recvSeq), line);

                            // Take opportunity to check for ignorable responses
                            if (!ignoreList.isEmpty()) {
                                for (int i : ignoreList)
                                    responses.remove(i);
                            }

                            // Notify listeners that a command has completed
                            receiveLock.notifyAll();
                        }
                    }
                }
            } catch (SocketException e) {
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to read from socket: {0}", e);
            }
        }
    }
}
