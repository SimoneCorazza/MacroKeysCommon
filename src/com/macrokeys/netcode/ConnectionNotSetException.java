package com.macrokeys.netcode;

import java.io.Serializable;

/**
 * Eccezione per le operazioni che richiedono una connessione che non è stata stabilita
 */
public class ConnectionNotSetException extends RuntimeException {

	/** Seriale per {@link Serializable} */
	private static final long serialVersionUID = 1L;

	public ConnectionNotSetException() {

    }

    public ConnectionNotSetException(String Message) {
        super(Message);
    }

}

