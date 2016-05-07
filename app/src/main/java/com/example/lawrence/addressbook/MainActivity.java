package com.example.lawrence.addressbook;

import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentTransaction;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.net.Uri;


// hosts the app's fragments and handles communication between them
// on phones, MainActivity displays one Fragment at a time, starting with ContactsFragment.
// on tablets, MainActivity always displays ContactsFragment at the left of the layout and depending on the context, displays either the DetailFragment or the AddEditFragment in the fith 2/3rds of the layout.
public class MainActivity
   extends    AppCompatActivity
   implements ContactsFragment.ContactsFragmentListener, // contains callbacks used to tell MainActivity when user selects a contacct in the contact list or adds a new contact.
              DetailFragment.DetailFragmentListener, // contains callbacks that used to delete a contact or edit and existing contact
              AddEditFragment.AddEditFragmentListener // callbacks for saving a new contact or saves changes to exisiting contacct
{

    // key for storing a contact's Uri in a Bundle passed to a fragment
    // key-val pair passed between MainActivity and various Fragments (which specific Frag depends on what operation)
    public static final String CONTACT_URI = "contact_uri";

    // displays contacts list
    private ContactsFragment mContactsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // if layout contains fragmentContainer, the phone layout is in use;
        // create and display a ContactsFragment
        // if Activity is being restored after being shutdown or recreated from a configuration change, savedInstanceState will not be null.
        if( savedInstanceState == null
                && findViewById(R.id.fragmentContainer) != null ){

            // create ContactsFragment
            mContactsFragment = new ContactsFragment();

            // add the fragment to FrameLayout
            // FragmentTransactions are used to add/remove a Fragment from screen.
            FragmentTransaction fragTransaction =
                getSupportFragmentManager().beginTransaction();
            fragTransaction.add(R.id.fragmentContainer, mContactsFragment);
            fragTransaction.commit(); // display ContactsFragment
        } else {
            // reattach the existing ContactsFragment.
            mContactsFragment = (ContactsFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.contactsFragment);
        }
    }

    // display DetailsFragment for selected contact
    @Override
    public void onContactSelected(Uri contactUri) {
        if( findViewById(R.id.fragmentContainer) != null ){
            // phone

            displayContact(contactUri, R.id.fragmentContainer);
        } else {
            // tablet

            // removes top of backstack
            getSupportFragmentManager().popBackStack();

            displayContact(contactUri, R.id.rightPaneContainer);
        }
    }

    // display AddEditFragment to add a new contact
    @Override
    public void onAddContact(){
        if( findViewById(R.id.fragmentContainer) != null ){
            // phone

            displayAddEditFragment(R.id.fragmentContainer, null);
        } else {
            // tablet

            displayAddEditFragment(R.id.rightPaneContainer, null);
        }
    }

    // display a contact
    private void displayContact(Uri contactUri, int viewID){
        DetailFragment detailFragment = new DetailFragment();

        // specify contact's Uri as an argument to the DetailFragment
        Bundle args = new Bundle();
        args.putParcelable(CONTACT_URI, contactUri);
        detailFragment.setArguments(args);

        // use a FragmentTransaction to display the DetailFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes DetailFragment to display
    }

    // display fragment for adding a new or editing an exisiting contact
    // viewID: int representing the view resource after it is inflated
    // contactUri: the contact to be added
    private void displayAddEditFragment(int viewID, Uri contactUri){
        AddEditFragment addEditFragment = new AddEditFragment();

        // if editing exisiting contact, provide contactUri as an arugment
        if( contactUri != null ){
            Bundle args = new Bundle();
            args.putParcelable(CONTACT_URI, contactUri);
            addEditFragment.setArguments(args);
        }

        // new contact being added
        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction =
            getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
    }

    // return to contact list when displayed contact deleted
    public void onContactDeleted(){
        // removes top of backstack
        getSupportFragmentManager().popBackStack();
        mContactsFragment.updateContactList(); // refresh contacts
    }

    // display the AddEditFragment to edit an existing contact
    @Override
    public void onEditContact(Uri contactUri){
        if( findViewById(R.id.fragmentContainer) != null ){
            displayAddEditFragment(R.id.fragmentContainer, contactUri);
        } else {
            displayAddEditFragment(R.id.rightPaneContainer, contactUri);
        }
    }

    // update GUI after new contact or updated contact saved
    @Override
    public void onAddEditCompleted(Uri contactUri){
        // removes top of back stack
        getSupportFragmentManager().popBackStack();
        mContactsFragment.updateContactList(); // refresh contacts

        if( findViewById(R.id.fragmentContainer) == null ){
            getSupportFragmentManager().popBackStack();

            // on tablet, display contact that was just added or edited
            displayContact(contactUri, R.id.rightPaneContainer);
        }
    }
}
