package com.power.max.indoornavigation.Model;

/**
 * Class that represents a lat-lng coordinate.
 */
public class Coordinate {

    private double lat, lng;

    public Coordinate() {}

    public Coordinate(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * Constructor that accepts 2d double array.
     * @param point first entry lat, second entry lng.
     */
    public Coordinate(double[][] point) {
        if (point.length == 2) {
            this.lat = point[0][0];
            this.lng = point[1][0];
        } else {
            throw new IllegalArgumentException("Wrong array size!");
        }
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "lat: " + String.valueOf(lat) + " lng: " + String.valueOf(lng);
    }
}
