package com.example.android.whattowear.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.example.android.whattowear.data.ClothesContract.ClothesEntry;

import java.io.File;
import java.io.FileOutputStream;

/**
 * {@link ContentProvider} for WhatToWear app.
 */
public class ClothesProvider extends ContentProvider {

    /** Database helper that will provide us access to the database */
    private ClothesDbHelper mDbHelper;

    /** Tag for the log messages */
    public static final String LOG_TAG = ClothesProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the clothes table */
    private static final int CLOTHES = 100;

    /** URI matcher code for the content URI for a single article in the clothes table */
    private static final int ARTICLE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(ClothesContract.CONTENT_AUTHORITY, ClothesContract.PATH_CLOTHES, CLOTHES);
        sUriMatcher.addURI(ClothesContract.CONTENT_AUTHORITY, ClothesContract.PATH_CLOTHES + "/#", ARTICLE_ID);

    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new ClothesDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case CLOTHES:
                cursor = database.query(ClothesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ARTICLE_ID:
                selection = ClothesEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(ClothesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the cursor,
        // so we know what content URI the cursor was created for.
        // If the data at this URI changes, then we know we have to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // Check that the category/subcategory combination is valid
        Integer category = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_CATEGORY);
        Integer subcategory = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY);
        if (category == null || subcategory == null || !ClothesEntry.isValidSubCategory(category,
                subcategory)) {
            throw new IllegalArgumentException("Item's subcategory must match category");
        }

        // Check that the material combination is valid
        Integer cotton = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_COTTON);
        Integer polyester = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_POLYESTER);
        Integer rayon = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_RAYON);
        Integer spandex = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX);
        Integer wool = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WOOL);
        if (cotton == null || polyester == null || rayon == null || spandex == null ||
                wool == null || !ClothesEntry.isValidMaterial(cotton, polyester,
                rayon, spandex, wool)) {
            throw new IllegalArgumentException("Item's fabric percentages do not add up to 100");
        }

        // If the weight is provided, check that it's greater than or equal to 0 g
        Integer weight = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Item requires valid weight");
        }

        // compute the clo value
        double clo = ClothesEntry.calculateCloValue(values);
        values.put(ClothesEntry.COLUMN_ARTICLE_CLO_VALUE, clo);

        // No need to check the name or image file name, any value is valid (including null).
        // TODO: check that image path is valid?

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new clothing item with the given values
        long id = database.insert(ClothesEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {
            // TODO: savepic
        }

        // Notify all listeners that the data has changed for the clothing item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }
    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ARTICLE_ID:
                // For the ARTICLE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ClothesEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more clothing items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Check that the category/subcategory combination is valid
        Integer category = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_CATEGORY);
        Integer subcategory = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY);
        if (category == null || subcategory == null || !ClothesEntry.isValidSubCategory(category,
                subcategory)) {
            throw new IllegalArgumentException("Item's subcategory must match category");
        }

        // Check that the material combination is valid
        Integer cotton = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_COTTON);
        Integer polyester = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_POLYESTER);
        Integer rayon = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_RAYON);
        Integer spandex = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX);
        Integer wool = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WOOL);
        if (cotton == null || polyester == null || rayon == null || spandex == null ||
                wool == null || !ClothesEntry.isValidMaterial(cotton, polyester,
                rayon, spandex, wool)) {
            throw new IllegalArgumentException("Item's subcategory must match category");
        }

        // If the {@link ClothesEntry#COLUMN_ARTICLE_WEIGHT} key is present,
        // check that the weight value is not negative.
        if (values.containsKey(ClothesEntry.COLUMN_ARTICLE_WEIGHT)) {
            Integer weight = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WEIGHT);
            if (weight < 0) {
                throw new IllegalArgumentException("Item requires valid weight");
            }
        }

        // compute the clo value
        double clo = ClothesEntry.calculateCloValue(values);
        values.put(ClothesEntry.COLUMN_ARTICLE_CLO_VALUE, clo);

        // No need to check the image file name, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Notify all listeners that the data has changed for the clothing item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ClothesEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ClothesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ARTICLE_ID:
                // Delete a single row given by the ID in the URI
                selection = ClothesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ClothesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                return ClothesEntry.CONTENT_LIST_TYPE;
            case ARTICLE_ID:
                return ClothesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Saves a picture to internal memory and returns new path to location in internal mamory
     *
     * @param itemId the ID of the clothing item associated with the picture
     * @param picture bitmap of the image
     */
    public String savePicture(long itemId, Bitmap picture) {
        // Saves the new picture to the internal storage with the unique identifier of the item as
        // the name. That way, there will never be two item pictures with the same name.
        File internalStorage = getContext().getDir("ClothingPictures", Context.MODE_PRIVATE);
        File itemPicFilePath = new File(internalStorage, itemId + ".png");
        String picturePath = itemPicFilePath.toString();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(itemPicFilePath);
            picture.compress(Bitmap.CompressFormat.PNG, 100 , fos);
            fos.close();
        }
        catch (Exception ex) {
            Log.i("DATABASE", "Problem saving picture", ex);
            picturePath = "";
        }

        return picturePath;
    }

}