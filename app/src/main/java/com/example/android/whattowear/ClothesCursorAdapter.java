package com.example.android.whattowear;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.whattowear.data.ClothesContract;
import com.example.android.whattowear.data.ClothesContract.ClothesEntry;
import com.example.android.whattowear.R;

import org.w3c.dom.Text;

/**
 * {@link ClothesCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of clothing data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ClothesCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ClothesCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ClothesCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_clothing, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // get the text views to populate
        TextView subcategoryTextView = (TextView) view.findViewById(R.id.subcategory);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        ImageView imageView = (ImageView) view.findViewById(R.id.item_image);

        // get the data from the cursor
        Integer itemSubCategory = cursor.getInt(cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_SUBCATEGORY));
        String itemName = cursor.getString(cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_NAME));
        String picturePath = cursor.getString(cursor.getColumnIndex(ClothesEntry.COLUMN_ARTICLE_IMAGE));

        // If the item name is empty string or null, then use some default text
        // that says " ", so the TextView isn't blank.
        if (TextUtils.isEmpty(itemName)) {
            itemName = context.getString(R.string.no_item_name);
        }

        // Update the TextViews with the attributes for the current pet
        subcategoryTextView.setText(ClothesEntry.getSubCategoryName(itemSubCategory));
        nameTextView.setText(itemName);

        if (!TextUtils.isEmpty(picturePath)) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}