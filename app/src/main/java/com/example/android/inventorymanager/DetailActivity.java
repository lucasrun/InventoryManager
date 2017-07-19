package com.example.android.inventorymanager;

/**
 * Created by mhesah on 2017-07-18.
 */

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorymanager.data.DbContract.DbEntry;

import java.io.ByteArrayOutputStream;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView mItemImage;
    private EditText mItemName;
    private EditText mItemPrice;
    private TextView mMaxQuantity;
    private TextView mCurrentQuantity;
    private Button mIncreaseQuantity;
    private Button mDecreaseQuantity;
    private Uri currentUri;
    private boolean mValidator = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mValidator = true;
            return false;
        }
    };

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        currentUri = intent.getData();

        if (currentUri == null) {
            setTitle(R.string.add_item);
        } else {
            setTitle(R.string.edit_item);
            invalidateOptionsMenu();
            getLoaderManager().initLoader(0, null, this);
        }

        mItemImage = (ImageView) findViewById(R.id.item_edit_image);
        mItemName = (EditText) findViewById(R.id.name_edit_text);
        mItemPrice = (EditText) findViewById(R.id.price_edit_text);
        mMaxQuantity = (TextView) findViewById(R.id.max_quantity_amount);
        mCurrentQuantity = (TextView) findViewById(R.id.current_quantity_text);
        mDecreaseQuantity = (Button) findViewById(R.id.decrease_quantity_button);
        mIncreaseQuantity = (Button) findViewById(R.id.increase_quantity_button);

        mItemName.setOnTouchListener(mTouchListener);
        mItemPrice.setOnTouchListener(mTouchListener);
        mDecreaseQuantity.setOnTouchListener(mTouchListener);
        mIncreaseQuantity.setOnTouchListener(mTouchListener);

        Button loadImage = (Button) findViewById(R.id.button_load_image);
        loadImage.setOnTouchListener(mTouchListener);

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mCurrentQuantity.getText().toString().trim());
                if (quantity > 0) {
                    quantity--;
                }
                mCurrentQuantity.setText(String.valueOf(quantity));

            }
        });

        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mCurrentQuantity.getText().toString().trim());
                quantity++;
                mCurrentQuantity.setText(String.valueOf(quantity));

            }
        });

        Button orderButton = (Button) findViewById(R.id.order_button);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentUri == null && (mItemName.getText().toString().trim().equals("") || Integer.parseInt(mCurrentQuantity.getText().toString().trim()) == 0)) {
                    Toast.makeText(getBaseContext(), R.string.select_item_plz, Toast.LENGTH_LONG).show();
                } else {

                    int finalPrice = Integer.parseInt(mCurrentQuantity.getText().toString().trim()) * Integer.parseInt(mItemPrice.getText().toString().trim());

                    String type = "*/*";
                    String email = "lucas.run@gmail.com";
                    String subject = "I.M. Order";
                    String text = DbEntry.COLUMN_ITEM_NAME + ": " + mItemName.getText().toString().trim() + "\n" +
                            DbEntry.COLUMN_ITEM_QUANTITY + ": " + mCurrentQuantity.getText().toString().trim() + "\n" +
                            "price/unit: " + mItemPrice.getText().toString().trim() + "$" + "\n" +
                            "final price: " + finalPrice + "$";

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(type);
                    intent.putExtra(Intent.EXTRA_EMAIL, email);
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, text);

                    itemSave();

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if (picturePath != null) {
                mItemImage.setImageURI(data.getData());
                mItemImage.setTag(data.getData());
            }
        }
    }

    // menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.menu_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mValidator) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_save:
                itemSave();
                finish();
                return true;
            case R.id.menu_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mValidator) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // cursor
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                DbEntry._ID,
                DbEntry.COLUMN_ITEM_IMAGE,
                DbEntry.COLUMN_ITEM_NAME,
                DbEntry.COLUMN_ITEM_QUANTITY,
                DbEntry.COLUMN_ITEM_PRICE
        };
        return new CursorLoader(this, currentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()) {
            int imageColumnIndex = data.getColumnIndex(DbEntry.COLUMN_ITEM_IMAGE);
            int nameColumnIndex = data.getColumnIndex(DbEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = data.getColumnIndex(DbEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(DbEntry.COLUMN_ITEM_PRICE);

            byte[] image = data.getBlob(imageColumnIndex);
            String name = data.getString(nameColumnIndex);
            int price = data.getInt(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);

            Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

            mItemImage.setImageBitmap(imageBitmap);
            mItemName.setText(name);
            mCurrentQuantity.setText(String.valueOf(quantity));
            mItemPrice.setText(String.valueOf(price));
            mMaxQuantity.setText(String.valueOf(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemImage.setImageBitmap(null);
        mItemName.setText(null);
        mCurrentQuantity.setText(null);
        mItemPrice.setText(null);
        mMaxQuantity.setText(null);
    }

    // item methods
    private void itemSave() {

        String itemName = mItemName.getText().toString().trim();

        int itemPrice = 0;
        if (!TextUtils.isEmpty(mItemPrice.getText().toString().trim())) {
            itemPrice = Integer.parseInt(mItemPrice.getText().toString().trim());
        }

        int currentQuantity = Integer.parseInt(mCurrentQuantity.getText().toString().trim());

        if (currentUri == null && (TextUtils.isEmpty(itemName) || itemPrice == 0 || currentQuantity == 0 || mItemImage.getDrawable() == null)) {
            Toast.makeText(getBaseContext(), getString(R.string.full_item_info_required), Toast.LENGTH_LONG).show();
            return;
        }

        byte[] imageByteArray = getBytes(((BitmapDrawable) mItemImage.getDrawable()).getBitmap());

        ContentValues values = new ContentValues();
        values.put(DbEntry.COLUMN_ITEM_IMAGE, imageByteArray);
        values.put(DbEntry.COLUMN_ITEM_NAME, itemName);
        values.put(DbEntry.COLUMN_ITEM_QUANTITY, currentQuantity);
        values.put(DbEntry.COLUMN_ITEM_PRICE, itemPrice);

        if (currentUri == null) {
            Uri newEntryUri = getContentResolver().insert(DbEntry.CONTENT_URI, values);
            if (newEntryUri == null) {
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.success_saving, Toast.LENGTH_SHORT).show();
            }
        } else {
            int changed = getContentResolver().update(currentUri, values, null, null);

            if (changed == 0) {
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.success_saving, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void itemDelete() {

        if (currentUri != null) {
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.error_deleting, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.success_deleting, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    // dialog methods
    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_item));
        builder.setPositiveButton(getString(R.string.menu_delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                itemDelete();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.discard_changes));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}