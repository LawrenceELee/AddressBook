package com.example.lawrence.addressbook.data;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lawrence.addressbook.data.DatabaseDescription.Contact;

// SQLiteOpenHelper subclass that defines the app's database
public class AddressBookDatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "AddressBook.db";
    private static final int DATABASE_VERSION = 1;

    // constructor
    public AddressBookDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // context is the the "context/activity/fragment" in which the db is being created or opened
        // the database name that we defined earlier
        // null is the CursorFactory argument (not used)
        // the database version number (starts with 1)
    }

    // creates contacts table when database is created
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // SQL code to create contacts table
        final String CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + Contact.TABLE_NAME + "(" +
                Contact._ID + " integer primary key, " +
                Contact.COLUMN_NAME + " TEXT, " +
                Contact.COLUMN_PHONE + " TEXT, " +
                Contact.COLUMN_EMAIL + " TEXT, " +
                Contact.COLUMN_STREET + " TEXT, " +
                Contact.COLUMN_CITY + " TEXT, " +
                Contact.COLUMN_STATE + " TEXT, " +
                Contact.COLUMN_ZIP + " TEXT);";

        // execute the sql command
        sqLiteDatabase.execSQL(CREATE_CONTACTS_TABLE);
    }

    // not used by needs to be overridden
    // this callback is used to upgrade a db from version 1 to 2 or 2 to 3, etc.
    // where you add more columns from 1 to 2.
    // for example, we add a facebook user name to contact details.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { /* not used */ }

    // similarly, there is a onDowngrade() method that goes from 2 to 1
    // but it doesn't need to be explicitly overridden.
}


