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
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
    private int mTag;

    private int mTasksLeft;
    private boolean mIsLoadingDone;
    private boolean mHasWarmthRequest;

    private Cursor m_topCursor;
    private Cursor m_bottomCursor;

    public OutfitLogic(Context context, LoaderManager loaderManager, int tag) {
        m_context = context;
        mTag = tag;
        mTasksLeft = 2;
        mIsLoadingDone = false;
        mHasWarmthRequest = false;
        loaderManager.initLoader(tag + TOP_LOADER_ID, null, topLoaderListener);
        loaderManager.initLoader(tag + BOTTOM_LOADER_ID, null, bottomLoaderListener);
    }

    public void FetchOutfit(double warmth_request) {
        mHasWarmthRequest = true;
        if (mTasksLeft==0){
            onAllTasksCompleted();
        }
    }

    public void onAllTasksCompleted() {
        // TODO: launch outfit logic
        // Get references to the necessary item views
        ImageView topView;
        ImageView bottomView;
        switch (mTag) {
            case FORMAL_TAG:
                topView = (ImageView) ((Activity)m_context).findViewById(R.id.formal_top_view);
                bottomView = (ImageView) ((Activity)m_context).findViewById(R.id.formal_bottom_view);
                break;
            case CASUAL_TAG:
            default:
                topView = (ImageView) ((Activity)m_context).findViewById(R.id.casual_top_view);
                bottomView = (ImageView) ((Activity)m_context).findViewById(R.id.casual_bottom_view);
        }
        View emptyView = ((Activity)m_context).findViewById(R.id.empty_outfit_view);

        // get the imagess
        String picturePathTop = null;
        if (m_topCursor.moveToFirst()) {
            picturePathTop = m_topCursor.getString(m_topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
        }
        String picturePathBottom = null;
        if (m_bottomCursor.moveToFirst()) {
            picturePathBottom = m_bottomCursor.getString(m_bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
        }

        displayItem(topView, picturePathTop);
        displayItem(bottomView, picturePathBottom);
        emptyView.setVisibility(View.GONE);
    }

    public void taskComplete() {
        mTasksLeft--;
        if (mTasksLeft==0){
            mIsLoadingDone = true;
            if (mHasWarmthRequest) {
                onAllTasksCompleted();
            }
        }
    }

    private void displayItem(ImageView view, String picturePath) {
        if (!TextUtils.isEmpty(picturePath)) {
            view.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            view.setVisibility(View.VISIBLE);
        }
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
                    selection,                   // No selection clause
                    selectionArgs,                // No selection arguments
                    null);                 // Default sort order
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
                    selection,                   // No selection clause
                    selectionArgs,                // No selection arguments
                    null);                 // Default sort order
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
