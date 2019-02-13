package com.macrokeys.comunication;

/**
 * Classe statica che racchiude i parametri comuni per la comunicazione
 */
final class ComunicationParameters {

	private ComunicationParameters() { }
	
	
	/**
	 * Timeout per la ricezione di informazioni dal client o dal server.
	 * Parametro passato a {@link MessageProtocol}.
	 */
	public static final int TIMEOUT_MESSAGES = 5000;
}
