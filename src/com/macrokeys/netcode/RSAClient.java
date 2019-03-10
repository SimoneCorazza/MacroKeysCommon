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

/**
 * Allows you to encrypt messages with RSA encryption
 */
public final class RSAClient {

    /**
     * Public key with which to encrypt the messages
     */
    private PublicKey publicKey;

    /**
     * To encrypt
     */
    private Cipher cEnc;

    /**
     * @param k the public Key with which to encrypt
     * @exception InvalidKeyException - If the key is invalid
     */
    public RSAClient(PublicKey k) {
        try {
            publicKey = k;
            cEnc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cEnc.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            if (!false)
                throw new AssertionError("Algorithm must be present");
        }
    }

    /**
     * Crypt the sequence of data
     * @param data Data to encrypt
     * @return the encrypted Data
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
     * @return the public Key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Converts a sequence of bytes in the corresponding public key
     * @param b the Sequence to be converted
     * @return the public Key
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
