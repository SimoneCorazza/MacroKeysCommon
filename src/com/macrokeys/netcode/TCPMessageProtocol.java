package com.macrokeys.netcode;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import org.eclipse.jdt.annotation.NonNull;

import com.macrokeys.comunication.MessageProtocol;

/** 
 * Implementation of the TCP protocol {@link MessageProtocol}
 */
public class TCPMessageProtocol implements MessageProtocol {
	
	private static final byte CODE_MESSAGE = 0x00;
	private static final byte CODE_KEEP_ALIVE = 0x01;
	
	/** Socket to use */
	private final Socket socket;
	
	/** Input stream from the {@link #socket} */
	private final DataInputStream inStr;
	
	/** Output stream from the {@link #socket} */
	private final DataOutputStream outStr;
	
	/** Thread for the keep alive */
	private Thread threadKeepalive = null;
	
	/** Timeout for the input stream; 0 for infinity, always > 0 */
	private int timeoutInput = 0;
	
	/** Messages sent in the last period*/
	private int messageSent = 0;
	
	/** Semaphore for the socket */
	private Semaphore sem = new Semaphore(1, true);
	
	/**
	 * @param socket Socket
	 * @throws IOExceptionIf an IO error occurs
	 */
	public TCPMessageProtocol(@NonNull Socket socket) throws IOException {
		Objects.requireNonNull(socket);
		
		this.socket = socket;
		
		this.inStr = new DataInputStream(socket.getInputStream());
		this.outStr = new DataOutputStream(socket.getOutputStream());
	}
	
	
	/**
	 * Sets the TCP no delay for the underlayng socket
	 * @param flag True enable, false otherwise
	 * @throws IOException If an IO error occurs
	 * @see Socket#setTcpNoDelay(boolean)
	 */
	public void setTcpNoDelay(boolean flag) throws IOException {
		socket.setTcpNoDelay(flag);
	}
	
	
	@Override
	public void setInputKeepAlive(int time) {
		if(time < 0) {
			throw new IllegalArgumentException("Time must be >= 0");
		}
		timeoutInput = time;
		
		if(time == 0 && threadKeepalive != null) {
			threadKeepalive.interrupt();
			threadKeepalive = null;
		} else {
			if(threadKeepalive != null) {
				threadKeepalive.interrupt();
			}
			threadKeepalive = new Thread(new ThreadKeepAlive());
			threadKeepalive.start();
		}
	}

	@Override
	public void setOutputKeepAlive(int time) {
		if(time < 0) {
			throw new IllegalArgumentException("Time must be >= 0");
		}
		try {
			socket.setSoTimeout(time);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public int getInputKeepAlive() {
		return timeoutInput;
	}



	@Override
	public int getOutputKeepAlive() {
		try {
			return socket.getSoTimeout();
		} catch (SocketException e) {
			// In case of error return infinite timeout
			return 0;
		}
	}

	@Override
	public void sendMessage(byte[] payload) throws IOException {
		Objects.requireNonNull(payload);
		
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			return;
		}
		
		outStr.writeByte(CODE_MESSAGE);
		outStr.writeInt(payload.length);
		outStr.write(payload);
		
		messageSent++;
		
		sem.release();
	}

	@Override
	public byte[] receiveMessage() throws IOException {
		byte code = inStr.readByte();
		
		while(code == CODE_KEEP_ALIVE) {
			switch(code) {
			case CODE_MESSAGE:
				break;
				
			case CODE_KEEP_ALIVE:
				break;
				
				default: assert false : "Unkown message code";
			}
			
			code = inStr.readByte();
		}
		
		// Read the rest of the message
		int leng = inStr.readInt();
		byte[] payload = new byte[leng];
		inStr.readFully(payload);
		return payload;
	}



	@Override
	public boolean isConnected() {
		return socket.isConnected() && !socket.isClosed();
	}
	
	
	
	@Override
	public void close() throws IOException {
		socket.close();
	}

	
	
	@Override
	public String toString() {
		assert socket != null;
		
		String s = socket.getInetAddress().toString();
		return s.substring(1);
	}
	
	
	/** Keep alive thread */
	private class ThreadKeepAlive implements Runnable {

		@Override
		public void run() {
			while(true) {
				if(messageSent <= 0) {
					// Send the keep alive message
					try {
						sem.acquire();
						outStr.writeByte(CODE_KEEP_ALIVE);
					} catch(InterruptedException | IOException e) {
						Thread.currentThread().interrupt();
						break;
					} finally {
						sem.release();
					}
					
					// Sleep
					try {
						Thread.sleep(timeoutInput / 2);
					} catch(InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
				
				// Reset the messages
				messageSent = 0;
			}
		}
		
	}
}
