package com.example.tom.HushText;

import android.net.Uri;
import android.provider.Telephony;

import java.util.ArrayList;

/**
 * Created by Tom on 15-02-20.
 * Contains tools for managing a conversation (not sure if this will end up being used).
 * @author Tom Hadlaw.
 * @since 2015-04-06
 */
public class ConversationManager {
    private String address;
    private Uri ConversationUri = Telephony.Sms.Conversations.CONTENT_URI;
    private ArrayList<Message> messageList;
    private ArrayList<String> messageStringList;
    private static final int MESSAGE_COLUMN = 12;
    private static final int DATE_COLUMN = 4;
    private static final int ADDRESS_COLUMN = 2;
    public static void getMessageList() {}
}
