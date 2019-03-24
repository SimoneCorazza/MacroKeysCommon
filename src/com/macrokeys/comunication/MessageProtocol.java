package com.macrokeys.comunication;
import java.io.IOException;

/** 
 * Interface that comunicates with messages.
 * <p>This interface permits the set of timeouts. Timeouts are only for read operations: {@link #receiveMessage()}
 * <br>
 * The purpose of timeouts is to assert that the connection is still up and runnging
 * If the underlaying layer (TCP, UDP, Bluetooth, ...) automatically checks if the connection
 * is still active then the implementation can avoid to implements the timeout mechanics
 */
public interface MessageProtocol {
	
	/**
	 * @return True if the connection is up and working, false otherwise
	 */
	boolean isConnected();
	
	/**
	 * Sets the timeout for read operations
	 * The timeout check is executed only after a call to {@link #receiveMessage()}.
	 * @param time Timeout is milliseconds; 0 to disable it
	 * @throws IllegalArgumentException If {@code time} is < 0
	 */
	void setInputKeepAlive(int time);
	
	/**
	 * @return Timeout for read operations; 0 is none
	 */
	int getInputKeepAlive();
	
	/**
	 * Sets the timeout for write operations
	 * The timeout check is executed only after a call to {@link #receiveMessage()}.
	 * @param time Timeout is milliseconds; 0 to disable it
	 * @throws IllegalArgumentException If {@code time} is < 0
	 */
	void setOutputKeepAlive(int time);
	
	/**
	 * @return Timeout for write operations; 0 is none
	 */
	int getOutputKeepAlive();
	
	/**
	 * Send a message. Sincorous call
	 * @param payload Message payload; can be empty
	 * @throws IOException If an IO error occur
	 */
	void sendMessage(byte[] payload) throws IOException;
	
	/**
	 * Wait the reception of a message
	 * @return Payload of the message
	 * @throws IOException If an IO error occur
	 */
	byte[] receiveMessage() throws IOException;
	
	
	/**
	 * Close the connection and terminates the transmission
	 * @throws IOException If an IO error occur
	 */
	void close() throws IOException;
}
