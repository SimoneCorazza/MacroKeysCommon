package com.macrokeys.netcode;

import java.nio.charset.StandardCharsets;

/**
 * Static class to collect common information between the client and the server (e.g., keys, id)
 */
final class NetStatic {

    /**
     * Sequence that allows the receiving Server to identify the Client
     * during the SSDP (multicast)
     */
    public static final byte[] SSDP_CLIENT_KEY = new byte[] { (byte) 209, (byte) 177, (byte) 27, (byte) 182, (byte) 232, (byte) 145, (byte) 160, (byte) 241, (byte) 19, (byte) 193, (byte) 12, (byte) 104, (byte) 140, (byte) 27, (byte) 124, (byte) 17, (byte) 198, (byte) 234, (byte) 116, (byte) 152, (byte) 195, (byte) 14, (byte) 233, (byte) 3, (byte) 99, (byte) 52, (byte) 254, (byte) 147, (byte) 48, (byte) 166, (byte) 150, (byte) 230, (byte) 229, (byte) 239, (byte) 96, (byte) 82, (byte) 171, (byte) 240, (byte) 157, (byte) 225, (byte) 201, (byte) 144, (byte) 135, (byte) 19, (byte) 209, (byte) 47, (byte) 135, (byte) 3, (byte) 159, (byte) 250, (byte) 217, (byte) 28, (byte) 187, (byte) 224, (byte) 185, (byte) 112, (byte) 123, (byte) 179, (byte) 244, (byte) 135, (byte) 174, (byte) 236, (byte) 202, (byte) 78, (byte) 150, (byte) 46, (byte) 181, (byte) 187, (byte) 92, (byte) 206, (byte) 160, (byte) 46, (byte) 196, (byte) 202, (byte) 191, (byte) 205, (byte) 3, (byte) 152, (byte) 113, (byte) 134, (byte) 242, (byte) 17, (byte) 60, (byte) 245, (byte) 233, (byte) 27, (byte) 107, (byte) 86, (byte) 238, (byte) 188, (byte) 248, (byte) 115, (byte) 26, (byte) 148, (byte) 61, (byte) 231, (byte) 13, (byte) 35, (byte) 15, (byte) 193, (byte) 47, (byte) 198, (byte) 53, (byte) 78, (byte) 126, (byte) 4, (byte) 23, (byte) 28, (byte) 184, (byte) 113, (byte) 254, (byte) 204, (byte) 17, (byte) 249, (byte) 16, (byte) 254, (byte) 246, (byte) 156, (byte) 141, (byte) 79, (byte) 186, (byte) 188, (byte) 94, (byte) 225, (byte) 45, (byte) 78, (byte) 71, (byte) 100 };

    /**
     * Sequence that identifies the Server during the SSDP (multicast)
     */
    public static final byte[] SSDP_SERVER_KEY = new byte[] { (byte) 141, (byte) 230, (byte) 81, (byte) 166, (byte) 47, (byte) 112, (byte) 110, (byte) 221, (byte) 248, (byte) 247, (byte) 200, (byte) 189, (byte) 95, (byte) 79, (byte) 129, (byte) 105, (byte) 77, (byte) 141, (byte) 129, (byte) 254, (byte) 136, (byte) 89, (byte) 134, (byte) 197, (byte) 27, (byte) 40, (byte) 52, (byte) 88, (byte) 79, (byte) 58, (byte) 70, (byte) 121, (byte) 157, (byte) 109, (byte) 114, (byte) 214, (byte) 202, (byte) 20, (byte) 210, (byte) 49, (byte) 82, (byte) 33, (byte) 9, (byte) 95, (byte) 216, (byte) 179, (byte) 210, (byte) 53, (byte) 57, (byte) 49, (byte) 187, (byte) 207, (byte) 213, (byte) 30, (byte) 186, (byte) 145, (byte) 126, (byte) 127, (byte) 61, (byte) 1, (byte) 126, (byte) 106, (byte) 9, (byte) 195, (byte) 180, (byte) 78, (byte) 60, (byte) 76, (byte) 137, (byte) 70, (byte) 174, (byte) 64, (byte) 180, (byte) 0, (byte) 95, (byte) 28, (byte) 137, (byte) 108, (byte) 230, (byte) 247, (byte) 103, (byte) 58, (byte) 117, (byte) 127, (byte) 28, (byte) 175, (byte) 244, (byte) 228, (byte) 58, (byte) 95, (byte) 175, (byte) 131, (byte) 40, (byte) 163, (byte) 31, (byte) 12, (byte) 118, (byte) 102, (byte) 147, (byte) 101, (byte) 119, (byte) 174, (byte) 185, (byte) 232, (byte) 68, (byte) 222, (byte) 235, (byte) 217, (byte) 76, (byte) 79, (byte) 5, (byte) 119, (byte) 217, (byte) 37, (byte) 200, (byte) 161, (byte) 241, (byte) 192, (byte) 175, (byte) 193, (byte) 155, (byte) 156, (byte) 194, (byte) 148, (byte) 250, (byte) 252, (byte) 22, (byte) 36 };

    /**
     * Size of the name of the Server, in bytes, when the SSDP
     */
    public static final int SSDP_NAME_LENGTH = 32;

    /**
     * The multicast address for SSDP
     */
    public static final String MULTICAST_ADDR = "239.255.255.250";

    /**
     * Address brodcast
     */
    public static final String BRODCAST_ADDR = "255.255.255.255";

    /**
     * TCP port used for communication between {@link MacroNetClient} and
     * {@link MacroNetServer}.
     * This same port is used for the service of SSDP, using UDP.
     */
    public static final int PORT = 10414;

    /**
     * The Header that allows you to identify the server
     */
    public static final byte[] SERVER_IDENTIFIER = "pinopanco".getBytes(StandardCharsets.UTF_8);
}