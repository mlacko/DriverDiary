package sk.upjs.michal.driverdiary;

import android.content.pm.ActivityInfo;
import android.media.ExifInterface;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    AlertDialog.Builder builder;
    private SharedPreferences prefs;
    ImageView iv;
    TextView tv;
    private String spz;
    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isTablet = getResources().getBoolean(R.bool.isTablet);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addRecord = (Button) findViewById(R.id.add_record_button);
        Button carGallery = (Button) findViewById(R.id.gallery_button);
        Button addCarRecord = (Button) findViewById(R.id.add_car_record_button);
        iv = (ImageView) findViewById(R.id.predefined_car_imageView);
        tv = (TextView) findViewById(R.id.predefined_spz_textView);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        iv.setAdjustViewBounds(true);
        if (!isTablet) {
            iv.setMaxWidth(displaymetrics.widthPixels - (displaymetrics.widthPixels / 5));
            iv.setMaxHeight(displaymetrics.widthPixels - (displaymetrics.widthPixels / 5));
        } else {
            iv.setMaxWidth(displaymetrics.heightPixels - (displaymetrics.heightPixels / 2));
            iv.setMaxHeight(displaymetrics.heightPixels - (displaymetrics.heightPixels / 2));
        }
        prefs = getSharedPreferences("sk.upjs.michal.driverdiary", MODE_PRIVATE);

        addCarRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tv.getText().toString().equalsIgnoreCase(getResources().getString(R.string.no_favourite_car))) {
                    Intent intent = new Intent(MainActivity.this, AddRecordActivity.class );
                    intent.putExtra("SPZ", tv.getText().toString());
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getResources().getString(R.string.no_favourite_car));
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

        addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AddCarActivity.class);
                startActivityForResult(intent, 1);

            }
        });
        carGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CarGalleryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.succesfull_record), Toast.LENGTH_LONG).show();
                /*builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.succesfull_record));
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();*/
            } else {
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.create_failed), Toast.LENGTH_LONG).show();
                    /*builder = new AlertDialog.Builder(this);
                    builder.setMessage(getResources().getString(R.string.create_failed));
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();*/
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    protected void onResume() { // zabezpecenie automatickej aktualizacie aktivity
        super.onResume();
        Log.e("on resume", "dosol");
        if (prefs.getString("sk.upjs.michal.driverdiary.favourite", null)!=null){
            String spz = prefs.getString("sk.upjs.michal.driverdiary.favourite", null);
            String json = prefs.getString("sk.upjs.michal.driverdiary."+spz, null);
            Log.e("spzka",json);
            try {
                JSONObject carJSON = new JSONObject(json);
                Vehicle vehicle = Vehicle.vehicleFromJSON(carJSON);
                if (!vehicle.getPath().equalsIgnoreCase("no photo available")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(vehicle.getPath());
                    ExifInterface exifInterface = null;
                    exifInterface = new ExifInterface(vehicle.getPath());
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                    iv.setImageDrawable(new BitmapDrawable(VehicleAdapter.rotateBitmap(bitmap,orientation)));
                } else {
                    iv.setImageResource(R.drawable.no_image_available);
                }
                tv.setText(vehicle.getSpz());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            iv.setImageResource(R.drawable.no_image_available);
            tv.setText(getResources().getString(R.string.no_favourite_car));
        }
    }
}
