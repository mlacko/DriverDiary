package sk.upjs.michal.driverdiary;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Michal on 28.5.2015.
 */
public class Vehicle implements Serializable {
    private String spz;
    private String brand;
    private String model;
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSpz() {
        return spz;
    }

    public void setSpz(String spz) {
        this.spz = spz;
    }

    public static Vehicle vehicleFromJSON (JSONObject carJSON){
        Vehicle vehicle = new Vehicle();
        try {
            vehicle.setBrand(carJSON.getString("brand"));
            vehicle.setModel(carJSON.getString("model"));
            vehicle.setPath(carJSON.getString("img_resource"));
            vehicle.setSpz(carJSON.getString("spz"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vehicle;
    }
}
