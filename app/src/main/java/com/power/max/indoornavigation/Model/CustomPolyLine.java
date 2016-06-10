package com.power.max.indoornavigation.Model;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.power.max.indoornavigation.Math.DataSmoothing;

import java.util.ArrayList;

/**
 * Class to draw a Polyline on a GoogleMap object with marker as anchors.
 */
public class CustomPolyLine {

    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private GoogleMap mMap;
    private BitmapDescriptor iconAchor;
    private BitmapDescriptor iconStart;
    private MarkerOptions markerOptions;
    private PolylineOptions polylineOptions;

    public CustomPolyLine(GoogleMap mMap,
                          BitmapDescriptor iconAchor,
                          BitmapDescriptor iconStart,
                          MarkerOptions markerOptions,
                          PolylineOptions polylineOptions) {
        this.mMap = mMap;
        this.iconAchor = iconAchor;
        this.iconStart = iconStart;
        this.markerOptions = markerOptions;
        this.polylineOptions = polylineOptions;
    }

    /**
     * Function to delete a marker.
     * Redrawing Polylines.
     * Deleting marker from database, if exists.
     * Removing Marker from map.
     * @param marker to delete.
     */
    public void removeLineMarker(Marker marker) {
        marker.remove();

        // replace two polylines with one new.
        if (markers.contains(marker)) {

            if (markers.indexOf(marker) == 0 && polylines.size() > 0) {
                polylines.get(0).remove();
                polylines.remove(polylines.get(0));
                if (markers.size() > 1) markers.get(1).setIcon(iconStart);
            } else if (markers.indexOf(marker) == markers.size() - 1  && polylines.size() > 0) {
                polylines.get(polylines.size() - 1).remove();
                polylines.remove(polylines.size() - 1);
            } else if (polylines.size() > 0) {
                int firstLineIndex = markers.indexOf(marker) - 1;
                int secondLineIndex = firstLineIndex + 1;

                if (firstLineIndex >= 0 && secondLineIndex >= 0
                        && firstLineIndex < polylines.size() && secondLineIndex < polylines.size()) {

                    Polyline firstLine = polylines.get(firstLineIndex);
                    Polyline secondLine = polylines.get(secondLineIndex);

                    // remove from map.
                    firstLine.remove();
                    secondLine.remove();

                    // replace first line with new line.
                    polylines.set(firstLineIndex, mMap.addPolyline(new PolylineOptions()
                            .add(firstLine.getPoints().get(0), secondLine.getPoints().get(1))
                            .width(4.0f)));

                    // remove seconde line from array.
                    polylines.remove(secondLine);
                }
            }

            // remove marker from route.
            markers.remove(marker);
        }
    }

    /**
     * Function to redraw the polylines of a route when a route marker is dragged.
     * @param marker that is dragged.
     */
    public void updateLineMarker(Marker marker) {
        int markerPosition = markers.indexOf(marker);
        PolylineOptions newPLineOptions = new PolylineOptions().width(4);

        if (markerPosition == 0) {
            if (polylines.get(0) != null) {
                polylines.get(0).remove();
                polylines.set(0, mMap.addPolyline(newPLineOptions
                        .add(marker.getPosition(), polylines.get(0).getPoints().get(1))));
            }
        } else if (markerPosition == markers.size() - 1) {
            Polyline polyline = polylines.get(polylines.size() - 1);
            if (polyline != null) {
                polyline.remove();
                polylines.set(polylines.size() - 1, mMap.addPolyline(newPLineOptions
                        .add(polyline.getPoints().get(0), marker.getPosition())));
            }
        } else {
            int firstLineIndex = markers.indexOf(marker) - 1;
            int secondLineIndex = markers.indexOf(marker);

            if (firstLineIndex >= 0 && secondLineIndex >= 0
                    && firstLineIndex < polylines.size() && secondLineIndex < polylines.size()) {
                Polyline firstLine = polylines.get(firstLineIndex);
                firstLine.remove();
                polylines.set(firstLineIndex, mMap.addPolyline(newPLineOptions
                        .add(firstLine.getPoints().get(0), marker.getPosition())));

                Polyline secondLine = polylines.get(secondLineIndex);
                secondLine.remove();
                polylines.set(secondLineIndex, mMap.addPolyline(newPLineOptions
                        .add(marker.getPosition(), secondLine.getPoints().get(1))));
            }
        }
    }

    public void add(Marker marker) {
        PolylineOptions newPLineOptions = new PolylineOptions().width(4);
        markers.add(marker);
        if (markers.size() > 1)
            polylines.add(mMap.addPolyline(newPLineOptions
                    .add(markers.get(markers.size() - 2).getPosition(),
                            marker.getPosition())));
    }

    public Marker get(int index) {
        return this.markers.get(index);
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<Marker> markers) {
        this.markers = markers;
    }

    public void setAnchorIcon(BitmapDescriptor iconAchor) {
        this.iconAchor = iconAchor;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

    public void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
    }

    public void setMarkerIcon(Marker marker, BitmapDescriptor icon) {
        if (markers.contains(marker))
            marker.setIcon(icon);
    }

    public int size() {
        return this.markers.size();
    }

    public boolean contains(Marker marker) {
        return this.markers.contains(marker);
    }

    public void clear() {
        markers.clear();
        polylines.clear();
        mMap.clear();
    }
}
