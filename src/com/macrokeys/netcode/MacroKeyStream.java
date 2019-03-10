package com.macrokeys.netcode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;
import com.macrokeys.MacroKey;

/**
 * Class that abstracts the communication layer TCP for communication
 * between the client and the server for sending of the macro
 */
final class MacroKeyStream {

    private final Socket socket;

    private final DataOutputStream outStr;

    private final DataInputStream inStr;

    /**
     * @param s the Socket for the communication
     * @throws IOException In case of IO error
     */
    public MacroKeyStream(Socket s) throws IOException {
        Objects.requireNonNull(s);
        this.socket = s;
        OutputStream str = socket.getOutputStream();
        this.outStr = new DataOutputStream(str);
        this.inStr = new DataInputStream(socket.getInputStream());
    }

    /**
     * Sends a message to keep the connection.
     * To avoid the timeout of the server.
     * @throws IOException In case of IO error
     */
    public void sendKeepAlive() throws IOException {
        // Flag for the start of a
        outStr.writeBoolean(true);
    }

    /**
     * Notify the server of the execution of the action
     * @param mk the Key subject to the action
     * @param action the Action relative to the key. True: pressed, False: released
     * @throws IOException In case of IO error
     */
    public void sendKeyAction(MacroKey mk, boolean action) throws IOException {
        outStr.writeBoolean(false);
        outStr.writeInt(mk.getId());
        outStr.writeBoolean(action);
    }

    /**
     * Waits to receive the next action of a button
     * @return key Action
     * @throws IOException In case of IO error
     */
    public MacroKeyAction receiveKeyAction() throws IOException {
        boolean messageFlag;
        do {
            // I read the type of message
            messageFlag = inStr.readBoolean();
        } while (messageFlag);
        int id = inStr.readInt();
        boolean ac = inStr.readBoolean();
        return new MacroKeyAction(id, ac);
    }

    public class MacroKeyAction {

        public final boolean action;

        public final int macroKeyId;

        public MacroKeyAction(int macroKeyId, boolean action) {
            this.macroKeyId = macroKeyId;
            this.action = action;
        }
    }
}
