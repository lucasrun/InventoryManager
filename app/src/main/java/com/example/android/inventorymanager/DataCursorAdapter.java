package com.example.android.inventorymanager;

/**
 * Created by mhesah on 2017-07-18.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorymanager.data.DbContract.DbEntry;
import com.example.android.inventorymanager.data.DbHelper;

public class DataCursorAdapter extends CursorAdapter {

    private LayoutInflater mLayoutInflater;

    public DataCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.list_item, parent, false);
        return view;
    }

    @Override
    public void bindView(final View view, Context context, final Cursor cursor) {

        TextView name = (TextView) view.findViewById(R.id.item_name);
        TextView price = (TextView) view.findViewById(R.id.item_price);
        final TextView mQuantity = (TextView) view.findViewById(R.id.item_quantity);
        Button mSale = (Button) view.findViewById(R.id.sale_button);

        String itemName = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_ITEM_NAME));
        String itemQuantity = Integer.toString(cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_ITEM_QUANTITY)));
        String itemPrice = Integer.toString(cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_ITEM_PRICE)));

        name.setText(itemName);
        mQuantity.setText(itemQuantity);
        price.setText(itemPrice);

        final int position = cursor.getPosition();

        mSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(position);

                int quantity = Integer.parseInt(mQuantity.getText().toString().trim());

                if (quantity > 0) {
                    quantity--;
                } else {
                    Toast.makeText(view.getContext(), R.string.out_of_stock, Toast.LENGTH_LONG).show();
                }

                mQuantity.setText(String.valueOf(quantity));

                DbHelper dbHelper = new DbHelper(view.getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DbEntry.COLUMN_ITEM_QUANTITY, quantity);

                int id = cursor.getInt(cursor.getColumnIndex(DbEntry._ID));
                db.update(DbEntry.TABLE_NAME, values, "_id=" + id, null);
                db.close();
            }
        });
    }
}