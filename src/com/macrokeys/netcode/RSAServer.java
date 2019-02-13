package com.macrokeys.netcode;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/** Permette di criptare e decriptare messaggi con la crittografia RSA */
public final class RSAServer {
	
	private static final String ALG = "RSA";
	
	/** Coppia di chiavi pubblica e privata per la comunicazione */
	private KeyPair keyPair;
	
	/** Per criptare */
	private Cipher cEnc;
	
	/** Per decriptare */
	private Cipher cDec;
	
	/**
	 * @param Length - Lunghezza della chiave in bit
	 * @throws InvalidParameterException - Parametro errato
	 */
	public RSAServer(int Length) {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance(ALG);
			gen.initialize(Length);
			keyPair = gen.generateKeyPair();
			
			cEnc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			cEnc.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			
			cDec = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			cDec.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			assert false : e.getMessage();
		}
	}
	
	/**
	 * Cripta la sequenza di dati
	 * @param data - Dati da criptare
	 * @return Dati criptati
	 */
	public byte[] encript(byte[] data) {
		try {
			byte[] cipherData = cEnc.doFinal(data);
			return cipherData;
		} catch (IllegalBlockSizeException | BadPaddingException e1) {
			assert false : "Error during encription";
			return null;
		}
	}
	
	
	/**
	 * Decripta la sequenza di byte
	 * @param data - Dati da decriptare
	 * @return Dati decriptati
	 */
	public byte[] decript(byte[] data) {
		try {
			byte[] cipherData = cDec.doFinal(data);
			return cipherData;
		} catch (IllegalBlockSizeException | BadPaddingException e1) {
			assert false : "Error during decription";
			return null;
		}
	}
	
	/**
	 * @return Chiave pubblica
	 */
	public byte[] getPublicKey() {
		return keyPair.getPublic().getEncoded();
	}
}
