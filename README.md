Address Book Android app to hold contact information (name, phone number, email, address, etc).

Learned:
* FragmentTransactions:
    + used to attach/detach Fragments from GUI.
* RecyclerView:
    + used to display data.
* SQLLite:
    + SQLiteOpenHelper, SQLiteDatabase are used to interact with SQLite database on the phone.
    + this is more expensive than SharedPreferences, but is prefer when dealing with more complex data.
* ContentProvider:
    + used to interact with SQLite database.
* ContentResolver:
    + used to invoke methods of a ContentProvider to perform taks with a database.
* LoaderManager, Loaders:
    + used to access database asynchronously.
* Cursors:
    + used to manipulate database query results.
