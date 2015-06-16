package sk.upjs.michal.driverdiary;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class AddRecordActivity extends ActionBarActivity implements LocationListener, SensorEventListener {

    private Timer t;
    private int seconds = 0;
    private SharedPreferences prefs;
    private String spz;
    private ArrayList<double[]> locations;
    private LocationManager locationManager;
    private TextView tv;
    private TextView locationtv;
    private GpsStatus.Listener mGPSStatusListener;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
    private ProgressDialog progressDialog;
    private Boolean tracking = false;
    private boolean isTablet;
    private boolean hasGPS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        Button startDriveButton = (Button) findViewById(R.id.start_recording_button);
        tv = (TextView) findViewById(R.id.timer_textView);
        locationtv = (TextView) findViewById(R.id.location_text_view);
        Bundle bundle = getIntent().getExtras();
        spz = bundle.getString("SPZ");
        prefs = getSharedPreferences("sk.upjs.michal.driverdiary", MODE_PRIVATE);
        locations = new ArrayList<>();
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        startDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();

            }
        });

        Button stoptDriveButton = (Button) findViewById(R.id.stop_recording_button);
        stoptDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (t != null) {
                    t.cancel();
                    t.purge();
                    t = null;
                    locationManager.removeUpdates(AddRecordActivity.this);
                    Calendar dateAndTime = Calendar.getInstance();
                    Date date = dateAndTime.getTime();
                    JSONObject json = new JSONObject();
                    if (locations.size() >= 2) {
                        if (prefs.getInt("sk.upjs.michal.driverdiary." + spz + ".count", 0) == 0) { // tvorime xml s datami o jazde
                            Log.e("prefs", "sk.upjs.michal.driverdiary." + spz + ".count");
                            try {
                                json.put("date", date.toString());
                                json.put("duration", seconds);
                                addLocationsToJSON(json);
                                prefs.edit().putString("sk.upjs.michal.driverdiary." + spz + ".drive1", json.toString())
                                        .putInt("sk.upjs.michal.driverdiary." + spz + ".count", 1)
                                        .apply();
                                Intent intent = new Intent();
                                intent.putExtra("Drive", Drive.driveFromJSON(json));

                                setResult(RESULT_OK, intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            int count = prefs.getInt("sk.upjs.michal.driverdiary." + spz + ".count", 0);
                            Log.e("prefs", "" + count);
                            try {
                                json.put("date", date.toString());
                                json.put("duration", seconds);
                                addLocationsToJSON(json);
                                count++;
                                prefs.edit().putString("sk.upjs.michal.driverdiary." + spz + ".drive" + count, json.toString())
                                        .putInt("sk.upjs.michal.driverdiary." + spz + ".count", count)
                                        .apply();
                                Intent intent = new Intent();
                                intent.putExtra("Drive", Drive.driveFromJSON(json));

                                setResult(RESULT_OK, intent);
                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddRecordActivity.this);
                        builder.setMessage(getResources().getString(R.string.not_enough_positions));
                        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }

                } else {
                    setResult(RESULT_CANCELED);
                    finish();
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
    public void onLocationChanged(Location location) {
        double[] locationArray = new double[2];
        locationArray[0] = location.getLatitude();
        locationArray[1] = location.getLongitude();
        locations.add(locationArray);
        String s = "";

        s += "\tLatitude:  " + location.getLatitude() + "\n";
        s += "\tLongitude: " + location.getLongitude() + "\n";

        locationtv.setText(s);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void startRecord() {
        if (!tracking) {
            tracking = true;
            t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seconds++;
                            tv.setText(improveTimestamp());
                        }
                    });
                }
            }, 1000, 1000);
        }
    }


    public void addLocationsToJSON(JSONObject json) throws JSONException {
        for (int i = 0; i < locations.size(); i++) {
            /*locationArray[0] = location.getLatitude();
            locationArray[1] = location.getLongitude();*/
            double[] locationArray = locations.get(i);
            if (locationArray != null) {
                json.put("lat" + i, locationArray[0]);
                json.put("lng" + i, locationArray[1]);
            }
        }
        json.put("count", locations.size());
        Log.e("json", json.toString());
    }

    @Override
    public void onSensorChanged(SensorEvent event) { // praca so senzorom z http://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if ((speed > SHAKE_THRESHOLD) && !tracking) {
                    if (hasGPS)
                        startRecord();

                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        senSensorManager.unregisterListener(this);//odregistrujeme trasenie
        if (!tracking) {
            locationManager.removeUpdates(AddRecordActivity.this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeGPS();
    }

    public void initializeGPS() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mGPSStatusListener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        Toast.makeText(AddRecordActivity.this, getResources().getString(R.string.gps_status_searching), Toast.LENGTH_SHORT).show();
                        progressDialog = new ProgressDialog(AddRecordActivity.this);
                        progressDialog.setMessage(getResources().getString(R.string.gps_status_searching));
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        System.out.println("TAG - GPS searching: ");
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        System.out.println("TAG - GPS Stopped");
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:

                /*
                 * GPS_EVENT_FIRST_FIX Event is called when GPS is locked
                 */
                        Toast.makeText(AddRecordActivity.this, getResources().getString(R.string.gps_status_locked), Toast.LENGTH_SHORT).show();
                        Location gpslocation = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        progressDialog.cancel();
                        hasGPS = true;
                        if (gpslocation != null) {
                            System.out.println("GPS Info:" + gpslocation.getLatitude() + ":" + gpslocation.getLongitude());

                    /*
                     * Removing the GPS status listener once GPS is locked
                     */
                            locationManager.removeGpsStatusListener(mGPSStatusListener);
                        }

                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        //                 System.out.println("TAG - GPS_EVENT_SATELLITE_STATUS");
                        break;
                }
            }
        };
        locationManager.addGpsStatusListener(mGPSStatusListener);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(AddRecordActivity.this);
            builder.setTitle(getResources().getString(R.string.location_services));
            builder.setMessage(getResources().getString(R.string.permission));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 4);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, AddRecordActivity.this);
        }
    }

    public String improveTimestamp() {
        String timestamp = "";
        if (seconds / 3600 < 10) {
            timestamp += "0" + seconds / 3600 + ":";
        } else {
            timestamp += seconds / 3600;
        }

        if ((seconds / 60) % 60 < 10) {
            timestamp += "0" + (seconds / 60) % 60 + ":";
        } else {
            timestamp += (seconds / 60) % 60 + ":";
        }
        if (seconds % 60 < 10) {
            timestamp += "0" + seconds % 60;
        } else {
            timestamp += seconds % 60;
        }


        return timestamp;
    }
}
