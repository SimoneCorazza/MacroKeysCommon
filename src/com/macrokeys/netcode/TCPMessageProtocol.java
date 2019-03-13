package com.macrokeys.netcode;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import com.macrokeys.comunication.MessageProtocol;

/** 
 * Implementazione TCP del protocollo {@link MessageProtocol}
 */
public class TCPMessageProtocol implements MessageProtocol {
	
	private static final byte CODE_MESSAGE = 0x00;
	private static final byte CODE_KEEP_ALIVE = 0x01;
	
	/** Socket utilizzata */
	private final Socket socket;
	/** Stream in input alla {@link #socket} */
	private final DataInputStream inStr;
	/** Stream in output alla {@link #socket} */
	private final DataOutputStream outStr;
	
	/** Thread per il l'invio dei keep alive */
	private Thread threadKeepalive = null;
	/** Tempo per il timeout dello strem in output; 0 per infinito, sempre > 0 */
	private int timeoutInput = 0;
	/** Messaggi inviati dalla scorsa volta che sono stati inviati nell'ultimo periodo */
	private int messageSent = 0;
	
	/** Semaforo per l'accesso alla socket */
	private Semaphore sem = new Semaphore(1, true);
	
	/**
	 * @param socket Socket
	 * @throws IOException Se c'è un errore nella creazione degli stream per la socket
	 * @throws NullPointerException Se {@code socket} è null
	 */
	public TCPMessageProtocol(Socket socket) throws IOException {
		Objects.requireNonNull(socket);
		
		this.socket = socket;
		
		this.inStr = new DataInputStream(socket.getInputStream());
		this.outStr = new DataOutputStream(socket.getOutputStream());
	}
	
	
	/**
	 * Imposta il TCP no delay per la socket sottostante
	 * @param flag Flag di abilitazione del no delay
	 * @throws IOException Errore di IO
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
			// Caso di errore sottostante ritorno timout infinito
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
		
		// Leggo il resto del messaggio
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
	
	
	/** Thread per la funzione di keep alive */
	private class ThreadKeepAlive implements Runnable {

		@Override
		public void run() {
			while(true) {
				if(messageSent <= 0) {
					// Invio il messaggio
					try {
						sem.acquire();
						outStr.writeByte(CODE_KEEP_ALIVE);
					} catch(InterruptedException | IOException e) {
						break;
					} finally {
						sem.release();
					}
					
					// Sleep
					try {
						Thread.sleep(timeoutInput / 2);
					} catch(InterruptedException e) {
						break;
					}
				}
				messageSent = 0; // Resetto i messaggi
			}
		}
		
	}
}
