package com.macrokeys.netcode;

import java.io.Serializable;

/**
 * Exception used when a required connection was not established
 */
public class ConnectionNotSetException extends RuntimeException {

    /**
     * Serial for {@link Serializable}
     */
    private static final long serialVersionUID = 1L;

    public ConnectionNotSetException() {
    }

    public ConnectionNotSetException(String Message) {
        super(Message);
    }
}
