package com.macrokeys.netcode;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.jdt.annotation.NonNull;

/** Class to encrypt messages with RSA */
public final class RSAClient {

    /** Pubblic key */
    private PublicKey publicKey;

    /** Cifer RSA */
    private Cipher cEnc;

    /**
     * @param k Pubblic key
     * @exception InvalidKeyException If the key is invalid
     */
    public RSAClient(@NonNull PublicKey k) {
    	Objects.requireNonNull(k);
    	
        try {
            publicKey = k;
            cEnc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cEnc.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new AssertionError("Algorithm must be present");
        }
    }

    /**
     * Crypt the given data
     * @param data Data to crypt
     * @return Encrypted data
     */
    public byte[] encript(@NonNull byte[] data) {
    	Objects.requireNonNull(data);
    	
        try {
            byte[] cipherData = cEnc.doFinal(data);
            return cipherData;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new AssertionError("Error during encription");
        }
    }

    /**
     * @return Public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Converts a byte array in their respective pubblic key
     * @param b Byte sequence
     * @return Public key
     */
    public static PublicKey byteToPublicKey(@NonNull byte[] b) {
    	Objects.requireNonNull(b);
    	
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(b);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        	throw new AssertionError();
        }
    }
}


