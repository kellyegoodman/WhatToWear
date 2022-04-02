package com.example.android.whattowear;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class OutfitAdapter extends ArrayAdapter<Outfit> {

    private Activity mContext;

    private double mDesiredCloValue;

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

        // Get the {@link Outfit} object located at this position in the list
        Outfit currentOutfit = getItem(position);
        // TODO: have empty view in outfit_display, handle empty outfit here
        if (!currentOutfit.isValid()) {
            return listItemView;
        }
        List<Outfit.ClothingItem> outfit_list = getItem(position).getClothes();

        // get views
        ImageView topView = (ImageView) listItemView.findViewById(R.id.top_view);
        ImageView bottomView  = (ImageView) listItemView.findViewById(R.id.bottom_view);
        ImageView dressView = (ImageView) listItemView.findViewById(R.id.dress_view);
        ImageView jacketView  = (ImageView) listItemView.findViewById(R.id.jacket_view);
        LinearLayout topBottomWrapper = (LinearLayout) listItemView.findViewById(R.id.top_bottom_wrapper);
        LinearLayout dressWrapper = (LinearLayout) listItemView.findViewById(R.id.dress_wrapper);
        LinearLayout jacketWrapper = (LinearLayout) listItemView.findViewById(R.id.jacket_wrapper);
        LinearLayout.LayoutParams top_bottom_view_params = (LinearLayout.LayoutParams) topBottomWrapper.getLayoutParams();
        LinearLayout.LayoutParams dress_view_params = (LinearLayout.LayoutParams) dressWrapper.getLayoutParams();
        LinearLayout.LayoutParams jacket_view_params = (LinearLayout.LayoutParams) jacketWrapper.getLayoutParams();

        for (Outfit.ClothingItem item : outfit_list) {
            if (item.getImage() != null) {
                switch (item.getType()) {
                    case Outfit.TOP:
                        topView.setImageBitmap(item.getImage());
                        dress_view_params.weight = 0.0f;
                        dressWrapper.setLayoutParams(dress_view_params);
                        top_bottom_view_params.weight = 0.8f;
                        topBottomWrapper.setLayoutParams(top_bottom_view_params);
                        break;
                    case Outfit.DRESS:
                        dressView.setImageBitmap(item.getImage());
                        dress_view_params.weight = 0.8f;
                        dressWrapper.setLayoutParams(dress_view_params);
                        top_bottom_view_params.weight = 0.0f;
                        topBottomWrapper.setLayoutParams(top_bottom_view_params);
                        break;
                    case Outfit.OUTER1:
                        jacketView.setImageBitmap(item.getImage());
                        jacket_view_params.weight = 1.0f;
                        jacketWrapper.setLayoutParams(jacket_view_params);
                        break;
                    case Outfit.BOTTOM:
                    default:
                        bottomView.setImageBitmap(item.getImage());
                        break;
                }
            }
        }

        TextView summary_view = (TextView) listItemView.findViewById(R.id.outfit_header);
        summary_view.setText(getContext().getString(R.string.outfit_default_text,
                position+1));//,currentOutfit.getWarmth(),mDesriedWarmth));

        // Return the whole list item layout (containing 3 TextViews)
        // so that it can be shown in the ListView
        return listItemView;
    }

    // TODO: remove, for debugging
    public void setDesiredCloValue(double desiredCloValue) {
        mDesiredCloValue = desiredCloValue;
    }
}
