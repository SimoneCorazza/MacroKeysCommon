package com.macrokeys.netcode;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/** Permette di criptare messaggi con la crittografia RSA */
public final class RSAClient {

    /** Chiave pubblica tramite la quale criptare i messaggi */
    private PublicKey publicKey;

    /** Per criptare */
    private Cipher cEnc;

    /**
     * @param k Chiave pubblica con la quale criptare
     * @exception InvalidKeyException - Se la chaive è invalida
     */
    public RSAClient(PublicKey k) {
        try {
            publicKey = k;
            cEnc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cEnc.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            if (!false) throw new AssertionError("Algorithm must be present");
        }
    }

    /**
     * Cripta la sequenza di dati
     * @param data Dati da criptare
     * @return Dati criptati
     */
    public byte[] encript(byte[] data) {
        try {
            byte[] cipherData = cEnc.doFinal(data);
            return cipherData;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new AssertionError("Error during encription");
        }
    }

    /**
     * @return Chiave pubblica
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Converte una sequenza di byte nella rispettiva chiave pubblica
     * @param b Sequenza da convertire
     * @return Chiave pubblica
     */
    public static PublicKey byteToPublicKey(byte[] b) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(b);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        	throw new AssertionError();
        }
    }
}


