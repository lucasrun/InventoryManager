package com.example.android.inventorymanager.data;

/**
 * Created by mhesah on 2017-07-18.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventorymanager.R;

import static com.example.android.inventorymanager.data.DbContract.CONTENT_AUTHORITY;
import static com.example.android.inventorymanager.data.DbContract.DbEntry;
import static com.example.android.inventorymanager.data.DbContract.PATH_ITEMS;

public class DbProvider extends ContentProvider {

    public static final String LOG_TAG = DbProvider.class.getSimpleName();
    public static final int ITEMS = 100;
    public static final int ITEMS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_ITEMS + "/#", ITEMS_ID);
    }

    private DbHelper mDbHelper;

    // main
    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    // cursor
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(DbEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEMS_ID:
                selection = DbEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(DbEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                Toast.makeText(getContext(), "Query failed, unknown URI: " + uri, Toast.LENGTH_LONG).show();
        }

        if (cursor != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    // adding methods
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, values);
            default:
                Toast.makeText(getContext(), "Insertion is not supported for " + uri, Toast.LENGTH_LONG).show();
                return null;
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {

        // item fields
        byte[] image = values.getAsByteArray(DbEntry.COLUMN_ITEM_IMAGE);
        String name = values.getAsString(DbEntry.COLUMN_ITEM_NAME);
        int quantity = values.getAsInteger(DbEntry.COLUMN_ITEM_QUANTITY);
        int price = values.getAsInteger(DbEntry.COLUMN_ITEM_PRICE);

        // item field check
        if (image == null) {
            Toast.makeText(getContext(), R.string.invalid_image, Toast.LENGTH_LONG).show();
        }

        if (name == null) {
            Toast.makeText(getContext(), R.string.invalid_name, Toast.LENGTH_LONG).show();
        }

        if (quantity <= 0) {
            Toast.makeText(getContext(), R.string.invalid_quantity, Toast.LENGTH_LONG).show();
        }

        if (price <= 0) {
            Toast.makeText(getContext(), R.string.invalid_price, Toast.LENGTH_LONG).show();
        }

        // database item save
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(DbEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    // updating methods
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEMS_ID:
                selection = DbEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                Toast.makeText(getContext(), "Update is not supported for " + uri, Toast.LENGTH_LONG).show();
                return -1;
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(DbEntry.COLUMN_ITEM_IMAGE)) {
            byte[] image = values.getAsByteArray(DbEntry.COLUMN_ITEM_IMAGE);
            Log.e("update", "" + image);
            if (image == null) {
                Toast.makeText(getContext(), R.string.invalid_image, Toast.LENGTH_LONG).show();
            }
        }

        if (values.containsKey(DbEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(DbEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                Toast.makeText(getContext(), R.string.invalid_name, Toast.LENGTH_LONG).show();
            }
        }

        if (values.containsKey(DbEntry.COLUMN_ITEM_QUANTITY)) {
            int quantity = values.getAsInteger(DbEntry.COLUMN_ITEM_QUANTITY);
            if (quantity <= 0) {
                Toast.makeText(getContext(), R.string.invalid_quantity, Toast.LENGTH_LONG).show();
            }
        }

        if (values.containsKey(DbEntry.COLUMN_ITEM_PRICE)) {
            int price = values.getAsInteger(DbEntry.COLUMN_ITEM_PRICE);
            if (price <= 0) {
                Toast.makeText(getContext(), R.string.invalid_price, Toast.LENGTH_LONG).show();
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int numOfRows = db.update(DbEntry.TABLE_NAME, values, selection, selectionArgs);
        if (numOfRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numOfRows;
    }

    // removing methods
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                int rowsDeleted = database.delete(DbEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case ITEMS_ID:
                selection = DbEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int rowDeleted = database.delete(DbEntry.TABLE_NAME, selection, selectionArgs);
                if (rowDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowDeleted;
            default:
                Toast.makeText(getContext(), "Deletion is not supported for " + uri, Toast.LENGTH_LONG).show();
                return -1;
        }
    }

    // MIME handler
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return DbEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return DbEntry.CONTENT_ITEM_TYPE;
            default:
                Toast.makeText(getContext(), "Unknown URI " + uri + " with match " + match, Toast.LENGTH_LONG).show();
                return null;
        }
    }
}