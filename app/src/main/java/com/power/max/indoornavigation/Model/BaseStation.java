package com.power.max.indoornavigation.Model;

import com.power.max.indoornavigation.Database.DbTables;

/**
 * Class that represents a base station in the database.
 */
public class BaseStation {
    private String ssid, bssid, ip, mac;
    private int channel;
    private Coordinate latLng;
    private double distance;

    public BaseStation() {}

    public BaseStation(Coordinate latLng, double distance) {
        this.latLng = latLng;
        this.distance = distance;
    }

    public BaseStation(String ssid, String bssid, String ip, String mac,
                       int channel, Coordinate latLng) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.ip = ip;
        this.mac = mac;
        this.channel = channel;
        this.latLng = latLng;
    }

    public Coordinate getLatLng() {
        return latLng;
    }

    public void setLatLng(Coordinate latLng) {
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
}
