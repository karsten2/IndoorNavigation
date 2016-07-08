package com.indoornavigation.Model;

import android.content.ContentValues;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.indoornavigation.Database.DbTables;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Class that represents a base station in the database.
 */
public class BaseStation implements Serializable, Comparator<BaseStation>, Comparable<BaseStation> {
    private String ssid, bssid, ip, mac;

    private int channel;
    private LatLng latLng;
    private double distance;
    private double rssi;
    private String timeStamp;
    private int dbId;

    private double rss1_1m;
    private double lat_const;

    private static final String TAG = "BaseStation";

    public BaseStation() {}

    public BaseStation(ScanResult scanResult) {
        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.mac = scanResult.BSSID;
    }

    public BaseStation(ScanResult scanResult, LatLng latLng) {
        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.mac = scanResult.BSSID;
        this.latLng = latLng;
    }

    public BaseStation(String ssid, double rssi) {
        this.ssid = ssid;
        this.rssi = rssi;
    }

    public BaseStation(LatLng latLng, double distance, String timeStamp) {
        this.latLng = latLng;
        this.distance = distance;
        this.timeStamp = timeStamp;
    }

    public BaseStation(String ssid,
                       String bssid,
                       String ip,
                       String mac,
                       int channel,
                       LatLng latLng) {

        this.ssid = ssid;
        this.bssid = bssid;
        this.ip = ip;
        this.mac = mac;
        this.channel = channel;
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public double getRss1_1m() {
        return rss1_1m;
    }

    public void setRss1_1m(double rss1_1m) {
        this.rss1_1m = rss1_1m;
    }

    public double getLat_const() {
        return lat_const;
    }

    public void setLat_const(double lat_const) {
        this.lat_const = lat_const;
    }

    /**
     * Function to convert the class' values into a key->value set.
     * Required for Sqlite Database insert.
     * @return Key value pairs of type {@see ContentValues}.
     */
    public ContentValues toDbValues() {
        ContentValues ret = new ContentValues();

        ret.put(DbTables.BaseStation.COL_SSID, this.ssid);
        ret.put(DbTables.BaseStation.COL_BSSID, this.bssid);
        ret.put(DbTables.BaseStation.COL_LAT, this.latLng.latitude);
        ret.put(DbTables.BaseStation.COL_LNG, this.latLng.longitude);

        return ret;
    }

    @Override
    public String toString() {
        return timeStamp + ";" + ssid;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o instanceof BaseStation) {
            BaseStation toCompare = (BaseStation) o;
            if (this.ssid.equals(toCompare.getSsid()))
                return true;
        }
        if (o instanceof String) {
            if (this.ssid.equals(o.toString()))
                return true;
        }

        return false;
    }

    @Override
    public int compare(BaseStation lhs, BaseStation rhs) {
        if (lhs.getDbId() < rhs.getDbId())
            return -1;
        if (lhs.getDbId() > rhs.getDbId())
            return 1;

        return 0;
    }

    @Override
    public int compareTo(@NonNull BaseStation another) {
        return compare(this, another);
    }
}
