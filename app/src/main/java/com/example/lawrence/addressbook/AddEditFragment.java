package com.example.lawrence.addressbook;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;

import android.net.Uri;

import android.os.Bundle;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.lawrence.addressbook.data.DatabaseDescription.Contact;

// provides a GUI for adding new or editing existing contacts
// by managing the TextInputLayouts and FAB (for adding/editing contacts).
public class AddEditFragment
   extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

    // Nested interface defines callback methods implemented by MainActivity 
    // so that it can respond when a new or updated contact is saved.
    public interface AddEditFragmentListener {
        // called when contact is saved
        void onAddEditCompleted(Uri contactUri);
    }

    // constant used to define Loader
    // by convention, each Loader should have a unique number,
    // even though we only use 1 loader, it is good programming practice to
    // number it
    // (Loaders are a subclass of AsyncTaskLoaders and are used to execute
    // a task on a separate thread so that main GUI thread remains responsive.
    private static final int CONTACT_LOADER = 0;

    // notify MainActivity AddEditFragmentListener
    private AddEditFragmentListener mListener;

    // Uri of selected contact (contact to edit)
    private Uri mContactUri;

    // flag to determine if adding (boolean == true) or editing (boolean == false) contact
    private boolean mAddingNewContact = true;

    // view widgets
    private TextInputLayout mNameTextInputLayout;
    private TextInputLayout mPhoneTextInputLayout;
    private TextInputLayout mEmailTextInputLayout;
    private TextInputLayout mStreetTextInputLayout;
    private TextInputLayout mCityTextInputLayout;
    private TextInputLayout mStateTextInputLayout;
    private TextInputLayout mZipTextInputLayout;
    private FloatingActionButton mSaveContactFAB;

    // view container, used with SnackBars
    private CoordinatorLayout mCoordinatorLayout;

    // set AddEditFragmentListener when Fragment attached
    // we attach/detach because it is good programming practice
    // and it conserves battery life when app is not in foreground.
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mListener = (AddEditFragmentListener) context;
    }

    // remove frag when detached to save battery life
    @Override
    public void onDetach(){
        super.onDetach();
        mListener = null;
    }

    // called when Fragment's view needs to be creatd
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        // inflate layout and get refs to EditText widgets (TextInputLayout are just EditText wrapped so that it displays the "hint" text).
        View view =
           inflater.inflate(R.layout.fragment_add_edit, container, false);
        mNameTextInputLayout = (TextInputLayout) view.findViewById(R.id.nameTextInputLayout);
        mNameTextInputLayout.getEditText().addTextChangedListener(nameChangeListener);
        mPhoneTextInputLayout = (TextInputLayout) view.findViewById(R.id.phoneTextInputLayout);
        mEmailTextInputLayout = (TextInputLayout) view.findViewById(R.id.emailTextInputLayout);
        mStreetTextInputLayout = (TextInputLayout) view.findViewById(R.id.streetTextInputLayout);
        mCityTextInputLayout = (TextInputLayout) view.findViewById(R.id.cityTextInputLayout);
        mStateTextInputLayout = (TextInputLayout) view.findViewById(R.id.stateTextInputLayout);
        mZipTextInputLayout = (TextInputLayout) view.findViewById(R.id.zipTextInputLayout);

        // set FAB's event listener
        mSaveContactFAB = (FloatingActionButton) view.findViewById(R.id.saveFloatingActionButton);
        mSaveContactFAB.setOnClickListener(saveContactButtonClicked);
        updateSaveButtonFAB();

        // display SnackBars
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        Bundle args = getArguments(); // null if creating new contact

        if( args != null ){
            mAddingNewContact = false;
            mContactUri = args.getParcelable(MainActivity.CONTACT_URI);
        }

        // if editing an existing contact, create Loader to get contact
        if( mContactUri != null ){
            getLoaderManager().initLoader(CONTACT_LOADER, null, this);
        }

        return view;
    } // end onCreateView

    // detects when the text in the nameTextInputLayout's EditText changes
    // to hide or show mSaveButton FAB
    private final TextWatcher nameChangeListener = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveButtonFAB();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after){ /* not used */ }

        @Override
        public void afterTextChanged(Editable s){ /* not used */ }

    };

    // show mSaveButtonFAB only if the name is not empty
    private void updateSaveButtonFAB(){
        String input = mNameTextInputLayout.getEditText().getText().toString();

        // if user entered non-blank name, show FAB
        if( input.trim().length() != 0 ){
            mSaveContactFAB.show();
        } else {
            mSaveContactFAB.hide();
        }
    }

    // responds to event generated when user saves a contact
    private final View.OnClickListener saveContactButtonClicked =
        new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // hide virtual/software keyboard
                ((InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getView().getWindowToken(), 0);

                saveContact(); // save contact to db
            }
        };
    
    // save contact info entered in EditTexts to db
    private void saveContact(){
        // create ContentValues object containing contact's key-val pairs
        ContentValues contentValues = new ContentValues();
        contentValues.put(
                Contact.COLUMN_NAME,
                mNameTextInputLayout.getEditText().getText().toString()
        );
        contentValues.put(
                Contact.COLUMN_PHONE,
                mPhoneTextInputLayout.getEditText().getText().toString()
        );
        contentValues.put(
                Contact.COLUMN_EMAIL,
                mEmailTextInputLayout.getEditText().getText().toString()
        );
        contentValues.put(
                Contact.COLUMN_STREET,
                mStreetTextInputLayout.getEditText().getText().toString()
        );
        contentValues.put(
                Contact.COLUMN_CITY,
                mCityTextInputLayout.getEditText().getText().toString()
        );
        contentValues.put(
                Contact.COLUMN_STATE,
                mStateTextInputLayout.getEditText().getText().toString()
        );
        contentValues.put(
                Contact.COLUMN_ZIP,
                mZipTextInputLayout.getEditText().getText().toString()
        );

        if( mAddingNewContact ){
            // use Activity's ContentResolver to invoke
            // insert on the AddressBookContentProvider

            // insert db
            Uri newContactUri = getActivity().getContentResolver()
                    .insert(Contact.CONTENT_URI, contentValues);

            // display msg if contact created or error
            if( newContactUri != null ){
                Snackbar.make(mCoordinatorLayout, R.string.contact_added, Snackbar.LENGTH_LONG).show();
                mListener.onAddEditCompleted(newContactUri);
            } else {
                Snackbar.make(mCoordinatorLayout, R.string.contact_not_added, Snackbar.LENGTH_LONG).show();
            }

        } else {
            // use Activity's ContentResolver to invoke
            // insert on the AddressBookContentProvider

            // update db
            int updateRows = getActivity().getContentResolver().update(
                mContactUri, contentValues, null, null
            );

            if( updateRows > 0 ){
                mListener.onAddEditCompleted(mContactUri);
                Snackbar.make(mCoordinatorLayout,
                       R.string.contact_updated, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(mCoordinatorLayout,
                       R.string.contact_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }
    } // end saveContact()

    //**************************************************
    // LoaderManager.LoaderCallbacks<Cursor> methods that need to be overriden
    //**************************************************

    // called by LoaderManager to create a Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        // create an appropriate CursorLoader based on the id argument;
        // only one Loader in this fragment, so the switch is unnecessary
        switch( id ){
            case CONTACT_LOADER:
                return new CursorLoader(
                        getActivity(),      
                        mContactUri,         // Uri of contact to display
                        null,               // null projection returns all cols
                        null,               // null selection returns all rows
                        null,               // selection args
                        null                // sort order
                );
            default:
                return null;
        }
    }

    // called by LoaderManger when loading completes
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        // if contact exists in db, display it's data
        if( data != null && data.moveToFirst() ){
            // get column idx for each data item
            int nameIdx = data.getColumnIndex(Contact.COLUMN_NAME);
            int phoneIdx = data.getColumnIndex(Contact.COLUMN_PHONE);
            int emailIdx = data.getColumnIndex(Contact.COLUMN_EMAIL);
            int streetIdx = data.getColumnIndex(Contact.COLUMN_STREET);
            int cityIdx = data.getColumnIndex(Contact.COLUMN_CITY);
            int stateIdx = data.getColumnIndex(Contact.COLUMN_STATE);
            int zipIdx = data.getColumnIndex(Contact.COLUMN_ZIP);

            // fill EditTexts with data from db
            mNameTextInputLayout.getEditText().setText(data.getString(nameIdx));
            mPhoneTextInputLayout.getEditText().setText(data.getString(phoneIdx));
            mEmailTextInputLayout.getEditText().setText(data.getString(emailIdx));
            mStreetTextInputLayout.getEditText().setText(data.getString(streetIdx));
            mCityTextInputLayout.getEditText().setText(data.getString(cityIdx));
            mStateTextInputLayout.getEditText().setText(data.getString(stateIdx));
            mZipTextInputLayout.getEditText().setText(data.getString(zipIdx));

            updateSaveButtonFAB();
        }
    }

    // called by LoaderManager when Loader is being reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { /* not used */ }

}

























