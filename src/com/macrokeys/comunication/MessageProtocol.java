package com.macrokeys.comunication;
import java.io.IOException;

/** 
 * Interfaccia che consente di comunicare tramite messaggi.
 * <p>Consente anche di impostare dei timeout per la comunicazione.
 * I timeout vengono contati solamente all'esecuzione di una lettura:
 * {@link #receiveMessage()}. <br>
 * Lo scopo del timeout è quello di verificare che il canale di comunicazione sia
 * ancora attivo.
 * Se lo strato sottostante (TCP, UDP, Bluetooth, ...) rileva automaticamente la
 * caduta della connessione l'implementazione può fare a meno di implementare le
 * feature di timeout.
 * </p>
 */
public interface MessageProtocol {
	
	/**
	 * @return True se la connessione è attiva e funzionante, False altrimenti.
	 */
	boolean isConnected();
	
	/**
	 * Imposta il timeout per il flusso di dati in input.
	 * Questo controllo viene effettuato solamente durante una
	 * chiamata a {@link #receiveMessage()}.
	 * @param time Tempo del timeout in millisecondi; 0 per disabilitarlo
	 * @throws IllegalArgumentException Se {@code time} è < 0
	 */
	void setInputKeepAlive(int time);
	
	/**
	 * @return Timeout per il flusso di dati in input; 0 se non impostato.
	 */
	int getInputKeepAlive();
	
	/**
	 * Imposta il timeout del flusso di dati in output
	 * @param time Tempo del timeout in millisecondi; 0 per disabilitarlo
	 * @throws IllegalArgumentException Se {@code time} è < 0
	 */
	void setOutputKeepAlive(int time);
	
	/**
	 * @return Timeout per il flusso di dati in output; 0 se non impostato.
	 */
	int getOutputKeepAlive();
	
	/**
	 * Invia un messaggio. Chiamata sincrona
	 * @param payload Contenuto del messaggio, anche vuoto
	 * @throws IOException Se c'è un errore di IO
	 */
	void sendMessage(byte[] payload) throws IOException;
	
	/**
	 * Aspetta la ricezione di un messaggio
	 * @return Peyload del messaggio
	 * @throws IOException Se c'è un errore di IO
	 */
	byte[] receiveMessage() throws IOException;
	
	
	/**
	 * Chiude la connessione e termina la trasmissione
	 * @throws IOException In caso di errore
	 */
	void close() throws IOException;
}
