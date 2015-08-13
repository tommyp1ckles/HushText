package com.example.tom.HushText;

/* Conceal */
import com.facebook.crypto.util.NativeCryptoLibrary;

        import android.util.Base64;

        import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

        import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
        import javax.crypto.KeyGenerator;

        import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
        import javax.crypto.spec.IvParameterSpec;

        import java.security.*;

/**
 * Created by Tom on 15-01-20.
 * Contains tools for symmetric encryption.
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-01
 */
public class SymmetricCryptoUtils {
    protected NativeCryptoLibrary cryptoLib;
    private Key symmetricKey;
    public final int SMS_MESSAGE_SIZE = 160;

    /**
     * Prints available crypto algorithms and their respective provider.
     */
    public static void printProviderList() {
        for (Provider provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key: provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }
    }

    /**
     * Generates a new symmetric key of a given type.
     * @param type Type of key to generate (algorithm).
     * @return Generated new SecretKey.
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateSymmetricKey(String type) throws NoSuchAlgorithmException {
        int keySize = 192;
        /* SecureRandom seeded automatically */
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        //128, 192, 256
        /* This is the correct way to initialize the keygenerator */
        keyGenerator.init(keySize, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    /**
     * Encrypts message string using a given symmetric key.
     * @param msg Message string to encrypt.
     * @param key Key to encrypt message with.
     * @return Byte array of encrypted and encoded message.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] encryptMessage(String msg, SecretKey key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES");
        byte[] init = new byte[128 / 8];
        //SecureRandom secureRandom = new SecureRandom();
        //secureRandom.nextBytes(init);
        for (int i = 0; i < 16; i++)
            init[i] = 0;
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(init));
        byte[] msgBytes = msg.getBytes();
        //System.out.println(android.util.Base64.encode(msgBytes, Base64.DEFAULT));
        byte[] msgCipherBytes = cipher.doFinal(msgBytes);
        return msgCipherBytes;
    }

    /**
     * Decrypts a encrypted and encoded message given a key.
     * @param cipherBytes
     * @param key
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    public static byte[] decryptMessage(byte[] cipherBytes, SecretKey key) throws NoSuchAlgorithmException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException,
            InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        //ipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        byte[] init = new byte[128/ 8];
        SecureRandom secureRandom = new SecureRandom();
        //secureRandom.nextBytes(init);
        for (int i = 0; i < 16; i++)
            init[i] = 0;
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(init));
        byte[] textBytes = cipher.doFinal(cipherBytes);
        return textBytes;
    }

    /**
     * Tests a SecretKey.
     * @param key Key to test.
     */
    public static void testKey(SecretKey key) {
        String testMsg = "this is a test!";
        try {
            byte[] ct = SymmetricCryptoUtils.encryptMessage(testMsg, key);
            byte[] dt = SymmetricCryptoUtils.decryptMessage(ct, key);
            if (testMsg.equals(new String(dt))) {
                System.out.println("Key Test Successful!");
                return;
            }
            else {
                System.out.println("Key Error!");
                return;
            }
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Encryption Test: No such algorithm exception.");
        }
        catch (NoSuchPaddingException e) {
            System.out.println("Encryption Test: No such padding exception.");
        }
        catch (InvalidKeyException e) {
            System.out.println("Encryption Test: Invalid key exception.");
        }
        catch (IllegalBlockSizeException e) {
            System.out.println("Encryption Test: Illegal block size exception.");
        }
        catch (BadPaddingException e) {
            System.out.println("Encryption Test: Bad padding exception.");
        }
        catch (InvalidAlgorithmParameterException e) {
            System.out.println("Encryption Test: Invalid algorithm parameter exception.");
        }
    }
}
