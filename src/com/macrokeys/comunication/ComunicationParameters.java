package com.macrokeys.comunication;

/**
 * Static class for commpon protocol parameters
 */
final class ComunicationParameters {

    private ComunicationParameters() {
    }

    /**
     * Timeout for the communication from the client and server
     * <br/>
     * Parameter passed at {@link MessageProtocol}.
     */
    public static final int TIMEOUT_MESSAGES = 5000;
}
