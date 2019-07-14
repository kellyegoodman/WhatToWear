package com.example.android.whattowear;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.whattowear.Outfit;
import com.example.android.whattowear.R;
import com.example.android.whattowear.WeatherHour;

import java.util.ArrayList;
import java.util.List;

public class OutfitAdapter extends ArrayAdapter<Outfit> {

    private Activity mContext;

    public OutfitAdapter(Activity context, ArrayList<Outfit> input) {
        super(context, 0, input);
        mContext = context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.outfit_display, parent, false);
        }
        TextView summary_view = (TextView) listItemView.findViewById(R.id.outfit_header);
        summary_view.setText(getContext().getString(R.string.outfit_default_text, position+1));

        // Get the {@link Outfit} object located at this position in the list
        List<Outfit.ClothingItem> currentOutfit = getItem(position).getClothes();

        // get views
        ImageView topView = (ImageView) listItemView.findViewById(R.id.top_view);
        ImageView bottomView  = (ImageView) listItemView.findViewById(R.id.bottom_view);

        for (Outfit.ClothingItem item : currentOutfit) {
            if (!TextUtils.isEmpty(item.getImage())) {
                switch (item.getType()) {
                    case Outfit.TOP:
                        topView.setImageBitmap(BitmapFactory.decodeFile(item.getImage()));
                        break;
                    case Outfit.BOTTOM:
                    default:
                        bottomView.setImageBitmap(BitmapFactory.decodeFile(item.getImage()));
                        break;
                }
            }
        }

        // Return the whole list item layout (containing 3 TextViews)
        // so that it can be shown in the ListView
        return listItemView;
    }
}
