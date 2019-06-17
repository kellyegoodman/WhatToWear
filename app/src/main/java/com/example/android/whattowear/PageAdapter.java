package com.example.android.whattowear;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * {@link PageAdapter} is a {@link FragmentPagerAdapter} that can provide the layout for
 * each fragment
 */
public class PageAdapter extends FragmentPagerAdapter {

    /** Context of the app */
    private Context mContext;

    /**
     * Create a new {@link PageAdapter} object.
     *
     * @param fm is the fragment manager that will keep each fragment's state in the adapter
     *           across swipes.
     */
    public PageAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new OutfitFragment();
        } else if (position == 1) {
            return new WeatherFragment();
        } else {
            return new CatalogFragment();
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.page_outfit_recommendations);
        } else if (position == 1) {
            return mContext.getString(R.string.page_weather);
        } else {
            return mContext.getString(R.string.page_catalog);
        }
    }
}