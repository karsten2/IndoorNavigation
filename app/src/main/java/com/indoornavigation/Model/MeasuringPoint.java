package com.indoornavigation.Model;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

/**
 * Represents a measuring point.
 */
public class MeasuringPoint implements Comparable<MeasuringPoint>, Comparator<MeasuringPoint> {
    String name;
    LatLng latLng;
    int id;

    public MeasuringPoint() {}

    public MeasuringPoint(String name, LatLng latLng, int id) {
        this.name = name;
        this.latLng = latLng;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull MeasuringPoint another) {
        return compare(this, another);
    }

    @Override
    public int compare(MeasuringPoint lhs, MeasuringPoint rhs) {
        if (lhs.getId() > rhs.getId())
            return 1;
        if (lhs.getId() < rhs.getId())
            return -1;
        return 0;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
