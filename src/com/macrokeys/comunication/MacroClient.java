package com.macrokeys.comunication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.macrokeys.MSLoadException;
import com.macrokeys.MacroKey;
import com.macrokeys.MacroSetup;
import com.macrokeys.netcode.ConnectionNotSetException;

/**
 * Classe astratta per un client che riceve le {@link MacroSetup}
 * e invia invia i {@link MacroKey} premuti
 */
public abstract class MacroClient {

	/** Identificativi dei tasti attualmente premuti */
	private final Set<MacroKey> pressedKeys = new HashSet<>();
	
	private MessageProtocol messProt;
	
	/** Stato del client attuale */
	private State state = State.NoConnection;
	
	
	/**
	 * Consente di connettersi al server
	 * @throws IOException In caso di errore di IO durante la connessione
	 * @throws IllegalStateException Se lo stato attuale non è
	 * {@link State#NoConnection}
     * @see #getState()
	 */
	public final void connectToServer() throws IOException {
		if(!getState().equals(State.NoConnection)) {
			throw new IllegalStateException();
		}
		assert this.messProt == null;
		
		this.messProt = innerConnectToServer();
		messProt.setInputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
		messProt.setOutputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
		state = State.Comunication;
	}
	
	
	
	/**
	 * Effettua la connessione in base alla tipologia di comunicazione
	 * intrapresa
	 * @return Livello di comunicazione a messaggi con il server
	 */
	protected abstract MessageProtocol innerConnectToServer() throws IOException;
    
    
    
    /**
     * Invia l'evento di rascio del tasto
     * @param macroKey Tasto soggetto all'evento
     * @throws IOException Se c'è un errore con la connessione
     * @throws NullPointerException Se {@code macroKey} è null
     * @throws IllegalStateException Se lo stato attuale non è
     * {@link State#Comunication}
     * @see #getState()
     */
	public synchronized final void keyUp(MacroKey macroKey) throws IOException {
		Objects.requireNonNull(macroKey);
		if(!getState().equals(State.Comunication)) {
			throw new IllegalStateException();
		}
		
		if(pressedKeys.contains(macroKey)) {
			pressedKeys.remove(macroKey);
			sendActionKey(macroKey, false);
		}
	}
	
	
	
	
	/**
     * Invia l'evento di pressione del tasto
     * @param macroKey Tasto soggetto all'evento
     * @throws IOException Se c'è un errore con la connessione
     * @throws NullPointerException Se {@code macroKey} è null
     * @throws IllegalStateException Se lo stato attuale non è {@link State#Comunication}
     * @see #getState()
     */
	public synchronized final void keyDown(MacroKey macroKey) throws IOException {
		Objects.requireNonNull(macroKey);
		if(!getState().equals(State.Comunication)) {
			throw new IllegalStateException();
		}
		
		
		if(!pressedKeys.contains(macroKey)) {
			pressedKeys.add(macroKey);
			sendActionKey(macroKey, true);
		}
	}
	
	
	
    /**
     * Invia un'azione relativa al tasto
     * @param mk Tasto al quale l'azione si riferisce
     * @param action True: premuto, False: rilasciato
     * @throws IOException In caso di errore di IO
     */
    private void sendActionKey(MacroKey mk, boolean action) throws IOException {
    	Objects.requireNonNull(mk);
    	
        if(isConnected()) {
        	ByteArrayOutputStream str = new ByteArrayOutputStream();
        	DataOutputStream dataStr = new DataOutputStream(str);
        	
        	dataStr.writeInt(mk.getId());
        	dataStr.writeBoolean(action);
    		
        	messProt.sendMessage(str.toByteArray());
        	
    		dataStr.close();
        	str.close();
        } else {
            throw new ConnectionNotSetException();
        }
    }
	
	
    /**
     * @return Stato attule del cleint
     */
    public final State getState() {
    	return state;
    }
    
	
	
    /**
     * Aspetta, riceve e carica la {@link MacroSetup} inviata dal {@link MacroServer}
     * <p>Metodo sincrono; evitare sull'UI thread</p>
     * @return MacroSetup inviata dal PC
     * @throws IOException Nel caso di errori di comunicazione
     * @throws MSLoadException Nel caso di errore nei dati ricevuti
     * @throws IllegalStateException Se lo stato attuale non è {@link State#Comunication}
     * @see #getState()
     */
    public final MacroSetup reciveMacroSetup() throws IOException, MSLoadException {
    	if (!isConnected()) {
            return null;
        } else if(!getState().equals(State.Comunication)) {
			throw new IllegalStateException();
		}
        
        byte[] payload = messProt.receiveMessage();
        ByteArrayInputStream str = new ByteArrayInputStream(payload);
        MacroSetup m = MacroSetup.load(str);
        str.close();
        
        return m;
    }
	
    
    
    
    /** @return True se la connessione con {@link MacroServer} è stabilita */
    public final boolean isConnected() {
    	return messProt != null && messProt.isConnected();
    }
    
    
    
    
    /**
     * Chiude la connessione se attiva
     * @throws IOException Se c'è un errore nella chiusura della connessione
     * @throws IllegalStateException Se lo stato attuale non è {@link State#Comunication}
     * @see #getState()
     */
    public final void close() throws IOException {
    	if(!getState().equals(State.Comunication)) {
    		throw new IllegalStateException();
    	}    	
    	messProt.close();
    	state = State.Closed;
    }
    
    
    /** Stato relativo al client */
    public enum State {
    	/** La connessione con il server non è stata ancora stabilita */
    	NoConnection,
    	/** La connessione con il server è stata stabilita */
    	Comunication,
    	/** La connessione con il server è stata chiusa */
    	Closed
    }
    
    
}
