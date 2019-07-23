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

    public boolean isEmpty() { return mClothingList.isEmpty(); }

    public boolean isValid() {
        // does outfit have ((top AND bottom) XOR dress)
        // does outfit have no more than one top, bottom, or dress
        boolean hasTop = false;
        boolean hasBottom = false;
        boolean hasDress = false;

        for (ClothingItem item : mClothingList) {
            switch (item.getType()) {
                case TOP:
                    if (hasTop) return false;
                    hasTop = true;
                    break;
                case BOTTOM:
                    if (hasBottom) return false;
                    hasBottom = true;
                    break;
                case DRESS:
                    if (hasDress) return false;
                    hasDress = true;
                    break;
                default:
                    break;
            }
        }

        return ((hasTop & hasBottom) ^ hasDress);
    }

    public List<ClothingItem> getClothes() { return mClothingList; }
}
