package com.example.android.whattowear;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.app.LoaderManager;
import com.example.android.whattowear.Outfit.ClothingItem;
import com.example.android.whattowear.data.ClothesContract.ClothesEntry;

import java.io.ByteArrayInputStream;

public class OutfitLogic {
    /** Tag for log messages */
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

    /** Loaders for the different clothing categories */
    private ClothesLoader mTopLoaderListener;
    private ClothesLoader mBottomLoaderListener;
    private ClothesLoader mDressLoaderListener;
    private ClothesLoader mOuterLoaderListener;

    public OutfitLogic(Context context, OutfitFragment parent, LoaderManager loaderManager, int tag) {
        m_context = context;
        mParent = parent;
        mTag = tag;
        mTasksLeft = 4;
        mHasWarmthRequest = false;
        mIsLoadingDone = false;

        // construct category loaders
        switch (mTag) {
            case FORMAL_TAG:
                mTopLoaderListener = new ClothesLoader(m_context, this,
                        new String[]{String.valueOf(ClothesEntry.SUBCATEGORY_FORMAL_SHIRT)});
                mBottomLoaderListener = new ClothesLoader(m_context, this,
                        new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_SLACKS),
                        String.valueOf(ClothesEntry.SUBCATEGORY_SKIRT)});
                mDressLoaderListener = new ClothesLoader(m_context, this,
                        new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_FORMAL_DRESS)});
                mOuterLoaderListener = new ClothesLoader(m_context, this,
                        new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_SWEATER),
                        String.valueOf(ClothesEntry.SUBCATEGORY_CARDIGAN),
                        String.valueOf(ClothesEntry.SUBCATEGORY_COAT)});
                break;
            case CASUAL_TAG:
            default:
                mTopLoaderListener = new ClothesLoader(m_context, this,
                        new String[]{String.valueOf(ClothesEntry.SUBCATEGORY_TSHIRT),
                        String.valueOf(ClothesEntry.SUBCATEGORY_LONGSLEEVE)});
                mBottomLoaderListener = new ClothesLoader(m_context, this,
                        new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_CASUAL_PANTS),
                        String.valueOf(ClothesEntry.SUBCATEGORY_SHORTS),
                        String.valueOf(ClothesEntry.SUBCATEGORY_SKIRT)});
                mDressLoaderListener = new ClothesLoader(m_context, this,
                        new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_CASUAL_DRESS)});
                mOuterLoaderListener = new ClothesLoader(m_context, this,
                        new String[] {String.valueOf(ClothesEntry.SUBCATEGORY_HOODIE),
                        String.valueOf(ClothesEntry.SUBCATEGORY_SWEATER),
                        String.valueOf(ClothesEntry.SUBCATEGORY_JACKET)});
        }

        // run the category querys
        loaderManager.initLoader(tag + TOP_LOADER_ID, null, mTopLoaderListener);
        loaderManager.initLoader(tag + BOTTOM_LOADER_ID, null, mBottomLoaderListener);
        loaderManager.initLoader(tag + OUTER_LOADER_ID, null, mOuterLoaderListener);
        loaderManager.initLoader(tag + DRESS_LOADER_ID, null, mDressLoaderListener);
    }

    /** This sets the requested warmth, if this is the last async task to completed, launch
     * the outfit logic */
    public void FetchOutfit(double warmth_request) {
        mWarmthRequest = warmth_request;
        mHasWarmthRequest = true;
        if (mTasksLeft==0){
            onAllTasksCompleted();
        }
    }

    /** Callback to keep track of how many category loaders have finished loading */
    public void taskComplete() {
        mTasksLeft--;
        if ((mTasksLeft==0) & mHasWarmthRequest){
            onAllTasksCompleted();
        }
    }

    /** Launch the outfit logic once all category loaders have finished */
    public void onAllTasksCompleted() {
        if (mIsLoadingDone) {
            // nothing to do
            return;
        }

        Outfit temp;
        // get best top, bottom combo
        Outfit best_outfit = getBestTopBottom(mWarmthRequest);
        double diff = Math.abs(mWarmthRequest - best_outfit.getWarmth());

        // get best dress
        temp = getBestDress(mWarmthRequest);
        if (best_outfit.isEmpty() || (!temp.isEmpty() && (Math.abs(mWarmthRequest - temp.getWarmth()) < diff))) {
            best_outfit = temp;
            diff = Math.abs(mWarmthRequest - temp.getWarmth());
        }

        // get best jacket outfit
        temp= getBestWithJacket(mWarmthRequest);
        if (best_outfit.isEmpty() || (!temp.isEmpty() && (Math.abs(mWarmthRequest - temp.getWarmth()) < diff))) {
            best_outfit = temp;
            diff = Math.abs(mWarmthRequest - temp.getWarmth());
        }

        mIsLoadingDone = true;
        if (!best_outfit.isEmpty() && best_outfit.isValid()) {
            mParent.addOutfit(best_outfit);
        }
    }

    // TODO: binary search
    private Outfit getBestDress(double desired_sum) {
        Cursor dressCursor = mDressLoaderListener.getCursor();
        Outfit outfit = new Outfit();
        double diff = Double.MAX_VALUE;
        int result_position = 0;
        double dress_warmth;
        if (dressCursor.moveToFirst()) {
            while (!dressCursor.isAfterLast()) {
                dress_warmth = dressCursor.getDouble(dressCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                if (Math.abs(dress_warmth - desired_sum) < diff) {
                    diff = Math.abs(dress_warmth - desired_sum);
                    result_position = dressCursor.getPosition();
                }
                dressCursor.moveToNext();
            }

            if (dressCursor.moveToPosition(result_position)) {
                byte[] imageByteArray = dressCursor.getBlob(dressCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = dressCursor.getDouble(dressCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                if (imageByteArray != null && imageByteArray.length > 0)
                {
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
                    outfit.addItem(new ClothingItem(Outfit.DRESS, BitmapFactory.decodeStream(imageStream), warmth));
                } else {
                    outfit.addItem(new ClothingItem(Outfit.DRESS, null, warmth));
                }
            }
        }
        return outfit;
    }
    private Outfit getBestTopBottom(double desired_sum) {
        Cursor topCursor = mTopLoaderListener.getCursor();
        Cursor bottomCursor = mBottomLoaderListener.getCursor();
        Outfit outfit = new Outfit();
        double diff = Double.MAX_VALUE;
        int result_top_position = 0;
        int result_bottom_position = 0;
        double top_warmth;
        double bottom_warmth;
        if (topCursor.moveToFirst() && bottomCursor.moveToLast()) {
            while (!topCursor.isAfterLast() && !bottomCursor.isBeforeFirst()) {
                // if this combo is closer to desired, update result
                top_warmth = topCursor.getDouble(topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                bottom_warmth = bottomCursor.getDouble(bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                if (Math.abs(top_warmth + bottom_warmth - desired_sum) < diff) {
                    diff = Math.abs(top_warmth + bottom_warmth - desired_sum);
                    result_top_position = topCursor.getPosition();
                    result_bottom_position = bottomCursor.getPosition();
                }

                // shift next check based on whether this combo is greater than or less than desired
                if (top_warmth + bottom_warmth < desired_sum) {
                    topCursor.moveToNext();
                } else {
                    bottomCursor.moveToPrevious();
                }
            }

            if (topCursor.moveToPosition(result_top_position)) {
                byte[] imageByteArray = topCursor.getBlob(topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = topCursor.getDouble(topCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                if (imageByteArray != null && imageByteArray.length > 0)
                {
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
                    outfit.addItem(new ClothingItem(Outfit.TOP, BitmapFactory.decodeStream(imageStream), warmth));
                } else {
                    outfit.addItem(new ClothingItem(Outfit.TOP, null, warmth));
                }

            }

            if (bottomCursor.moveToPosition(result_bottom_position)) {
                byte[] imageByteArray = bottomCursor.getBlob(bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = bottomCursor.getDouble(bottomCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                if (imageByteArray != null && imageByteArray.length > 0)
                {
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
                    outfit.addItem(new ClothingItem(Outfit.BOTTOM, BitmapFactory.decodeStream(imageStream), warmth));
                } else {
                    outfit.addItem(new ClothingItem(Outfit.BOTTOM, null, warmth));
                }
            }
        }
        return outfit;
    }

    // O(n^2) need to optimize
    private Outfit getBestWithJacket(double desired_sum) {
        Cursor jacketCursor = mOuterLoaderListener.getCursor();
        Outfit best_outfit = new Outfit();
        Outfit temp_top_bottom;
        Outfit temp_dress;
        double diff = Double.MAX_VALUE;
        double jacket_warmth = 0;
        int result_jacket_position = 0;
        if (jacketCursor.moveToFirst()) {
            while (!jacketCursor.isAfterLast()) {
                jacket_warmth = jacketCursor.getDouble(jacketCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));
                temp_top_bottom = getBestTopBottom(desired_sum - jacket_warmth);
                temp_dress = getBestDress(desired_sum - jacket_warmth);

                if (Math.abs(temp_top_bottom.getWarmth() + jacket_warmth - desired_sum) < diff) {
                    diff = Math.abs(temp_top_bottom.getWarmth() + jacket_warmth - desired_sum);
                    result_jacket_position = jacketCursor.getPosition();
                    best_outfit = temp_top_bottom;
                }
                if (Math.abs(temp_dress.getWarmth() + jacket_warmth - desired_sum) < diff) {
                    diff = Math.abs(temp_dress.getWarmth() + jacket_warmth - desired_sum);
                    result_jacket_position = jacketCursor.getPosition();
                    best_outfit = temp_dress;
                }

                jacketCursor.moveToNext();
            }

            if (!best_outfit.isEmpty() && jacketCursor.moveToPosition(result_jacket_position)) {
                byte[] imageByteArray = jacketCursor.getBlob(jacketCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));
                double warmth = jacketCursor.getDouble(jacketCursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WARMTH));

                if (imageByteArray != null && imageByteArray.length > 0)
                {
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
                    best_outfit.addItem(new ClothingItem(Outfit.OUTER1, BitmapFactory.decodeStream(imageStream), warmth));
                } else {
                    best_outfit.addItem(new ClothingItem(Outfit.OUTER1, null, warmth));
                }
            }
        }
        return best_outfit;
    }

}
