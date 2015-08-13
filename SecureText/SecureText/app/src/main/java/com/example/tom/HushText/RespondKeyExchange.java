package com.example.tom.HushText;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by Tom on 15-02-18.
 * Provides tools used when responding to a key exchange (other party initiated).
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-01
 */
public class RespondKeyExchange {
    /**
     * Creates a HushText response message string based on a key exchange request received.
     * @param symmetricKey Symmetric key to respond with.
     * @param publicKey Public key to respond with.
     * @param algorithm Public key algorithm.
     * @param keySize Public key size.
     * @return Response string message.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String RespondKeyExchange(
            SecretKey symmetricKey, PublicKey publicKey, String algorithm, int keySize)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        //Cipher cipher = Cipher.getInstance(algorithm, new BouncyCastleProvider());
        //Cipher cipher = Cipher.getInstance(algorithm, HushTextConstants.CRYPTO_PROVIDER);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encodedSymKey = symmetricKey.getEncoded();
        byte[] encryptedSymmetricKeyBytes  = cipher.doFinal(encodedSymKey);
        return Base64.encodeToString(encryptedSymmetricKeyBytes, Base64.DEFAULT);
        //String base64EncodedString = Base64.encodeToString(encodedSymKey, Base64.DEFAULT);
        //return base64EncodedString;
    }
}
