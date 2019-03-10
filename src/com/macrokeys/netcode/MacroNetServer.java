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

/**
 * The Server that is running the macro that is sent from {@link MacroNetClient}
 */
public final class MacroNetServer extends MacroServer {

    /**
     * Charset used for the communication with the client
     */
    public static final Charset CHARSET_CLIENT = StandardCharsets.UTF_8;

    /**
     * Server name (used to identify this Server when a client performs a SSDP)
     */
    private static final String serverName = serverName();

    /**
     * Indicates if the Server accepts new incoming connections
     */
    private boolean seekConnection = false;

    /**
     * Soket server
     */
    private ServerSocket serverSocket;

    /**
     * Socket for UDP packet related to SSDP
     */
    private DatagramSocket udpSocket;

    /**
     * @param setup setup to use initially
     */
    public MacroNetServer(MacroSetup setup) throws IOException, AWTException {
        super(setup);
    }

    @Override
    protected void innerStart() throws IOException {
        serverSocket = new ServerSocket(NetStatic.PORT);
        seekConnection = true;
    }

    @Override
    protected void introduceServerToClient() {
        // I compose the reply message to the client
        final byte[] sendData = new byte[NetStatic.SSDP_SERVER_KEY.length + NetStatic.SSDP_NAME_LENGTH];
        final byte[] serverNameBytes = stringToByteWithEnding(serverName);
        // Identification key server-side
        System.arraycopy(NetStatic.SSDP_SERVER_KEY, 0, sendData, 0, NetStatic.SSDP_SERVER_KEY.length);
        // In case the server name is too long for the trunk
        int nameLength = Math.min(serverNameBytes.length, NetStatic.SSDP_NAME_LENGTH);
        // The name of the server
        System.arraycopy(serverNameBytes, 0, sendData, NetStatic.SSDP_SERVER_KEY.length, nameLength);
        int localPort = serverSocket.getLocalPort();
        try {
            udpSocket = new DatagramSocket(localPort);
            while (true) {
                byte[] data = new byte[NetStatic.SSDP_CLIENT_KEY.length];
                DatagramPacket receve = new DatagramPacket(data, data.length);
                udpSocket.receive(receve);
                SocketAddress client = receve.getSocketAddress();
                // Control which is the client for the application
                if (Arrays.equals(receve.getData(), NetStatic.SSDP_CLIENT_KEY)) {
                    DatagramPacket send = new DatagramPacket(sendData, sendData.length, client);
                    udpSocket.send(send);
                }
            }
        } catch (IOException e) {
        } finally {
            if (udpSocket != null) {
                udpSocket.close();
            }
        }
    /*
//Compose the response message to the client 
final byte[] sendData = new byte[NetStatic.SSDP_SERVER_KEY.length 
+ NetStatic.SSDP_NAME_LENGTH]*/
    }

    @Override
    protected MessageProtocol waitNewClientConnection() throws IOException {
        final Socket s = serverSocket.accept();
        TCPMessageProtocol messProt = new TCPMessageProtocol(s);
        messProt.setTcpNoDelay(true);
        return messProt;
    }

    /**
     * Converts the string {@code s} in bytes (in the format given by {@link #CHARSET_CLIENT})
     * making sure that the sequence ends with {@code '\0'}
     * @param s the String to convert to bytes
     * @return the Sequence of bytes of the string ending with a 0
     */
    private static byte[] stringToByteWithEnding(String s) {
        Objects.requireNonNull(s);
        byte[] b = s.getBytes(CHARSET_CLIENT);
        int i = b.length - 1;
        while (i >= 0 && (char) b[i] == '\0') {
            i--;
        }
        byte[] r = new byte[i + 1 + 1];
        System.arraycopy(b, 0, r, 0, i + 1);
        r[r.length - 1] = '\0';
        return r;
    }

    /**
     * @return server Name for this machine
     */
    public static String serverName() {
        String name;
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            name = "Unknown";
        }
        return name;
    }

    /**
     * @return Gets the IP address and port of the server
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
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (udpSocket != null) {
                udpSocket.close();
            }
        } catch (IOException e) {
        }
    }
}