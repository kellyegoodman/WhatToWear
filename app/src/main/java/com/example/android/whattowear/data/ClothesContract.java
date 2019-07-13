package com.example.android.whattowear.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * API Contract for the WhatToWear app.
 */
public final class ClothesContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ClothesContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.whattowear";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_CLOTHES = "clothes";

    /**
     * Inner class that defines constant values for the clothes database table.
     * Each entry in the table represents a single article of clothing.
     */
    public static final class ClothesEntry implements BaseColumns {

        /** The content URI to access the clothing data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CLOTHES);

        /** Name of database table for wardrobe */
        public final static String TABLE_NAME = "clothes";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of clothing.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLOTHES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single article of clothing.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLOTHES;


        /**
         * Unique ID number for the clothing article (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Category of the item.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ARTICLE_CATEGORY = "category";

        /**
         * Subcategory of the item.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ARTICLE_SUBCATEGORY = "subCategory";

        /**
         * Name of the item.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ARTICLE_NAME ="name";

        /**
         * Weight of the item.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ARTICLE_WEIGHT = "weight";

        /**
         * Image file of the item.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ARTICLE_IMAGE ="image";


        /**
         * Material composition of the item.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ARTICLE_COTTON = "cotton";
        public final static String COLUMN_ARTICLE_POLYESTER = "polyester";
        public final static String COLUMN_ARTICLE_RAYON = "rayon";
        public final static String COLUMN_ARTICLE_NYLON = "nylon";
        public final static String COLUMN_ARTICLE_SPANDEX = "spandex";
        public final static String COLUMN_ARTICLE_WOOL = "wool";

        /**
         * Computed warmth factor of the item.
         *
         * Type: REAL
         */
        public final static String COLUMN_ARTICLE_WARMTH = "warmth";

        /**
         * Enum of materials
         */
        public static final int COTTON = 0;
        public static final int POLYESTER = 1;
        public static final int RAYON = 2;
        public static final int NYLON = 3;
        public static final int SPANDEX = 4;
        public static final int WOOL = 5;
        public static final int NUM_MATERIALS = 6;

        /**
         * Possible values for the categories.
         */
        public static final int CATEGORY_TOP = 0;
        public static final int CATEGORY_BOTTOM = 1;
        public static final int CATEGORY_DRESS = 2;
        public static final int CATEGORY_OUTER1 = 3;
        public static final int CATEGORY_OUTER2 = 4;

        public static String getCategoryName(int category_id) {
            switch (category_id) {
                case CATEGORY_TOP:
                    return "Top";
                case CATEGORY_BOTTOM:
                    return "Bottom";
                case CATEGORY_DRESS:
                    return "Dress";
                case CATEGORY_OUTER1:
                    return "Light Jacket";
                case CATEGORY_OUTER2:
                    return "Heavy Jacket";
                default:
                    return "";
            }
        }

        /**
         * Possible values for the subcategories.
         */
        public static final int SUBCATEGORY_TSHIRT = 0;
        public static final int SUBCATEGORY_LONGSLEEVE = 1;
        public static final int SUBCATEGORY_BLOUSE = 2;
        public static final int SUBCATEGORY_CASUAL_PANTS = 3;
        public static final int SUBCATEGORY_SHORTS = 4;
        public static final int SUBCATEGORY_SLACKS = 5;
        public static final int SUBCATEGORY_SKIRT = 6;
        public static final int SUBCATEGORY_CASUAL_DRESS = 7;
        public static final int SUBCATEGORY_FORMAL_DRESS = 8;
        public static final int SUBCATEGORY_SWEATER = 9;
        public static final int SUBCATEGORY_HOODIE = 10;
        public static final int SUBCATEGORY_CARDIGAN = 11;
        public static final int SUBCATEGORY_JACKET = 12;
        public static final int SUBCATEGORY_COAT = 13;
        public static final int NUM_SUBCATEGORIES = 14;

        public static String getSubCategoryName(int subcategory_id) {
            switch (subcategory_id) {
                case SUBCATEGORY_TSHIRT:
                    return "T Shirt";
                case SUBCATEGORY_LONGSLEEVE:
                    return "Longsleeve Shirt";
                case SUBCATEGORY_BLOUSE:
                    return "Blouse";
                case SUBCATEGORY_CASUAL_PANTS:
                    return "Pants";
                case SUBCATEGORY_SHORTS:
                    return "Shorts";
                case SUBCATEGORY_SLACKS:
                    return "Slacks";
                case SUBCATEGORY_SKIRT:
                    return "Skirt";
                case SUBCATEGORY_CASUAL_DRESS:
                    return "Casual Dress";
                case SUBCATEGORY_FORMAL_DRESS:
                    return "Fancy Dress";
                case SUBCATEGORY_SWEATER:
                    return "Sweater";
                case SUBCATEGORY_HOODIE:
                    return "Hoodie";
                case SUBCATEGORY_CARDIGAN:
                    return "Cardigan";
                case SUBCATEGORY_JACKET:
                    return "Jacket";
                case SUBCATEGORY_COAT:
                    return "Coat";
                default:
                    return "";
            }
        }

        /**
         * Mapping between subcategories and categories
         */
        private static final Map<Integer, Integer> SUBCATEGORY_MAP;
        static {
            Map<Integer, Integer> aMap = new HashMap<Integer, Integer>();
            aMap.put(SUBCATEGORY_TSHIRT, CATEGORY_TOP);
            aMap.put(SUBCATEGORY_LONGSLEEVE, CATEGORY_TOP);
            aMap.put(SUBCATEGORY_BLOUSE, CATEGORY_TOP);
            aMap.put(SUBCATEGORY_CASUAL_PANTS, CATEGORY_BOTTOM);
            aMap.put(SUBCATEGORY_SHORTS, CATEGORY_BOTTOM);
            aMap.put(SUBCATEGORY_SLACKS, CATEGORY_BOTTOM);
            aMap.put(SUBCATEGORY_SKIRT, CATEGORY_BOTTOM);
            aMap.put(SUBCATEGORY_CASUAL_DRESS, CATEGORY_DRESS);
            aMap.put(SUBCATEGORY_FORMAL_DRESS, CATEGORY_DRESS);
            aMap.put(SUBCATEGORY_SWEATER, CATEGORY_OUTER1);
            aMap.put(SUBCATEGORY_HOODIE, CATEGORY_OUTER1);
            aMap.put(SUBCATEGORY_CARDIGAN, CATEGORY_OUTER1);
            aMap.put(SUBCATEGORY_JACKET, CATEGORY_OUTER2);
            aMap.put(SUBCATEGORY_COAT, CATEGORY_OUTER2);
            SUBCATEGORY_MAP = Collections.unmodifiableMap(aMap);
        }

        /**
         * Material properties
         */
        private static final double[] densities = {1.5, 1.38, 1.49, 1.15, 1.32, 1.29};
        private static final double[] thermalConductivity = {0.04, 0.05, 0.06, 0.15, 0.08, 0.20};

        // warmth factor derivation
        // density = pi*%i
        // volume = weight / density
        // warmth factor = V * ( ki*%i )

        public static double getWarmthFactor(ContentValues values) {
            // get materials and weight
            Integer cotton = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_COTTON);
            Integer polyester = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_POLYESTER);
            Integer rayon = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_RAYON);
            Integer nylon = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_NYLON);
            Integer spandex = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_SPANDEX);
            Integer wool = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WOOL);

            double density = (cotton*densities[0] +
                    polyester*densities[1] +
                    rayon*densities[2] +
                    nylon*densities[3] +
                    spandex*densities[4] +
                    wool*densities[5]) / 100;

            Integer weight = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WEIGHT);

            double warmth = weight * (cotton*thermalConductivity[0] +
                    polyester*thermalConductivity[1] +
                    rayon*thermalConductivity[2] +
                    nylon*thermalConductivity[3] +
                    spandex*thermalConductivity[4] +
                    wool*thermalConductivity[5]) / density;
            return warmth;
        }


        /**
         * Returns the density of the given material
         */
        public static double getDensity(int material_code) {
            if (material_code < 0 || material_code >= NUM_MATERIALS) {
                return 0;
            }
            return densities[material_code];
        }

        /**
         * Returns the thermal conductivity of the given material
         */
        public static double getThermalConductivity(int material_code) {
            if (material_code < 0 || material_code >= NUM_MATERIALS) {
                return 0;
            }
            return thermalConductivity[material_code];
        }

        /**
         * Returns corresponding category for subcategory
         */
        public static int getCategory(int subcategory) {
            if (SUBCATEGORY_MAP.containsKey(subcategory)) {
                return SUBCATEGORY_MAP.get(subcategory);
            }
            return SUBCATEGORY_TSHIRT;
        }

        /**
         * Returns whether or not the given category and subcategory combination is valid.
         */
        public static boolean isValidSubCategory(int category, int subcategory) {
            if (SUBCATEGORY_MAP.containsKey(subcategory)) {
                if (SUBCATEGORY_MAP.get(subcategory) == category) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns whether or not the given material percentage is valid.
         */
        public static boolean isValidMaterial(int _cotton, int _polyester, int _rayon,
                                              int _nylon, int _spandex, int _wool) {
            if (_cotton + _polyester + _rayon + _nylon + _spandex + _wool == 100) {
                return true;
            }
            return false;
        }
    }

}
