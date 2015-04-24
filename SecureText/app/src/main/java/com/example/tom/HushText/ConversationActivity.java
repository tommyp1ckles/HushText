package com.example.tom.HushText;
/**
 * Conversation activity provides user interface for HushText SMS messaging as well as functionality
 * for key exchange and secure communication.
 * @version 1.0
 * @since 2015-03-31
 */
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.DialogInterface;
import android.app.AlertDialog;

public class ConversationActivity extends Activity {
    private HushTextConstants constants;
    private ArrayAdapter<String> adapter;
    private ArrayList<Message> messages;
    private ArrayList<String> messageStringList;
    private ListView messageListView;
    private SymmetricCryptoUtils symmetricCryptoUtils;
    private static int MESSAGE_COLUMN = 12;
    private static int DATE_COLUMN = 4;
    private static int ADDRESS_COLUMN = 2;
    private Uri InboxUri = HushTextConstants.InboxUri;
    private Uri sentUri = HushTextConstants.sentUri;
    private boolean initializeConversation = true;
    private static final int DIALOG_YES = -1;
    private static final int DIALOG_NO = -1;
    private boolean DEBUG = false;
    private boolean PRINT_CONV = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extra = getIntent().getExtras();
            String thread_id = extra.getString("ADDRESS");
            resolveHushTextProtocol(thread_id);
            populateMessageList();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Security.addProvider(new BouncyCastleProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);
        Bundle extra = getIntent().getExtras();
        setTitle(extra.getString("ADDRESS"));
        //-----------------------------------------------------------------------------//
        // Check if conversation key exists and if not show "establish key" dialog.
        //-----------------------------------------------------------------------------//
        if (initializeConversation) {
            String conversationThreadId =
                    extra.getString(HushTextConstants.EXTRA_CONVERSATION_ID_KEY);
            KeyDatabaseManager keyDatabaseManager = new KeyDatabaseManager(getApplicationContext());
            try {
                if (keyDatabaseManager.testForConversationKey(conversationThreadId) !=
                        KeyDatabaseManager.VALID_KEY) {
                    System.out.println("NO CONVERSATION KEY");
                    if (keyDatabaseManager.checkForKeyExchange(conversationThreadId) ==
                            KeyDatabaseManager.PENDING_EXCHANGE) {
                        System.out.printf("PENDING EXCHANGE");
                        resolveResponses();
                        System.out.println("DONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                    else {
                        System.out.println("NO PENDING EXCHANGE");
                        boolean ret = resolveRequests();
                        System.out.println("RET====================================> " + ret);
                        if (!ret)
                            establishKeyDialog();
                    }
                }
                else {
                    System.out.println("Valid key!");
                }
            }
            catch (Exception e) {
                System.out.println("?????????");
                //e.printStackTrace();
            }
        }
        populateMessageList();
        registerReceiver(broadcastReceiver, new IntentFilter("NEWSMS"));
        initializeConversation = false;
        //----------------------------//
    }

    /**
     * Resolves HushText protocol
     * @param thread_id current thread_id.
     */
    public void resolveHushTextProtocol(String thread_id) {
        String conversationThreadId = thread_id;
        KeyDatabaseManager keyDatabaseManager = new KeyDatabaseManager(getApplicationContext());
        try {
            if (keyDatabaseManager.testForConversationKey(conversationThreadId) !=
                    KeyDatabaseManager.VALID_KEY) {
                if (keyDatabaseManager.checkForKeyExchange(conversationThreadId) ==
                        KeyDatabaseManager.PENDING_EXCHANGE) {
                    resolveResponses();
                }
                /*else {
                    resolveRequests();
                }*/
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiates a key exchange between user and conversation address (phone number of other person
     * in conversation).
     */
    private void initKeyExchange() {
        Bundle extra = getIntent().getExtras();
        KeyDatabaseManager keyDatabaseManager = new KeyDatabaseManager(getApplicationContext());
        try {
            //Init
            InitKeyExchange keyExchange = new InitKeyExchange(
                    HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM, 256);
            PublicKey publicKey = keyExchange.getPublicKey();
            PrivateKey privateKey = keyExchange.getPrivateKey();
            keyDatabaseManager.addRequestKey(extra.getString("CONVERSATION_ID"), privateKey,
                    HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM);
            String publicKeyMessage =  Base64.encodeToString(publicKey.getEncoded(),
                    Base64.DEFAULT);
            publicKeyMessage = HushTextConstants.HUSHTEXT_PUBLIC_KEY_PREFIX  + publicKeyMessage;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(extra.getString("ADDRESS"), null,
                    publicKeyMessage, null, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates yes/no dialog prompting user if they want to begin secure conversation, if yes is
     * entered then key exchange is initiated by calling the initKeyExchange method. Please note
     * that if no is entered then the activity simply behaves like a regular SMS client.
     */
    private void establishKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(HushTextConstants.CREATE_NEW_KEY_DIALOG_TITLE);
        builder.setMessage(HushTextConstants.CREATE_NEW_KEY_DIALOG_MESSAGE);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                initKeyExchange();
                dialog.dismiss();
            }

        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Creates dialog that alerts the user that a conversation key has succesfully been established.
     */
    private void keyEstablishedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(HushTextConstants.KEY_ESTABLISHED_TITLE);
        builder.setMessage(HushTextConstants.KEY_ESTABLISHED_MESSAGE);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Searches all recieved conversation messages for HushText secure conversation request
     * (PublicKey) and attempts to create a conversation key (symmetric key who's algorithm is
     * specified in a static context in the HushTextConstants class) and respond to the HushText
     * secure conversation request.
     * @return True if request was found and succesfully responded to and false otherwise.
     */
    private boolean resolveRequests() {
        Bundle extra = getIntent().getExtras();
        //String selection = "thread_id=" + extra.getString(HushTextConstants.CONVERSATION_ID_KEY);
        //Cursor cursor = getContentResolver().query(InboxUri, null, selection, null, "date Desc");
        String selection = "thread_id=" + extra.getString(HushTextConstants.CONVERSATION_ID_KEY);
        Cursor cursor = getContentResolver().query(InboxUri, null, selection, null, "date Desc");
        messageStringList = new ArrayList<String>();
        //messageStringList = new ArrayList<String>();
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String messageString = cursor.getString(MESSAGE_COLUMN);
            if (DEBUG)
                System.out.println("MessageString: " + messageString);
            if (HushTextMessageUtils.isHushTextPublicKey(messageString)) {
                try {
                    String keyString = HushTextMessageUtils.getEncodedKeyString(messageString);
                    PublicKey pkey = AsymmetricCryptoEncodingUtils.makePublicKeyFromString(keyString);
                    SecretKey secretKey = SymmetricCryptoUtils.generateSymmetricKey(
                            HushTextConstants.SYMETRIC_ENCRYPTION_ALGORITHM);
                    KeyDatabaseManager keyDatabaseManager = new KeyDatabaseManager(
                            getApplicationContext());
                    keyDatabaseManager.addThreadKey(extra.getString(
                            HushTextConstants.EXTRA_CONVERSATION_ID_KEY), secretKey, -1, -1,
                            HushTextConstants.SYMETRIC_ENCRYPTION_ALGORITHM);
                    String responseMessage = RespondKeyExchange.RespondKeyExchange(secretKey,
                            pkey, HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM,
                            HushTextConstants.ASYMMETRIC_ENCRYPTION_SIZE);
                    responseMessage = HushTextConstants.HUSHTEXT_ENCRYPED_KEY_PREFIX +
                            responseMessage;
                    SmsManager.getDefault().sendTextMessage(extra.getString(
                            HushTextConstants.EXTRA_ADDRESS_KEY), null, responseMessage,
                            null, null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }
        cursor.close();
        return false;
    }

    private static final int NO_RESPONSE = 0;
    private static final int NEW_KEY_ESTABLISHED = 1;
    private static final int RESPONSE_ERROR = 2;

    /**
     * Attempts to resolve any responses to HushText secure conversation request, if a valid
     * response is found (algorithm and response prefix can be specified in HushTextConstants).
     * then it stores the conversation key and removes the conversations key exchange from the
     * requests table (in SQLite database: keydatabase.db).
     * @return NO_RESPONSE if no response is found, NEW_KEY_ESTABLISHED if response is found and
     * new key is succesfully established and RESPONSE_ERROR if response is found by there was an
     * error with the response.
     */
    private int resolveResponses() {
        System.out.println("Attempting to resolve responses...");
        Bundle extra = getIntent().getExtras();
        String selection = "thread_id=" + extra.getString(HushTextConstants.CONVERSATION_ID_KEY);
        Cursor cursor = getContentResolver().query(InboxUri, null, selection, null, "date Desc");
        //cursor.moveToFirst();
        KeyDatabaseManager keyDatabaseManager = new KeyDatabaseManager(getApplicationContext());
        try {
            keyDatabaseManager.checkForKeyExchange(extra.getString(HushTextConstants.CONVERSATION_ID_KEY));
        }
        catch (Exception e) {
            e.printStackTrace();
            return RESPONSE_ERROR;
        }
        while (cursor.moveToNext()) {
            String message = cursor.getString(MESSAGE_COLUMN);
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + message);
            if (HushTextMessageUtils.isHushTextKeyResponse(message)) {
                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ IS RESP");
                try {
                    String keyString = HushTextMessageUtils.getResponseString(message);
                    byte[] encryptedSymmKeyBytes = Base64.decode(keyString, Base64.DEFAULT);
                    PrivateKey reqPrivateKey = keyDatabaseManager.getRequestKey(
                            extra.getString(HushTextConstants.CONVERSATION_ID_KEY));
                    Cipher cipher = Cipher.getInstance(
                            HushTextConstants.ASYMMETRIC_ENCRYPTION_ALGORITHM);
                    cipher.init(Cipher.DECRYPT_MODE, reqPrivateKey);
                    byte[] decryptedSymmKeyBytes = cipher.doFinal(encryptedSymmKeyBytes);
                    SecretKey key = new SecretKeySpec(decryptedSymmKeyBytes,
                            HushTextConstants.SYMETRIC_ENCRYPTION_ALGORITHM);
                    keyDatabaseManager.addThreadKey(
                            extra.getString(HushTextConstants.CONVERSATION_ID_KEY),
                            key, -1, -1,
                            HushTextConstants.SYMETRIC_ENCRYPTION_ALGORITHM
                    );
                    keyEstablishedDialog();
                    //HUSHTEXT_SECURE_CONVERSATION_ACK
                    SmsManager.getDefault().sendTextMessage(
                            extra.getString(HushTextConstants.EXTRA_ADDRESS_KEY),
                            null,
                            HushTextConstants.HUSHTEXT_SECURE_CONVERSATION_ACK,
                            null,
                            null);
                    return NEW_KEY_ESTABLISHED;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG)
            System.out.println("No response...");
        return NO_RESPONSE;
    }

    /**
     * Populates ListView in ConversationView the current conversations messages, as well if there
     * exists a conversation key for the conversation then it will decrypt any HushText encrypted
     * messages (Prefix is stored statically in HushTextConstants).
     */
    private void populateMessageList() {
        Bundle extra = getIntent().getExtras();
        String selection = "thread_id=" + extra.getString(HushTextConstants.CONVERSATION_ID_KEY);
        Cursor cursor = getContentResolver().query(InboxUri, null, selection, null, "date Desc");
        messageStringList = new ArrayList<String>();
        messages = new ArrayList<Message>();
        while (cursor.moveToNext()) {
            messageStringList.add(cursor.getString(MESSAGE_COLUMN));
            messages.add(new Message(cursor.getString(MESSAGE_COLUMN),
                    cursor.getLong(DATE_COLUMN), cursor.getString(ADDRESS_COLUMN),
                    false));
        }
        cursor.close();

        Cursor cursor2 = getContentResolver().query(
                sentUri, null, selection, null, "date Desc");
        while (cursor2.moveToNext()) {
           messageStringList.add(cursor2.getString(MESSAGE_COLUMN));
           messages.add(new Message(cursor2.getString(MESSAGE_COLUMN),
                    cursor2.getLong(DATE_COLUMN), cursor2.getString(ADDRESS_COLUMN),
                    true));
        }

        Collections.sort(messages);
        String[] messageStringArray = new String[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            if (DEBUG) {
                if (messages.get(i).getMessage() == null) {
                    System.out.println("NULL MESSAGE?: " + messages.get(i).getAddress());
                }
            }

            if (HushTextMessageUtils.isHushTextEncryptedMessage(messages.get(i).getMessage())) {
                try {
                    KeyDatabaseManager kdm = new KeyDatabaseManager(getApplicationContext());
                    SecretKey key = kdm.getKey(extra.getString("CONVERSATION_ID"));
                    String msg = messages.get(i).getMessage();
                    msg = msg.substring(HushTextConstants.ENCRYPTED_MESSAGE_PREFIX.length(),
                            msg.length());
                    byte[] msgBytes = Base64.decode(msg, Base64.DEFAULT);
                    byte[] decryptedMessageBytes =
                            symmetricCryptoUtils.decryptMessage(msgBytes, key);
                    symmetricCryptoUtils.testKey(key);
                    msg = new String(decryptedMessageBytes);
                    msg = HushTextConstants.HUSHTEXT_MESSAGE_PREFIX + msg;
                    messages.get(i).setMessage(msg);
                }
                catch (javax.crypto.BadPaddingException e) {
                    //Old key messages.
                }
                catch(NoConversationKeyException e1){
                    if (DEBUG)
                        System.out.println("No Conversation Key");
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            //messageStringArray[i] = messages.get(i).getMessage();
            String messageString = messages.get(i).getMessage();
            if (!messages.get(i).isSent()) {
                messageString = extra.getString(HushTextConstants.EXTRA_ADDRESS_KEY) + ": " +
                        messageString;
            }
            messageStringArray[i] = messageString;

        }
        messageListView = (ListView) findViewById(R.id.listView2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.message,
                messageStringArray);
        messageListView.setAdapter(adapter);
        messageListView.setSelection(adapter.getCount() - 1);
        cursor.close();
    }

    /**
     * Sends SMS message from text found in editText field in the conversation view, if no
     * conversation key exists then it simply sends a regular SMS messages, however if conversation
     * key does exist then it will encrypt the message with the conversation key.
     * @param view The view.
     */
    public void sendMessage(View view) {
        if (DEBUG)
            System.out.println("---------Send message!---------");
        EditText textField = (EditText) findViewById(R.id.editText);
        String messageString = textField.getText().toString();
        if (messageString.length() > HushTextConstants.MAXIMUM_ENCRYPTED_MESSAGE_SIZE) {
            messageString = messageString.substring(0,
                    HushTextConstants.MAXIMUM_ENCRYPTED_MESSAGE_SIZE);
        }
        if (DEBUG)
            System.out.println("Message: " + messageString);
        Bundle extra = getIntent().getExtras();
        messages.add(new Message(messageString, -1,
                extra.getString(HushTextConstants.EXTRA_ADDRESS_KEY), true));
        KeyDatabaseManager kdm = new KeyDatabaseManager(getApplicationContext());
        int keyStatus = -1;
        try {
            keyStatus = kdm.testForConversationKey(extra.getString(
                    HushTextConstants.CONVERSATION_ID_KEY));
        }
        catch (Exception e) {
            if (DEBUG)
                System.out.println("Unexpected error testing for conversation key: " +
                        e.getMessage());
                System.out.println("Message will not be sent...");
            return;
        }
        if (keyStatus == KeyDatabaseManager.VALID_KEY) {
            System.out.println("Key exists, attempting to send secure message.");
            try {
                SecretKey key = kdm.getKey(extra.getString(HushTextConstants.CONVERSATION_ID_KEY));
                byte[] ct = SymmetricCryptoUtils.encryptMessage(messageString, key);
                String encryptedMessage = HushTextConstants.ENCRYPTED_MESSAGE_PREFIX +
                        Base64.encodeToString(ct, Base64.DEFAULT);
                /*
                System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;Original IS: " + messageString);
                System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;MESSAGE IS: " + encryptedMessage);
                */
                SmsManager.getDefault().sendTextMessage(
                        extra.getString(HushTextConstants.EXTRA_ADDRESS_KEY),
                        null,
                        encryptedMessage,
                        null,
                        null);
            }
            catch (Exception e) {
               System.out.println("Error retrieving the conversation key (although one exists)" +
                    " message will not be sent...");
               e.printStackTrace();
               return;
            }
        }
        else {
            SmsManager.getDefault().sendTextMessage(
                    extra.getString(HushTextConstants.EXTRA_ADDRESS_KEY),
                    null,
                    messageString,
                    null,
                    null);
        }
        messages.add(new Message(
                messageString,
                -1,
                extra.getString(HushTextConstants.CONVERSATION_ID_KEY),
                true));
        String[] messageStringArray = new String[messages.size()];
        for (int i = 0; i < messageStringArray.length; i++)
            messageStringArray[i] = messages.get(i).getMessage();
        textField.setText("");
        messageListView = (ListView) findViewById(R.id.listView2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.message,
                messageStringArray);
        messageListView.setAdapter(adapter);
        for (int i = 0; i < messageListView.getAdapter().getCount(); i++) {
            long id = messageListView.getAdapter().getItemId(i);
        }
        messageListView.setSelection(adapter.getCount() - 1);
    }

    /**
     * Method called by the "Drop Key" button that drops the conversation key for the conversation.
     * @param view The calling view.
     */
    public void dropConversationKey(View view) {
        System.out.println("DROP DROP DROP DROP");
        KeyDatabaseManager keyDatabaseManager =
                new KeyDatabaseManager(getApplicationContext());
        Bundle extra = getIntent().getExtras();
        String id = extra.getString(HushTextConstants.EXTRA_CONVERSATION_ID_KEY);
        keyDatabaseManager.dropConversationKey(id);
        keyDatabaseManager.flushRequest(id);
        if (DEBUG) {
            System.out.println("Conversation key for thread_id=" + id
                    + "has been dropped!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
