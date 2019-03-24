package com.macrokeys.netcode;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import com.macrokeys.MacroSetup;
import com.macrokeys.comunication.MacroServer;
import com.macrokeys.comunication.MessageProtocol;

/** Seerver that exectues the macro sent by {@link MacroNetClient} */
public final class MacroNetServer extends MacroServer {
	
	/** Charset used for the comunication with the client */
	public static final Charset CHARSET_CLIENT = StandardCharsets.UTF_8;
    
	
	
	
	/** name of the server
	 * <p>
	 * Used for the identification of this server when doind the SSDP
	 */
	private static final String serverName = serverName();
	
	/** Indicates whether this server accepts new connections */
	private boolean seekConnection = false;
	
	
	/** Server socket */
	private ServerSocket serverSocket;
	
	/** UDP socket for the SSDP */
	private DatagramSocket udpSocket;
	
	
	/**
	 * @param setup initial setap to use
	 * @throws IOException If an IO error occurs
	 * @throws AWTException If an error occur while initializing {@link Robot}
	 */
	public MacroNetServer(@NonNull MacroSetup setup)
			throws IOException, AWTException {
		super(setup);
	}
	
	
	@Override
	protected void innerStart() throws IOException {
		serverSocket = new ServerSocket(NetStatic.PORT);
		seekConnection = true;
	}
	
	
	
	@Override
	protected void introduceServerToClient() {
		// Creating the message for the client
		final byte[] sendData = new byte[NetStatic.SSDP_SERVER_KEY.length 
		                                 + NetStatic.SSDP_NAME_LENGTH];
		final byte[] serverNameBytes = stringToByteWithEnding(serverName);
		
		// Identification key for the serv er
		System.arraycopy(NetStatic.SSDP_SERVER_KEY, 0, sendData, 0, NetStatic.SSDP_SERVER_KEY.length);
		
		// If the name of the server is too long is truncated
		int nameLength = Math.min(serverNameBytes.length, NetStatic.SSDP_NAME_LENGTH);
		
		// Name of the server
		System.arraycopy(serverNameBytes, 0, sendData, NetStatic.SSDP_SERVER_KEY.length, nameLength);
		
		int localPort = serverSocket.getLocalPort();
		
		try {
			udpSocket = new DatagramSocket(localPort);
			
			while(true) {
				byte[] data = new byte[NetStatic.SSDP_CLIENT_KEY.length];
				DatagramPacket receve = new DatagramPacket(data, data.length);
				udpSocket.receive(receve);
				SocketAddress client = receve.getSocketAddress();
				
				// CHecks is the application client
				if(Arrays.equals(receve.getData(), NetStatic.SSDP_CLIENT_KEY)) {
					DatagramPacket send = new DatagramPacket(sendData, sendData.length, client);
					udpSocket.send(send);
				}
			}
		} catch (IOException e) {
			
		} finally {
			if(udpSocket != null) {
				udpSocket.close();
			}
		}
		
		/*
		//Compongo il messaggio di risposta per i client
		final byte[] sendData = new byte[NetStatic.SSDP_SERVER_KEY.length 
		                                 + NetStatic.SSDP_NAME_LENGTH];
		final byte[] serverNameBytes = stringToByteWithEnding(serverName);
		// Chiave di identificazione lato server
		System.arraycopy(NetStatic.SSDP_SERVER_KEY, 0, sendData, 0, NetStatic.SSDP_SERVER_KEY.length);
		// In caso il nome del server sia troppo lungo lo tronco
		int nameLength = Math.min(serverNameBytes.length, NetStatic.SSDP_NAME_LENGTH);
		// Nome del server
		System.arraycopy(serverNameBytes, 0, sendData, NetStatic.SSDP_SERVER_KEY.length, nameLength);

		multicastSocket = null;
		try {
			int localPort = socket.getLocalPort();
			multicastSocket = new MulticastSocket(localPort);
			InetAddress group = InetAddress.getByName(NetStatic.MULTICAST_ADDR);
			multicastSocket.joinGroup(group);
			while (!socket.isClosed()) {
				byte[] data = new byte[NetStatic.SSDP_CLIENT_KEY.length];
				DatagramPacket receve = new DatagramPacket(data, data.length);
				multicastSocket.receive(receve);
				SocketAddress client = receve.getSocketAddress();
				// Controllo che è il client per l'applicazione
				if (Arrays.equals(receve.getData(), NetStatic.SSDP_CLIENT_KEY)) {
					DatagramPacket send = new DatagramPacket(sendData, sendData.length, client);
					multicastSocket.send(send);
				}
			}
			multicastSocket.leaveGroup(group);
		} catch (IOException e) {

		} finally {
			multicastSocket.close();
		}*/
	}




	@Override
	protected MessageProtocol waitNewClientConnection() throws IOException {
        final Socket s = serverSocket.accept();
        TCPMessageProtocol messProt = new TCPMessageProtocol(s);
        messProt.setTcpNoDelay(true);
        return messProt;
	}
	

	
	/**
	 * Converts a string in bytes in the format given by {@link #CHARSET_CLIENT}.
	 * The string will terminate with a {@code '\0'}
	 * @param s String to convert
	 * @return byte sequence of the string it terminates with a {@code '\0'}
	 */
	private static byte[] stringToByteWithEnding(String s) {
		Objects.requireNonNull(s);
		
		byte[] b = s.getBytes(CHARSET_CLIENT);
		int i = b.length - 1;
		while(i >= 0 && (char)b[i] == '\0') {
			i--;
		}
		
		byte[] r = new byte[i + 1 + 1];
		System.arraycopy(b, 0, r, 0, i + 1);
		r[r.length - 1] = '\0';
		return r;
	}

	/**
	 * @return Name of the server
	 */
	public static String serverName() {
		String name;
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch(IOException e) {
			name = "Unknown";
		}
		
		return name;
	}
	
	
	/**
	 * @return IP addres and port of this server
	 */
	public String getSocketInfo() {
		return serverSocket.getLocalSocketAddress().toString();
	}
	

	@Override
	public boolean isSeekConnection() {
		return seekConnection;
	}
	

	@Override
	public void setSeekConnection(boolean b) {
		seekConnection = b;
	}
	
	@Override
	protected void innerClose() {
		try {
			if(serverSocket != null) {
				serverSocket.close();
			}
			if(udpSocket != null) {
				udpSocket.close();
			}
		} catch (IOException e) { }
	}
}
