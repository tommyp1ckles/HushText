package com.example.tom.HushText;

import android.provider.BaseColumns;

/**
 * Created by Tom on 15-02-18.
 * Database contract class for Key Database defines constants for KeyDatabaseHelper and
 * KeyDatabaseManager.
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-01
 */
public class KeyDatabaseContract {
    public KeyDatabaseContract() {}
    public static abstract class KeyDatabaseEntry implements BaseColumns {
        public static final String TABLE_NAME = "keys";
        public static final String COLUMN_NAME_THREAD_ID = "thread_id";
        public static final String COLUMN_NAME_CREATIONDATE = "creation_date";
        public static final String COLUMN_NAME_EXPIRYDATE = "expiry_date";
        public static final String COLUMN_NAME_TYPE = "key_type";
        public static final String COLUMN_NAME_KEY = "key";
        //--------------------------------------------------------------------//
        public static final String REQUESTS_TABLE_NAME = "requests";
        public static final String COLUMN_NAME_PRIVATE_KEY = "privatekey";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_A_KEYTYPE = "akey_type";
    }
}