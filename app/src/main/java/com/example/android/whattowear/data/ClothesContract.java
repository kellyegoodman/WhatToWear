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
     * Possible path (appended to base content URI for possible URI's).
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
        public final static String COLUMN_ARTICLE_SUBCATEGORY = "sub_category";

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
        public final static String COLUMN_ARTICLE_NYLON_SPANDEX = "nylon_spandex";
        public final static String COLUMN_ARTICLE_WOOL = "wool";

        /**
         * Computed clo value of the item.
         *
         * Type: REAL
         */
        public final static String COLUMN_ARTICLE_CLO_VALUE = "clo_value";

        /**
         * Enum of materials
         */
        public static final int COTTON = 0;
        public static final int POLYESTER = 1;
        public static final int RAYON = 2;
        public static final int NYLON_SPANDEX = 3;
        public static final int WOOL = 4;
        public static final int NUM_MATERIALS = 5;

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
        public static final int SUBCATEGORY_FORMAL_SHIRT = 2;
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
                case SUBCATEGORY_FORMAL_SHIRT:
                    return "Formal Shirt";
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
            aMap.put(SUBCATEGORY_FORMAL_SHIRT, CATEGORY_TOP);
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
         * Material properties of plain, woven fabrics
         *
         * Siddiqui, MOR & Sun, D 2018, 'Thermal Analysis of Conventional and Performance Plain Woven Fabrics by
         * Finite Element Method', Journal of Industrial Textiles, vol. 48, no. 4, pp. 685-712.
         * https://doi.org/10.1177/1528083717736104
         * https://pure.hw.ac.uk/ws/portalfiles/portal/15876620/2017_Thermal_Analysis_of_Conventional_and_Performance_Plain_Woven_Fabrics_by_Finite_Element_Method_author_version.pdf
         */
        private static final double[] densities = {1520, 1390, 1490, 1320, 1310};     // kg / m^2
        private static final double[] thermalConductivity = {0.056, 0.048, 0.048, 0.039, 0.041};   // W / m K

        /**
         * Estimated surface area of average person is 1.8 m²:
         * https://www.engineeringtoolbox.com/clo-clothing-thermal-insulation-d_732.html
         */
        private static final double surfaceArea = 1.8;  // m^2
        private static final double fudgeFactor = 20.0;

        /**
         * Calculating Clo value for a clothing item:
         *
         * 1 clo = 0.155 m²K/W
         * thermal conductivity k, units: [W / m K]
         * thermal resistance (R-value) R = thickness / k, units: [m²K/W]
         *
         * 1. compute item density (weighted sum of fabric densities)
         *
         * 2. estimate the material thickness?
         *      volume = weight / density*1000(g/kg)
         *      thickness = volume / surface_area
         *
         * 3. Calculate Clo value
         *      R-value = thickness / k
         *      clo = R-value / 0.155 m²K/W (literature states 0.155 m²K/W, here need to use fudge factor of 20)
          */

        public static double calculateCloValue(ContentValues values) {
            // get materials and weight
            Integer cotton = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_COTTON);
            Integer polyester = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_POLYESTER);
            Integer rayon = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_RAYON);
            Integer spandex = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_NYLON_SPANDEX);
            Integer wool = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WOOL);

            double density = (cotton*densities[0] +
                    polyester*densities[1] +
                    rayon*densities[2] +
                    spandex*densities[3] +
                    wool*densities[4]) / 100;

            Integer weight = values.getAsInteger(ClothesEntry.COLUMN_ARTICLE_WEIGHT);

            double volume = weight / (density * 1000);
            double thickness = volume / surfaceArea;

            double k = (cotton*thermalConductivity[0] +
                    polyester*thermalConductivity[1] +
                    rayon*thermalConductivity[2] +
                    spandex*thermalConductivity[3] +
                    wool*thermalConductivity[4]) / 100;
            double clo = (fudgeFactor * thickness) / (k * 0.155);

            return clo;
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
                                              int _spandex, int _wool) {
            if (_cotton + _polyester + _rayon + _spandex + _wool == 100) {
                return true;
            }
            return false;
        }
    }

}
