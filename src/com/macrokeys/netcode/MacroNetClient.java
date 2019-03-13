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

/** Gestisce la connessione e la tasmissione dei tasti al {@link MacroNetServer} */
public class MacroNetClient extends MacroClient {

    /** Timeout per la connessione al server */
    private static final int TIMEOUT_CONNECTION = 10000;
    
    /** Indirizzo del server */
    private final SocketAddress address;
    
    
    /**
     * Avvia una nuova connessione verso l'indirizzo indicato
     * <p>Metodo sincrono; evitare sull'UI thread</p>
     * @param ip Indirizzo IP del server; non null
     * @throws IOException Se c'è un errore con la creazione della connessione
     * @throws IllegalArgumentException Se {@code ip} è null
     */
    public MacroNetClient(String ip) throws IOException {
    	this(new InetSocketAddress(ip, NetStatic.PORT));
    }
    
    
    /**
     * Avvia una nuova connessione verso l'indirizzo indicato
     * <p>Metodo sincrono; evitare sull'UI thread</p>
     * @param socket Socket alla qualle connettersi; non null
     * @throws IOException Se c'è un errore con la creazione della connessione
     * @throws NullPointerException Se {@code ip} è null
     * @throws IllegalArgumentException Se la porta di {@code socket} è diversa
     * da quella usata dal protocollo
     */
    public MacroNetClient(InetSocketAddress socket) throws IOException {
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
     * Permette di trovare i server tramite multicast, nella rete locale.
     * Vengono ottenuti i server che rispondono prima del timeout.
     * Esegue un solo send per il multicast e l'eccezzione
     * {@link SocketTimeoutException} è gestita dal metodo
     * @param timeout Tempo, in millisecondi, da aspettare prima che il metodo finisca; > 0
     * @return Sequenza di Server scoperti con le relative informazioni
     * @throws IOException Se c'è un errore di IO
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
                //Estraggo dai dati ricevuti la chiave del Server:
                byte[] recivedKey = new byte[NetStatic.SSDP_SERVER_KEY.length];
                System.arraycopy(receve, 0, recivedKey, 0, recivedKey.length);
                
                //Confronto la chiave inviata dal Server
                if (Arrays.equals(NetStatic.SSDP_SERVER_KEY, recivedKey)) {
                	InetSocketAddress address = (InetSocketAddress)
                			receivePacket.getSocketAddress();

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


        clientSocket.close();

        //To array
        SSDPServerInfo[] arr = new SSDPServerInfo[serversInfo.size()];
        return serversInfo.toArray(arr);
    }

 

    /** Classe che memorizza le informazioni di un Server scoperto */
    public static class SSDPServerInfo {
        /** Indirizzo socket per la comunicazione con il Server */
        public final InetSocketAddress address;
        /** Nome del server */
        public final String name;

        /**
         * @param address Socket del server per la comunicazione
         * @param name Nome del server
         * @throws NullPointerException Se {@code address} o {@code name} sono null
         */
        private SSDPServerInfo(InetSocketAddress address, String name) {
            assert address != null;
            assert name != null;
            
        	this.address = address;
            this.name = name;
        }
    }
}

