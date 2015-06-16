package sk.upjs.michal.driverdiary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michal on 28.5.2015.
 */
public class VehicleAdapter extends ArrayAdapter<Vehicle> {

    public VehicleAdapter(Context context, ArrayList<Vehicle> objects) {
        super(context, R.layout.item_vehicle, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Vehicle vehicle = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_vehicle, parent, false);
        }
        ImageView iv = (ImageView) convertView.findViewById(R.id.car_image);

        TextView tv = (TextView) convertView.findViewById(R.id.spz_gallery_textView);
        tv.setText(vehicle.getSpz());
        if (!vehicle.getPath().equalsIgnoreCase("no photo available")) {
            File imgFile = new File(vehicle.getPath());
            if (imgFile.exists()) {
                try {
                    ExifInterface exifInterface = new ExifInterface(imgFile.getAbsolutePath());
                    if (exifInterface.getThumbnail() != null) {
                        Bitmap myBitmap = BitmapFactory.decodeByteArray(exifInterface.getThumbnail(), 0, exifInterface.getThumbnail().length);
                        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                        iv.setImageBitmap(rotateBitmap(myBitmap, orientation));
                    } else {
                        iv.setImageResource(R.drawable.no_image_available);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            iv.setImageResource(R.drawable.no_image_available);
        }

        return convertView;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
