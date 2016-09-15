package com.example.xiandalong.contactbrowser;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * Created by xiandalong on 9/13/16.
 */
public class ContactsFragment extends Fragment {

    // Defines the id of the loader for later reference
    public static final int CONTACT_LOADER_ID = 78; // From docs: A unique identifier for this loader. Can be whatever you want.
    public static final String QUERY_NAME = "nameQuery";
    private SimpleCursorAdapter adapter;
    private LoaderManager.LoaderCallbacks<Cursor> contactsLoader =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                // Create and return the actual cursor loader for the contacts data
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    String query = args.getString(QUERY_NAME);
                    // Define the columns to retrieve
                    String[] projectionFields = new String[]{ ContactsContract.Contacts._ID,
                                                              ContactsContract.Contacts.DISPLAY_NAME,
                                                              ContactsContract.CommonDataKinds.Phone.NUMBER };
                    // Construct the loader
                    CursorLoader cursorLoader = new CursorLoader(getActivity(),
                                                                 ContactsContract.CommonDataKinds.Phone.CONTENT_URI,// URI
                                                                 projectionFields,// projection fields
                                                                 query == null ? null :
                                                                         ContactsContract.Contacts.DISPLAY_NAME +
                                                                                 " like '%" + query + "%'",
// the selection criteria
                                                                 null,// the selection args
                                                                 "DISPLAY_NAME ASC"
                                                                 // the sort order
                    );
                    // Return the loader for use
                    return cursorLoader;
                }

                // When the system finishes retrieving the Cursor through the CursorLoader,
                // a call to the onLoadFinished() method takes place.
                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    // The swapCursor() method assigns the new Cursor to the adapter
                    adapter.swapCursor(cursor);
                }

                // This method is triggered when the loader is being reset
                // and the loader data is no longer available. Called if the data
                // in the provider changes and the Cursor becomes stale.
                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    // Clear the Cursor we were using with another call to the swapCursor()
                    adapter.swapCursor(null);
                }

            };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        // inflate rootView
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setupCursorAdapter();


        // Initialize the loader with a special ID and the defined callbacks from above
        getActivity().getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,
                new Bundle(), contactsLoader);
        // Find list and bind to adapter
        ListView lvContacts = (ListView) rootView.findViewById(R.id.contact_list);
        lvContacts.setAdapter(adapter);
        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TextView numberTextView = (TextView) view.findViewById(R.id.number_text_view);
                String phoneNumber = numberTextView.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });


        return rootView;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {


        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.search_option_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchView(((AppCompatActivity) getActivity()).getSupportActionBar()
                                                       .getThemedContext());
        MenuItemCompat.setShowAsAction(item,
                                       MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW |
                                               MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateLoader(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateLoader(newText);
                return true;
            }

            private void updateLoader(String query) {
                Bundle args = new Bundle();
                args.putString(QUERY_NAME, query);
                getLoaderManager().restartLoader(CONTACT_LOADER_ID, args, contactsLoader);
            }
        });


    }

    // Create simple cursor adapter to connect the cursor dataset we load with a ListView
    private void setupCursorAdapter() {
        // Column data from cursor to bind views from
        String[] uiBindFrom = {ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        // View IDs which will have the respective column data inserted
        int[] uiBindTo = {R.id.name_text_view,
                R.id.number_text_view};
        // Create the simple cursor adapter to use for our list
        // specifying the template to inflate (item_contact),
        adapter = new SimpleCursorAdapter(
                getActivity(), R.layout.contact_list_item,
                null, uiBindFrom, uiBindTo,
                0);


    }


}
