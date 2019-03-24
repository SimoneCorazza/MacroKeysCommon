package com.macrokeys.netcode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import com.macrokeys.MacroKey;

/** 
 * Class that abstract the TCP comunication between client and server
 */
final class MacroKeyStream {

	private final Socket socket;
	private final DataOutputStream outStr;
	private final DataInputStream inStr;
	
	/**
	 * @param s Comunication socket
	 * @throws IOException In case of an IO error
	 */
	public MacroKeyStream(@NonNull Socket s) throws IOException {
		Objects.requireNonNull(s);
		
		this.socket = s;
		
		OutputStream str = socket.getOutputStream();
		this.outStr = new DataOutputStream(str);
		
		this.inStr = new DataInputStream(socket.getInputStream());
	}
	
	/**
	 * Send a message as a keepalive for the connetion.
	 * To avoid timeout form the server.
	 * @throws IOException In case of an IO error
	 */
	public void sendKeepAlive() throws IOException {
		outStr.writeBoolean(true); //Flag per l'inizio di un 
	}
	
	/**
	 * Inform the server of the execution of the action
	 * @param mk Key subject to the action
	 * @param action True: pressed, False: released
	 * @throws IOException In case of an IO error
	 */
	public void sendKeyAction(@NonNull MacroKey mk, boolean action) throws IOException {
		outStr.writeBoolean(false);
		outStr.writeInt(mk.getId());
		outStr.writeBoolean(action);
	}
	
	/**
	 * Waits the next action of a key
	 * @return Action of a key
	 * @throws IOException In case of an IO error
	 */
	public MacroKeyAction receiveKeyAction() throws IOException {
		boolean messageFlag;
		do {
			// Reads the type of message
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
