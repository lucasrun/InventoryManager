package com.example.android.inventorymanager.data;

/**
 * Created by mhesah on 2017-07-18.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class DbContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventorymanager";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "items";

    public DbContract() {
    }

    public static final class DbEntry implements BaseColumns {

        // main link do database
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        // cursor statics
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // table
        public final static String TABLE_NAME = "items";

        // columns
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_IMAGE = "image";
        public final static String COLUMN_ITEM_NAME = "name";
        public final static String COLUMN_ITEM_QUANTITY = "quantity"; //max quantity
        public final static String COLUMN_ITEM_PRICE = "price";
    }
}