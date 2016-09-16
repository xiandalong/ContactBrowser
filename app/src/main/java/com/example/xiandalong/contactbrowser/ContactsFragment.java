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


public class ContactsFragment extends Fragment {

    public static final int CONTACT_LOADER_ID = 78;
    public static final String QUERY_TEXT = "Query Text";
    private SimpleCursorAdapter adapter;
    private LoaderManager.LoaderCallbacks<Cursor> contactsLoader;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setupCursorAdapter();
        setupContactsLoader();
        getActivity().getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,
                new Bundle(), contactsLoader);
        ListView lvContacts = (ListView) rootView.findViewById(R.id.contact_list);
        setupContactsListView(lvContacts);

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.search_option_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchView(getActivity());
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
                args.putString(QUERY_TEXT, query);
                getLoaderManager().restartLoader(CONTACT_LOADER_ID, args, contactsLoader);
            }
        });


    }

    private void setupContactsListView(ListView lvContacts) {
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
        lvContacts.setFastScrollEnabled(true);
    }

    private void setupContactsLoader() {
        contactsLoader =
                new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        String query = args.getString(QUERY_TEXT);
                        String[] projectionFields = new String[] { ContactsContract.Contacts._ID,
                                                                   ContactsContract.Contacts.DISPLAY_NAME,
                                                                   ContactsContract.CommonDataKinds.Phone.NUMBER };
                        return new CursorLoader(getActivity(),
                                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,// URI
                                                projectionFields,// projection fields
                                                query == null ? null :
                                                        ContactsContract.Contacts.DISPLAY_NAME +
                                                                " like '%" + query + "%'",
                                                null,
                                                ContactsContract.Contacts.DISPLAY_NAME + " " +
                                                        getResources().getString(R.string.sort_order)
                        );

                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                        adapter.swapCursor(cursor);
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {
                        adapter.swapCursor(null);
                    }

                };
    }


    private void setupCursorAdapter() {
        String[] uiBindFrom = {ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        int[] uiBindTo = {R.id.name_text_view,
                R.id.number_text_view};
        adapter = new SimpleCursorAdapter(
                getActivity(), R.layout.contact_list_item,
                null, uiBindFrom, uiBindTo,
                0);
    }


}
