package com.macrokeys.comunication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import com.macrokeys.MSLoadException;
import com.macrokeys.MacroKey;
import com.macrokeys.MacroSetup;
import com.macrokeys.netcode.ConnectionNotSetException;

/**
 * Abstract class that receive {@link MacroSetup} and send the {@link MacroKey} pressed
 */
public abstract class MacroClient {

    /**
     * Identifiers of the actual pressed keys
     */
    private final Set<MacroKey> pressedKeys = new HashSet<>();

    private MessageProtocol messProt;

    /**
     * Current state of the client
     */
    private State state = State.NoConnection;

    /**
     * Connects to the server
     * @throws IOException In case of an IO error
     * @throws IllegalStateException If {@link #getState()} is not {@link State#NoConnection}
     * @see #getState()
     */
    public final void connectToServer() throws IOException {
        if (!getState().equals(State.NoConnection)) {
            throw new IllegalStateException();
        }
        assert this.messProt == null;
        this.messProt = innerConnectToServer();
        messProt.setInputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
        messProt.setOutputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
        state = State.Comunication;
    }

    /**
     * Connects to the server (implementation)
     * @return Communication interface with the server
     */
    protected abstract MessageProtocol innerConnectToServer() throws IOException;

    /**
     * Send the release of the key
     * @param macroKey Key released
     * @throws IOException If there is an IO error
     * @throws IllegalStateException If {@link #getState()} is not {@link State#Communication}
     * @see #getState()
     */
    public synchronized final void keyUp(@NonNull MacroKey macroKey) throws IOException {
        Objects.requireNonNull(macroKey);
        if (!getState().equals(State.Comunication)) {
            throw new IllegalStateException();
        }
        if (pressedKeys.contains(macroKey)) {
            pressedKeys.remove(macroKey);
            sendActionKey(macroKey, false);
        }
    }

    /**
     * Send the pression of the key
     * @param macroKey Key pressed
     * @throws IOException If there is an IO error
     * @throws IllegalStateException If {@link #getState()} is not {@link State#Communication}
     * @see #getState()
     */
    public synchronized final void keyDown(@NonNull MacroKey macroKey) throws IOException {
        Objects.requireNonNull(macroKey);
        if (!getState().equals(State.Comunication)) {
            throw new IllegalStateException();
        }
        if (!pressedKeys.contains(macroKey)) {
            pressedKeys.add(macroKey);
            sendActionKey(macroKey, true);
        }
    }

    /**
     * Send an action of the key
     * @param mk the Key subject to the action
     * @param action True: pressed, released otherwise
     * @throws IOException In case of an IO error
     */
    private void sendActionKey(@NonNull MacroKey mk, boolean action) throws IOException {
        Objects.requireNonNull(mk);
        if (isConnected()) {
            ByteArrayOutputStream str = new ByteArrayOutputStream();
            DataOutputStream dataStr = new DataOutputStream(str);
            dataStr.writeInt(mk.getId());
            dataStr.writeBoolean(action);
            messProt.sendMessage(str.toByteArray());
            dataStr.close();
            str.close();
        } else {
            throw new ConnectionNotSetException();
        }
    }

    /**
     * @return Current state of the connection
     */
    public final State getState() {
        return state;
    }

    /**
     * Receive the {@link MacroSetup} sent by the server
     * <p>This is a sync method that waits until the server send the key</p>
     * @return the Setup sent by the server
     * @throws IOException In case of an IO error
     * @throws MSLoadException In case of error in the loading of the {@link MacroSetup}
     * @throws IllegalStateException If {@link #getState()} is not {@link State#Communication}
     * @see #getState()
     */
    public final MacroSetup reciveMacroSetup() throws IOException, MSLoadException {
        if (!isConnected()) {
            return null;
        } else if (!getState().equals(State.Comunication)) {
            throw new IllegalStateException();
        }
        byte[] payload = messProt.receiveMessage();
        ByteArrayInputStream str = new ByteArrayInputStream(payload);
        MacroSetup m = MacroSetup.load(str);
        str.close();
        return m;
    }

    /**
     * @return True if the connection with the server is established
     */
    public final boolean isConnected() {
        return messProt != null && messProt.isConnected();
    }

    /**
     * Close the connection if active
     * @throws IOException If there is an IO error
     * @throws IllegalStateException If {@link #getState()} is not {@link State#Communication}
     * @see #getState()
     */
    public final void close() throws IOException {
        if (!getState().equals(State.Comunication)) {
            throw new IllegalStateException();
        }
        messProt.close();
        state = State.Closed;
    }

    /**
     * The State of the client
     */
    public enum State {

        /**
         * The connection with the server was not established yet
         */
        NoConnection,
        /**
         * The connection with the server is established
         */
        Comunication,
        /**
         * The connection with the server is closed
         */
        Closed
    }
}
