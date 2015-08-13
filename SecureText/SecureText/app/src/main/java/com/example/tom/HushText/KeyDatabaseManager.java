package com.example.tom.HushText;
/**
 * Created by Tom on 15-02-18.
 * Provides management and all necessary functions required for HushText to use the KeyDatabase.
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-02
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLDataException;
import java.util.Calendar;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyDatabaseManager {
    private KeyDatabaseHelper keyDatabaseHelper;

    /**
     * Constructor method for KeyDatabaseManager, simply creates the KeyDatabaseHelper used by
     * the methods of the class.
     * @param context Application context.
     */
    public KeyDatabaseManager(Context context) {
        keyDatabaseHelper = new KeyDatabaseHelper(context);
    }

    public static final int VALID_KEY = 0;
    public static final int NO_KEY = 1;
    public static final int EXPIRED_KEY = 2;
    private static final int MILLISEC_TO_SEC = 1000;
    private static final int KEY_DOES_NOT_EXPIRE = -1;
    public static final int NO_PENDING_EXCHANGE = 1;
    public static final int PENDING_EXCHANGE = 2;
    // Sent request!

    /**
     * Checks for outstanding key exchange in the requests table. Note: this table will have an
     * entry for a given thread_id only if the device has initiated the key exchange.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @return Returns NO_PENDING_EXCHANGE if there is no pending exchange and PENDING_EXCHANGE
     * otherwise.
     * @throws SQLDataException
     */
    public int checkForKeyExchange(String thread_id) throws SQLDataException{
        SQLiteDatabase database = keyDatabaseHelper.getReadableDatabase();
        String queryString = "SELECT * FROM " + KeyDatabaseContract.KeyDatabaseEntry.REQUESTS_TABLE_NAME +
                " WHERE thread_id=?";
        String[] selectionArgs = {thread_id};
        Cursor cursor = database.rawQuery(queryString, selectionArgs);
        if (cursor.getCount() == 0) {
            database.close();
            return NO_PENDING_EXCHANGE;
        }
        else {
            database.close();
            return PENDING_EXCHANGE;
        }
    }

    /**
     * Adds request, this is called when a user initiates the key exchange within a conversation
     * at which point a keypair is generated and is stored in the request database.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @param key Private key from keypair.
     * @param algorithm Asymmetric crypto algorithm for keypair.
     */
    public void addRequestKey(String thread_id, PrivateKey key, String algorithm) {
        SQLiteDatabase database = keyDatabaseHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        String encodedKeyString = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_PRIVATE_KEY,
                encodedKeyString);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_A_KEYTYPE, algorithm);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_THREAD_ID, thread_id);
        long newRowId = database.insert(KeyDatabaseContract.KeyDatabaseEntry.REQUESTS_TABLE_NAME,
                null, newValues);
        database.close();
    }

    /**
     * Gets a private key from a particular conversation key exchange request.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @return PrivateKey from the request entry.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public PrivateKey getRequestKey(String thread_id) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        SQLiteDatabase database = keyDatabaseHelper.getReadableDatabase();
        String rawQueryString = "SELECT * FROM " +
                KeyDatabaseContract.KeyDatabaseEntry.REQUESTS_TABLE_NAME +
                " WHERE thread_id=?";
        String[] selectionArgs = {thread_id};
        Cursor cursor = database.rawQuery(rawQueryString, selectionArgs);
        cursor.moveToFirst();
        String algorithm = cursor.getString(
                cursor.getColumnIndex(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_A_KEYTYPE));
        String encodedPrivateKey =
                cursor.getString(
                    cursor.getColumnIndex(
                            KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_PRIVATE_KEY));
        return  AsymmetricCryptoEncodingUtils.makePrivateKeyFromString(encodedPrivateKey);
    }

    /**
     * Checks for the existence of a conversation (asymmetric key), this is done upon opening
     * a new ConversationActivity to see if the activity should prompt the user for key exchange.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @return NO_KEY if no conversation key exists, VALID_KEY if valid conversation key exists
     * and EXPIRED_KEY if key is expired.
     * @throws SQLDataException
     */
    public int testForConversationKey(String thread_id) throws SQLDataException {
        SQLiteDatabase database = keyDatabaseHelper.getReadableDatabase();
        String rawQueryString = "SELECT * FROM " + KeyDatabaseContract.KeyDatabaseEntry.TABLE_NAME +
                " WHERE thread_id=?";
        String[] selectionArgs = {thread_id};
        Cursor cursor = database.rawQuery(rawQueryString, selectionArgs);
        if (cursor.getCount() == 0)
            return NO_KEY;
        cursor.moveToFirst();
        long expiry = cursor.getLong(cursor.getColumnIndex(
                KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_EXPIRYDATE));
        if (expiry == KEY_DOES_NOT_EXPIRE)
            return VALID_KEY;
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis() / MILLISEC_TO_SEC;
        database.close();
        if (current > expiry)
            return EXPIRED_KEY;
        return VALID_KEY;
    }

    /**
     * Gets encoded conversation key and decodes/builds key into a SecretKey object which is
     * returned.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @return SecretKey Conversation key.
     * @throws NoConversationKeyException
     */
    public SecretKey getKey(String thread_id) throws NoConversationKeyException {
        SQLiteDatabase database = keyDatabaseHelper.getReadableDatabase();
        String rawQueryString = "SELECT * FROM " + KeyDatabaseContract.KeyDatabaseEntry.TABLE_NAME +
                " WHERE thread_id=?";
        String[] queryArgs = {thread_id};
        Cursor cursor = database.rawQuery(rawQueryString, queryArgs);
        if (cursor.getCount() == 0) {
            throw new NoConversationKeyException();
        }
        cursor.moveToFirst();
        String keyString = cursor.getString(
                cursor.getColumnIndex(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_KEY));
        String keyAlgorithm = cursor.getString(
                cursor.getColumnIndex(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_TYPE));
        byte[] encodedKeyBytes = Base64.decode(keyString, Base64.DEFAULT);
        database.close();
        return new SecretKeySpec(encodedKeyBytes, 0, encodedKeyBytes.length, keyAlgorithm);
    }

    /**
     * Used to renew a existing key in the KeyDatabase.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @param newKey The new key to replace the old one with.
     * @param creationDate Creation date of new key.
     * @param expiryDate Expiry date of new key.
     * @param algorithm Type of key it is (algorithm).
     */
    public void renewKey(String thread_id, SecretKey newKey, long creationDate, long expiryDate,
                         String algorithm) {
        SQLiteDatabase database = keyDatabaseHelper.getWritableDatabase();
        String rawSQL = "DELETE FROM " + KeyDatabaseContract.KeyDatabaseEntry.TABLE_NAME
                + " WHERE thread_id=?";
        String[] selectionArgs = {thread_id};
        database.execSQL(rawSQL, selectionArgs);
        addThreadKey(thread_id, newKey, creationDate, expiryDate, algorithm);
        database.close();
    }

    /**
     * Adds a conversation key to database.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @param key Conversation key.
     * @param creationDate Creation date.
     * @param expiryDate Expiry date (-1 if does not expire).
     * @param algorithm Type of key key is (algorithm).
     */
    public void add(String thread_id, SecretKey key, long creationDate,
                             long expiryDate, String algorithm) {
        SQLiteDatabase database = keyDatabaseHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        String encodedKeyString = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_THREAD_ID,
                Long.parseLong(thread_id));
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_KEY, encodedKeyString);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_CREATIONDATE, creationDate);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_EXPIRYDATE, expiryDate);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_TYPE, algorithm);
        long newRowID = database.insert("keys", null, newValues);
        database.close();
    }

    /**
     * Adds a conversation key to database.
     * @param thread_id Thread id corresponding to the conversation you are checking for.
     * @param key Conversation key.
     * @param creationDate Creation date.
     * @param expiryDate Expiry date (-1 if does not expire).
     * @param algorithm Type of key key is (algorithm).
     */
    public void addThreadKey(String thread_id, SecretKey key, long creationDate,
                          long expiryDate, String algorithm) {
        SQLiteDatabase database = keyDatabaseHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        String encodedKeyString = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_THREAD_ID,
                Long.parseLong(thread_id));
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_KEY, encodedKeyString);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_CREATIONDATE, creationDate);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_EXPIRYDATE, expiryDate);
        newValues.put(KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_TYPE, algorithm);
        long newRowID = database.insert("keys", null, newValues);
        String removeReqSQL = "DELETE FROM " +
                KeyDatabaseContract.KeyDatabaseEntry.REQUESTS_TABLE_NAME +
                " WHERE thread_id=" + thread_id;
        database.execSQL(removeReqSQL);
        database.close();
    }

    /**
     * Drops the conversation key entry in the key database.
     * @param thread_id Key database key of entry to drop.
     */
    public void dropConversationKey(String thread_id) {
        SQLiteDatabase database = keyDatabaseHelper.getWritableDatabase();
        //DELETE FROM Customers
        //WHERE CustomerName='Alfreds Futterkiste' AND ContactName='Maria Anders';
        String SQL = "DELETE FROM " + KeyDatabaseContract.KeyDatabaseEntry.TABLE_NAME +
                " WHERE " + KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_THREAD_ID + "=" +
                thread_id;
        String[] args = {thread_id};
        database.execSQL(SQL);
        database.close();
    }

    /**
     * Removes specified request entry from request table.
     * @param thread_id Key database key of entry to drop.
     */
    public void flushRequest(String thread_id) {
        SQLiteDatabase database = keyDatabaseHelper.getWritableDatabase();
        String SQL = "DELETE FROM " + KeyDatabaseContract.KeyDatabaseEntry.REQUESTS_TABLE_NAME +
                " WHERE " + KeyDatabaseContract.KeyDatabaseEntry.COLUMN_NAME_THREAD_ID +
                "=" + thread_id;
        database.execSQL(SQL);
        database.close();
    }

    /**
     * Attempts to find what column messages are stored in the message database (annoyingly
     * different manufacterers have different table columns) This may or may not always work so
     * for localization it would be better to just have a big table of phones and their
     * compatibility variables.
     * @return Column number of where messages (*should*) be stored in message database.
     */
    public static int messageColumnIndex() {
        String model = Build.MODEL;
        if (model.equals("Nexus 5"))
            return 12;
        else if (model.equals("LG-D852")) {
            return 13;
        }
        return -1;
    }

    /**
     * Drops entire key database.
     * Note: The KeyDatabaseHelper will automatically create a new database if none exists when
     * the database is accessed.
     * @param context
     */
    public static void dropKeyDatabase(Context context) {
        context.deleteDatabase("keydatabase.db");
    }

}
