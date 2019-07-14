package com.example.android.whattowear;

import java.util.ArrayList;
import java.util.List;

public class Outfit {

    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int DRESS = 2;
    public static final int OUTER1 = 3;
    public static final int OUTER2 = 4;

    public static class ClothingItem {
        private int mType;
        private String mImagePath;
        private double mWarmth;

        public ClothingItem(int type, String imagePath, double warmth) {
            mType = type;
            mImagePath = imagePath;
            mWarmth = warmth;
        }

        public int getType() { return mType; }
        public String getImage() { return mImagePath; }
        public double getWarmth() { return mWarmth; }
    }

    private ArrayList<ClothingItem> mClothingList = new ArrayList<>();

    public Outfit() {
        // nothing to do
    }

    public void addItem(ClothingItem item) {
        int type = item.getType();
        if ((type >= TOP) & (type <= OUTER2)) {
            mClothingList.add(item);
        }
    }

    public double getWarmth() {
        double warmth = 0;
        for (ClothingItem item : mClothingList) {
            warmth += item.getWarmth();
        }
        return warmth;
    }

    public List<ClothingItem> getClothes() {
        return mClothingList;
    }
}
