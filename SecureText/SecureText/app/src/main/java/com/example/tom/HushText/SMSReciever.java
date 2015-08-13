package com.example.tom.HushText;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

/**
 * Created by Tom on 15-02-22.
 * Broadcast receive that receives incomming SMS broadcast.
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-01
 */
public class SMSReciever extends BroadcastReceiver {
    private final static String SMS_RECIEVED = "android.provider.Telephony.SMS_RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECIEVED)) {
            context.sendBroadcast(new Intent("NEWSMS"));
        }
    }
}



