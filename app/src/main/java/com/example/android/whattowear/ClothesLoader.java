package com.example.android.whattowear;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import com.example.android.whattowear.data.ClothesContract.ClothesEntry;

public class ClothesLoader
    implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Tag for log messages */
    public static final String LOG_TAG = ClothesLoader.class.getName();

    private Context mContext;
    private OutfitLogic mLogicCallback;
    private String[] mSelectionArgs;
    private Cursor mCursor;

    /**
     * Constructs a new {@link ClothesLoader}.
     *
     * @param context of the activity
     * @param logic parent to callback when load has finished
     * @param category_list list of subcategories to get from database
     */
    public ClothesLoader(Context context, OutfitLogic logic, String[] category_list) {
        mContext = context;
        mLogicCallback = logic;
        mSelectionArgs = category_list;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Define a projection that specifies which table columns we care about.
        String[] projection = {
                ClothesEntry._ID,
                ClothesEntry.COLUMN_ARTICLE_CATEGORY,
                ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY,
                ClothesEntry.COLUMN_ARTICLE_IMAGE,
                ClothesEntry.COLUMN_ARTICLE_CLO_VALUE};

        String selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
        for (int s = 0; s < mSelectionArgs.length - 1; s++) {
            selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR " + selection;
        }

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(mContext,    // Parent activity's context
                ClothesEntry.CONTENT_URI,       // Provider content URI to query
                projection,                     // Columns to include in the resulting cursor
                selection,                   // selection clause
                mSelectionArgs,                // selection arguments
                ClothesEntry.COLUMN_ARTICLE_CLO_VALUE);   // sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // store cursor
        mCursor = cursor;
        Log.v(LOG_TAG, "onLoadFinished");
        mLogicCallback.taskComplete();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Callback called when data needs to be deleted
        Log.v(LOG_TAG, "onLoaderReset");
    }
}
