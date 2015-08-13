package com.example.tom.HushText;

import android.telephony.SmsManager;

/**
 * Created by Tom on 15-02-27.
 * Provides tools for sending SMS messages.
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-01
 */
public class SMSSender {
    /**
     * Sends an SMS message (one part).
     * @param address Address to send to (phone number).
     * @param message Message to send.
     */
    public static void sendSMSMessage(String address, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(address, null, message, null, null);
    }
}
