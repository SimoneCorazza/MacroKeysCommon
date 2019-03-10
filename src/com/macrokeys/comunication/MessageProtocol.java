package com.macrokeys.comunication;

import java.io.IOException;

/**
 * Interface that allows you to communicate via messages.
 * <p>Allows you to set the timeout for the communication.
 * Timeouts are counted only for the execution of a read:
 * {@link #receiveMessage()}. <br / >
 * The purpose of the timeout is to verify that the communication channel is
 * still active.
 * If the underlying layer (TCP, UDP, Bluetooth, ...) will automatically detect the
 * connection is lost, the implementation can fail to implement the
 * feature timeout.
 * </p>
 */
public interface MessageProtocol {

    /**
     * @return True if the connection is active and working, False otherwise.
     */
    boolean isConnected();

    /**
     * Sets the timeout for the stream of input data.
     * This control is performed only during a
     * call to {@link #receiveMessage()}.
     * @param time Time of the timeout, in milliseconds
     */
    void setInputKeepAlive(int time);

    /**
     * @return the Timeout for the stream of input data
     */
    int getInputKeepAlive();

    /**
     * Sets the timeout of the data stream in output
     * @param time Time of the timeout, in milliseconds
     */
    void setOutputKeepAlive(int time);

    /**
     * @return the Timeout for the flow of data in the output
     */
    int getOutputKeepAlive();

    /**
     * Send a message. Synchronous call
     * @param payload the Content of the message, even empty
     * @throws IOException If there is an IO error
     */
    void sendMessage(byte[] payload) throws IOException;

    /**
     * Awaits the receipt of a message
     * @return Peyload of the message
     * @throws IOException If there is an IO error
     */
    byte[] receiveMessage() throws IOException;

    /**
     * Closes the connection and terminates the transmission
     * @throws IOException In case of error
     */
    void close() throws IOException;
}
