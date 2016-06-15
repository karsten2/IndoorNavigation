package com.indoornavigation.Model;

import android.content.ContentValues;
import android.net.wifi.ScanResult;

import com.google.android.gms.maps.model.LatLng;
import com.indoornavigation.Database.DbTables;

import java.io.Serializable;

/**
 * Class that represents a base station in the database.
 */
public class BaseStation implements Serializable {
    private String ssid, bssid, ip, mac;
    private int channel;
    private LatLng latLng;
    private double distance;
    private String timeStamp;

    private static final String TAG = "BaseStation";

    public BaseStation() {}

    public BaseStation(ScanResult scanResult, LatLng latLng) {
        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.mac = scanResult.BSSID;
        this.latLng = latLng;
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

    /**
     * Function to convert the class' values into a key->value set.
     * Required for Sqlite Database insert.
     * @return Key value pairs of type {@see ContentValues}.
     */
    public ContentValues toDbValues() {
        ContentValues ret = new ContentValues();

        ret.put(DbTables.RadioMap.COL_SSID, this.ssid);
        ret.put(DbTables.RadioMap.COL_BSSID, this.bssid);
        ret.put(DbTables.RadioMap.COL_LAT, this.latLng.latitude);
        ret.put(DbTables.RadioMap.COL_LNG, this.latLng.longitude);

        return ret;
    }

    @Override
    public String toString() {
        return timeStamp + ";" + ssid + ";" + distance;
    }
}