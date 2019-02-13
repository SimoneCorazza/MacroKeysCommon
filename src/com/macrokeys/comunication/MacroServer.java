package com.macrokeys.comunication;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.macrokeys.MacroKey;
import com.macrokeys.MacroKeyType;
import com.macrokeys.LimitedKeySequence;
import com.macrokeys.MacroSetup;

/**
 * Classe astratta per un server che invia le {@link MacroSetup} ai {@link MacroClient} e ne riceve i
 * {@link MacroKey} premuti
 */
public abstract class MacroServer {
	
	/** Attuatore delle pressioni */
	private final KeyPresser keyPresser;
	
	/** Listeners agli eventi di {@code this}; mai null */
	private final List<EventListener> eventListeners = new ArrayList<>();
	
	
	/** {@link MacroKey} attualmente premuti (key down) */
	private final HashMap<MacroKey, KeyDown> pressedKeys = new HashMap<>();
	
	/** Thread per la pressione dei tasti di tipologia {@link MacroKeyType#Normal} */
	private final Thread normalPresser = new Thread(new ThreadRunnable());
	
	/** Indica se il server è in modalità sospensione */
	private boolean suspend = false;
	
	/** Setup attualmente utilizzata; mai null */
	private MacroSetup setup;
	
	/** 
	 * Dati rappresentanti la {@link MacroSetup} attualmente utilizzata,
	 * reperibile con {@link #setup}; mai null
	 */
	private byte[] macroSetupData;
	
	
	
	
	/** Thread che accoglie le nuove connessioni da parte dei client */
	private Thread threadListener;
	/** Thread che informa i client del servizio offerto dal server */
	private Thread threadIntroduce;
	
	/** 
	 * Raccolta dei client connessi; la chiave è l'identificativo a
	 * stringa dei client
	 */
	private final ConcurrentHashMap<String, ClientInfo> clients =
			new ConcurrentHashMap<>();
	
	
	/** Stato attuale del server */
	private State state = State.WaitStart;
	
	
	/**
	 * @param setup Setup da utilizzare inizialmente; non null
	 * @throws AWTException Se c'è un problema con l'inizializzazione di {@link Robot}
	 * @throws NullPointerException Se {@code setup} è null
	 */
	public MacroServer(MacroSetup setup) throws AWTException {
		Objects.requireNonNull(setup);
		
		this.setup = setup;
		try {
			this.macroSetupData = setup.saveAsByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			assert false : "Should not happend";
		}
		keyPresser = new KeyPresser();
	}
	
	@Override
	protected void finalize() throws Throwable {
		//Interrompo il thread per le pressioni
		normalPresser.interrupt();
		//Rilascio tutti i tasti premuti 
		for(MacroKey k : pressedKeys.keySet()) {
			releaseKey(k);
		}
	}
	
	
	
	
	/**
	 * Fa partire il server
	 * @throws IOException In caso di un errore di IO nell'inizializzazione
	 * @throws IllegalStateException Se this non si trova nello stato
	 * {@link State#WaitStart}
	 * @see #getState()
	 */
	public final void start() throws IOException {
		if(!getState().equals(State.WaitStart)) {
			throw new IllegalStateException();
		}
		
		
		innerStart();
		
		normalPresser.start();
		
		threadListener = new Thread() {
			@Override
			public void run() {
				newClientListener();
			}
		};
		threadIntroduce = new Thread() {
			@Override
			public void run() {
				introduceServerToClient();
			}
		};
		threadListener.start();
		threadIntroduce.start();
		
		state = State.Functional;
	}
	
	
	
	/**
	 * Inizializza il server, mettendolo in ascolto dei client
	 * @throws IOException In caso di un errore di IO nell'inizializzazione
	 */
	protected abstract void innerStart() throws IOException;
	
	
	
	/**
	 * @return Stato corrente del server
	 */
	public final State getState() {
		return state;
	}
	
	
	
	/**
	 * Permette ai client di sapere che questa machina offre il
	 * servizio di Server.
	 * <p>
	 * Eseguito su un thread separato apposito.
	 * </p>
	 */
	protected abstract void introduceServerToClient();
	
	
	
	
	/**
	 * Permette di attendere nuovi client che si connettino.
	 * <p>
	 * Eseguito sul thread {@link #threadListener}
	 * </p>
	 */
	private void newClientListener() {
		while(!threadListener.isInterrupted()) {
			final MessageProtocol messProt;
			
			try {
				messProt = waitNewClientConnection();
			} catch(IOException e) {
				continue;
			}
			
			
			// Se il server non cerca connessioni la chiudo
			if(!isSeekConnection()) {
				try {
					messProt.close();
				} catch (IOException e) {
					continue;
				}
			}
			
			// Imposto il timeout per la comunicazione
			messProt.setInputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
			messProt.setOutputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
				
			// Eseguo il login del client ed ottengo l'identificativo del
			// client
			String clientId;
			try {
				clientId = loginClient(messProt);
			} catch(IOException e) {
				continue;
			}
			
			// In caso il client sia già loggato
			if(clients.containsKey(clientId)) {
				continue;
			}
			
			final ClientInfo info = new ClientInfo(clientId, messProt);
			clients.put(clientId, info);
			
			// Faccio partire il thread per servire il client
			Thread th = new Thread() {
				@Override
				public void run() {
					serveClient(info);
				}
			};
			th.start();
			
			
			// Genero l'evento della connessione di un nuovo client
			// in maniera da non appesantire questo thread ed evitare
			// che venga interroto a causa di eccezioni
			Thread startEvents = new Thread() {
				@Override
				public void run() {
					fireClientConnectListener(info.clientId);
				}
			};
			startEvents.start();
		}
		
	}
	
	
	/**
	 * Server il cleint indicato. In particolare gestisce le eccezioni
	 * di {@link #ServeClientExceptions(ClientInfo)}
	 * @param info 
	 * @see #ServeClientExceptions(ClientInfo)
	 */
	private void serveClient(ClientInfo info) {		
		try {
			serveClientExceptions(info);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				info.messProt.close();
			} catch (IOException e) { }
			
			// Rimuovo le infirmazioni del client dalla lista
			clients.remove(info.clientId);
			
			// Rilascio i tasti premuti dal client che si è disconnesso
			for(Map.Entry<MacroKey, KeyDown> p : pressedKeys.entrySet()) {
				MacroKey mk = p.getKey();
				KeyDown k = p.getValue();
				assert mk != null && k != null;
				
				if(k.clientId.equals(info.clientId)) {
					releaseKey(mk);
				}
			}
			
			// Genero l'evento di disconnessione
			fireClientDisconnectListener(info.clientId);
		}
	}
	
	
	
	/**
	 * Esegue il login del client
	 * @param messProt Livello di comunicazione a messaggi con il client
	 * @return Identificativo del client
	 */
	private String loginClient(MessageProtocol messProt) throws IOException {
		// TODO: implementare sicurezza ed identificazione del cleint
		return messProt.toString();
	}

	/**
	 * Gestisce l'input del client indicato
	 * @param info Informazioni relative al client da gestire
	 * @throws IOException In caso di errore IO
	 */
	private void serveClientExceptions(ClientInfo info)
			throws IOException {
		assert info != null;
		assert getMacroSetup() != null;
		
		
		// Invio la macro setup
		byte[] macroSetupData = getMacroSetupData();
		assert macroSetupData != null;
		info.messProt.sendMessage(macroSetupData);
		
		
		
		//Ricevo le richieste di effettuare macro
		while(info.messProt.isConnected()) {
			byte[] mess = info.messProt.receiveMessage();
			ByteArrayInputStream str = new ByteArrayInputStream(mess);
			DataInputStream dataStr = new DataInputStream(str);
			
			int macroID = dataStr.readInt();
			boolean state = dataStr.readBoolean();
			
			dataStr.close();
			str.close();
			
			//Esecizione della macro:
			MacroSetup setup = getMacroSetup();
			synchronized(setup) {
				MacroKey k = setup.macroKeyFromID(macroID);
				assert k != null && k.getId() == macroID :
					"MacroID has not been found correctly: " + macroID;
					
				if(state) {
					pressKey(k, info.clientId);
				} else {
					releaseKey(k);
				}
			}
		}
	}
	
	
	
	/**
	 * Attende la connessione di un nuovo client.
	 * @return Livello di comunicazione a messaggi con il nuovo client
	 */
	protected abstract MessageProtocol waitNewClientConnection() throws IOException;
	
	
	
	/**
	 * Setta il flag che indica se il Server eseguirà le macro ricevute
	 * @param s Nuovo stato del flag
	 * @throws IllegalStateException Se this non si trova nello stato
	 * {@link State#Functional}
	 * @see #getState()
	 */
	public final void setSuspend(boolean s) {
		if(!getState().equals(State.Functional)) {
			throw new IllegalStateException();
		}
		
		if(suspend != s) {
			suspend = s;
			
			if(suspend) {
				//Rilascio tutti i tasti premuti 
				for(MacroKey k : pressedKeys.keySet()) {
					releaseKey(k);
				}
				pressedKeys.clear();
			}
			fireServerSuspendEvent(s);
		}
	}
	
	
	
	
	/**
	 * @return True se il Server non esegue più le pressioni delle macro ricevute
	 */
	public final boolean isSuspended() {
		return suspend;
	}
	
	
	
	
	/**
	 * @return True se il Server accetta nuove connessioni
	 */
	public abstract boolean isSeekConnection();
	
	
	
	
	/**
	 * Setta il flag che indica se il Server accetta nuove connessioni
	 * @param b Nuovo stato del flag
	 */
	public abstract void setSeekConnection(boolean b);
	
	
	
	/** 
	 * Chiude il server interrompendone l'attività e cambiandone lo stato.
	 * @throws IllegalStateException Se this non si trova nello stato {@link State#Functional}
	 * @see #getState()
	 */
	public final void close() {
		if(!getState().equals(State.Functional)) {
			throw new IllegalStateException();
		}
		
		//Chiudo tutte le connessioni
		for(ClientInfo s : clients.values()) {
			try {
				s.messProt.close();
			} catch (IOException e) { }
		}
		
		threadIntroduce.interrupt();
		threadListener.interrupt();
		normalPresser.interrupt();
		
		
		innerClose();
		state = State.Closed;
		
		fireServerCloseEvent();
	}
	
	/**
	 * Implementa la chiusura del server.
	 * Chiamato da {@link #close()}
	 */
	protected abstract void innerClose();
	
	
	
	/**
	 * Permette di cambiare la {@link MacroSetup} attuale
	 * <p>Metodo sincrono; meglio non eseguire sul thread dell'UI</p>
	 * @param m Nuova setup da utilizzare; non null
	 * @throws NullPointerException Se {@code m} è null
	 */
	public final void changeMacroSetup(MacroSetup m) {
		Objects.requireNonNull(m);
		assert setup != null;
		
		this.setup = m;
		try {
			this.macroSetupData = setup.saveAsByteArray();
		} catch(IOException e) {
			e.printStackTrace();
			assert false : "Should not happend";
		}
		
		
		// Invio ai client la nuova MacroSetup
		// 
		byte[] macroSetupData = getMacroSetupData();
		for(ClientInfo p : clients.values()) {
			try {
				p.messProt.sendMessage(macroSetupData);
			} catch(IOException e) {
				// Niente
			}
		}

		
		
		fireServerChangeMacroSetup(m);
	}
	
	
	/**
	 * @return Ottiele il setup attualmente utilizzato
	 */
	public final MacroSetup getMacroSetup() {
		return this.setup;
	}
	
	
	/**
	 * @return Rappresentazione binaria della {@link MacroSetup} attualmente
	 * selezionata (ottenibile con {@link #getMacroSetup()})
	 */
	protected final byte[] getMacroSetupData() {
		return macroSetupData;
	}
	
	
	
	/**
	 * Preme il {@link MacroKey} se il flag {@link #isSuspended()} è false
	 * @param mk Tasto da premere; non null
	 * @param clientId Identificativo del client che ha premuto il tasto
	 * @return True se il tasto è stato premuto o il flag{@link #isSuspended()}
	 * è true. False se il tasto è già stato premuto.
	 */
	private boolean pressKey(MacroKey mk, String clientId) {
		assert mk != null;
		assert clients.containsKey(clientId);
		assert getState().equals(State.Functional);
		
		// Se sospeso o già premuto ignoro la pressione
		if(suspend || pressedKeys.containsKey(mk)) {
			return false;
		}
		
		// Controllo che non sia già stato premuto
		
		switch(mk.getType()) {
		case Game:
			keyPresser.press(mk.getKeySeq());
			break;
		case Normal:
			break;
		case OnRelease:
			break;
			
		default: assert false : "Unkown case";
			break;
			
		}
		
		//Indico il tasto come premuto
		pressedKeys.put(mk, new KeyDown(clientId, mk.getType()));
		
		fireKeyRecivedListener(clientId, mk, true);
		
		return true;
	}
	
	
	
	/**
	 * Rilascia il {@link MacroKey}.
	 * Non influenzato dal flag {@link #isSuspended()}.
	 * @param mk Tasto da rilasciare; non null
	 * @return True se il tasto è stato rilasciato.
	 * 	False se il tasto è già stato rilasciato.
	 */
	protected boolean releaseKey(MacroKey mk) {
		assert mk != null;
		assert getState().equals(State.Functional);
		
		if(pressedKeys.containsKey(mk)) {
			if(mk.getType() == MacroKeyType.Game) {
				keyPresser.release(mk.getKeySeq());
			} else if(mk.getType() == MacroKeyType.OnRelease) {
				pressAndReleaseKey(mk);
			}
			String clientId = pressedKeys.get(mk).clientId;
			pressedKeys.remove(mk);
			
			fireKeyRecivedListener(clientId, mk, false);
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Preme e rilascia il {@link MacroKey}.
	 * Non influenzato dal flag {@link #suspend}.
	 * @param mk Tasto su cui effettuare l'azione; non null
	 */
	private void pressAndReleaseKey(MacroKey mk) {
		assert mk != null;
		assert getState().equals(State.Functional);
		
		LimitedKeySequence l = mk.getKeySeq();
		keyPresser.press(l);
		keyPresser.release(l);
	}
	
	
	
	
	
	
	/**
	 * Informazioni relative ad un client attualmente connesso
	 */
	private final static class ClientInfo {
		private final String clientId;
		private final MessageProtocol messProt;
		
		
		/**
		 * @param clientId Identificativo del client
		 * @param p Strato di comunicazione con il client a messaggi
		 */
		public ClientInfo(String clientId, MessageProtocol p) {
			if(clientId == null || p == null) {
				throw new NullPointerException();
			}
			
			this.clientId = clientId;
			this.messProt = p;
		}
	}
	
	
	
	// --- THREAD PER PRESSIONI NORMALI ---
	
	private class ThreadRunnable implements Runnable {
    	/** Tempo da aspettare in ms prima di considerare una pressione prolungata su un tasto
         * come delle pressioni */
        final static int KEY_REPEAT_WAIT = 750;
        /** Numero di keystroke inviati al secondo  */
        final static int KEY_FREQ = 30;
        /** Periodo di attesa (in ms) prima di riconoscere la peressione consecutiva per un tasto */
        final static int KEY_SLEEP = 1000 / KEY_FREQ;
        /** Tempo di riposo del tread tra una passata dei tasti e l'altra */
        final int FREE_TIME = Math.max(1, Math.min(KEY_REPEAT_WAIT, KEY_SLEEP) / 2);

        @Override
        public void run() {
        	while(true) {
	    		try {
					Thread.sleep(FREE_TIME);
				} catch (InterruptedException e) {
					return;
				}
	    		
	    		for(Map.Entry<MacroKey, KeyDown> p : pressedKeys.entrySet()) {
	    			MacroKey k = p.getKey();
	    			KeyDown u = p.getValue();
	    			
	    			assert k != null && u != null;
	    			
	    			if(u.type == MacroKeyType.Normal) {
						long d = System.currentTimeMillis() - u.lastDownTime;
						assert d >= 0;
						
						//Non è stato premuto per la prima volta
						if(!u.firstDown) {
							u.firstDown = true;
							u.lastDownTime = System.currentTimeMillis();
							pressAndReleaseKey(k);
							//E' stato premuto per la prima volta, aspetto prima di ripetere la pressione
						} else if(!u.firsPressOccurred && d >= KEY_REPEAT_WAIT) {
							u.firsPressOccurred = true;
							u.lastDownTime = System.currentTimeMillis();
							pressAndReleaseKey(k);
							//Aspetto per le pressioni successive
						} else if(u.firsPressOccurred && d >= KEY_SLEEP) {
							u.lastDownTime = System.currentTimeMillis();
							pressAndReleaseKey(k);
						}
					}
	    		}
        	}
        }
    }
	
    private class KeyDown {
    	
    	/** Identificativo del client che ha premuto il tasto */
    	private String clientId;
    	
    	/** Tipologia di pressione associata */
    	private MacroKeyType type;
    	
    	/**
    	 Istante di tempo in millisecondi, nel quale la richiesta
    	 di pressione è stata fatta
    	 */
    	private long lastDownTime = 0;
    	/**
    	 * True: indica se il tasto è stato già premuto per la prima volta
    	 */
    	private boolean firstDown = false;
    	
    	private boolean firsPressOccurred = false;
    	
    	private KeyDown(String clientId, MacroKeyType type) {
    		this.clientId = clientId;
    		this.type = type;
    	}
    }
	
	
	//--- LISTENERS ---
	
	
	/**
	 * Aggiunge un listener per il compimento di un'azione
	 * @param l Listener da aggiungere
	 * @throws NullPointerException Se {@code l} è null
	 */
	public final void addEventListener(EventListener l) {
		Objects.requireNonNull(l);
		
		synchronized (eventListeners) {
			eventListeners.add(l);
		}
	}
	
	/**
	 * Rimuove tutte le istanze del listener indicato
	 * @param l Listener da rimuovere
	 * @throws NullPointerException Se {@code l} è null
	 */
	public final void removeEventListener(EventListener l) {
		Objects.requireNonNull(l);
		
		synchronized (eventListeners) {
			removeInstanceOf(eventListeners, l);
		}
		
	}
	
	
	
	/**
	 * Rimuove tutte le istanze di {@code instance} in {@code list}
	 * @param list Lista dalla quale rimuovere le istanze; non null
	 * @param instance Istanza da rimuovere
	 */
	private static <T> void removeInstanceOf(List<T> list, T instance) {
		assert list != null;
		
		Iterator<T> it = list.iterator();
		while(it.hasNext()) {
			if(it.next() == instance) {
				it.remove();
			}
		}
	}
	
	
	
	/** 
	 * Genera l'evento di tasto premuto o rilasciato
	 * @param source Identificativo del client che ha inviato l'azione; non null
	 * @param mk Tasto premuto o rilasciato; non null
	 * @param action True: pressione; False rilascio
	 */
	private void fireKeyRecivedListener(String source,
			MacroKey mk, boolean action) {
		assert source != null;
		assert mk != null;
		
		synchronized (eventListeners) {
			for(EventListener l : eventListeners) {
				l.onKeyReceved(this, source, mk, action);
			}
		}
		
	}
	
	
	
	/**
	 * Genera l'evento di connessione da parte di un client
	 * @param s Identificativo del client connesso; non null
	 */
	private void fireClientConnectListener(String s) {
		assert s != null;
		
		synchronized (eventListeners) {
			for(EventListener l : eventListeners) {
				l.onConnectListener(this, s);
			}
		}
	}
	
	
	
	
	/**
	 * Genera l'evento di connessione da parte di un client
	 * @param s Identificativo del cleint; non null
	 */
	private void fireClientDisconnectListener(String s) {
		assert s != null;
		
		synchronized (eventListeners) {
			for(EventListener l : eventListeners) {
				l.onDisconnectListener(this, s);
			}
		}
	}
	
	
	
	/**
	 * Genera l'evento di chiusura del server
	 */
	private void fireServerCloseEvent() {
		synchronized(eventListeners) {
			for(EventListener l : eventListeners) {
				l.onClose(this);
			}
		}
	}
	
	
	
	/**
	 * Genera l'evento di cambio dello stato di sospensione del server
	 * @param newState Nuovo stato di sospensione
	 */
	private void fireServerSuspendEvent(boolean newState) {
		synchronized(eventListeners) {
			for(EventListener l : eventListeners) {
				l.onSuspendChanged(this, newState);
			}
		}
	}
	
	
	
	/**
	 * Genera l'evento di cambiamento della {@link MacroSetup}
	 * @param actual Nuova {@link MacroSetup} utilizzata, null se nessuna
	 */
	private void fireServerChangeMacroSetup(MacroSetup actual) {		
		synchronized(eventListeners) {
			for(EventListener l : eventListeners) {
				l.onMacroSetupChanged(this, actual);
			}
		}
	}
	


	/**
	 * Listener per eventi associati al server
	 */
	public interface EventListener {
		/**
		 * Generato alla ricezione di un tasto
		 * @param server Istanza del server che ha generato l'evento; non null
		 * @param sender Identificativo del client mittente
		 * @param mk Tasto premuto
		 * @param action True pressione; False rilascio
		 * @throws NullPointerException Se {@code sender} o {@code k} sono {@code null}
		 */
		void onKeyReceved(MacroServer server, String sender, MacroKey mk, boolean action);
		
		/**
		 * Alla connessione di un nuovo client
		 * @param server Istanza del server che ha generato l'evento; non null
		 * @param sender Identificativo del client connesso
		 * @throws NullPointerException Se {@code s} è null
		 */
		void onConnectListener(MacroServer server, String sender);
		
		/**
		 * Alla disconnessione di un client
		 * @param server Istanza del server che ha generato l'evento; non null
		 * @param sender Identificativo del client disconnesso
		 * @throws NullPointerException Se {@code s} è null
		 */
		void onDisconnectListener(MacroServer server, String sender);
		
		/**
		 * Server chiude la connessione
		 * @param server Istanza del server che ha generato l'evento; non null
		 */
		void onClose(MacroServer server);
		
		/**
		 * Server viene sospeso
		 * @param server Istanza del server che ha generato l'evento; non null
		 * @param newState Nuovo stato: True sospeso, False altrimenti
		 */
		void onSuspendChanged(MacroServer server, boolean newState);
		
		/**
		 * Server cambia {@link MacroSetup} utilizzata
		 * @param server Istanza del server che ha generato l'evento; non null
		 * @param actual Nuova {@link MacroSetup}; null se nessuna
		 */
		void onMacroSetupChanged(MacroServer server, MacroSetup actual);
	}
	
	
	
	/**
	 * Stato attuale del server
	 */
	public enum State {
		/** Server aspetta l'avvio tramite metodo {@link MacroServer#start()}*/
		WaitStart,
		/** Il server funziona regolarmente */
		Functional,
		/** Il server è stato chiuso */
		Closed
	}
}
