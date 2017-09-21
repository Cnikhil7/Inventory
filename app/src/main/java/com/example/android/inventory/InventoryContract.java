package com.example.android.inventory;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Nikhil on 9/21/2017.
 */

public class InventoryContract {
    //Contract authority
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    //Base Content Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Content path
    public static final String PATH_INVENTORY = "inventory";


    private InventoryContract() {
    }


    public static class InventoryEntry implements BaseColumns {

        //MIME Type of list of items
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //MIME Type of single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        //content uri
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);


        //Table Name and Table Constants
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_CATEGORY = "category";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_QUANTITY = "qty";
        public static final String COLUMN_ITEM_SELLER = "seller";
        public static final String COLUMN_ITEM_IN_STOCK = "in_stock";


        //Category Constants
        public static final int CAT_OTHER = 0;
        public static final int CAT_ELECTRONICS = 1;
        public static final int CAT_APPAREL_AND_ACCESSORIES = 2;
        public static final int CAT_HEALTH_AND_BEAUTY = 3;
        public static final int CAT_MUSIC = 4;
        public static final int CAT_BOOKS = 5;
        public static final int CAT_VIDEO_AND_GAMES = 6;
        public static final int CAT_TOYS = 7;
        public static final int CAT_GROCERY = 8;

        //Stock Constants
        public static final int STOCK_AVAILABLE = 0;
        public static final int STOCK_NOT_AVAILABLE = 1;


        public static boolean isValidCategory(int cat) {

            switch (cat) {
                case CAT_APPAREL_AND_ACCESSORIES:
                case CAT_ELECTRONICS:
                case CAT_HEALTH_AND_BEAUTY:
                case CAT_MUSIC:
                case CAT_BOOKS:
                case CAT_VIDEO_AND_GAMES:
                case CAT_TOYS:
                case CAT_GROCERY:
                case CAT_OTHER:
                    return true;
                default:
                    return false;
            }

        }

        public static boolean isValidAvailability(int availability) {
            return availability == STOCK_AVAILABLE || availability == STOCK_NOT_AVAILABLE;
        }


    }
}
