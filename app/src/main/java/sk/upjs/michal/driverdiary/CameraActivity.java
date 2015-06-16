package sk.upjs.michal.driverdiary;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CameraActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private String spz;
    private String picturePath;
    private Button okButton;
    private Button declineButton;
    private Button captureButton;
    private boolean photoSaved = false;
    private boolean isTablet;


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    private byte[] data;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            setData(data);
            okButton.setVisibility(View.VISIBLE);
            declineButton.setVisibility(View.VISIBLE);
            captureButton.setVisibility(View.INVISIBLE);


        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) { //http://developer.android.com/guide/topics/media/camera.html
        isTablet = getResources().getBoolean(R.bool.isTablet);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getSupportActionBar().hide();
        Bundle extras = getIntent().getExtras();
        spz = extras.getString("SPZ");
        Log.e("cesta", spz);
        // Create an instance of Camera
        mCamera = getCameraInstance();
        changeLayout();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        //final FrameLayout preview2 = (FrameLayout) findViewById(R.id.camera_preview2);
        //preview2.addView(mPreview);
        //preview2.setVisibility(View.INVISIBLE);
        //changeLayout();
        captureButton = (Button) findViewById(R.id.button_capture);
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setVisibility(View.INVISIBLE);
        declineButton = (Button) findViewById(R.id.decline_button);
        declineButton.setVisibility(View.INVISIBLE);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCamera.takePicture(null, null, mPicture);
                //preview.setVisibility(View.INVISIBLE);
                //preview.removeAllViews();
                /*preview2.addView(mPreview);
                preview2.setVisibility(View.VISIBLE);*/
                //changeLayout();

            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File pictureFile = getOutputMediaFile(1);
                Log.e("cesta:", picturePath);
                if (pictureFile == null) {
                    return;
                }

                try {
                    if (!photoSaved) {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                        photoSaved = true;
                    }
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //koli tomu aby galeria prescanovala subory a nacitala nove foto
                    mediaScanIntent.setData(Uri.parse(picturePath));
                    sendBroadcast(mediaScanIntent);
                    Intent intent = new Intent();
                    intent.putExtra("path", picturePath);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });


        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okButton.setVisibility(View.INVISIBLE);
                declineButton.setVisibility(View.INVISIBLE);
                captureButton.setVisibility(View.VISIBLE);
                mPreview = new CameraPreview(CameraActivity.this, mCamera);
                preview.removeAllViews();
                preview.addView(mPreview);
                //preview2.removeAllViews();
                /*preview2.addView(mPreview);*/
                //preview2.setVisibility(View.INVISIBLE);
                preview.setVisibility(View.VISIBLE);
                //mCamera.startPreview();
            }
        });
        mCamera.startPreview();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
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

    /**
     * A safe way to get an instance of the Camera object. http://developer.android.com/guide/topics/media/camera.html
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
            // attempt to get a Camera instance
            Camera.Parameters params = c.getParameters();
            params.setAutoWhiteBalanceLock(true);
            Camera.Size size = params.getPictureSize();
            params.setPictureSize(1000, 1000);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setJpegQuality(50);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            for (Camera.Size size1 : sizes) {
                Log.e("cesta", size1.height + "x" + size1.width);
            }
            List<String> flashModes = params.getSupportedFlashModes();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            int width = displaymetrics.widthPixels;
            int height = displaymetrics.heightPixels;
            params.setPreviewSize(size.height, size.height);

            params.setRotation(90);
            setCameraDisplayOrientation(CameraActivity.this, 0, c);
            c.setParameters(params);

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) { //pouzite z http://developer.android.com/guide/topics/media/camera.html#saving-media
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Driver Diary"/*Context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)*/);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("blah", "returning null");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + spz + ".jpg");
            picturePath = mediaFile.getPath();
        } else if (type == 2) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + spz + ".mp4");
            picturePath = mediaFile.getPath();
        } else {
            return null;
        }

        return mediaFile;
    }

    private Uri getOutputMediaFileUri(int type) { //pouzite z http://developer.android.com/guide/topics/media/camera.html#saving-media
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public void setCameraDisplayOrientation(ActionBarActivity activity,
                                            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mCamera.release();
    }

    public void changeLayout() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.camera_lienar_layout);
        ViewGroup.LayoutParams lp = ll.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int width = displaymetrics.widthPixels;
        lp.height = width;
        ll.setLayoutParams(lp);
    }
}
