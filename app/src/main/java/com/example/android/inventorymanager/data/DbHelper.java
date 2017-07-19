package com.example.android.inventorymanager.data;

/**
 * Created by mhesah on 2017-07-18.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventorymanager.data.DbContract.DbEntry;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory_manager.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE = "CREATE TABLE " + DbEntry.TABLE_NAME +
                " (" + DbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbEntry.COLUMN_ITEM_IMAGE + " BLOB, " +
                DbEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                DbEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                DbEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + DbEntry.TABLE_NAME);
        onCreate(db);
    }
}