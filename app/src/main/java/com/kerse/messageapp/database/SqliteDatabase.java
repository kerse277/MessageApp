package com.kerse.messageapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SqliteDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "messages_sqlitedb";

    private static final String USER_TABLE = "user_list";
    private static String USER_FULLNAME = "fullname";
    private static String USER_ID = "id";
    private static String USER_UNIQUEID = "uniqueid";
    private static String USER_PROFILEPHOTO = "photo";
    private static String USER_LOCATION = "location";
    private static String USER_PROFILETEXT = "ptxt";

    private static final String MY_TABLE = "my_table";
    private static String MY_FULLNAME = "fullname";
    private static String MY_ID = "id";
    private static String MY_UNIQUEID = "uniqueid";
    private static String MY_PROFILEPHOTO = "photo";
    private static String MY_PHONE = "phone";
    private static String MY_STATUSTEXT = "ptxt";

    private static final String MESSAGE_TABLE = "send_message";
    private static String MESSAGE = "message";
    private static String MESSAGE_ID = "id";
    private static String MESSAGE_DATE = "mdate";
    private static String MESSAGE_USERID = "userid";
    private static String MESSAGE_TYPE = "type";
    private static String MESSAGE_UNIQUEID = "uniqueid";
    private static String MESSAGE_STATUS = "status";

    private static final String OFLINE_MESSAGE_TABLE = "ofline_send_message";
    private static String OFLINE_MESSAGE = "message";
    private static String OFLINE_MESSAGE_ID = "id";
    private static String OFLINE_MESSAGE_DATE = "mdate";
    private static String OFLINE_MESSAGE_USERID = "userid";
    private static String OFLINE_MESSAGE_TYPE = "type";
    private static String OFLINE_MESSAGE_UNIQUEID = "uniqueid";
    private static String OFLINE_MESSAGE_STATUS = "status";


    Context context;

    public SqliteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + "("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_FULLNAME + " TEXT,"
                + USER_PROFILEPHOTO + " TEXT,"
                + USER_PROFILETEXT + " TEXT,"
                + USER_LOCATION + " TEXT,"
                + USER_UNIQUEID + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_MY_TABLE = "CREATE TABLE " + MY_TABLE + "("
                + MY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MY_FULLNAME + " TEXT,"
                + MY_PROFILEPHOTO + " TEXT,"
                + MY_PHONE + " TEXT,"
                + MY_STATUSTEXT + " TEXT,"
                + MY_UNIQUEID + " TEXT" + ")";
        db.execSQL(CREATE_MY_TABLE);

        String CREATE_MESSAGE_TABLE = "CREATE TABLE " + MESSAGE_TABLE + "("
                + MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MESSAGE + " TEXT,"
                + MESSAGE_USERID + " TEXT,"
                + MESSAGE_TYPE + " TEXT,"
                + MESSAGE_UNIQUEID + " TEXT,"
                + MESSAGE_STATUS + " TEXT,"
                + MESSAGE_DATE + " TEXT" + ")";
        db.execSQL(CREATE_MESSAGE_TABLE);

        String CREATE_OFLINE_MESSAGE_TABLE = "CREATE TABLE " + OFLINE_MESSAGE_TABLE + "("
                + OFLINE_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OFLINE_MESSAGE + " TEXT,"
                + OFLINE_MESSAGE_USERID + " TEXT,"
                + OFLINE_MESSAGE_TYPE + " TEXT,"
                + OFLINE_MESSAGE_UNIQUEID + " TEXT,"
                + OFLINE_MESSAGE_STATUS + " TEXT,"
                + OFLINE_MESSAGE_DATE + " TEXT" + ")";
        db.execSQL(CREATE_OFLINE_MESSAGE_TABLE);

    }


    public void deleteUser(int id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USER_TABLE, USER_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteMessage(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGE_TABLE, MESSAGE_UNIQUEID + " = ?",
                new String[]{id});
        db.close();
    }


    public void deleteOflineMessage(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(OFLINE_MESSAGE_TABLE, OFLINE_MESSAGE_UNIQUEID + " = ?",
                new String[]{id});
        db.close();
    }

    public void deleteMe(int id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MY_TABLE, MY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void addMe(String fullName, String phone, String photo, String uniqueID, String pTxt) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MY_FULLNAME, fullName);
        values.put(MY_PROFILEPHOTO, photo);
        values.put(MY_UNIQUEID, uniqueID);
        values.put(MY_STATUSTEXT, pTxt);
        values.put(MY_PHONE, phone);
        db.insert(MY_TABLE, null, values);
        db.close();
    }

    public void addUser(String fullName, String photo, String uniqueID, String pTxt, String location) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_FULLNAME, fullName);
        values.put(USER_PROFILEPHOTO, photo);
        values.put(USER_UNIQUEID, uniqueID);
        values.put(USER_PROFILETEXT, pTxt);
        values.put(USER_LOCATION, location);
        db.insert(USER_TABLE, null, values);
        db.close();
    }

    public void addMessage(String messeage, String userID, String date, String uniqueID, String status, String type) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MESSAGE, messeage);
        values.put(MESSAGE_USERID, userID);
        values.put(MESSAGE_DATE, date);
        values.put(MESSAGE_UNIQUEID, uniqueID);
        values.put(MESSAGE_TYPE, type);
        values.put(MESSAGE_STATUS, status);
        db.insert(MESSAGE_TABLE, null, values);
        db.close();
    }

    public void addOflineMessage(String messeage, String userID, String date, String uniqueID, String status, String type) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OFLINE_MESSAGE, messeage);
        values.put(OFLINE_MESSAGE_USERID, userID);
        values.put(OFLINE_MESSAGE_DATE, date);
        values.put(OFLINE_MESSAGE_UNIQUEID, uniqueID);
        values.put(OFLINE_MESSAGE_TYPE, type);
        values.put(OFLINE_MESSAGE_STATUS, status);
        db.insert(OFLINE_MESSAGE_TABLE, null, values);
        db.close();
    }

    public HashMap<String, String> meInfo() {


        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + MY_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(MY_FULLNAME, cursor.getString(1));
            user.put(MY_PROFILEPHOTO, cursor.getString(2));
            user.put(MY_PHONE, cursor.getString(3));
            user.put(MY_STATUSTEXT, cursor.getString(4));
            user.put(MY_UNIQUEID, cursor.getString(5));
        }
        cursor.close();
        db.close();

        return user;
    }

    public HashMap<String, String> userInfo(String id) {


        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT " + USER_FULLNAME + "," + USER_UNIQUEID + "," + USER_PROFILEPHOTO + "," + USER_LOCATION + " FROM " + USER_TABLE + " WHERE uniqueid='" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(USER_FULLNAME, cursor.getString(0));
            user.put(USER_UNIQUEID, cursor.getString(1));
            user.put(USER_PROFILEPHOTO, cursor.getString(2));
            user.put(USER_LOCATION, cursor.getString(3));

        }
        cursor.close();
        db.close();

        return user;
    }

    public HashMap<String, String> messageInfo(int id) {


        HashMap<String, String> message = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + MESSAGE_TABLE + " WHERE id=" + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            message.put(MESSAGE, cursor.getString(1));
            message.put(MESSAGE_USERID, cursor.getString(2));
            message.put(MESSAGE_TYPE, cursor.getString(3));
            message.put(MESSAGE_DATE, cursor.getString(4));

        }
        cursor.close();
        db.close();

        return message;
    }


    public ArrayList<HashMap<String, String>> users() {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + USER_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<HashMap<String, String>> userlist = new ArrayList<HashMap<String, String>>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }

                userlist.add(map);
            } while (cursor.moveToNext());
        }
        db.close();

        return userlist;
    }

    public ArrayList<HashMap<String, String>> messages(String receiverID) {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + MESSAGE_TABLE + " WHERE userid='" + receiverID + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<HashMap<String, String>> messages = new ArrayList<HashMap<String, String>>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }

                messages.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        return messages;
    }

    public ArrayList<HashMap<String, String>> oflineMessages() {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + OFLINE_MESSAGE_TABLE;

        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<HashMap<String, String>> messages = new ArrayList<HashMap<String, String>>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }

                messages.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        return messages;
    }

    public HashMap<String, String> lastMessage(String receiverID) {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + MESSAGE_TABLE + " WHERE userid='" + receiverID + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);
        HashMap<String, String> message = new HashMap<>();

        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    message.put(cursor.getColumnName(i), cursor.getString(i));
                }


            }
        }
        db.close();
        return message;
    }


    public void updateMessage(String messeage, String userID, String date, String uniqueID, String status, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        //Bu methodda ise var olan veriyi g�ncelliyoruz(update)
        ContentValues values = new ContentValues();
        values.put(MESSAGE, messeage);
        values.put(MESSAGE_USERID, userID);
        values.put(MESSAGE_DATE, date);
        values.put(MESSAGE_TYPE, type);
        values.put(MESSAGE_STATUS, status);

        // updating row
        db.update(MESSAGE_TABLE, values, MESSAGE_UNIQUEID + " = ?",
                new String[]{uniqueID});
        db.close();
    }

    public void updateMessageStatus(String uniqueID, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        //Bu methodda ise var olan veriyi g�ncelliyoruz(update)
        ContentValues values = new ContentValues();
        values.put(MESSAGE_STATUS, status);

        // updating row
        db.update(MESSAGE_TABLE, values, MESSAGE_UNIQUEID + " = ?",
                new String[]{uniqueID});
        db.close();
    }

    public int getRowCountUsers() {

        String countQuery = "SELECT  * FROM " + USER_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        return rowCount;
    }

    public int getRowCountMessage() {

        String countQuery = "SELECT  * FROM " + MESSAGE_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        return rowCount;
    }


    public void resetUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USER_TABLE, null, null);
        db.close();
    }

    public void resetMe() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MY_TABLE, null, null);
        db.close();
    }

    public void resetMessage() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGE_TABLE, null, null);
        db.close();
    }

    public void resetOflineMessage() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(OFLINE_MESSAGE_TABLE, null, null);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

}
