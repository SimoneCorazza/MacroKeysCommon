package com.macrokeys.comunication;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import com.macrokeys.MacroKey;
import com.macrokeys.MacroKeyType;
import com.macrokeys.LimitedKeySequence;
import com.macrokeys.MacroSetup;

/**
 * Abstract class for a server that sends the {@link MacroSetup} to the {@link MacroClient} and receives a
 * {@link MacroKey} pressed
 */
public abstract class MacroServer {

    /**
     * The actuator of the pressure
     */
    private final KeyPresser keyPresser;

    /**
     * Listeners to the events of {@code this}
     */
    private final List<EventListener> eventListeners = new ArrayList<>();

    /**
     * {@link MacroKey} currently pressed (key down)
     */
    private final HashMap<MacroKey, KeyDown> pressedKeys = new HashMap<>();

    /**
     * The Thread for the keys of type {@link MacroKeyType#Normal}
     */
    private final Thread normalPresser = new Thread(new ThreadRunnable());

    /**
     * Indicates if the server is in sleep mode
     */
    private boolean suspend = false;

    /**
     * Setup currently used
     */
    private MacroSetup setup;

    /**
     * Data representing the {@link MacroSetup} that is currently used,
     * available with {@link #setup}
     */
    private byte[] macroSetupData;

    /**
     * The Thread that accepts the new connections from the client
     */
    private Thread threadListener;

    /**
     * The Thread that informs the client of the service offered by the server
     */
    private Thread threadIntroduce;

    /**
     * Collection of connected clients
     */
    private final ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();

    /**
     * The current status of the server
     */
    private State state = State.WaitStart;

    /**
     * @param setup setup to use initially
     */
    public MacroServer(MacroSetup setup) throws AWTException {
        Objects.requireNonNull(setup);
        this.setup = setup;
        try {
            this.macroSetupData = setup.saveAsByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Should not happend";
        }
        keyPresser = new KeyPresser();
    }

    @Override
    protected void finalize() throws Throwable {
        // I interrupt the thread to the pressures
        normalPresser.interrupt();
        // Release all the pressed keys
        for (MacroKey k : pressedKeys.keySet()) {
            releaseKey(k);
        }
    }

    /**
     * Starts up the server
     * @throws IOException In case of an error of I in the initialization
     * @throws IllegalStateException If this is not in the state
     * {@link State#WaitStart}
     * @see #getState()
     */
    public final void start() throws IOException {
        if (!getState().equals(State.WaitStart)) {
            throw new IllegalStateException();
        }
        innerStart();
        normalPresser.start();
        threadListener = new Thread() {

            @Override
            public void run() {
                newClientListener();
            }
        };
        threadIntroduce = new Thread() {

            @Override
            public void run() {
                introduceServerToClient();
            }
        };
        threadListener.start();
        threadIntroduce.start();
        state = State.Functional;
    }

    /**
     * Initializes the server, putting it in the listening of the client
     * @throws IOException In case of an error of I in the initialization
     */
    protected abstract void innerStart() throws IOException;

    /**
     * @return current State of the server
     */
    public final State getState() {
        return state;
    }

    /**
     * Allows the client to know that this car offers
     * service of the Server.
     * <p>
     * Run on a separate thread is appropriate.
     * </p>
     */
    protected abstract void introduceServerToClient();

    /**
     * Allows to wait for new client connettino.
     * <p>
     * Executed on thread {@link #threadListener}
     * </p>
     */
    private void newClientListener() {
        while (!threadListener.isInterrupted()) {
            final MessageProtocol messProt;
            try {
                messProt = waitNewClientConnection();
            } catch (IOException e) {
                continue;
            }
            // If the server does not search for connections I close it
            if (!isSeekConnection()) {
                try {
                    messProt.close();
                } catch (IOException e) {
                    continue;
                }
            }
            // Imposed the timeout for the communication
            messProt.setInputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
            messProt.setOutputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
            // Do I login as the client and I get the id of the
            // client
            String clientId;
            try {
                clientId = loginClient(messProt);
            } catch (IOException e) {
                continue;
            }
            // In case the client is already logged in
            if (clients.containsKey(clientId)) {
                continue;
            }
            final ClientInfo info = new ClientInfo(clientId, messProt);
            clients.put(clientId, info);
            // I start the thread to serve the client
            Thread th = new Thread() {

                @Override
                public void run() {
                    serveClient(info);
                }
            };
            th.start();
            // Son-in-law the event of the connection of a new client
            // so as not to burden this thread and avoid
            // that is interrupted due to exceptions
            Thread startEvents = new Thread() {

                @Override
                public void run() {
                    fireClientConnectListener(info.clientId);
                }
            };
            startEvents.start();
        }
    }

    /**
     * Server the cleint indicated. In particular, handles exceptions
     * {@link #ServeClientExceptions(ClientInfo)}
     * @param info
     * @see #ServeClientExceptions(ClientInfo)
     */
    private void serveClient(ClientInfo info) {
        try {
            serveClientExceptions(info);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                info.messProt.close();
            } catch (IOException e) {
            }
            // I remove the infirmazioni of the client from the list
            clients.remove(info.clientId);
            // Release the keys pressed by the client that has disconnected
            for (Map.Entry<MacroKey, KeyDown> p : pressedKeys.entrySet()) {
                MacroKey mk = p.getKey();
                KeyDown k = p.getValue();
                assert mk != null && k != null;
                if (k.clientId.equals(info.clientId)) {
                    releaseKey(mk);
                }
            }
            // Son-in-law in the event of a disconnection
            fireClientDisconnectListener(info.clientId);
        }
    }

    /**
     * Performs the login of the client
     * @param messProt Level communication messages with the client
     * @return Id of the client
     */
    private String loginClient(MessageProtocol messProt) throws IOException {
        // TODO: implement safety and identification of the cleint
        return messProt.toString();
    }

    /**
     * Manages the input of the client indicated
     * @param info Information about the client to manage
     * @throws IOException In case of IO error
     */
    private void serveClientExceptions(ClientInfo info) throws IOException {
        assert info != null;
        assert getMacroSetup() != null;
        // Enter the macro setup
        byte[] macroSetupData = getMacroSetupData();
        assert macroSetupData != null;
        info.messProt.sendMessage(macroSetupData);
        // I get requests to make macro
        while (info.messProt.isConnected()) {
            byte[] mess = info.messProt.receiveMessage();
            ByteArrayInputStream str = new ByteArrayInputStream(mess);
            DataInputStream dataStr = new DataInputStream(str);
            int macroID = dataStr.readInt();
            boolean state = dataStr.readBoolean();
            dataStr.close();
            str.close();
            // Esecizione of the macro:
            MacroSetup setup = getMacroSetup();
            synchronized (setup) {
                MacroKey k = setup.macroKeyFromID(macroID);
                assert k != null && k.getId() == macroID : "MacroID has not been found correctly: " + macroID;
                if (state) {
                    pressKey(k, info.clientId);
                } else {
                    releaseKey(k);
                }
            }
        }
    }

    /**
     * Waits for the connection of a new client.
     * @return the Level of the communication messages with the new client
     */
    protected abstract MessageProtocol waitNewClientConnection() throws IOException;

    /**
     * Sets the flag that indicates if the Server will run the macro received
     * @param s New state of the flag
     * @throws IllegalStateException If this is not in the state
     * {@link State#Functional}
     * @see #getState()
     */
    public final void setSuspend(boolean s) {
        if (!getState().equals(State.Functional)) {
            throw new IllegalStateException();
        }
        if (suspend != s) {
            suspend = s;
            if (suspend) {
                // Release all the pressed keys
                for (MacroKey k : pressedKeys.keySet()) {
                    releaseKey(k);
                }
                pressedKeys.clear();
            }
            fireServerSuspendEvent(s);
        }
    }

    /**
     * @return True if the Server is not running, the more the pressures of the macro received
     */
    public final boolean isSuspended() {
        return suspend;
    }

    /**
     * @return True if the Server accepts new connections
     */
    public abstract boolean isSeekConnection();

    /**
     * Sets the flag that indicates if the Server accepts new connections
     * @param b the New state of the flag
     */
    public abstract void setSeekConnection(boolean b);

    /**
     * Closes the server, interrupting the task and changing the status.
     * @throws IllegalStateException If this is not in the state {@link State#Functional}
     * @see #getState()
     */
    public final void close() {
        if (!getState().equals(State.Functional)) {
            throw new IllegalStateException();
        }
        // I close all the connections
        for (ClientInfo s : clients.values()) {
            try {
                s.messProt.close();
            } catch (IOException e) {
            }
        }
        threadIntroduce.interrupt();
        threadListener.interrupt();
        normalPresser.interrupt();
        innerClose();
        state = State.Closed;
        fireServerCloseEvent();
    }

    /**
     * Implements the closing of the server.
     * Called by {@link #close()}
     */
    protected abstract void innerClose();

    /**
     * Allows you to change the {@link MacroSetup} current
     * <p>synchronous Method
     */
    public final void changeMacroSetup(MacroSetup m) {
        Objects.requireNonNull(m);
        assert setup != null;
        this.setup = m;
        try {
            this.macroSetupData = setup.saveAsByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Should not happend";
        }
        // Sending to the client the new MacroSetup
        // 
        byte[] macroSetupData = getMacroSetupData();
        for (ClientInfo p : clients.values()) {
            try {
                p.messProt.sendMessage(macroSetupData);
            } catch (IOException e) {
            // Nothing
            }
        }
        fireServerChangeMacroSetup(m);
    }

    /**
     * @return Ottiele the setup currently used
     */
    public final MacroSetup getMacroSetup() {
        return this.setup;
    }

    /**
     * @return the binary Representation of the {@link MacroSetup} currently
     * selected (that can be obtained with {@link #getMacroSetup()})
     */
    protected final byte[] getMacroSetupData() {
        return macroSetupData;
    }

    /**
     * Press the {@link MacroKey} if the flag {@link #isSuspended()} is false
     * @param mk the Key you press
     */
    private boolean pressKey(MacroKey mk, String clientId) {
        assert mk != null;
        assert clients.containsKey(clientId);
        assert getState().equals(State.Functional);
        // If the suspended or already hold, nor have the pressure
        if (suspend || pressedKeys.containsKey(mk)) {
            return false;
        }
        switch(mk.getType()) {
            case Game:
                keyPresser.press(mk.getKeySeq());
                break;
            case Normal:
                break;
            case OnRelease:
                break;
            default:
                assert false : "Unkown case";
                break;
        }
        // I indicate the button as pressed
        pressedKeys.put(mk, new KeyDown(clientId, mk.getType()));
        fireKeyRecivedListener(clientId, mk, true);
        return true;
    }

    /**
     * Releases the {@link MacroKey}.
     * Not influenced by the flags {@link #isSuspended()}.
     * @param mk the Key to be release
     */
    protected boolean releaseKey(MacroKey mk) {
        assert mk != null;
        assert getState().equals(State.Functional);
        if (pressedKeys.containsKey(mk)) {
            if (mk.getType() == MacroKeyType.Game) {
                keyPresser.release(mk.getKeySeq());
            } else if (mk.getType() == MacroKeyType.OnRelease) {
                pressAndReleaseKey(mk);
            }
            String clientId = pressedKeys.get(mk).clientId;
            pressedKeys.remove(mk);
            fireKeyRecivedListener(clientId, mk, false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Press and release the {@link MacroKey}.
     * Not influenced by the flags {@link #suspend}.
     * @param mk the Key on which to perform the action
     */
    private void pressAndReleaseKey(MacroKey mk) {
        assert mk != null;
        assert getState().equals(State.Functional);
        LimitedKeySequence l = mk.getKeySeq();
        keyPresser.press(l);
        keyPresser.release(l);
    }

    /**
     * Information relating to a client currently connected
     */
    private final static class ClientInfo {

        private final String clientId;

        private final MessageProtocol messProt;

        /**
         * @param clientId Id of the client
         * @param p the Layer of communication with the client to messages
         */
        public ClientInfo(String clientId, MessageProtocol p) {
            if (clientId == null || p == null) {
                throw new NullPointerException();
            }
            this.clientId = clientId;
            this.messProt = p;
        }
    }

    private class ThreadRunnable implements Runnable {

        /**
         * Time to wait in ms before considering a long press on a key
         * as of the pressures
         */
        final static int KEY_REPEAT_WAIT = 750;

        /**
         * The number of keystrokes sent per second
         */
        final static int KEY_FREQ = 30;

        /**
         * Waiting period (in ms) before acknowledging the peressione in a row and a key
         */
        final static int KEY_SLEEP = 1000 / KEY_FREQ;

        /**
         * Time the rest of the tread between a pass key and the other
         */
        final int FREE_TIME = Math.max(1, Math.min(KEY_REPEAT_WAIT, KEY_SLEEP) / 2);

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(FREE_TIME);
                } catch (InterruptedException e) {
                    return;
                }
                for (Map.Entry<MacroKey, KeyDown> p : pressedKeys.entrySet()) {
                    MacroKey k = p.getKey();
                    KeyDown u = p.getValue();
                    assert k != null && u != null;
                    if (u.type == MacroKeyType.Normal) {
                        long d = System.currentTimeMillis() - u.lastDownTime;
                        assert d >= 0;
                        // Has not been pressed for the first time
                        if (!u.firstDown) {
                            u.firstDown = true;
                            u.lastDownTime = System.currentTimeMillis();
                            pressAndReleaseKey(k);
                        // It was down for the first time, look before repeating the pressure
                        } else if (!u.firsPressOccurred && d >= KEY_REPEAT_WAIT) {
                            u.firsPressOccurred = true;
                            u.lastDownTime = System.currentTimeMillis();
                            pressAndReleaseKey(k);
                        // Look for the pressures later
                        } else if (u.firsPressOccurred && d >= KEY_SLEEP) {
                            u.lastDownTime = System.currentTimeMillis();
                            pressAndReleaseKey(k);
                        }
                    }
                }
            }
        }
    }

    private class KeyDown {

        /**
         * Id of the client who has pressed the button
         */
        private String clientId;

        /**
         * The type of pressure associated with
         */
        private MacroKeyType type;

        /**
         * Moment of time, in milliseconds, in which the request
         * pressure was made
         */
        private long lastDownTime = 0;

        /**
         * True: indicates if the button has already been pressed for the first time
         */
        private boolean firstDown = false;

        private boolean firsPressOccurred = false;

        private KeyDown(String clientId, MacroKeyType type) {
            this.clientId = clientId;
            this.type = type;
        }
    }

    // --- LISTENERS ---
    /**
     * Adds a listener for the completion of an action
     * @param l the Listener to add
     * @throws NullPointerException If {@code l} is null
     */
    public final void addEventListener(EventListener l) {
        Objects.requireNonNull(l);
        synchronized (eventListeners) {
            eventListeners.add(l);
        }
    }

    /**
     * Removes all instances of the listener indicated
     * @param l the Listener to remove
     * @throws NullPointerException If {@code l} is null
     */
    public final void removeEventListener(EventListener l) {
        Objects.requireNonNull(l);
        synchronized (eventListeners) {
            removeInstanceOf(eventListeners, l);
        }
    }

    /**
     * Removes all instances of {@code instance} in {@code list}
     * @param list the List from which to remove instances
     */
    private static <T> void removeInstanceOf(List<T> list, T instance) {
        assert list != null;
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (it.next() == instance) {
                it.remove();
            }
        }
    }

    /**
     * * * Generate the event key is pressed or released
     * @param source Id of the client that sent the action
     */
    private void fireKeyRecivedListener(String source, MacroKey mk, boolean action) {
        assert source != null;
        assert mk != null;
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.onKeyReceved(this, source, mk, action);
            }
        }
    }

    /**
     * Generates the connection event from a client
     * @param s the Identifier of the connected client
     */
    private void fireClientConnectListener(String s) {
        assert s != null;
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.onConnectListener(this, s);
            }
        }
    }

    /**
     * Generates the connection event from a client
     * @param s the Identifier of the cleint
     */
    private void fireClientDisconnectListener(String s) {
        assert s != null;
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.onDisconnectListener(this, s);
            }
        }
    }

    /**
     * * * Generate the closing event of the server
     */
    private void fireServerCloseEvent() {
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.onClose(this);
            }
        }
    }

    /**
     * Generates the event of a change in the status of suspension of the server
     * @param newState the New state of suspension
     */
    private void fireServerSuspendEvent(boolean newState) {
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.onSuspendChanged(this, newState);
            }
        }
    }

    /**
     * Generates the changed event of the {@link MacroSetup}
     * @param actual the New {@link MacroSetup} used, null if none
     */
    private void fireServerChangeMacroSetup(MacroSetup actual) {
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.onMacroSetupChanged(this, actual);
            }
        }
    }

    /**
     * Listener for events associated with the server
     */
    public interface EventListener {

        /**
         * Generated upon reception of a key
         * @param server the server Instance that generated the event
         */
        void onKeyReceved(MacroServer server, String sender, MacroKey mk, boolean action);

        /**
         * Connection of a new client
         * @param server the server Instance that generated the event
         */
        void onConnectListener(MacroServer server, String sender);

        /**
         * To disconnect a client
         * @param server the server Instance that generated the event
         */
        void onDisconnectListener(MacroServer server, String sender);

        /**
         * The Server closes the connection
         * @param server the server Instance that generated the event
         */
        void onClose(MacroServer server);

        /**
         * The Server is suspended
         * @param server the server Instance that generated the event
         */
        void onSuspendChanged(MacroServer server, boolean newState);

        /**
         * Server changes the {@link MacroSetup} used
         * @param server the server Instance that generated the event
         */
        void onMacroSetupChanged(MacroServer server, MacroSetup actual);
    }

    /**
     * Current status of the server
     */
    public enum State {

        /**
         * The Server waits for the startup via the method {@link MacroServer#start()}
         */
        WaitStart,
        /**
         * The server works regularly
         */
        Functional,
        /**
         * The server has been closed
         */
        Closed
    }
}
