package com.macrokeys.netcode;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/** Class to encrypt and decrypt messages with RSA */
public final class RSAServer {
	
	private static final String ALG = "RSA";
	
	/** Key pair (public and private) */
	private KeyPair keyPair;
	
	/** Cipher for the encryption */
	private Cipher cEnc;
	
	/** Cipher for the decryption */
	private Cipher cDec;
	
	/**
	 * @param length Lenght of the key to generate 
	 * @throws InvalidParameterException If the keylength is not supported
	 */
	public RSAServer(int length) {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance(ALG);
			gen.initialize(length);
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
	 * Encrypt the given sequence of data
	 * @param data Data to encrypt
	 * @return Encrypted data
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
	 * Decrypts the given sequence
	 * @param data Data to decrypt
	 * @return Decrypted data
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
	 * @return Publick key
	 */
	public byte[] getPublicKey() {
		return keyPair.getPublic().getEncoded();
	}
}
