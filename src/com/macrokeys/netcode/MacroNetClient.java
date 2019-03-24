package com.macrokeys.netcode;

import com.macrokeys.comunication.MacroClient;
import com.macrokeys.comunication.MessageProtocol;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

/** Handles the connection and the transmission of data at the {@link MacroNetServer} */
public class MacroNetClient extends MacroClient {

    /** Server connection timeout */
    private static final int TIMEOUT_CONNECTION = 10000;
    
    /** Server address */
    private final SocketAddress address;
    
    
    /**
     * Init a new connection at the given address
     * <p>Sincronous method; avoid in the UI thread</p>
     * @param ip Server IP address
     * @throws IOException If an IO error occurrs
     */
    public MacroNetClient(@NonNull String ip) throws IOException {
    	this(new InetSocketAddress(ip, NetStatic.PORT));
    }
    
    
    /**
     * Init a new connection at the given address
     * <p>Sincronous method; avoid in the UI thread</p>
     * @param socket Socket to cennect at
     * @throws IOException If an IO error occurrs
     * @throws IllegalArgumentException If the port of {@code socket} is different from witch of the protocol
     */
    public MacroNetClient(@NonNull InetSocketAddress socket) throws IOException {
    	Objects.requireNonNull(socket);
    	
    	if(socket.getPort() != NetStatic.PORT) {
    		throw new IllegalArgumentException("Wrong port");
    	}
    	
        this.address = socket;
    }
    
    

	@Override
	protected MessageProtocol innerConnectToServer() throws IOException {
		Socket socket = new Socket();
        socket.connect(address, TIMEOUT_CONNECTION);
        TCPMessageProtocol messProto = new TCPMessageProtocol(socket);
        messProto.setTcpNoDelay(true);
        
        return messProto;
	}
    
    
    /**
     * Find the server in the local newtwork.
     * It ghets the server that respond before the timeout.
     * The {@link SocketTimeoutException} is handled
     * @param timeout Time limit to the answars of the server
     * @return Discovered servers
     * @throws IOException If an IO error occurrs
     * @throws IllegalArgumentException If {@code timeout} is <= 0
     * @see <a href="link https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol">SSDP protocol port</a>
     */
    public static SSDPServerInfo[] findServer(int timeout) throws IOException {
        if(timeout <= 0) {
            throw new IllegalArgumentException("Parameter timeout must be > 0");
        }

        /*
        MulticastSocket mulSock = new MulticastSocket();
        InetAddress group = InetAddress.getByName(NetStatic.MULTICAST_ADDR);
        mulSock.joinGroup(group);
        DatagramPacket sendPacket = new DatagramPacket(NetStatic.SSDP_CLIENT_KEY, NetStatic.SSDP_CLIENT_KEY.length,
                group, port);
        mulSock.send(sendPacket);

        byte[] receve = new byte[NetStatic.SSDP_SERVER_KEY.length + NetStatic.SSDP_NAME_LENGTH];
        DatagramPacket receivePacket;
        mulSock.setSoTimeout(timeout);

        ArrayList<SSDPServerInfo> serversInfo = new ArrayList<>();
        try {
            while (!mulSock.isClosed()) {
                receivePacket = new DatagramPacket(receve, receve.length);
                mulSock.receive(receivePacket);
                //Estraggo dai dati ricevuti la chiave del Server:
                byte[] recivedKey = new byte[NetStatic.SSDP_SERVER_KEY.length];
                System.arraycopy(receve, 0, recivedKey, 0, recivedKey.length);
                if (Arrays.equals(NetStatic.SSDP_SERVER_KEY, recivedKey)) { //Confronto la chiave inviata dal Server
                    SocketAddress address = receivePacket.getSocketAddress();

                    //Estraggo dai dati ricevuti il nome del server:
                    byte[] recivedName = new byte[NetStatic.SSDP_NAME_LENGTH];
                    System.arraycopy(receve, NetStatic.SSDP_SERVER_KEY.length, recivedName, 0, recivedName.length);
                    String serverName = new String(recivedName);

                    serversInfo.add(new SSDPServerInfo(address, serverName));
                }
            }
        } catch (SocketTimeoutException e) {
            //L'eccezione viene sempre generata e il metodo continua
            
        }


        mulSock.close();

        //To array
        SSDPServerInfo[] arr = new SSDPServerInfo[serversInfo.size()];
        return serversInfo.toArray(arr);
        */
        
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress brodcast = InetAddress.getByName(NetStatic.BRODCAST_ADDR);
        DatagramPacket sendPacket = new DatagramPacket(NetStatic.SSDP_CLIENT_KEY, NetStatic.SSDP_CLIENT_KEY.length,
        		brodcast, NetStatic.PORT);
        clientSocket.send(sendPacket);

        byte[] receve = new byte[NetStatic.SSDP_SERVER_KEY.length + NetStatic.SSDP_NAME_LENGTH];
        DatagramPacket receivePacket;
        clientSocket.setSoTimeout(timeout);

        ArrayList<SSDPServerInfo> serversInfo = new ArrayList<>();
        try {
            while (!clientSocket.isClosed()) {
            	receivePacket = new DatagramPacket(receve, receve.length);
                clientSocket.receive(receivePacket);
                
                // Extract from the data the server key:
                byte[] recivedKey = new byte[NetStatic.SSDP_SERVER_KEY.length];
                System.arraycopy(receve, 0, recivedKey, 0, recivedKey.length);
                
                // Check the key sent by the server
                if (Arrays.equals(NetStatic.SSDP_SERVER_KEY, recivedKey)) {
                	InetSocketAddress address = (InetSocketAddress)
                			receivePacket.getSocketAddress();
                	
                	// Extract from the data the name of the server:
                    byte[] recivedName = new byte[NetStatic.SSDP_NAME_LENGTH];
                    System.arraycopy(receve, NetStatic.SSDP_SERVER_KEY.length, recivedName, 0, recivedName.length);
                    String serverName = new String(recivedName);

                    serversInfo.add(new SSDPServerInfo(address, serverName));
                }
            }
        } catch (SocketTimeoutException e) {
            // This exception is always generated and the method must continue
        }


        clientSocket.close();

        // To array
        SSDPServerInfo[] arr = new SSDPServerInfo[serversInfo.size()];
        return serversInfo.toArray(arr);
    }

 

    /** Info of a discovered server */
    public static class SSDPServerInfo {
        /** Socket address to comunicate with the server */
        public final InetSocketAddress address;
        
        /** Name of the server */
        public final String name;

        /**
         * @param address Socket address to comunicate with the server
         * @param name name of the server
         */
        private SSDPServerInfo(@NonNull InetSocketAddress address, @NonNull String name) {
            assert address != null;
            assert name != null;
            
        	this.address = address;
            this.name = name;
        }
    }
}

