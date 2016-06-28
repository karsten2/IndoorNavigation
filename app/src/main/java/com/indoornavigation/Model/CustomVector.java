package com.indoornavigation.Model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Class that holds a coordinate and n vector values.
 */
public class CustomVector {
    LatLng latLng;
    ArrayList<Double> values = new ArrayList<>();

    public CustomVector() {}

    public CustomVector(LatLng latLng, ArrayList<Double> values) {
        this.latLng = latLng;
        this.values = values;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public void setValues(ArrayList<Double> values) {
        this.values = values;
    }

    public void add (Double value) { this.values.add(value); }
}
