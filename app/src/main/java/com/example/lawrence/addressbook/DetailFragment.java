package com.example.lawrence.addressbook;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;

import android.database.Cursor;

import android.net.Uri;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.lawrence.addressbook.data.DatabaseDescription.Contact;

// class manages the styled TextViews that display a selected contact's details
// and app bar items that enable user to edit/delete the currently displayed
// contact.
public class DetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    // the nested interface defines callback methods implemented by MainActivity
    // to respond when a contact is deleted or when a user touched the app bar
    // item to edit a contact.

    // pass Uri of contact to edit to DetailFragmentListener
    // this methods are implemented in MainActivity.
    public interface DetailFragmentListener{
        void onContactDeleted();
        void onEditContact(Uri contactUri);
    }

    // uniquely number Loader(s)
    // Loaders query ContentProviders
    private static final int CONTACT_LOADER = 0;

    // reference to listener in MainActivity used to notify when
    // user deletes or edits a contact
    private DetailFragmentListener mListener;

    // Uri of selected contact
    private Uri contactUri;

    // refs to textview widgets
    private TextView mNameTextView;
    private TextView mPhoneTextView;
    private TextView mEmailTextView;
    private TextView mStreetTextView;
    private TextView mCityTextView;
    private TextView mStateTextView;
    private TextView mZipTextView;

    // set DetailFragmentListener when fragment attached
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mListener = (DetailFragmentListener) context;
    }

    // detach listener
    @Override
    public void onDetach(){
        super.onDetach();
        mListener = null;
    }

    // call when DetailFragmentListener's view need to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        // we need to use Bundle and Parcelables to pass info from Activity to
        // another Activity.
        //
        // get Bundle of args then extract the contact's uri
        Bundle args = getArguments();

        // if is passing info from previous Activity
        if( args != null ){
            contactUri = args.getParcelable(MainActivity.CONTACT_URI);
        }

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        // assign textview widgets
        mNameTextView = (TextView) view.findViewById(R.id.nameTextView);
        mPhoneTextView = (TextView) view.findViewById(R.id.phoneTextView);
        mEmailTextView = (TextView) view.findViewById(R.id.emailTextView);
        mStreetTextView = (TextView) view.findViewById(R.id.streetTextView);
        mCityTextView = (TextView) view.findViewById(R.id.cityTextView);
        mStateTextView = (TextView) view.findViewById(R.id.stateTextView);
        mZipTextView = (TextView) view.findViewById(R.id.zipTextView);

        // load contact
        getLoaderManager().initLoader(CONTACT_LOADER, null, this);

        return view;
    }

    // display fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    // handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch( item.getItemId() ){
            case R.id.action_edit:
                mListener.onEditContact(contactUri); // pass Uri to mListener
                return true;
            case R.id.action_delete:
                deleteContact();
                return true;
        }

        // if not one of the options we defined, then pass to parent.
        return super.onOptionsItemSelected(item);
    }

    // method to delete contact
    private void deleteContact(){
        // use FragmentManager to display the confirmDelete DialogFragment
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }

    // DialogFragment to confirm delete of contact
    private final DialogFragment confirmDelete = new DialogFragment() {

        @Override
        public Dialog onCreateDialog(Bundle bundle){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            builder.setPositiveButton(
                    R.string.button_delete, 
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int button){
                            // use Activity's ContentResolver to invoke delete
                            // on the AddressBookContentProvider
                            getActivity().getContentResolver().delete(
                                contactUri, null, null
                            );

                            // notify listener
                            mListener.onContactDeleted(); 
                        }
                    }
            );

            builder.setNegativeButton(R.string.button_cancel, null);

            // return ref to AlertDialog object
            return builder.create();
        }
    };

    // called by LoaderManager to create a Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        // create an appropriate CursorLoader based on the id arg,

        CursorLoader cursorLoader;

        // pick the right Loader to make based on id passed
        switch( id ){
            case CONTACT_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity(),
                        contactUri,
                        null,
                        null,
                        null,
                        null
                );
                break;
            default:
                cursorLoader = null;
                break;
        }

        return cursorLoader;
    }

    // called by LoaderManager when loading completes
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        // if contact exists in db, display its data
        if( data != null && data.moveToFirst() ){
            // get column idx for each data item
            int nameIdx = data.getColumnIndex(Contact.COLUMN_NAME);
            int phoneIdx = data.getColumnIndex(Contact.COLUMN_PHONE);
            int emailIdx = data.getColumnIndex(Contact.COLUMN_EMAIL);
            int streetIdx = data.getColumnIndex(Contact.COLUMN_STREET);
            int cityIdx = data.getColumnIndex(Contact.COLUMN_CITY);
            int stateIdx = data.getColumnIndex(Contact.COLUMN_STATE);
            int zipIdx = data.getColumnIndex(Contact.COLUMN_ZIP);

            // fill TextViews with retrieved data
            mNameTextView.setText(data.getString(nameIdx));
            mPhoneTextView.setText(data.getString(phoneIdx));
            mEmailTextView.setText(data.getString(emailIdx));
            mStreetTextView.setText(data.getString(streetIdx));
            mCityTextView.setText(data.getString(cityIdx));
            mStateTextView.setText(data.getString(stateIdx));
            mZipTextView.setText(data.getString(zipIdx));
        }
    }

    // call by LoaderManager when Loader is being reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { /* not used */ }

}
















































