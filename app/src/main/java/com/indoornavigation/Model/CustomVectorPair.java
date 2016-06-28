package com.indoornavigation.Model;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import java.util.Comparator;

/**
 * Class holds a latlng coordinate and a double value.
 * When vectors are subtracted and magnified a value pair is needed to represent the data.
 */
public class CustomVectorPair implements Comparator<CustomVectorPair>, Comparable<CustomVectorPair> {

    LatLng latLng;
    Double value;

    public CustomVectorPair() {}

    public CustomVectorPair(LatLng latLng, Double value) {
        this.latLng = latLng;
        this.value = value;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public int compareTo(@NonNull CustomVectorPair another) {
        return compare(this, another);
    }

    @Override
    public int compare(CustomVectorPair lhs, CustomVectorPair rhs) {
        if (lhs.getValue() < rhs.getValue())
            return -1;
        else if (lhs.getValue() > rhs.getValue())
            return 1;

        return 0;
    }
}
