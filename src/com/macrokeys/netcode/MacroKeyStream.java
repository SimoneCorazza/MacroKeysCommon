package com.macrokeys.netcode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

import com.macrokeys.MacroKey;

/** 
 * Classe che astrae il livello di comunicazione TCP per la comunicazione
 * tra client e server per l'invio delle macro
 */
final class MacroKeyStream {

	private final Socket socket;
	private final DataOutputStream outStr;
	private final DataInputStream inStr;
	
	/**
	 * @param s Socket per la comunicazione 
	 * @throws IOException In caso di errore di IO
	 */
	public MacroKeyStream(Socket s) throws IOException {
		Objects.requireNonNull(s);
		
		this.socket = s;
		
		OutputStream str = socket.getOutputStream();
		this.outStr = new DataOutputStream(str);
		
		this.inStr = new DataInputStream(socket.getInputStream());
	}
	
	/**
	 * Invia un messaggio per mantenere in vita la connessione.
	 * Per evitare il timeout del server.
	 * @throws IOException In caso di errore di IO
	 */
	public void sendKeepAlive() throws IOException {
		outStr.writeBoolean(true); //Flag per l'inizio di un 
	}
	
	/**
	 * Avvisa il server dell'esecuzione dell'azione
	 * @param mk Tasto soggetto all'azione
	 * @param action Azione relativa al tasto. True: premuto, False: rilasciato
	 * @throws IOException In caso di errore di IO
	 */
	public void sendKeyAction(MacroKey mk, boolean action) throws IOException {
		outStr.writeBoolean(false);
		outStr.writeInt(mk.getId());
		outStr.writeBoolean(action);
	}
	
	/**
	 * Aspetta la ricezione della prossima azione di un tasto
	 * @return Azione del tasto
	 * @throws IOException In caso di errore di IO
	 */
	public MacroKeyAction receiveKeyAction() throws IOException {
		boolean messageFlag;
		do {
			//Leggo la tipologia di messaggio
			messageFlag = inStr.readBoolean();
		} while(messageFlag);
		
		int id = inStr.readInt();
		boolean ac = inStr.readBoolean();
		return new MacroKeyAction(id, ac);
	}
	
	
	
	public class MacroKeyAction {
		public final boolean action;
		public final int macroKeyId;
		
		public MacroKeyAction(int macroKeyId, boolean action) {
			this.macroKeyId = macroKeyId;
			this.action = action;
		}
	}
}
