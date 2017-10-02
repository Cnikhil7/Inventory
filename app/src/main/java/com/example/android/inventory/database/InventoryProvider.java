package com.example.android.inventory.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventory.database.InventoryContract.InventoryEntry;


public class InventoryProvider extends ContentProvider {


    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }


    private InventoryHelper inventoryHelper;


    @Override
    public boolean onCreate() {
        inventoryHelper = new InventoryHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
        Cursor cursor;
        SQLiteDatabase inventory = inventoryHelper.getReadableDatabase();

        int match = uriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                cursor = inventory.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        strings, s, strings1, null, null, s1);
                break;
            case INVENTORY_ID:
                s = InventoryEntry._ID + " =? ";
                strings1 = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = inventory.query(InventoryEntry.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            default:
                throw new IllegalArgumentException("Cannot query Unknown Uri " + uri);
        }

        //notify cursor loader if data to this uri was modified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {


        int match = uriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);

        }
    }

    private Uri insertItem(Uri uri, ContentValues contentValues) {

        //check if data is valid and if false, do not insert values and return;
        if (!validateInsertData(contentValues)) {
            return null;
        }

        //insert data
        SQLiteDatabase database = inventoryHelper.getWritableDatabase();
        long id = database.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        //check if data was inserted else return null;
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify listener that data has changed for uri
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {

        SQLiteDatabase inventory = inventoryHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);

        int rowDeleted;
        switch (match) {
            case INVENTORY:
                //delete all rows
                rowDeleted = inventory.delete(InventoryEntry.TABLE_NAME, s, strings);
                break;
            case INVENTORY_ID:

                s = InventoryEntry._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = inventory.delete(InventoryEntry.TABLE_NAME, s, strings);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        int match = uriMatcher.match(uri);

        //validate data if false return here
        if (!validateUpdateData(contentValues)) {
            return 0;
        }


        SQLiteDatabase inventory = inventoryHelper.getWritableDatabase();
        int rowUpdated;

        switch (match) {
            case INVENTORY:
                rowUpdated = inventory.update(InventoryEntry.TABLE_NAME, contentValues, s, strings);
                break;
            case INVENTORY_ID:
                s = InventoryEntry._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowUpdated = inventory.update(InventoryEntry.TABLE_NAME, contentValues, s, strings);
                break;
            default:
                throw new IllegalArgumentException("Update not supported for " + uri);
        }

        //notify listener if data is updated
        if (rowUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }


        return rowUpdated;
    }


    //validates the input data and returns true if valid
    private boolean validateInsertData(ContentValues contentValues) {

        String name = contentValues.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        String seller = contentValues.getAsString(InventoryEntry.COLUMN_ITEM_SELLER);
        int category = contentValues.getAsInteger(InventoryEntry.COLUMN_ITEM_CATEGORY);
        int qty = contentValues.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        int price = contentValues.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
        int availability = contentValues.getAsInteger(InventoryEntry.COLUMN_ITEM_IN_STOCK);


        if (name == null || name.isEmpty()) {
            makeToast("Invalid Name");
            return false;
        }

        if (seller == null || seller.isEmpty()) {
            makeToast("Invalid Seller");
            return false;
        }

        if (!InventoryEntry.isValidCategory(category)) {
            makeToast("Invalid Category");
            return false;
        }

        if (qty < 0) {
            makeToast("Invalid Quantity");
            return false;
        }

        if (price < 0) {
            makeToast("Invalid Price");
            return false;
        }

        if (!InventoryEntry.isValidAvailability(availability)) {
            makeToast("Invalid Availability");
            return false;
        }

        return true;

    }

    //validates the input data for update operation and returns true if valid
    private boolean validateUpdateData(ContentValues contentValues) {

        //if no values to update
        if (contentValues.size() == 0) {
            return false;
        }

        String nameKey = InventoryEntry.COLUMN_ITEM_NAME;
        String categoryKey = InventoryEntry.COLUMN_ITEM_CATEGORY;
        String quantityKey = InventoryEntry.COLUMN_ITEM_QUANTITY;
        String sellerKey = InventoryEntry.COLUMN_ITEM_SELLER;
        String availabilityKey = InventoryEntry.COLUMN_ITEM_IN_STOCK;
        String priceKey = InventoryEntry.COLUMN_ITEM_PRICE;

        if (contentValues.containsKey(nameKey)) {
            String name = contentValues.getAsString(nameKey);
            if (name == null || name.isEmpty()) {
                makeToast("Invalid Name");
                return false;
            }
        }

        if (contentValues.containsKey(sellerKey)) {
            String seller = contentValues.getAsString(sellerKey);
            if (seller == null || seller.isEmpty()) {
                makeToast("Invalid Seller");
                return false;
            }
        }


        if (contentValues.containsKey(categoryKey)) {
            int category = contentValues.getAsInteger(categoryKey);
            if (!InventoryEntry.isValidCategory(category)) {
                makeToast("Invalid Category");
                return false;
            }
        }

        if (contentValues.containsKey(priceKey)) {
            int price = contentValues.getAsInteger(priceKey);
            if (price < 0) {
                makeToast("Invalid Price");
                return false;
            }
        }

        if (contentValues.containsKey(availabilityKey)) {
            int availability = contentValues.getAsInteger(availabilityKey);
            if (!InventoryEntry.isValidAvailability(availability)) {
                makeToast("Invalid Availability");
                return false;
            }
        }

        if (contentValues.containsKey(quantityKey)) {
            int qty = contentValues.getAsInteger(quantityKey);
            if (qty < 0) {
                makeToast("Invalid Quantity");
                return false;
            }
        }


        return true;

    }

    private void makeToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
