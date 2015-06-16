package sk.upjs.michal.driverdiary;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CarDetailFragment extends Fragment {

    private ImageView iv;
    private TextView nameTextView;
    private TextView spzTextView;
    private ListView lv;
    private DriveAdapter adapter;
    private ArrayList<Drive> drives;
    private SharedPreferences prefs;
    private String path;
    File pictureFile;
    private boolean isTablet;
    private Button addRecordButton;
    private Button shareButton;
    public String getSpz() {
        return spz;
    }
    private Vehicle vehicle;

    private String spz;
    public CarDetailFragment() {
        // Required empty public constructor
    }

    public void setDetails(String brand, String model, String spz, String path) {
        if (nameTextView == null) {
            Log.e("nullTest", "je null");
        }
        nameTextView.setText(brand + " " + model);
        this.spz = spz;
        spzTextView.setText(spz);
        pictureFile = new File(path);
        if (!path.equalsIgnoreCase("no photo available")) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            this.path = path;
            ExifInterface exifInterface = null;
            try {
                exifInterface = new ExifInterface(pictureFile.getAbsolutePath());
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                iv.setImageDrawable(new BitmapDrawable(VehicleAdapter.rotateBitmap(bitmap,orientation)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            iv.setImageResource(R.drawable.no_image_available);
        }

    }

    public void getDrives (){
        drives = new ArrayList<Drive>();
        if (prefs.getInt("sk.upjs.michal.driverdiary."+spzTextView.getText().toString() +".count",0) != 0){
            Log.e("prefs","naslo daco");
            int count = prefs.getInt("sk.upjs.michal.driverdiary."+spzTextView.getText().toString() +".count",0);
            for (int i = 1; i <= count; i++){
                String json = prefs.getString("sk.upjs.michal.driverdiary."+spzTextView.getText().toString() +".drive" + i,null);
                Log.e("prefs", json);
                try {
                    JSONObject driveJSON = new JSONObject(json);
                    Drive drive = Drive.driveFromJSON(driveJSON);
                    drives.add(drive);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            adapter = new DriveAdapter(getActivity(),drives);
            lv.setAdapter(adapter);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isTablet = getResources().getBoolean(R.bool.isTablet);
        View rootView = inflater.inflate(R.layout.fragment_car_detail, container, false);
        iv = (ImageView) rootView.findViewById(R.id.car_imageView);
        nameTextView = (TextView) rootView.findViewById(R.id.name_textView);
        spzTextView = (TextView) rootView.findViewById(R.id.spz_textView);
        prefs = getActivity().getSharedPreferences("sk.upjs.michal.driverdiary", Context.MODE_PRIVATE);

        lv = (ListView) rootView.findViewById(R.id.drives_listView);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        iv.setAdjustViewBounds(true);
        if (!isTablet) {
            iv.setMaxWidth(displaymetrics.widthPixels - (displaymetrics.widthPixels / 5));
            iv.setMaxHeight(displaymetrics.widthPixels - (displaymetrics.widthPixels / 5));
        } else {
            iv.setMaxWidth(displaymetrics.heightPixels - (displaymetrics.heightPixels / 2));
            iv.setMaxHeight(displaymetrics.heightPixels - (displaymetrics.heightPixels / 2));
        }


        /*addRecordButton = (Button) rootView.findViewById(R.id.add_drive_button);
        addRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddRecordActivity.class);
                intent.putExtra("SPZ", spzTextView.getText().toString());
                startActivityForResult(intent, 3);
            }
        });

        shareButton = (Button) rootView.findViewById(R.id.share_car_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                Uri uri = Uri.fromFile(pictureFile);
                Log.e("uri", uri.toString());
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, nameTextView.getText().toString());
                if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(shareIntent);
                }
            }
        });*/
        /*if (isTablet) {
            iv.setImageResource(R.drawable.no_image_available);
            addRecordButton.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);
            nameTextView.setText(getResources().getString(R.string.no_selected_vehicle));
        }*/

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),MapsActivity.class);
                Drive drive = drives.get(position);
                intent.putExtra("drive",drive);
                startActivity(intent);
            }
        });
        vehicle = (Vehicle) getArguments().getSerializable("vehicle");
        setDetails(vehicle.getBrand(),vehicle.getModel(),vehicle.getSpz(),vehicle.getPath());
        getDrives();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3){
            if (resultCode == getActivity().RESULT_OK){
                Drive drive =(Drive) data.getSerializableExtra("Drive");
                addDrive(drive);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("pustil sa", "true");
    }

    public void addDrive (Drive drive){
        drives.add(drive);
        adapter = new DriveAdapter(getActivity(),drives);
        lv.setAdapter(adapter);
    }

    public void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.fromFile(pictureFile);
        Log.e("uri", uri.toString());
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, nameTextView.getText().toString());
        if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(shareIntent);
        }
    }

    public void addRecord() {
        Intent intent = new Intent(getActivity(), AddRecordActivity.class);
        intent.putExtra("SPZ", spzTextView.getText().toString());
        startActivityForResult(intent, 3);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
