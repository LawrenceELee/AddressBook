package com.example.lawrence.addressbook.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

// this class describes the table and column names for this app's database
// and other info required by ContentProvider
public class DatabaseDescription {
    // ContentProvider's authority, the name that's supplied to a ContentResolver to locate a ContentProvider.
    // this is like the domain portion of the URL. it is typically package name.
    // analogous to URL, http://www.example.com/contactus
    // http is the scheme
    // www.example.com is the "authority"
    public static final String AUTHORITY = "com.example.lawrence.addressbook.data";

    // base URI used to interact with ContentProvider
    // they look similar to a linux path to a file
    // ex, "content://media/external/audio/media/710"
    // ex, "content://com.example.lawrence.addressbook.data/external/.../my_contacts_table.db"
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // nested class defines colums/fields/contents of contacts table
    public static final class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contacts";

        // Uri for contacts table
        private static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // column names for table
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_STREET = "street";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_ZIP = "zip";

        // create Uri for a specific contact
        public static Uri buildContactUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
