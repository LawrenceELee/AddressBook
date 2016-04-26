package com.example.lawrence.addressbook.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.lawrence.addressbook.R;
import com.example.lawrence.addressbook.data.DatabaseDescription.Contact;

// subclass of ContentProvider for manipulating the app's data
// this code will interact with the SQLite object that interact with SQLite db
// (i.e it defines the CRUD (create, read, update, delete) operations)
// In general, a ContentProvider exposes an app's data for use in that
// app or in other apps.
// For example, Android provides various built-in ContentProviders like
// Android's Contacts, Calendar, Camera apps.
public class AddressBookContentProvider extends ContentProvider{

    // constants used with UriMatcher to determine operation to perform
    private static final int ONE_CONTACT = 1;   // op code "1" means we want to manipulate one contact.
    private static final int CONTACTS = 2; // op code "2" means we want to manipulate contacts table.

    // member instance variable used to access the database
    private AddressBookDatabaseHelper mDBHelper;

    // UriMatcher helps ContentProvider determine operation to perform
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // static block to configure this ContentProvider's UriMatcher
    // the benefit of static blocks is that it is EXECUTED AND LOADED ONCE when code is run.
    static {
        // Uri for Contact with the specified id (#)
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Contact.TABLE_NAME + "/#", ONE_CONTACT);
        // looks like: "content://com.example.lawrence.addressbook.data/contacts/#"
        // where # is a wildcard that matches a string of numeric chars
        // in this case, the primary-key value for one specific contact from the table

        // Uri for Contacts table
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Contact.TABLE_NAME, CONTACTS);
        // looks like: "content://com.example.lawrence.addressbook.data/contacts"
        // which represents the entire contacts table.
        // when a Uri matches this form, the UriMatcher returns the constant CONTACTS.
    }

    // ContentResolvers work in partnership with ContentProviders.
    // when Android receives a request from a ContentResolver,
    // Android calls this callback/hook for when AddressBookContentProvider is created
    @Override
    public boolean onCreate() {
        // create AddressBookDatabaseHelper member instance variable object
        mDBHelper = new AddressBookDatabaseHelper(getContext());
        return true; // "true" means ContentProvider successfully created
    }

    // query the database
    // projection: string array representing the specific columns to retrieve.
    // selection: string containing selection criteria (e.g. WHERE clause in a SQL statement)
    // selectionArgs: string array containing args used to replace arg placeholders (?) in selection
    // sortOrder: string representing the sort order. (e.g ORDER BY clause in a SQL statement)
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // create SQLiteQueryBuilder for querying contacts table
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Contact.TABLE_NAME); // get data from contacts table

        switch( uriMatcher.match(uri) ){
            case ONE_CONTACT:
                // append WHERE clause to SQL query with contact's id
                // contact with specified id will be selected from table to display or edit
                queryBuilder.appendWhere(Contact._ID + "=" + uri.getLastPathSegment());
                break;
            case CONTACTS:
                // all contacts will be selected from table to display their names in contacts fragment
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_query_uri) + uri);
        } // end switch

        // execute the query to select one or all contacts
        // when SQL query is execute, it will return a "cursor" or arrow point to the beginning of
        // the row(s) that meet the criteria
        Cursor cursor = queryBuilder.query(
            mDBHelper.getReadableDatabase(), projection, selection,
            selectionArgs, null, null, sortOrder
        );

        // config to watch for content changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    } // end query

    // required to override method, not used
    // this is used with Intents with MIME types
    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    // insert a new contact into database
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri newContactUri = null;

        switch( uriMatcher.match(uri) ){
            case CONTACTS:
                // insert new contact -- success yields new contact's row id
                long rowId = mDBHelper.getWritableDatabase()
                            .insert(Contact.TABLE_NAME, null, contentValues);

                // if contact was inserted, create an appropriate Uri;
                // otherwise, throw an exception.
                if( rowId >= 1 ){ // SQLite row IDs start at 1
                    newContactUri = Contact.buildContactUri(rowId);

                    // notify observers that the database changed
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    throw new SQLException(
                       getContext().getString(R.string.insert_failed) + uri
                    );
                }
                break;
            default:
                throw new UnsupportedOperationException(
                    getContext().getString(R.string.invalid_insert_uri) + uri
                );
        } // end switch

        return newContactUri;
    }

    // update an existing contact in the database
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int numOfRowsUpdated; // 1 if update successful, 0 otherwise

        switch( uriMatcher.match(uri) ){
            case ONE_CONTACT:
                // get from uri the id of contact to update
                String id = uri.getLastPathSegment();

                // update contact
                numOfRowsUpdated = mDBHelper.getWritableDatabase().update(
                   Contact.TABLE_NAME, contentValues, Contact._ID + "=" + id, selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException(
                    getContext().getString(R.string.invalid_update_uri) + uri
                );

        } // end switch

        // if changes were made, notify observers that db changed
        if( numOfRowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numOfRowsUpdated;
    }

    // delete existing contact from db
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        int numOfRowsDeleted;

        switch( uriMatcher.match(uri) ){
            case ONE_CONTACT:
                // get from uri the id of contact to delete
                String id = uri.getLastPathSegment();

                // delete the contact
                numOfRowsDeleted = mDBHelper.getWritableDatabase().delete(
                    Contact.TABLE_NAME, Contact._ID + "=" + id, selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException(
                    getContext().getString(R.string.invalid_delete_uri) + uri
                );
        }

        // notify observers that the db changed
        if( numOfRowsDeleted != 0 ){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numOfRowsDeleted;
    }
}
