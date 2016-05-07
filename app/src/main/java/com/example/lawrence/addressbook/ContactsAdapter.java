package com.example.lawrence.addressbook;

import android.database.Cursor;

import android.net.Uri;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.lawrence.addressbook.data.DatabaseDescription.Contact;

// subclass of RecyclerView.Adapter and is used by ContactsFragment's RecyclerView to bind the sorted list of contact names to the RecyclerView.
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

   private Cursor mCursor = null;
   private final ContactClickListener mClickListener;

   public ContactsAdapter(ContactClickListener clickListener){
       mClickListener = clickListener;
   }

   // set up new list item and it's ViewHolder by inflating layout
   // when RecyclerView is created.
   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
       // use Android's system template for a simple list, instead of
       // our own custom layout.
       View view = LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false);

       // use ViewHolder's constructor to return a new ViewHolder.
       return new ViewHolder(view);
   }

   // set text of list item to display search tag
   @Override
   public void onBindViewHolder(ViewHolder holder, int position){
       // move SQLite cursor to the contact that corresponds to current RecyclerView item's position
       mCursor.moveToPosition(position);

       // set ViewHolder's rowID by looking up the column number of Contact._ID
       holder.setRowID(mCursor.getLong(mCursor.getColumnIndex(Contact._ID)));

       // set the list item's text to the contact's name
       holder.textView.setText(
               mCursor.getString(mCursor.getColumnIndex(Contact.COLUMN_NAME))
       );
   }

   // swap this adapters current Cursor for a new one
   public void swapCursor(Cursor cursor){
       mCursor = cursor;
       notifyDataSetChanged();
   }

   // return num of items that adapter binds
   @Override
   public int getItemCount() {
       if( mCursor != null )     return mCursor.getCount();
       else return 0;
   }

   // interface implemented by MainActivity.
   // onClick() callback executes when user touch an item in the RecyclerView list
   public interface ContactClickListener {
       void onClick(Uri contactUri);
   }

   // required nested inner class of RecyclerView.ViewHolder
   // used to implement view-holder pattern.
   public class ViewHolder extends RecyclerView.ViewHolder {
       public TextView textView;
       private long rowID;

       public ViewHolder(View view){
           super(view);
           textView = (TextView) view.findViewById(android.R.id.text1);

           // attach listener to view
           view.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        mClickListener.onClick(Contact.buildContactUri(rowID));
                    }
                }
           );
       }

       // set db row id for contact
       public void setRowID(long rowID){ this.rowID = rowID; }

   } // end ViewHolder nest class
}
