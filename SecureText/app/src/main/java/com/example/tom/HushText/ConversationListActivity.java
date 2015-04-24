package com.example.tom.HushText;
/**
 * Conversation List Activity displays all ongoing SMS conversations along with last message sent
 * or recieved. Provides user interface to open new HushText conversation.
 *
 * @author  Tom Hadlaw
 * @version 1.0
 * @since   2015-03-31
 */
import java.util.Set;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

import java.security.Security;
import java.security.Provider;

import android.provider.ContactsContract.PhoneLookup;

public class ConversationListActivity extends Activity {
    private static HushTextConstants constants;
    public final static String EXTRA_MESSAGE = "com.example.tom.securetext.Message";
    public ArrayList<String> sms;
    public ArrayList<String> key;
    public ArrayList<String> addresses;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            populateMessages();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //---------------------------------------------//
        //Leaving this uncommented ensures no persistence between runs of HushText.
        //deleteDatabase("keydatabase.db");
        //CryptoTest.test();
        //ListSupportedAlgorithms();
        //---------------------------------------------//
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(broadcastReceiver, new IntentFilter("NEWSMS"));
        populateMessages();

    }

    /**
     * Populates the ListView of the ConversationListActivity with all the current conversations
     * on the phone.
     */
    private void populateMessages() {
        sms = new ArrayList<String>();
        key = new ArrayList<String>();
        addresses = new ArrayList<String>();
        Uri uri = Uri.parse(constants.MMS_SMS_URI_STRING);
        Cursor cursor = getContentResolver().query(uri, null, null, null, "date desc");
        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            String read = cursor.getString(cursor.getColumnIndexOrThrow("read"));
            //Documentation for androids sms db is non-existant... assuming _id is the key column?
            String id = cursor.getString(cursor.getColumnIndex("thread_id"));
            key.add(id);
            addresses.add(address);
            String contactName = address;
            Uri Nameuri = Uri.withAppendedPath(
                    PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));

            Cursor cs = getContentResolver().query(Nameuri,
                    new String[]{PhoneLookup.DISPLAY_NAME},
                        PhoneLookup.NUMBER+"='"+address+"'",null,null);

            if(cs.getCount()>0) {
                cs.moveToFirst();
                contactName = cs.getString(cs.getColumnIndex(PhoneLookup.DISPLAY_NAME));
            }

            sms.add(contactName + "\n"+body);
        }
        String[] msgArr = new String[sms.size()];
        for (int i = 0; i < sms.size(); i++) {
            msgArr[i] = sms.get(i);
            msgArr[i] += HushTextConstants.LIST_PADDING;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.conversation, msgArr);
        ListView msgList = (ListView) findViewById(R.id.listView);
        msgList.setAdapter(adapter);
        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openConversation(position);
            }
        });
        cursor.close();
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("NEWSMS"));
    }

    /**
     * Lists all supported crypto algorithms for the device as well as their provider by printing
     * them to standard output.
     */
    public void ListSupportedAlgorithms() {
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            System.out.println("provider: " + provider.getName());
            Set<Provider.Service> services = provider.getServices();
            for (Provider.Service service : services) {
                System.out.println("algorithm: " + service.getAlgorithm());
            }
        }
    }

    /**
     * Opens a ConversationView corresponding to its position in the conversation list which are
     * queried from the conversation list in descending order (by date).
     * @param position Position of conversation to open.
     */
    public void openConversation(int position) {
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("CONVERSATION_ID", key.get(position));
        intent.putExtra("ADDRESS", addresses.get(position));
        startActivity(intent);
    }

    public void keySettingsMenuButton(View view) {
        Intent intent = new Intent(this, KeySettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
