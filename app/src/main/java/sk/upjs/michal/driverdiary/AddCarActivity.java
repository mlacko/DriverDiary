package sk.upjs.michal.driverdiary;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class AddCarActivity extends ActionBarActivity {

    SharedPreferences prefs;
    Set<String> stringSet;
    Boolean firstStart;
    private String path;
    String spzString;
    private EditText brand;
    private EditText model;
    private EditText spz;
    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isTablet = getResources().getBoolean(R.bool.isTablet);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);
        brand = (EditText) findViewById(R.id.brand_editText);
        model = (EditText) findViewById(R.id.model_editText);
        spz = (EditText) findViewById(R.id.spz_editText);
        brand.requestFocus();
        brand.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    model.requestFocus();
                    handled = true;
                }
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    model.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });

        model.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    spz.requestFocus();
                    handled = true;
                }
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    spz.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });

        spz.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    spz.clearFocus();
                    if (checkCameraHardware(AddCarActivity.this)) {
                        spzString = spz.getText().toString();
                        Intent intent = new Intent(AddCarActivity.this, CameraActivity.class);
                        intent.putExtra("SPZ", spzString);
                        startActivityForResult(intent, 2);

                    }
                    handled = true;
                }
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    spz.clearFocus();
                    if (!spz.getText().toString().matches("") && !brand.getText().toString().matches("") && !model.getText().toString().matches("") ) {
                        if (!spz.getText().toString().equalsIgnoreCase("null")) {
                            spzString = spz.getText().toString();
                            Intent intent = new Intent(AddCarActivity.this, CameraActivity.class);
                            intent.putExtra("SPZ", spzString);
                            startActivityForResult(intent, 2);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddCarActivity.this);
                            builder.setMessage(getResources().getString(R.string.not_enough_data));
                            builder.setCancelable(false);
                            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                        }


                    }
                    handled = true;
                }
                return handled;
            }
        });

        Button takePhoto = (Button) findViewById(R.id.take_photo_button);
        Button saveCar = (Button) findViewById(R.id.save_car_button);
        prefs = getSharedPreferences("sk.upjs.michal.driverdiary", MODE_PRIVATE);
        Calendar dateAndTime = Calendar.getInstance();
        final Date date = dateAndTime.getTime();
        stringSet = prefs.getStringSet("sk.upjs.michal.driverdiary.spz", null);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraHardware(AddCarActivity.this)) {
                    if (!spz.getText().toString().matches("") && !brand.getText().toString().matches("") && !model.getText().toString().matches("") ) {
                        spzString = spz.getText().toString();
                        Log.e("spz", spz.getText().toString());
                        Intent intent = new Intent(AddCarActivity.this, CameraActivity.class);
                        intent.putExtra("SPZ", spz.getText().toString());
                        startActivityForResult(intent, 2);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddCarActivity.this);
                        builder.setMessage(getResources().getString(R.string.not_enough_data));
                        builder.setCancelable(false);
                        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                }
            }
        });

        saveCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!spz.getText().toString().matches("") && !brand.getText().toString().matches("") && !model.getText().toString().matches("") ) {
                    String jsonString;
                    if (path != null) {
                        jsonString = "{\"brand\":\"" + brand.getText().toString() + "\",\"model\":\"" + model.getText().toString() + "\",\"timestamp\":\""
                                + date.toString() + "\",\"spz\":\"" + spzString + "\",\"img_resource\":\"" + path + "\"}";
                    } else {
                        jsonString = "{\"brand\":\"" + brand.getText().toString() + "\",\"model\":\"" + model.getText().toString() + "\",\"timestamp\":\""
                                + date.toString() + "\",\"spz\":\"" + spz.getText().toString() + "\",\"img_resource\":\"" + "no photo available" + "\"}";
                    }
                    if (stringSet == null) {
                        Set<String> spzSet = new HashSet<String>();
                        spzSet.add(spzString);
                        prefs.edit().putString("sk.upjs.michal.driverdiary." + spzString, jsonString).putStringSet("sk.upjs.michal.driverdiary.spz", spzSet).apply();
                    } else {     //ak ziaden nemame
                        stringSet.add(spzString);
                        prefs.edit().putString("sk.upjs.michal.driverdiary." + spzString, jsonString).putStringSet("sk.upjs.michal.driverdiary.spz", stringSet).apply();
                    }
                    Log.e("cesta", jsonString);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddCarActivity.this);
                    builder.setMessage(getResources().getString(R.string.not_enough_data));
                    builder.setCancelable(false);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                path = data.getStringExtra("path");
                Log.e("cesta", path);
            }
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}
