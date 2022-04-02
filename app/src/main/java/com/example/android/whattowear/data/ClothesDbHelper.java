package com.example.android.whattowear.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.whattowear.data.ClothesContract.ClothesEntry;

public class ClothesDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ClothesDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "wardrobe.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 2;

    /**
     * Constructs a new instance of {@link ClothesDbHelper}.
     *
     * @param context of the app
     */
    public ClothesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the clothes table
        String SQL_CREATE_CLOTHES_TABLE =  "CREATE TABLE " + ClothesEntry.TABLE_NAME + " ("
                + ClothesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ClothesEntry.COLUMN_ARTICLE_CATEGORY + " INTEGER NOT NULL, "
                + ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY + " INTEGER NOT NULL, "
                + ClothesEntry.COLUMN_ARTICLE_NAME + " TEXT, "
                + ClothesEntry.COLUMN_ARTICLE_IMAGE + " BLOB, "
                + ClothesEntry.COLUMN_ARTICLE_WEIGHT + " INTEGER NOT NULL DEFAULT 0, "
                + ClothesEntry.COLUMN_ARTICLE_COTTON + " INTEGER NOT NULL DEFAULT 0, "
                + ClothesEntry.COLUMN_ARTICLE_POLYESTER + " INTEGER NOT NULL DEFAULT 0, "
                + ClothesEntry.COLUMN_ARTICLE_RAYON + " INTEGER NOT NULL DEFAULT 0, "
                + ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX + " INTEGER NOT NULL DEFAULT 0, "
                + ClothesEntry.COLUMN_ARTICLE_WOOL + " INTEGER NOT NULL DEFAULT 0, "
                + ClothesEntry.COLUMN_ARTICLE_CLO_VALUE + " REAL NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_CLOTHES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
        Log.e(LOG_TAG, "Updating table from " + oldVersion + " to " + newVersion);

    }
}
