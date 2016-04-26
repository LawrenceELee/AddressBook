package com.example.lawrence.addressbook;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.design.widget.FloatingActionButton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;

import android.database.Cursor;

import android.net.Uri;

import com.example.lawrence.addressbook.data.DatabaseDescription.Contact;

// this class manages the contact-list RecyclerView and the FAB for adding contacts.
// On a phone, this is the first Fragment presented by MainActivity.
// On a tablet, MainActivity always displays this Fragment on the left side.
// The nexted interface defines callback methods implemented by MainActivity so
// that it can respond when a contact is selected of added.
public class ContactsFragment extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

    // callback methods implemented by MainActivity
    public interface ContactsFragmentListener{

        // callback when contact is selected in MainActivity
        void onContactsSelected(Uri contactUri);

        // callback when "add" button pressed in MainActivity
        void onAddContact();
    }

    // if using multiple Loaders, they should be uniquely numbered.
    // even though we're just using one, this is a good programming practice.
    // Loaders "load" the data from the db into the app b/c this might be a
    // long running operation (file/database access) so we preform it outside
    // the main GUI thread.
    // CursorLoader is a subclass of AsyncTaskLoader.
    // Loaders also optimize operations by only "committing" changes when all
    // changes that need to be process have been processed (instead of
    // commiting changes after every row changed).
    private static final int CONTACTS_LOADER = 0; // id's Loader

    // used to inform MainActivity when a contact is selected
    private ContactsFragmentListener mListener;

    // adapter for RecyclerView
    private ContactsManager mContactsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true); // fragment has menu item to display

        // inflate layout file to GUI
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        // recyclerView should display items in a vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));


        // create recyclerView's adapter and item click listener
        mContactAdapter = new ContactsAdapter(
            new ContactsAdapter.ContactClickListener(){
                @Override
                public void onClick(Uri contactUri){
                    mListener.onContactSelected(contactUri);
                }
            }
        );
        recyclerView.setAdapter(mContactsAdapter);

        // attach a customer ItemDecorator to draw dividers between items
        recyclerView.addItemDecoration(new ItemDivider(getContext()));

        // improves performance if RecyclerView's layout size never changes
        recyclerView.setHasFixedSize(true);

        // get FAB and configure its listener
        FloatingActionButton addButton =
           (FloatingActionButton) view.findViewById(R.id.addButton);
        addButton.setOnClickListener(
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onAddContact();
                }
            }
        );


        return view;
    } // end onCreateView

    // set ContactsFragmentListener when fragment attached
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mListener = (ContactsFragmentListener) context;
    }

    // remove ContactsFragmentListener when fragment detached
    // we attach/detach when fragment is fore/back ground to improve performance
    // and conserve battery life.
    @Override
    public void onDetach(){
        super.onDetach();
        mListener = null;
    }

    // initialize a Loader when this fragment's activity is created
    // Loader and LoaderManager are used to query the AddressBookContentProviderand recieve a Cursor that ContactsAdapter used to supply data to the RecyclerView.
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);
    }

    // called from MainActivity when other Fragment's update database
    public void updateContactList(){
        mContactsAdapter.notifyDataSetChanged();
    }

    // called by LoaderManager to create a Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        // create an appropriate CursorLoader based on the id arg;
        // we use switch even though only 1 case because it is good a programming practice.
        switch( id ){
            case CONTACTS_LOADER:
                return new CursorLoader(
                        getActivity(),
                        Contact.CONTENT_URI, // Uri of contacts table
                        null, // null for all columns
                        null, // null for all rows
                        null, // no selection args
                        Contact.COLUMN_NAME + " COLLATE NOCASE ASC"); // sort order
                break;
            default:
                return null;
        }
    }

    // called by LoaderManager when loading completes
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        mContactsAdapter.swapCursor(data);
    }

    // called by LoaderManager when Loader is being reset
    @Override
    public void onLoaderReset(){
        mContactsAdapter.swapCursor(null);
    }
}
