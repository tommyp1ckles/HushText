package com.example.tom.HushText;
/**
 * Created by Tom on 15-02-15.
 * Database helper for KeyDatabase, helps in creating, updating and managing the key database;
 * Mostly used by the KeyDatabaseManager class.
 * @author Tom Hadlaw.
 * @version 1.0
 * @since 2015-04-02
 */
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class KeyDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATA_VERSION = 1;
    public static final String DATABASE_NAME = "keydatabase.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_KEY_TABLE =
            "CREATE TABLE " + KeyDatabaseContract.KeyDatabaseEntry.TABLE_NAME + " (" +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_THREAD_ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_CREATIONDATE + " INTEGER" + COMMA_SEP +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_EXPIRYDATE + " INTEGER" + COMMA_SEP +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_TYPE + " CHAR(20)" + COMMA_SEP +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_KEY + " CHAR(300)" +
            " ); ";
    private static final String SQL_CREATE_REQUEST_TABLE =
            "CREATE TABLE " + KeyDatabaseContract.KeyDatabaseEntry.REQUESTS_TABLE_NAME + " (" +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_THREAD_ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_PRIVATE_KEY + " CHAR(300)" + COMMA_SEP +
                    KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_A_KEYTYPE + " CHAR(20)" +
            " ); ";

    private static final String DROP_TABLES =
            "DROP TABLE IF EXISTS " + KeyDatabaseContract.KeyDatabaseEntry.TABLE_NAME +"" +
                    "; DROP TABLE IF EXISTS ;" + KeyDatabaseContract.KeyDatabaseEntry.REQUESTS_TABLE_NAME;

    public KeyDatabaseHelper(android.content.Context context) {
        super(context, DATABASE_NAME, null, DATA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        System.out.printf("CREATING DATABASES...");
        database.execSQL(SQL_CREATE_KEY_TABLE);
        database.execSQL(SQL_CREATE_REQUEST_TABLE);
        System.out.println("DONE!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(DROP_TABLES);
        onCreate(database);
    }

    @Override
    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onUpgrade(database, oldVersion, newVersion);
    }
}