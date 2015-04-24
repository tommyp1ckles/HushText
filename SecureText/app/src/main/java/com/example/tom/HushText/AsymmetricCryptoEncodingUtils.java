package com.example.tom.HushText;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Provides static methods for building key objects from base 64 encoded key strings. Please note
 * that the encoded keys must be the same type of key as whats specified in HushTextConstants.
 * Created by Tom on 15-03-13.
 * @author  Tom Hadlaw
 * @version 1.0
 * @since   2015-03-31
 */
public class AsymmetricCryptoEncodingUtils {
    /**
     * Builds public key from base 64 encoded key string.
     * @param keyString Base 64 encoded keystring.
     * @return PublicKey object built from encoded key string.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey makePublicKeyFromString(String keyString) throws
            NoSuchAlgorithmException, InvalidKeySpecException {
        //Security.addProvider(new BouncyCastleProvider());
        byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        //PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        /*KeyFactory keyFactory = KeyFactory.getInstance(
                HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM,
                HushTextConstants.CRYPTO_PROVIDER);
        */
        KeyFactory keyFactory = KeyFactory.getInstance(
                HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Builds private key from base 64 encoded key string.
     * @param keyString Base 64 encoded keystring.
     * @return PrivateKey Object built from encoded key string.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey makePrivateKeyFromString(String keyString) throws
            NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        //X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(
                HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }
}
