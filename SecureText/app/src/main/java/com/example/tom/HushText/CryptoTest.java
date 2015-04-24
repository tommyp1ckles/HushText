package com.example.tom.HushText;

import android.util.Base64;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * Created by tom on 3/22/2015.
 * Class created for testing purposes, performs a series of simply crypto tests by encrypting and
 * decrypting the string "life imitates art", generally this class will not actually be used in the
 * regular usage of the application.
 */
public class CryptoTest {
    /**
     * Performs the crypto test and prints results to standard output.
     */
    public static void test() {
        System.out.println("#####################################################################");
        System.out.println("########################Starting Crypto Test#########################");
        System.out.println("#####################################################################");
        String message = "life imitates art";
        System.out.println("Test message is: " + message);
        try {
            InitKeyExchange ike = new InitKeyExchange(
                    HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM,
                    HushTextConstants.ASYMMETRIC_ENCRYPTION_SIZE);
            PrivateKey pri = ike.getPrivateKey();
            PublicKey pub = ike.getPublicKey();

            //--------------------------------------------------//
            System.out.println("Doing basic symm key test");
            SecretKey key = SymmetricCryptoUtils.generateSymmetricKey("AES");
            Cipher c1 = Cipher.getInstance("AES");
            c1.init(Cipher.ENCRYPT_MODE, key);
            byte[] messageBytes = message.getBytes();
            String encryptedMessageString = Base64.encodeToString(c1.doFinal(messageBytes), Base64.DEFAULT);
            byte[] decodedMessageBytes = Base64.decode(encryptedMessageString, Base64.DEFAULT);
            c1 = Cipher.getInstance("AES");
            c1.init(Cipher.DECRYPT_MODE, key);
            String finalString = new String(c1.doFinal(decodedMessageBytes));
            System.out.println("HEAR YEEE HEAR YEE THE UNECRYPTED MESSAGE IS: " + finalString + " !!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("Done!!!");
            //--------------------------------------------------//
            System.out.println("Doing Secondary symm key test");
            String encryptedMessage2 = Base64.encodeToString(SymmetricCryptoUtils.encryptMessage(message, key), Base64.DEFAULT);
            byte[] decodedMessageBytes2 = Base64.decode(encryptedMessage2, Base64.DEFAULT);
            //String decryptedMessage2 = new String(Base64.decode(SymmetricCryptoUtils.decryptMessage(decodedMessageBytes2, key), Base64.DEFAULT));
            String decryptedMessage2 = new String(SymmetricCryptoUtils.decryptMessage(decodedMessageBytes2, key));
            System.out.println("HEAR YEEE HEAR YEE THE SECOND UNECRYPTED MESSAGE IS: " + decryptedMessage2 + " !!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("Done!!!");
            //--------------------------------------------------//
            String keyString = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
            System.out.println("Encoded Symmetric Key: " + keyString);
            Base64.decode(keyString, Base64.DEFAULT);
            SymmetricCryptoUtils.encryptMessage(message, key);

        }
        catch (Exception e) {

        }
        System.out.println("#####################################################################");
        System.out.println("########################Ending Crypto Test#########################");
        System.out.println("#####################################################################");
    }
}
