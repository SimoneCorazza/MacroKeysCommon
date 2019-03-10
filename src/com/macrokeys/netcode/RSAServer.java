package com.macrokeys.netcode;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Allows you to encrypt and decrypt messages with RSA encryption
 */
public final class RSAServer {

    private static final String ALG = "RSA";

    /**
     * Pair of keys (public and private communication
     */
    private KeyPair keyPair;

    /**
     * To encrypt
     */
    private Cipher cEnc;

    /**
     * To decrypt
     */
    private Cipher cDec;

    /**
     * @param Length Length of the key in bits
     * @throws InvalidParameterException If a parameter is incorrect
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
     * Crypt the sequence of data
     * @param data Data to encrypt
     * @return The encrypted data
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
     * Decrypt the byte sequence
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
     * @return The public key
     */
    public byte[] getPublicKey() {
        return keyPair.getPublic().getEncoded();
    }
}
