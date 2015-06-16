package sk.upjs.michal.driverdiary;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Michal on 1.6.2015.
 */
public class Drive implements Serializable {
    private int seconds;
    private String date;

    public ArrayList<double[]> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<double[]> coordinates) {
        this.coordinates = coordinates;
    }

    private ArrayList<double[]> coordinates;
  
    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static Drive driveFromJSON (JSONObject driveJSON){
       Drive drive = new Drive();
        try {
            drive.setDate(driveJSON.getString("date"));
            drive.setSeconds(driveJSON.getInt("duration"));
            ArrayList<double[]> location = new ArrayList<>();
            for (int i = 0; i<driveJSON.getInt("count"); i++) {
                double[] latlng = new double[2];
                latlng[0] = driveJSON.getDouble("lat"+i);
                latlng[1] = driveJSON.getDouble("lng"+i);
                location.add(latlng);
            }
            drive.setCoordinates(location);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return drive;
    }

  
}
