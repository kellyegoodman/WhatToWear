package com.example.android.whattowear;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Context;

import com.github.gabrielbb.cutout.CutOut;

import com.example.android.whattowear.data.ClothesContract.ClothesEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Allows user to add a new clothing item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    /** Identifier for the item data loader */
    private static final int EXISTING_CLOTHING_LOADER = 0;
    /** Content URI for the existing clothing item (null if it's a new item) */
    private Uri mCurrentItemUri;
    /** EditText field to enter the items's subcategory */
    private Spinner mSubCategorySpinner;
    /** EditText field to enter the items's name */
    private EditText mNameEditText;
    /** EditText field to enter the item's weight */
    private EditText mWeightEditText;
    /** EditText field to enter the item's cotton percentage */
    private EditText mCottonEditText;
    /** EditText field to enter the item's polyester percentage */
    private EditText mPolyesterEditText;
    /** EditText field to enter the item's rayon percentage */
    private EditText mRayonEditText;
    /** EditText field to enter the item's nylon/spandex percentage */
    private EditText mNylonSpandexEditText;
    /** EditText field to enter the item's wool percentage */
    private EditText mWoolEditText;

    private ImageButton mImageButton;

    private Bitmap m_currentBitmap;
    private Uri mimageUri;

    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private static final int REQUEST_LOAD_IMAGE = 1;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    /**
     * Category of the itme. The possible valid values are in the ClothesContract.java file:
     */
    private int mCategory = ClothesEntry.CATEGORY_TOP;
    /**
     * Subcategory of the itme. The possible valid values are in the ClothesContract.java file:
     */
    private int mSubcategory = ClothesEntry.SUBCATEGORY_TSHIRT;

    /** Boolean flag that keeps track of whether the item has been edited (true) or not (false) */
    private boolean mItemHasChanged = false;

    public static final String LOG_TAG = EditorActivity.class.getName();

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Image of Clothing Item");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {

                    // check if camera permissions exist, request if needed, take photo if granted
                    if (ContextCompat.checkSelfPermission(EditorActivity.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(EditorActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        // permissions already granted, take photo
                        try {
                            // set up temporary uri
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "New Picture");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                            mimageUri = getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, mimageUri);
                            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Problem launching camera", e);
                        }
                    }

                } else if (options[item].equals("Choose from Gallery")) {

                    // check if external storage permissions exist, request if needed, open image gallery if granted
                    if (ContextCompat.checkSelfPermission(EditorActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(EditorActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    } else {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto , REQUEST_LOAD_IMAGE);
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new clothing item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        // If the intent DOES NOT contain a clothing item content URI, then we know that we are
        // creating a new clothing item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a clothing item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.editor_activity_title_edit_item));
            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_CLOTHING_LOADER, null, this);
        }
        // Find all relevant views that we will need to read user input from
        mSubCategorySpinner = (Spinner) findViewById(R.id.spinner_subcategory);
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mWeightEditText = (EditText) findViewById(R.id.edit_item_weight);
        mCottonEditText = (EditText) findViewById(R.id.edit_item_cotton);
        mPolyesterEditText = (EditText) findViewById(R.id.edit_item_polyester);
        mRayonEditText = (EditText) findViewById(R.id.edit_item_rayon);
        mNylonSpandexEditText = (EditText) findViewById(R.id.edit_item_nylon_spandex);
        mWoolEditText = (EditText) findViewById(R.id.edit_item_wool);
        mImageButton = (ImageButton) findViewById(R.id.edit_item_image);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mSubCategorySpinner.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mCottonEditText.setOnTouchListener(mTouchListener);
        mPolyesterEditText.setOnTouchListener(mTouchListener);
        mRayonEditText.setOnTouchListener(mTouchListener);
        mNylonSpandexEditText.setOnTouchListener(mTouchListener);
        mWoolEditText.setOnTouchListener(mTouchListener);
        mImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                selectImage(EditorActivity.this);
            }
        });

        setupSpinner();
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        if (bitmap == null)
        {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    // data is null when uri is passed to camera intent
                    // ignore data and use the saved image uri
                    if (resultCode == RESULT_OK) {  // && data != null

                        // modify picture
                        CutOut.activity()
                                .src(mimageUri)
                                .bordered()
                                .start(this);
                    }
                    break;
                case REQUEST_LOAD_IMAGE:
                    if (resultCode == RESULT_OK && data != null) {

                        Uri selectedImage = data.getData();

                        // modify picture
                        CutOut.activity()
                                .src(selectedImage)
                                .bordered()
                                .start(this);
                    }
                    break;
                case CutOut.CUTOUT_ACTIVITY_REQUEST_CODE:
                    if (resultCode == RESULT_OK && data != null) {
                        try
                        {
                            // try to save the stripped image
                            Uri imageUri = CutOut.getUri(data);
                            m_currentBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            mImageButton.setImageBitmap(m_currentBitmap);
                        }
                        catch (Exception ex)
                        {
                            Toast.makeText(this, "problem getting editted photo!", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                } else {
                    // permission denied, boo!
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Toast.makeText(this, "image gallery permission granted", Toast.LENGTH_LONG).show();
                } else {
                    // permission denied, boo!
                    Toast.makeText(this, "image gallery permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the subcategory of the item.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_subcategory_options, android.R.layout.simple_spinner_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mSubCategorySpinner.setAdapter(typeSpinnerAdapter);
        // Set the integer mSelected to the constant values
        mSubCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.subcategory_longsleeve))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_LONGSLEEVE;
                    } else if (selection.equals(getString(R.string.subcategory_formal_shirt))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_FORMAL_SHIRT;
                    } else if (selection.equals(getString(R.string.subcategory_casual_pants))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_CASUAL_PANTS;
                    } else if (selection.equals(getString(R.string.subcategory_shorts))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_SHORTS;
                    } else if (selection.equals(getString(R.string.subcategory_slacks))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_SLACKS;
                    } else if (selection.equals(getString(R.string.subcategory_skirt))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_SKIRT;
                    } else if (selection.equals(getString(R.string.subcategory_casual_dress))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_CASUAL_DRESS;
                    } else if (selection.equals(getString(R.string.subcategory_formal_dress))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_FORMAL_DRESS;
                    } else if (selection.equals(getString(R.string.subcategory_sweater))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_SWEATER;
                    } else if (selection.equals(getString(R.string.subcategory_hoodie))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_HOODIE;
                    } else if (selection.equals(getString(R.string.subcategory_cardigan))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_CARDIGAN;
                    } else if (selection.equals(getString(R.string.subcategory_jacket))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_JACKET;
                    } else if (selection.equals(getString(R.string.subcategory_coat))) {
                        mSubcategory = ClothesEntry.SUBCATEGORY_COAT;
                    } else {
                        mSubcategory = ClothesEntry.SUBCATEGORY_TSHIRT;
                    }
                }
            }
            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSubcategory = ClothesEntry.SUBCATEGORY_TSHIRT;
            }
        });
    }

    /**
     * Get user input from editor and save clothing item into database.
     */
    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        String cottonString = mCottonEditText.getText().toString().trim();
        String polyesterString = mPolyesterEditText.getText().toString().trim();
        String rayonString = mRayonEditText.getText().toString().trim();
        String nylonSpandexString = mNylonSpandexEditText.getText().toString().trim();
        String woolString = mWoolEditText.getText().toString().trim();

        // Check if this is supposed to be a new clothing item
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(weightString) &&
                TextUtils.isEmpty(cottonString) && TextUtils.isEmpty(polyesterString) &&
                mSubcategory == ClothesEntry.SUBCATEGORY_TSHIRT) {
            // Since no fields were modified, we can return early without creating a new clothing item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and clothing attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY, mSubcategory);
        values.put(ClothesEntry.COLUMN_ARTICLE_CATEGORY, ClothesEntry.getCategory(mSubcategory));
        values.put(ClothesEntry.COLUMN_ARTICLE_NAME, nameString);

        //values.put(ClothesEntry.COLUMN_ARTICLE_IMAGE, picturePath);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        int cotton = 0;
        int polyester  = 0;
        int rayon = 0;
        int nylon_spandex = 0;
        int wool = 0;
        if (!TextUtils.isEmpty(cottonString)) {
            cotton = Integer.parseInt(cottonString);
        }
        if (!TextUtils.isEmpty(polyesterString)) {
            polyester = Integer.parseInt(polyesterString);
        }
        if (!TextUtils.isEmpty(rayonString)) {
            rayon = Integer.parseInt(rayonString);
        }
        if (!TextUtils.isEmpty(nylonSpandexString)) {
            nylon_spandex = Integer.parseInt(nylonSpandexString);
        }
        if (!TextUtils.isEmpty(woolString)) {
            wool = Integer.parseInt(woolString);
        }
        values.put(ClothesEntry.COLUMN_ARTICLE_WEIGHT, weight);
        values.put(ClothesEntry.COLUMN_ARTICLE_COTTON, cotton);
        values.put(ClothesEntry.COLUMN_ARTICLE_POLYESTER, polyester);
        values.put(ClothesEntry.COLUMN_ARTICLE_RAYON, rayon);
        values.put(ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX, nylon_spandex);
        values.put(ClothesEntry.COLUMN_ARTICLE_WOOL, wool);

        // save image as blob
        if (m_currentBitmap != null) {
            values.put(ClothesEntry.COLUMN_ARTICLE_IMAGE, getBitmapAsByteArray(m_currentBitmap));
        }
        // TODO: better error reporting

        // Determine if this is a new or existing clothing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW clothing item, so insert a new item into the provider,
            // returning the content URI for the new clothing item.
            try {
                Uri newUri = getContentResolver().insert(ClothesEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri != null) {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Problem inserting item", e);

                // Some exception was reported, there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(LOG_TAG, mCurrentItemUri.toString());

            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the clothing item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the clothing item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing clothing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new clothing item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the clothes table
        String[] projection = {
                ClothesEntry._ID,
                ClothesEntry.COLUMN_ARTICLE_NAME,
                ClothesEntry.COLUMN_ARTICLE_CATEGORY,
                ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY,
                ClothesEntry.COLUMN_ARTICLE_WEIGHT,
                ClothesEntry.COLUMN_ARTICLE_IMAGE,
                ClothesEntry.COLUMN_ARTICLE_COTTON,
                ClothesEntry.COLUMN_ARTICLE_POLYESTER,
                ClothesEntry.COLUMN_ARTICLE_RAYON,
                ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX,
                ClothesEntry.COLUMN_ARTICLE_WOOL};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current clothing item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int typeColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY);
            int nameColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_NAME);
            int weightColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WEIGHT);
            int cottonColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_COTTON);
            int polyesterColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_POLYESTER);
            int rayonColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_RAYON);
            int nylonSpandexColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX);
            int woolColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_WOOL);
            int imageColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE);
            // Extract out the value from the Cursor for the given column index
            //String name = cursor.getString(nameColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int cotton = cursor.getInt(cottonColumnIndex);
            int polyester = cursor.getInt(polyesterColumnIndex);
            int rayon = cursor.getInt(rayonColumnIndex);
            int nylon_spandex = cursor.getInt(nylonSpandexColumnIndex);
            int wool = cursor.getInt(woolColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);
            byte[] imageByteArray=cursor.getBlob(imageColumnIndex);
            if (imageByteArray != null && imageByteArray.length > 0)
            {
                ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
                m_currentBitmap = BitmapFactory.decodeStream(imageStream);
            }

            // Update the views on the screen with the values from the database
            mCottonEditText.setText(Integer.toString(cotton));
            mPolyesterEditText.setText(Integer.toString(polyester));
            mRayonEditText.setText(Integer.toString(rayon));
            mNylonSpandexEditText.setText(Integer.toString(nylon_spandex));
            mWoolEditText.setText(Integer.toString(wool));
            mWeightEditText.setText(Integer.toString(weight));
            mNameEditText.setText(name);
            mImageButton.setImageBitmap(m_currentBitmap);
            // Subcategory is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options.
            // Then call setSelection() so that option is displayed on screen as the current selection.
            if (type >=0 || type <= ClothesEntry.SUBCATEGORY_COAT) {
                mSubCategorySpinner.setSelection(type);
            } else {
                mSubCategorySpinner.setSelection(0);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mWeightEditText.setText("");
        mCottonEditText.setText("");
        mPolyesterEditText.setText("");
        mRayonEditText.setText("");
        mNylonSpandexEditText.setText("");
        mWoolEditText.setText("");
        mSubCategorySpinner.setSelection(0); // Select "T Shirt" subcategory
        mImageButton.setImageResource(R.drawable.baseline_add_photo_alternate_black_36dp);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}