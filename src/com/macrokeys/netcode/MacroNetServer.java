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

import com.macrokeys.MacroSetup;
import com.macrokeys.comunication.MacroServer;
import com.macrokeys.comunication.MessageProtocol;

/** Server che esegue le macro inviate da {@link MacroNetClient} */
public final class MacroNetServer extends MacroServer {
	
	/** Charset utilizzato per la comunicazione con il client */
	public static final Charset CHARSET_CLIENT = StandardCharsets.UTF_8;
    
	
	
	
	/** Nome del Server (usato per l'identificazione di questo Server quando un client esegue un SSDP) */
	private static final String serverName = serverName();
	
	/** Indica se il Server accetta nuove connessioni in arrivo */
	private boolean seekConnection = false;
	
	
	/** Soket del server */
	private ServerSocket serverSocket;
	
	/** Socket per pacchetti UDP relativi al SSDP */
	private DatagramSocket udpSocket;
	
	
	/**
	 * @param setup Setup da utilizzare inizialmente; non null
	 * @throws IOException Se c'è un problema nella creazione del socket del server
	 * @throws AWTException Se c'è un problema con l'inizializzazione di {@link Robot}
	 * @throws NullPointerException Se {@code setup} è null
	 */
	public MacroNetServer(MacroSetup setup)
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
		//Compongo il messaggio di risposta per i client
		final byte[] sendData = new byte[NetStatic.SSDP_SERVER_KEY.length 
		                                 + NetStatic.SSDP_NAME_LENGTH];
		final byte[] serverNameBytes = stringToByteWithEnding(serverName);
		//Chiave di identificazione lato server
		System.arraycopy(NetStatic.SSDP_SERVER_KEY, 0, sendData, 0, NetStatic.SSDP_SERVER_KEY.length);
		//In caso il nome del server sia troppo lungo lo tronco
		int nameLength = Math.min(serverNameBytes.length, NetStatic.SSDP_NAME_LENGTH);
		//Nome del server
		System.arraycopy(serverNameBytes, 0, sendData, NetStatic.SSDP_SERVER_KEY.length, nameLength);
		
		int localPort = serverSocket.getLocalPort();
		
		try {
			udpSocket = new DatagramSocket(localPort);
			
			while(true) {
				byte[] data = new byte[NetStatic.SSDP_CLIENT_KEY.length];
				DatagramPacket receve = new DatagramPacket(data, data.length);
				udpSocket.receive(receve);
				SocketAddress client = receve.getSocketAddress();
				//Controllo che è il client per l'applicazione
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
	 * Converte la stringa {@code s} in byte (nel formato dato da {@link #CHARSET_CLIENT})
	 * assicurandosi che la sequenza termini con {@code '\0'}
	 * @param s Stringa da convertire in byte
	 * @return Sequenza di byte della stringa terminante con uno 0
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
	 * @return Nome del server per quasta macchina
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
	 * @return Ottiene indirizzo IP e porta del server
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
