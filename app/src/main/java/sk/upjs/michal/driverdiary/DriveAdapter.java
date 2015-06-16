package sk.upjs.michal.driverdiary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Michal on 1.6.2015.
 */
public class DriveAdapter extends ArrayAdapter<Drive>{

    public DriveAdapter(Context context, ArrayList<Drive> objects) {
        super(context,R.layout.item_drive,objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       Drive drive = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_drive, parent, false);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.date_textView);
        tv.setText(drive.getDate());
        return convertView;
    }
}
