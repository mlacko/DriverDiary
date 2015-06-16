package sk.upjs.michal.driverdiary;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;


public class CarGalleryActivity extends ActionBarActivity {

    private Set<String> spzSet;
    private SharedPreferences prefs;
    private ArrayList<Vehicle> vehicleList;
    private VehicleAdapter adapter;
    private CarDetailFragment carDetailFragment;
    private Boolean isFragmentActive = false;
    private Boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargallery);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cargallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_addcar) {

            Intent intent = new Intent(this, AddCarActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeGallery();
    }

    public void initializeGallery() {
        prefs = getSharedPreferences("sk.upjs.michal.driverdiary", MODE_PRIVATE);
        spzSet = prefs.getStringSet("sk.upjs.michal.driverdiary.spz", null);
        TextView tv = (TextView) findViewById(R.id.unfortunate_textView);
        vehicleList= new ArrayList<Vehicle>();
        if (spzSet != null) {
            String[] spzArray = spzSet.toArray(new String[spzSet.size()]);
            for (int i = 0; i < spzArray.length; i++) {
                String JSONString = prefs.getString("sk.upjs.michal.driverdiary." + spzArray[i], null);
                Log.e("Cesta ", JSONString);
                try {
                    JSONObject vehicleJSON = new JSONObject(JSONString);
                    vehicleList.add(Vehicle.vehicleFromJSON(vehicleJSON)); //vyroba vozidiel
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            GridView gv = (GridView) findViewById(R.id.car_gallery_grid_view);
            adapter = new VehicleAdapter(this, vehicleList);
            tv.setVisibility(View.INVISIBLE);
            gv.setAdapter(adapter);
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    /*if (isTablet) {
                        Vehicle vehicle = vehicleList.get(position);
                        carDetailFragment.setDetails(vehicle.getBrand(), vehicle.getModel(), vehicle.getSpz(), vehicle.getPath());
                        carDetailFragment.getDrives();
                        isFragmentActive = true;
                    } else {
                        Vehicle vehicle = vehicleList.get(position);
                        carDetailFragment.setDetails(vehicle.getBrand(), vehicle.getModel(), vehicle.getSpz(), vehicle.getPath());
                        carDetailFragment.getDrives();
                        getFragmentManager().beginTransaction().show(carDetailFragment).addToBackStack("detailFragment").commit();
                        isFragmentActive = true;
                    }*/
                    Intent intent = new Intent(CarGalleryActivity.this, SwipeGalleryActivity.class);
                    /*Bundle extras = new Bundle();
                    for (int i = 0; i < vehicleList.size(); i++) {
                        extras.putSerializable("vehicle" + i, vehicleList.get(i));
                    }
                    extras.putInt("count", vehicleList.size());
                    extras.putInt("position",position);
                    intent.putExtras(extras);*/
                    intent.putExtra("vehicles",vehicleList);
                    intent.putExtra("position",position);
                    startActivity(intent);
                }
            });
            gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CarGalleryActivity.this);
                    builder.setMessage("Set car as favourite");
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit()
                                    .putString("sk.upjs.michal.driverdiary.favourite", vehicleList.get(position).getSpz())
                                    .apply();
                            dialog.cancel();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }
    }
}

