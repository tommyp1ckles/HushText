package com.example.tom.HushText;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Created by Tom on 15-02-18
 * Provides tools for initiating HushText key exchange.
 */
public class InitKeyExchange {
    private KeyPairGenerator keyPairGenerator;
    private KeyPair keyPair;
    private String alg;
    private int ksize;
    //Initiate key exchange.

    /**
     * Constructor for InitKeyExchange, generates a keypair of specified algorithm and key size on
     * initiation.
     * @param Algorithm String of algorithm to generate keypair of.
     * @param keySize Size of key to generate (Note: different keytypes may have different
     *                restrictions on keysize).
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public InitKeyExchange(String Algorithm, int keySize)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        //keyPairGenerator = KeyPairGenerator.getInstance(Algorithm, new BouncyCastleProvider());
        keyPairGenerator = KeyPairGenerator.getInstance(Algorithm);
        keyPairGenerator.initialize(keySize);
        keyPair = keyPairGenerator.generateKeyPair();
    }

    /**
     * Gets the keypair generated on initiation.
     * @return KeyPair object generated on constructor.
     */
    public KeyPair getKeyPair() {
        return keyPair;
    }

    /**
     * Gets a PublicKey from the classes KeyPair generated on initiation.
     * @return Public key corresponding to instances keypair.
     */
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    /**
     * Gets a PrivateKey from the classes KeyPair generated on initiation.
     * @return PrivateKey corresponding to instances keypair.
     */
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    /**
     * Decrypts an encrypted encoded symmetric key that was encrypted by the instances public key
     * @param encryptedKeyString The encrypted symmetric key string.
     * @param symmetricAlgorithm String of symmetric algorithm.
     * @param asymmetricAlgorithm String of asymmetric algorithm.
     * @param asymmetricKeySize Size of asymmetric key.
     * @return The decoded and decrypted secret key.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public SecretKey decryptEncodedKey(String encryptedKeyString, String symmetricAlgorithm,
                                       String asymmetricAlgorithm, int asymmetricKeySize)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] encodedKeyBytes = Base64.decode(encryptedKeyString, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance(asymmetricAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decryptedKeyBytes = cipher.doFinal(encodedKeyBytes);
        SecretKey symmetricKey = new SecretKeySpec(
                decryptedKeyBytes, 0, decryptedKeyBytes.length, symmetricAlgorithm);
        return symmetricKey;
    }
}