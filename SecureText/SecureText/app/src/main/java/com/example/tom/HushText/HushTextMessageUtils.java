package com.example.tom.HushText;

/**
 * Created by Tom on 15-02-23.
 * Contains tools to test for HushText protocol messages as well as various other string
 */
public class HushTextMessageUtils {
    /**
     * Checks string to see if it matches HushText encrypted message format.
     * @param message Message string.
     * @return True if the string matches encrypted message format, false otherwise.
     */
    public static boolean isHushTextEncryptedMessage(String message) {
        if (message.length() < HushTextConstants.ENCRYPTED_MESSAGE_PREFIX.length())
            return false;
        String prefix = message.substring(0, HushTextConstants.ENCRYPTED_MESSAGE_PREFIX.length());
        if (prefix.equals(HushTextConstants.ENCRYPTED_MESSAGE_PREFIX))
            return true;
        else
            return false;
    }

    /**
     * Checks string to see if it matches HushText protool response format (encrypted symmetric
     * key).
     * @param message Message string.
     * @return True if it matches, false otherwise.
     */
    public static boolean isHushTextKeyResponse(String message) {
        if (message.length() < HushTextConstants.HUSHTEXT_ENCRYPED_KEY_PREFIX.length())
            return false;
        String prefix = message.substring(0,
                HushTextConstants.HUSHTEXT_ENCRYPED_KEY_PREFIX.length());
        if (prefix.equals(HushTextConstants.HUSHTEXT_ENCRYPED_KEY_PREFIX))
            return true;
        else
            return false;
    }

    /**
     * Checks string to see if it matches HushText Public key format.
     * @param message Message string.
     * @return True if it matches, false otherwise.
     */
    public static boolean isHushTextPublicKey(String message) {
        if (message.length() < HushTextConstants.HUSHTEXT_PUBLIC_KEY_PREFIX.length())
            return false;
        String prefix = message.substring(0,
                HushTextConstants.HUSHTEXT_PUBLIC_KEY_PREFIX.length());
        if (prefix.equals(HushTextConstants.HUSHTEXT_PUBLIC_KEY_PREFIX))
            return true;
        else
            return false;

    }

    /**
     * Strips all HushText protocl information from HushText public key message string (leaving
     * just the base 64 encoded key).
     * @param pubkeyMessage Message string.
     * @return Stripped string containing just the encoded key.
     */
    public static String getEncodedKeyString(String pubkeyMessage) {
        return pubkeyMessage.substring(HushTextConstants.HUSHTEXT_PUBLIC_KEY_PREFIX.length(),
                pubkeyMessage.length());
    }

    /**
     * Strips all HushText protocol information from HushText response message string.
     * @param responseString Message string.
     * @return Stripped string containing just the encoded data.
     */
    public static String getResponseString(String responseString) {
        return responseString.substring(HushTextConstants.HUSHTEXT_ENCRYPED_KEY_PREFIX.length(),
                responseString.length());
    }
}
