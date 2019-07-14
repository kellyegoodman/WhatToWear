package com.example.android.whattowear;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.example.android.whattowear.Outfit.ClothingItem;

import com.example.android.whattowear.data.ClothesContract.ClothesEntry;

public class OutfitLogic {

    public static final String LOG_TAG = OutfitLogic.class.getName();

    public static final int CASUAL_TAG = 3;
    public static final int FORMAL_TAG = 8;

    private static final int TOP_LOADER_ID = 0;
    private static final int BOTTOM_LOADER_ID = 1;
    private static final int DRESS_LOADER_ID = 2;
    private static final int OUTER1_LOADER_ID = 3;
    private static final int OUTER2_LOADER_ID = 3;

    private Context m_context;
    private OutfitFragment mParent;
    private int mTag;

    private int mTasksLeft;
    private boolean mIsLoadingDone;
    private boolean mHasWarmthRequest;
    private double mWarmthRequest;

    private Cursor m_topCursor;
    private Cursor m_bottomCursor;

    public OutfitLogic(Context context, OutfitFragment parent, LoaderManager loaderManager, int tag) {
        m_context = context;
        mParent = parent;
        mTag = tag;
        mTasksLeft = 2;
        mHasWarmthRequest = false;
        mIsLoadingDone = false;
        loaderManager.initLoader(tag + TOP_LOADER_ID, null, topLoaderListener);
        loaderManager.initLoader(tag + BOTTOM_LOADER_ID, null, bottomLoaderListener);
    }

    public void FetchOutfit(double warmth_request) {
        mWarmthRequest = warmth_request;
        mHasWarmthRequest = true;
        if (mTasksLeft==0){
            onAllTasksCompleted();
        }
    }

    public void taskComplete() {
        mTasksLeft--;
        if ((mTasksLeft==0) & mHasWarmthRequest){
            onAllTasksCompleted();
        }
    }

    public void onAllTasksCompleted() {
        // TODO: launch outfit logic
        if (mIsLoadingDone) {
            // nothing to do
            return;
        }

        Outfit best_outfit = getBestTopBottom();

        mIsLoadingDone = true;
        mParent.addOutfit(best_outfit);
    }

    private Outfit getBestTopBottom() {
        Outfit outfit = new Outfit();
        if (m_topCursor.moveToFirst()) {
            String imagePath = m_topCursor.getString(m_topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
            double warmth = m_topCursor.getDouble(m_topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

            outfit.addItem(new ClothingItem(Outfit.TOP, imagePath, warmth));
        }

        if (m_bottomCursor.moveToFirst()) {
            String imagePath = m_bottomCursor.getString(m_bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
            double warmth = m_bottomCursor.getDouble(m_bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

            outfit.addItem(new ClothingItem(Outfit.BOTTOM, imagePath, warmth));
        }
        return outfit;
    }

    // TODO: sort queries by warmth
    private LoaderManager.LoaderCallbacks<Cursor> topLoaderListener
            = new LoaderManager.LoaderCallbacks<Cursor>() {

        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
            // Define a projection that specifies which table columns we care about.
            String[] projection = {
                    ClothesEntry._ID,
                    ClothesEntry.COLUMN_ARTICLE_CATEGORY,
                    ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY,
                    ClothesEntry.COLUMN_ARTICLE_IMAGE,
                    ClothesEntry.COLUMN_ARTICLE_WARMTH};

            String selection;
            String[] selectionArgs;
            switch (mTag) {
                case FORMAL_TAG:
                    selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_BLOUSE)};
                    break;
                case CASUAL_TAG:
                default:
                    selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR "
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_TSHIRT),
                            String.valueOf(ClothesEntry.SUBCATEGORY_LONGSLEEVE)};
            }

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(m_context,    // Parent activity's context
                    ClothesEntry.CONTENT_URI,       // Provider content URI to query
                    projection,                     // Columns to include in the resulting cursor
                    selection,                   // selection clause
                    selectionArgs,                // selection arguments
                    ClothesEntry.COLUMN_ARTICLE_WARMTH);   // sort order
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
            // store cursor
            m_topCursor = cursor;
            Log.v(LOG_TAG, "onLoadFinished");
            taskComplete();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            // Callback called when data needs to be deleted
            Log.v(LOG_TAG, "onLoaderReset");
        }
    };

    // TODO: sort queries by warmth
    private LoaderManager.LoaderCallbacks<Cursor> bottomLoaderListener
            = new LoaderManager.LoaderCallbacks<Cursor>() {

        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
            // Define a projection that specifies which table columns we care about.
            String[] projection = {
                    ClothesEntry._ID,
                    ClothesEntry.COLUMN_ARTICLE_CATEGORY,
                    ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY,
                    ClothesEntry.COLUMN_ARTICLE_IMAGE,
                    ClothesEntry.COLUMN_ARTICLE_WARMTH};

            String selection;
            String[] selectionArgs;
            switch (mTag) {
                case FORMAL_TAG:
                    selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR "
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_SLACKS),
                            String.valueOf(ClothesEntry.SUBCATEGORY_SKIRT)};
                    break;
                case CASUAL_TAG:
                default:
                    selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR "
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR "
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_CASUAL_PANTS),
                            String.valueOf(ClothesEntry.SUBCATEGORY_SHORTS),
                            String.valueOf(ClothesEntry.SUBCATEGORY_SKIRT)};
            }

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(m_context,    // Parent activity's context
                    ClothesEntry.CONTENT_URI,       // Provider content URI to query
                    projection,                     // Columns to include in the resulting cursor
                    selection,                   // selection clause
                    selectionArgs,                // selection arguments
                    ClothesEntry.COLUMN_ARTICLE_WARMTH);                 // sort order
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
            // store cursor
            m_bottomCursor = cursor;
            Log.v(LOG_TAG, "onLoadFinished");
            taskComplete();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            // Callback called when data needs to be deleted
            Log.v(LOG_TAG, "onLoaderReset");
        }
    };

    // TODO: add other loaders

}
