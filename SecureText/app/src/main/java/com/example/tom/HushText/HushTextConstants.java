package com.example.tom.HushText;

import android.net.Uri;

import java.security.Provider;
import java.security.Security;

/**
 * Created by Tom on 15-02-14.
 * Contains various constant values for HushText, including the prefixes for HushText messages, the
 * encryption algorithms, crypto provider, key size etc.
 * @author Tom Hadlaw
 * @since 2015-03-31
 */
public class HushTextConstants {
    public static final Uri InboxUri = Uri.parse("content://sms/inbox");
    public static final Uri sentUri = Uri.parse("content://sms/sent");
    public static final String KEY_VERSION = "";
    public static final String CONVERSATION_ID_KEY = "CONVERSATION_ID";
    public final static String MMS_SMS_URI_STRING = "content://mms-sms/conversations/";
    /* Note: putting a square bracket as the first character (among others?) seems to cause the
    message to be corrupted. */
    public final static String HUSHTEXT_PUBLIC_KEY_PREFIX = "(HushTextPKey" + KEY_VERSION + ")";
    public final static String HUSHTEXT_ENCRYPED_KEY_PREFIX = "(HushTextResp" + KEY_VERSION + ")";
    public final static String ENCRYPTED_MESSAGE_PREFIX = "(HushTextMessage" + KEY_VERSION + ")";
    public final static String HUSHTEXT_SECURE_CONVERSATION_ACK = "(HushTextAcknowledge)";
    public final static String CREATE_NEW_KEY_DIALOG_TITLE = "Create key?";
    public final static String CREATE_NEW_KEY_DIALOG_MESSAGE = "No valid conversation key exists " +
            "would you like to attempt to establish a conversation key?";
    public final static String KEY_ESTABLISHED_MESSAGE = "Conversation key has been established!";
    public final static String KEY_ESTABLISHED_TITLE = "Alert!";
    public final static String EXTRA_CONVERSATION_ID_KEY = "CONVERSATION_ID";
    public final static String EXTRA_ADDRESS_KEY = "ADDRESS";
    public final static String ASYMMETRIC_ENCRYPTION_ALGORITHM = "RSA";
    public final static int ASYMMETRIC_ENCRYPTION_SIZE = 256;
    public final static String SYMETRIC_ENCRYPTION_ALGORITHM = "AES";
    //public final static String SYMETRIC_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS7PADDING";
    public final static String EXTRA_TRHEADID = "CONVERSATION_ID";
    public final static String LIST_PADDING = "                                                  " +
            "                                                  " +
            "                                                  ";
    public final static Provider CRYPTO_PROVIDER = Security.getProvider("BC");
    /* Note: These can have square bracket since this prefix is added on at the recievers side
    after decryption.
     */
    public final static String HUSHTEXT_MESSAGE_PREFIX =
            "[" + SYMETRIC_ENCRYPTION_ALGORITHM + "]";
    public final static int MAXIMUM_ENCRYPTED_MESSAGE_SIZE = 100;
}
