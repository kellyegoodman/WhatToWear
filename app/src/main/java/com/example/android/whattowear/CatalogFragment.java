package com.example.android.whattowear;

import android.content.ContentUris;
import android.content.ContentValues;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.android.whattowear.data.ClothesContract.ClothesEntry;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;

    ClothesCursorAdapter mCursorAdapter;


    public CatalogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_catalog, container, false);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find a reference to the {@link ListView} in the layout
        ListView clothingItemListView = (ListView) rootView.findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.wardrobe_empty_view);
        clothingItemListView.setEmptyView(emptyView);

        // Set up an adapter to create a list item for each row of clothes data in the cursor.
        // There is no clothing data yet (until the loader finishes) so pass in null for the cursor.
        mCursorAdapter = new ClothesCursorAdapter(getActivity(), null);
        clothingItemListView.setAdapter(mCursorAdapter);

        // Set up item click listener
        clothingItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent to go to {@link EditorActivity}
                Intent intent = new Intent(getActivity(), EditorActivity.class);

                // Form the content Uri to represent the clothing item that was clicked on
                Uri contentItemUri = ContentUris.withAppendedId(ClothesEntry.CONTENT_URI, id);

                // Set the intent data with the uri of the clothing item that was clicked
                intent.setData(contentItemUri);

                // Launch the intent (open EditorActivity with the clicked clothing item data)
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);

        // populate options menu
        setHasOptionsMenu(true);

        return rootView;
    }

    /**
     * Helper method to insert hardcoded clothing data into the database. For debugging purposes only.
     */
    private void insertDummyClothing() {

        // Create a ContentValues object where column names are the keys,
        // and the attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ClothesEntry.COLUMN_ARTICLE_CATEGORY, ClothesEntry.CATEGORY_TOP);
        values.put(ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY, ClothesEntry.SUBCATEGORY_TSHIRT);
        values.put(ClothesEntry.COLUMN_ARTICLE_NAME, "tshirt1");
        values.put(ClothesEntry.COLUMN_ARTICLE_WEIGHT, 100);
        values.put(ClothesEntry.COLUMN_ARTICLE_COTTON, 100);
        values.put(ClothesEntry.COLUMN_ARTICLE_POLYESTER, 0);
        values.put(ClothesEntry.COLUMN_ARTICLE_RAYON, 0);
        values.put(ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX, 0);
        values.put(ClothesEntry.COLUMN_ARTICLE_WOOL, 0);

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the clothes table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        Uri newUri = getActivity().getContentResolver().insert(ClothesEntry.CONTENT_URI, values);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.menu_catalog, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyClothing();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all clothes in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getActivity().getContentResolver().delete(ClothesEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from clothes database");
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Define a projection that specifies which table columns we care about.
        String[] projection = {
                ClothesEntry._ID,
                ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY,
                ClothesEntry.COLUMN_ARTICLE_NAME,
                ClothesEntry.COLUMN_ARTICLE_IMAGE,
                ClothesEntry.COLUMN_ARTICLE_CLO_VALUE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getActivity(),    // Parent activity's context
                ClothesEntry.CONTENT_URI,       // Provider content URI to query
                projection,                     // Columns to include in the resulting cursor
                null,                   // No selection clause
                null,                // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Update {@link ClothesCursorAdapter} with this new cursor containing updated clothing item data
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Callback called when data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

}
