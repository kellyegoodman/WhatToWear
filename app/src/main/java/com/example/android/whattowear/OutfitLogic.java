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
    private static final int OUTER_LOADER_ID = 3;

    private Context m_context;
    private OutfitFragment mParent;
    private int mTag;

    private int mTasksLeft;
    private boolean mIsLoadingDone;
    private boolean mHasWarmthRequest;
    private double mWarmthRequest;

    private Cursor m_topCursor;
    private Cursor m_bottomCursor;
    private Cursor m_jacketCursor;
    private Cursor m_dressCursor;

    public OutfitLogic(Context context, OutfitFragment parent, LoaderManager loaderManager, int tag) {
        m_context = context;
        mParent = parent;
        mTag = tag;
        mTasksLeft = 4;
        mHasWarmthRequest = false;
        mIsLoadingDone = false;
        loaderManager.initLoader(tag + TOP_LOADER_ID, null, topLoaderListener);
        loaderManager.initLoader(tag + BOTTOM_LOADER_ID, null, bottomLoaderListener);
        loaderManager.initLoader(tag + OUTER_LOADER_ID, null, jacketLoaderListener);
        loaderManager.initLoader(tag + DRESS_LOADER_ID, null, dressLoaderListener);
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

        // save best top, bottom combo
        Outfit best_outfit = getBestTopBottom(mWarmthRequest);
        double diff = Math.abs(mWarmthRequest - best_outfit.getWarmth());
        // save best dress
        Outfit best_dress = getBestDress(mWarmthRequest);
        double dress_diff = Math.abs(mWarmthRequest - best_dress.getWarmth());

        Outfit best_with_jacket = getBestWithJacket(mWarmthRequest);
        double jacket_diff = Math.abs(mWarmthRequest - best_with_jacket.getWarmth());

        // if the jacket combo is better, show the jacket combo
        if ((jacket_diff < diff) && (jacket_diff < dress_diff)) {
            best_outfit = best_with_jacket;
        } else if ((dress_diff < diff) && (dress_diff < jacket_diff)) {
            best_outfit = best_dress;
        }

        mIsLoadingDone = true;
        mParent.addOutfit(best_outfit);
    }

    // TODO: binary search
    private Outfit getBestDress(double desired_sum) {
        Outfit outfit = new Outfit();
        double diff = Double.MAX_VALUE;
        int result_position = 0;
        double dress_warmth;
        if (m_dressCursor.moveToFirst()) {
            while (!m_dressCursor.isAfterLast()) {
                dress_warmth = m_dressCursor.getDouble(m_dressCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                if (Math.abs(dress_warmth - desired_sum) < diff) {
                    diff = Math.abs(dress_warmth - desired_sum);
                    result_position = m_dressCursor.getPosition();
                }
                m_dressCursor.moveToNext();
            }

            if (m_dressCursor.moveToPosition(result_position)) {
                String imagePath = m_dressCursor.getString(m_dressCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = m_dressCursor.getDouble(m_dressCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                outfit.addItem(new ClothingItem(Outfit.DRESS, imagePath, warmth));
            }
        }
        return outfit;
    }
    private Outfit getBestTopBottom(double desired_sum) {
        Outfit outfit = new Outfit();
        double diff = Double.MAX_VALUE;
        int result_top_position = 0;
        int result_bottom_position = 0;
        double top_warmth;
        double bottom_warmth;
        if (m_topCursor.moveToFirst() && m_bottomCursor.moveToLast()) {
            while (!m_topCursor.isAfterLast() && !m_bottomCursor.isBeforeFirst()) {
                // if this combo is closer to desired, update result
                top_warmth = m_topCursor.getDouble(m_topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                bottom_warmth = m_bottomCursor.getDouble(m_bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                if (Math.abs(top_warmth + bottom_warmth - desired_sum) < diff) {
                    diff = Math.abs(top_warmth + bottom_warmth - desired_sum);
                    result_top_position = m_topCursor.getPosition();
                    result_bottom_position = m_bottomCursor.getPosition();
                }

                // shift next check based on whether this combo is greater than or less than desired
                if (top_warmth + bottom_warmth < desired_sum) {
                    m_topCursor.moveToNext();
                } else {
                    m_bottomCursor.moveToPrevious();
                }
            }

            if (m_topCursor.moveToPosition(result_top_position)) {
                String imagePath = m_topCursor.getString(m_topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = m_topCursor.getDouble(m_topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                outfit.addItem(new ClothingItem(Outfit.TOP, imagePath, warmth));
            }

            if (m_bottomCursor.moveToPosition(result_bottom_position)) {
                String imagePath = m_bottomCursor.getString(m_bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = m_bottomCursor.getDouble(m_bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                outfit.addItem(new ClothingItem(Outfit.BOTTOM, imagePath, warmth));
            }
        }
        return outfit;
    }

    // O(n^2) need to optimize
    private Outfit getBestWithJacket(double desired_sum) {
        Outfit best_outfit = new Outfit();
        Outfit temp_top_bottom;
        Outfit temp_dress;
        double diff = Double.MAX_VALUE;
        double jacket_warmth = 0;
        int result_jacket_position = 0;
        if (m_jacketCursor.moveToFirst()) {
            while (!m_jacketCursor.isAfterLast()) {
                jacket_warmth = m_jacketCursor.getDouble(m_jacketCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                temp_top_bottom = getBestTopBottom(desired_sum - jacket_warmth);
                temp_dress = getBestDress(desired_sum - jacket_warmth);

                if (Math.abs(temp_top_bottom.getWarmth() + jacket_warmth - desired_sum) < diff) {
                    diff = Math.abs(temp_top_bottom.getWarmth() + jacket_warmth - desired_sum);
                    result_jacket_position = m_jacketCursor.getPosition();
                    best_outfit = temp_top_bottom;
                }
                if (Math.abs(temp_dress.getWarmth() + jacket_warmth - desired_sum) < diff) {
                    diff = Math.abs(temp_dress.getWarmth() + jacket_warmth - desired_sum);
                    result_jacket_position = m_jacketCursor.getPosition();
                    best_outfit = temp_dress;
                }

                m_jacketCursor.moveToNext();
            }

            if (!best_outfit.isEmpty() && m_jacketCursor.moveToPosition(result_jacket_position)) {
                String imagePath = m_jacketCursor.getString(m_jacketCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = m_jacketCursor.getDouble(m_jacketCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                best_outfit.addItem(new ClothingItem(Outfit.OUTER1, imagePath, warmth));
            }
        }
        return best_outfit;
    }

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

    private LoaderManager.LoaderCallbacks<Cursor> jacketLoaderListener
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
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR "
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_SWEATER),
                            String.valueOf(ClothesEntry.SUBCATEGORY_CARDIGAN),
                            String.valueOf(ClothesEntry.SUBCATEGORY_COAT)};
                    break;
                case CASUAL_TAG:
                default:
                    selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR "
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=? OR "
                            + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_HOODIE),
                            String.valueOf(ClothesEntry.SUBCATEGORY_SWEATER),
                            String.valueOf(ClothesEntry.SUBCATEGORY_JACKET)};
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
            m_jacketCursor = cursor;
            Log.v(LOG_TAG, "onLoadFinished");
            taskComplete();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            // Callback called when data needs to be deleted
            Log.v(LOG_TAG, "onLoaderReset");
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> dressLoaderListener
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
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_FORMAL_DRESS)};
                    break;
                case CASUAL_TAG:
                default:
                    selection = ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + "=?";
                    selectionArgs = new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_CASUAL_DRESS)};
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
            m_dressCursor = cursor;
            Log.v(LOG_TAG, "onLoadFinished");
            taskComplete();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            // Callback called when data needs to be deleted
            Log.v(LOG_TAG, "onLoaderReset");
        }
    };
}
