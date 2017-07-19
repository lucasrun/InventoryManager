package com.example.android.inventorymanager;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;

import static com.example.android.inventorymanager.data.DbContract.DbEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private DataCursorAdapter mDataCursorAdapter;

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        final ListView itemList = (ListView) findViewById(R.id.item_list_view);
        View emptyView = findViewById(R.id.empty_view);
        itemList.setEmptyView(emptyView);

        mDataCursorAdapter = new DataCursorAdapter(this, null);
        itemList.setAdapter(mDataCursorAdapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.setData(ContentUris.withAppendedId(DbEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_dummy_item:
                insertItem();
                return true;
            case R.id.delete_all_items:
                getContentResolver().delete(DbEntry.CONTENT_URI, null, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                DbEntry._ID,
                DbEntry.COLUMN_ITEM_IMAGE,
                DbEntry.COLUMN_ITEM_NAME,
                DbEntry.COLUMN_ITEM_QUANTITY,
                DbEntry.COLUMN_ITEM_PRICE
        };

        return new CursorLoader(this, DbEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDataCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDataCursorAdapter.swapCursor(null);
    }

    private void insertItem() {

        byte[] convertedImage = getBitmapAsByteArray(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.dummy_item));
        ContentValues values = new ContentValues();
        values.put(DbEntry.COLUMN_ITEM_IMAGE, convertedImage);
        values.put(DbEntry.COLUMN_ITEM_NAME, "Item");
        values.put(DbEntry.COLUMN_ITEM_QUANTITY, 100);
        values.put(DbEntry.COLUMN_ITEM_PRICE, 1);

        Uri newEntryUri = getContentResolver().insert(DbEntry.CONTENT_URI, values);
    }
}