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

/**
 * Manages the connection and the tasmissione of the keys to the {@link MacroNetServer}
 */
public class MacroNetClient extends MacroClient {

    /**
     * Timeout for the connection to the server
     */
    private static final int TIMEOUT_CONNECTION = 10000;

    /**
     * The address of the server
     */
    private final SocketAddress address;

    /**
     * Initiates a new connection to the address listed
     * <p>synchronous Method
     */
    public MacroNetClient(String ip) throws IOException {
        this(new InetSocketAddress(ip, NetStatic.PORT));
    }

    /**
     * Initiates a new connection to the address listed
     * <p>synchronous Method
     */
    public MacroNetClient(InetSocketAddress socket) throws IOException {
        Objects.requireNonNull(socket);
        if (socket.getPort() != NetStatic.PORT) {
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
     * You can find the server using the multicast in the local network.
     * That are obtained by the server to respond before timing out.
     * Performs a single send to the multicast, and the exception
     * {@link SocketTimeoutException} is handled by the method
     * @param timeout Time, in milliseconds, to wait before the method ends
     */
    public static SSDPServerInfo[] findServer(int timeout) throws IOException {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Parameter timeout must be > 0");
        }
        /* MulticastSocket mulSock = new MulticastSocket()*/
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress brodcast = InetAddress.getByName(NetStatic.BRODCAST_ADDR);
        DatagramPacket sendPacket = new DatagramPacket(NetStatic.SSDP_CLIENT_KEY, NetStatic.SSDP_CLIENT_KEY.length, brodcast, NetStatic.PORT);
        clientSocket.send(sendPacket);
        byte[] receve = new byte[NetStatic.SSDP_SERVER_KEY.length + NetStatic.SSDP_NAME_LENGTH];
        DatagramPacket receivePacket;
        clientSocket.setSoTimeout(timeout);
        ArrayList<SSDPServerInfo> serversInfo = new ArrayList<>();
        try {
            while (!clientSocket.isClosed()) {
                receivePacket = new DatagramPacket(receve, receve.length);
                clientSocket.receive(receivePacket);
                // Extract from the received data, the Server key:
                byte[] recivedKey = new byte[NetStatic.SSDP_SERVER_KEY.length];
                System.arraycopy(receve, 0, recivedKey, 0, recivedKey.length);
                // Comparing the key sent from the Server
                if (Arrays.equals(NetStatic.SSDP_SERVER_KEY, recivedKey)) {
                    InetSocketAddress address = (InetSocketAddress) receivePacket.getSocketAddress();
                    // Extract from the received data the name of the server:
                    byte[] recivedName = new byte[NetStatic.SSDP_NAME_LENGTH];
                    System.arraycopy(receve, NetStatic.SSDP_SERVER_KEY.length, recivedName, 0, recivedName.length);
                    String serverName = new String(recivedName);
                    serversInfo.add(new SSDPServerInfo(address, serverName));
                }
            }
        } catch (SocketTimeoutException e) {
        // The exception is always generated and the method continues
        }
        clientSocket.close();
        // To array
        SSDPServerInfo[] arr = new SSDPServerInfo[serversInfo.size()];
        return serversInfo.toArray(arr);
    }

    /**
     * Class that stores information of a Server discovered
     */
    public static class SSDPServerInfo {

        /**
         * Socket address for the communication with the Server
         */
        public final InetSocketAddress address;

        /**
         * The name of the server
         */
        public final String name;

        /**
         * @param address the server Socket for communication
         * @param name the Name of the server
         * @throws NullPointerException If {@code address} or {@code name} is null
         */
        private SSDPServerInfo(InetSocketAddress address, String name) {
            assert address != null;
            assert name != null;
            this.address = address;
            this.name = name;
        }
    }
}
