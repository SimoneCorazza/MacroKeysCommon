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

import org.eclipse.jdt.annotation.NonNull;

import com.macrokeys.MacroKey;
import com.macrokeys.MacroKeyType;
import com.macrokeys.LimitedKeySequence;
import com.macrokeys.MacroSetup;

/**
 * Abstract class for a server that sends {@link MacroSetup} at the {@link MacroClient}
 * and receive from them the pressed {@link MacroKey}
 */
public abstract class MacroServer {
	
	/** Actuator of the keystrokes */
	private final KeyPresser keyPresser;
	
	/** Listener for the event of {@code this}; never null */
	private final List<EventListener> eventListeners = new ArrayList<>();
	
	
	/** {@link MacroKey} currently pressed (key down) */
	private final HashMap<MacroKey, KeyDown> pressedKeys = new HashMap<>();
	
	/** Thread for the pression of {@link MacroKeyType#Normal} keys */
	private final Thread normalPresser = new Thread(new ThreadRunnable());
	
	/** Flag for the suspended mode of the server */
	private boolean suspend = false;
	
	/** Setup currently used; never null */
	private MacroSetup setup;
	
	/** 
	 * Data rapresenting the {@link MacroSetup} currently used,
	 * get from {@link #setup}; never null
	 */
	private byte[] macroSetupData;
	
	
	
	
	/** Thread to welcome the new connection from clients */
	private Thread threadListener;
	
	/** Thread to inform the clients of the service of this server */
	private Thread threadIntroduce;
	
	/** 
	 * Clients currently connected; the key of the dictionary is the id of the client
	 */
	private final ConcurrentHashMap<String, ClientInfo> clients =
			new ConcurrentHashMap<>();
	
	
	/** Current state of the server */
	private State state = State.WaitStart;
	
	
	/**
	 * @param setup Initial setup
	 * @throws AWTException In case of error whili initializing an instance of {@link Robot}
	 */
	public MacroServer(@NonNull MacroSetup setup) throws AWTException {
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
		normalPresser.interrupt();
		for(MacroKey k : pressedKeys.keySet()) {
			releaseKey(k);
		}
	}
	
	
	
	
	/**
	 * Start the server
	 * @throws IOException In case of IO error
	 * @throws IllegalStateException If {@code this} is not in the state
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
	 * Init the server; the server start to listen to the incoming client requests
	 * @throws IOException In case of an IO error
	 */
	protected abstract void innerStart() throws IOException;
	
	
	
	/**
	 * @return Current state of the server
	 */
	public final State getState() {
		return state;
	}
	
	
	
	/**
	 * Informs the client that this machine offer this serive
	 * <p>
	 * Executent in a dedicated thread
	 * </p>
	 */
	protected abstract void introduceServerToClient();
	
	
	
	
	/**
	 * Waits new client's connections
	 * <p>
	 * Executed in the thread {@link #threadListener}
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
			
			
			if(!isSeekConnection()) {
				try {
					messProt.close();
				} catch (IOException e) {
					continue;
				}
			}
			
			// Set timeout for the comunication
			messProt.setInputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
			messProt.setOutputKeepAlive(ComunicationParameters.TIMEOUT_MESSAGES);
				
			String clientId;
			try {
				clientId = loginClient(messProt);
			} catch(IOException e) {
				continue;
			}
			
			// Client already logged-in
			if(clients.containsKey(clientId)) {
				continue;
			}
			
			final ClientInfo info = new ClientInfo(clientId, messProt);
			clients.put(clientId, info);
			
			// Server thread for the client
			Thread th = new Thread() {
				@Override
				public void run() {
					serveClient(info);
				}
			};
			th.start();
			
			// Fire the event for the new connection of a client.
			// Done it in a separate thread to relieve this thread and avoid interruption caused by exceptions.
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
	 * Serve the given client. In particular it handles the exceptions
	 * of {@link #ServeClientExceptions(ClientInfo)}
	 * @param info The client to serve
	 * @see #ServeClientExceptions(ClientInfo)
	 */
	private void serveClient(@NonNull ClientInfo info) {		
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
	 * Login for the client
	 * @param messProt Comunication layer with the client
	 * @return Id of the client
	 * @throws IOException In case of IO error
	 */
	private String loginClient(MessageProtocol messProt) throws IOException {
		// TODO: identify the client and implement securety
		return messProt.toString();
	}

	/**
	 * Handles the input of the given client
	 * @param info Client to handle
	 * @throws IOException In case of IO error
	 */
	private void serveClientExceptions(@NonNull ClientInfo info)
			throws IOException {
		assert info != null;
		assert getMacroSetup() != null;
		
		
		// Send the macro setup
		byte[] macroSetupData = getMacroSetupData();
		assert macroSetupData != null;
		info.messProt.sendMessage(macroSetupData);
		
		
		
		// Receive the request from the client
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
	 * Wait a connection of a client
	 * @return Comunication layer with the newly connected client
	 */
	protected abstract MessageProtocol waitNewClientConnection() throws IOException;
	
	
	
	/**
	 * Sets the flag of the suspension of the server. The macro sent by the client
	 * are not executed if true
	 * @param s Flag
	 * @throws IllegalStateException If {@code this} is not in the state
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
				for(MacroKey k : pressedKeys.keySet()) {
					releaseKey(k);
				}
				pressedKeys.clear();
			}
			fireServerSuspendEvent(s);
		}
	}
	
	
	
	
	/**
	 * @return True if the server does not execute the given macros from the client, false otherwise
	 */
	public final boolean isSuspended() {
		return suspend;
	}
	
	
	
	
	/**
	 * @return True if the server excepts new connections from clients, false otherwise
	 */
	public abstract boolean isSeekConnection();
	
	
	
	
	/**
	 * Sets the flag that indicates if the server accept new incomming connections
	 * @param b The state of the new flag
	 */
	public abstract void setSeekConnection(boolean b);
	
	
	
	/** 
	 * Close the server interrupting his activity.
	 * The state changes to {@link State#Closed} if this method not throws.
	 * @throws IllegalStateException If {@code this} is not in the state {@link State#Functional}
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
			} catch (IOException e) {
				// Nothing
			}
		}
		
		threadIntroduce.interrupt();
		threadListener.interrupt();
		normalPresser.interrupt();
		
		
		innerClose();
		state = State.Closed;
		
		fireServerCloseEvent();
	}
	
	/**
	 * Implements the close of the server
	 * Called by {@link #close()}
	 */
	protected abstract void innerClose();
	
	
	
	/**
	 * Change the actual {@link MacroSetup}
	 * <p>Syncronous method; better not call this on the UI thread</p>
	 * @param m New {@link MacroSetup} to use
	 */
	public final void changeMacroSetup(@NonNull MacroSetup m) {
		Objects.requireNonNull(m);
		assert setup != null;
		
		this.setup = m;
		try {
			this.macroSetupData = setup.saveAsByteArray();
		} catch(IOException e) {
			e.printStackTrace();
			assert false : "Should not happend";
		}
		
		
		// Send to the client the new MacroSetup
		byte[] macroSetupData = getMacroSetupData();
		for(ClientInfo p : clients.values()) {
			try {
				p.messProt.sendMessage(macroSetupData);
			} catch(IOException e) {
				// Nothing
			}
		}

		
		
		fireServerChangeMacroSetup(m);
	}
	
	
	/**
	 * @return Actual used {@link MacroSetup}
	 */
	public final @NonNull MacroSetup getMacroSetup() {
		return this.setup;
	}
	
	
	/**
	 * @return Binary representation of the {@link MacroSetup} actually selected
	 * @see #getMacroSetup()
	 */
	protected final @NonNull byte[] getMacroSetupData() {
		return macroSetupData;
	}
	
	
	
	/**
	 * Press the {@link MacroKey} if the flag {@link #isSuspended()} is false
	 * @param mk Key to press
	 * @param clientId Id of the client that pressed the key
	 * @return True if the key was pressed or the flag {@link #isSuspended()}
	 * is true. False if the key was already pressed.
	 */
	private boolean pressKey(@NonNull MacroKey mk, String clientId) {
		assert mk != null;
		assert clients.containsKey(clientId);
		assert getState().equals(State.Functional);
		
		// If suspended or already pressed i ignore the press
		if(suspend || pressedKeys.containsKey(mk)) {
			return false;
		}
		
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
		
		// Mark the key as pressed
		pressedKeys.put(mk, new KeyDown(clientId, mk.getType()));
		
		fireKeyRecivedListener(clientId, mk, true);
		
		return true;
	}
	
	
	
	/**
	 * Release the {@link MacroKey} regardless of the flag {@link #isSuspended()}.
	 * @param mk Key to release
	 * @return True if the key was released
	 * 	False if the key was not pressed.
	 */
	protected boolean releaseKey(@NonNull MacroKey mk) {
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
	 * Press and release the {@link MacroKey} regardless of the flag {@link #isSuspended()}.
	 * @param mk Key to press
	 */
	private void pressAndReleaseKey(@NonNull MacroKey mk) {
		assert mk != null;
		assert getState().equals(State.Functional);
		
		LimitedKeySequence l = mk.getKeySeq();
		keyPresser.press(l);
		keyPresser.release(l);
	}
	
	
	
	
	
	
	/**
	 * Information about a client connected to the server
	 */
	private final static class ClientInfo {
		private final String clientId;
		private final MessageProtocol messProt;
		
		
		/**
		 * @param clientId Id of the client
		 * @param p Comunication layer with the client
		 */
		public ClientInfo(@NonNull String clientId, @NonNull MessageProtocol p) {
			Objects.requireNonNull(clientId);
			Objects.requireNonNull(p);
			
			this.clientId = clientId;
			this.messProt = p;
		}
	}
	
	
	
	/** Thread for normal pressions */
	private class ThreadRunnable implements Runnable {
    	/** Time to wait in ms before repeating a key press of a pressed key */
        final static int KEY_REPEAT_WAIT = 750;
        
        /** Frequency of keystroke to send */
        final static int KEY_FREQ = 30;
        
        /** Period of wait (in ms) before recognizing a pression as multiple keystroke */
        final static int KEY_SLEEP = 1000 / KEY_FREQ;
        
        /** Sleep time between one keypress loop and the next */
        final int FREE_TIME = Math.max(1, Math.min(KEY_REPEAT_WAIT, KEY_SLEEP) / 2);

        @Override
        public void run() {
        	while(true) {
	    		try {
					Thread.sleep(FREE_TIME);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
	    		
	    		for(Map.Entry<MacroKey, KeyDown> p : pressedKeys.entrySet()) {
	    			MacroKey k = p.getKey();
	    			KeyDown u = p.getValue();
	    			
	    			assert k != null && u != null;
	    			
	    			if(u.type == MacroKeyType.Normal) {
						long d = System.currentTimeMillis() - u.lastDownTime;
						assert d >= 0;
						
						if(!u.firstDown) {
							u.firstDown = true;
							u.lastDownTime = System.currentTimeMillis();
							pressAndReleaseKey(k);
						} else if(!u.firsPressOccurred && d >= KEY_REPEAT_WAIT) {
							// It was pressed for the first time: waiting before repeating the pressure
							
							u.firsPressOccurred = true;
							u.lastDownTime = System.currentTimeMillis();
							pressAndReleaseKey(k);
						} else if(u.firsPressOccurred && d >= KEY_SLEEP) {
							// Wating for the following pressures
							
							u.lastDownTime = System.currentTimeMillis();
							pressAndReleaseKey(k);
						}
					}
	    		}
        	}
        }
    }
	
    private class KeyDown {
    	
    	/** Id of the client that pressed the key */
    	private String clientId;
    	
    	/** Pressure type */
    	private MacroKeyType type;
    	
    	/**
    	 Timestamp where the request was made
    	 */
    	private long lastDownTime = 0;
    	
    	/**
    	 * True: indicates that the key was already pressed for the first time
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
	 * Add a listener for the completition of an action
	 * @param l Listener to add
	 */
	public final void addEventListener(@NonNull EventListener l) {
		Objects.requireNonNull(l);
		
		synchronized (eventListeners) {
			eventListeners.add(l);
		}
	}
	
	/**
	 * Remove the listener instances for the action event
	 * @param l instance of listener to remove
	 */
	public final void removeEventListener(@NonNull EventListener l) {
		Objects.requireNonNull(l);
		
		synchronized (eventListeners) {
			removeInstanceOf(eventListeners, l);
		}
		
	}
	
	
	
	/**
	 * Remove all instnces of {@code instance} in {@code list}
	 * @param list List where remove the instance
	 * @param instance Instance to remove
	 */
	private static <T> void removeInstanceOf(@NonNull List<T> list, T instance) {
		assert list != null;
		
		Iterator<T> it = list.iterator();
		while(it.hasNext()) {
			if(it.next() == instance) {
				it.remove();
			}
		}
	}
	
	
	
	/** 
	 * Generates the event of key pressed or released
	 * @param source Id of the client that sent the action
	 * @param mk Key subject of the action
	 * @param action True: pression; False release
	 */
	private void fireKeyRecivedListener(@NonNull String source,
			@NonNull MacroKey mk, boolean action) {
		assert source != null;
		assert mk != null;
		
		synchronized (eventListeners) {
			for(EventListener l : eventListeners) {
				l.onKeyReceved(this, source, mk, action);
			}
		}
		
	}
	
	
	
	/**
	 * Generate a connection event of a client
	 * @param s Id of the connected client
	 */
	private void fireClientConnectListener(@NonNull String s) {
		assert s != null;
		
		synchronized (eventListeners) {
			for(EventListener l : eventListeners) {
				l.onConnectListener(this, s);
			}
		}
	}
	
	
	
	
	/**
	 * Generate a disconnection event of a client
	 * @param s Id of the disconnected client
	 */
	private void fireClientDisconnectListener(@NonNull String s) {
		assert s != null;
		
		synchronized (eventListeners) {
			for(EventListener l : eventListeners) {
				l.onDisconnectListener(this, s);
			}
		}
	}
	
	
	
	/**
	 * Generates the event of closure of the server
	 */
	private void fireServerCloseEvent() {
		synchronized(eventListeners) {
			for(EventListener l : eventListeners) {
				l.onClose(this);
			}
		}
	}
	
	
	
	/**
	 * Generates the event of a change in the suspension state of this server
	 * @param newState New state of suspension
	 */
	private void fireServerSuspendEvent(boolean newState) {
		synchronized(eventListeners) {
			for(EventListener l : eventListeners) {
				l.onSuspendChanged(this, newState);
			}
		}
	}
	
	
	
	/**
	 * Generates the event of change of {@link MacroSetup}
	 * @param actual New {@link MacroSetup} used; null if none
	 */
	private void fireServerChangeMacroSetup(MacroSetup actual) {		
		synchronized(eventListeners) {
			for(EventListener l : eventListeners) {
				l.onMacroSetupChanged(this, actual);
			}
		}
	}
	


	/**
	 * Listener for events of the server
	 */
	public interface EventListener {
		/**
		 * Generated at the receive of a key
		 * @param server Instance of the server that generated the event
		 * @param sender Client sender id
		 * @param mk Key pressed
		 * @param action True pression; False release
		 */
		void onKeyReceved(@NonNull MacroServer server, @NonNull String sender, @NonNull MacroKey mk, boolean action);
		
		/**
		 * Generated at the connection of a new client
		 * @param server Instance of the server that generated the event
		 * @param sender Id of the newly connected client
		 */
		void onConnectListener(@NonNull MacroServer server, @NonNull String sender);
		
		/**
		 * Generated at the disconnection of a client
		 * @param server Instance of the server that generated the event
		 * @param sender Client sender id
		 */
		void onDisconnectListener(@NonNull MacroServer server, @NonNull String sender);
		
		/**
		 * Generated when the server close the connection
		 * @param server Instance of the server that generated the event
		 */
		void onClose(@NonNull MacroServer server);
		
		/**
		 * Generated at the suspension of the server
		 * @param server Instance of the server that generated the event
		 * @param newState New state flag: True suspended, False otherwise
		 */
		void onSuspendChanged(@NonNull MacroServer server, boolean newState);
		
		/**
		 * Generated at the ghange of the {@link MacroSetup} used
		 * @param server Instance of the server that generated the event
		 * @param actual New {@link MacroSetup} used; null if none
		 */
		void onMacroSetupChanged(@NonNull MacroServer server, @NonNull MacroSetup actual);
	}
	
	
	
	/**
	 * Possible state of a server
	 */
	public enum State {
		/** The server waits the init by the method {@link MacroServer#start()} */
		WaitStart,
		
		/** The server works */
		Functional,
		
		/** The server is closed */
		Closed
	}
}
