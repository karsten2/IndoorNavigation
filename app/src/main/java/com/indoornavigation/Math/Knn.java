package com.indoornavigation.Math;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Andreas Thiele
 * http://thiele.nu/attachments/article/3/NearestNeighbour.java
 *
 * An implementation of knn.
 * Uses Euclidean distance weighted by 1/distance
 *
 * Main method to classify if entry is male or female based on:
 * Height, weight
 */
public class Knn {
    public static void main(){
        ArrayList<Knn.DataEntry> data = new ArrayList<>();
        data.add(new DataEntry(new double[]{175,80}, "Male"));
        data.add(new DataEntry(new double[]{193.5,110}, "Male"));
        data.add(new DataEntry(new double[]{183,92.8}, "Male"));
        data.add(new DataEntry(new double[]{160,60}, "Male"));
        data.add(new DataEntry(new double[]{177,73.1}, "Male"));
        data.add(new DataEntry(new double[]{175,80}, "Female"));
        data.add(new DataEntry(new double[]{150,55}, "Female"));
        data.add(new DataEntry(new double[]{159,63.2}, "Female"));
        data.add(new DataEntry(new double[]{180,70}, "Female"));
        data.add(new DataEntry(new double[]{163,110}, "Female"));
        Knn nn = new Knn(data, 3); //3 neighbours
        //Log.d("KNN", "Classified as: "+nn.classify(new DataEntry(new double[]{170, 60},"Ignore")));
    }


    private int k;
    private ArrayList<DataEntry> dataSet;
    /**
     *
     * @param dataSet The set
     * @param k The number of neighbours to use
     */
    public Knn(ArrayList<DataEntry> dataSet, int k){
        ArrayList<Object> classes = new ArrayList<>();
        this.k = k;
        this.dataSet = dataSet;

        //Load different classes
        for(DataEntry entry : dataSet){
            if(!classes.contains(entry.getY())) classes.add(entry.getY());
        }
    }

    private DataEntry[] getNearestNeighbourType(DataEntry x){
        DataEntry[] retur = new DataEntry[this.k];
        double fjernest = Double.MIN_VALUE;
        int index = 0;
        for(DataEntry tse : this.dataSet){
            double distance = distance(x,tse);
            if(retur[retur.length-1] == null){ //Hvis ikke fyldt
                int j = 0;
                while(j < retur.length){
                    if(retur[j] == null){
                        retur[j] = tse; break;
                    }
                    j++;
                }
                if(distance > fjernest){
                    index = j;
                    fjernest = distance;
                }
            }
            else{
                if(distance < fjernest){
                    retur[index] = tse;
                    double f = 0.0;
                    int ind = 0;
                    for(int j = 0; j < retur.length; j++){
                        double dt = distance(retur[j],x);
                        if(dt > f){
                            f = dt;
                            ind = j;
                        }
                    }
                    fjernest = f;
                    index = ind;
                }
            }
        }
        return retur;
    }

    private static double convertDistance(double d){
        return 1.0/d;
    }

    /**
     * Computes Euclidean distance
     * @param a From
     * @param b To
     * @return Distance
     */
    public static double distance(DataEntry a, DataEntry b){
        double distance = 0.0;
        int length = a.getX().length;
        for(int i = 0; i < length; i++){
            double t = a.getX()[i]-b.getX()[i];
            distance = distance+t*t;
        }
        return Math.sqrt(distance);
    }
    /**
     *
     * @param e Entry to be classifies
     * @return The class of the most probable class
     */
    public Object classify(DataEntry e){
        HashMap<Object,Double> classcount = new HashMap<>();
        DataEntry[] de = this.getNearestNeighbourType(e);
        for (DataEntry aDe : de) {
            double distance = Knn.convertDistance(Knn.distance(aDe, e));
            if (!classcount.containsKey(aDe.getY())) {
                classcount.put(aDe.getY(), distance);
            } else {
                classcount.put(aDe.getY(), classcount.get(aDe.getY()) + distance);
            }
        }
        //Find right choice
        Object o = null;
        double max = 0;
        for(Object ob : classcount.keySet()){
            if(classcount.get(ob) > max){
                max = classcount.get(ob);
                o = ob;
            }
        }

        return o;
    }

    public static class DataEntry{
        private double[] x;
        private Object y;

        public DataEntry(double[] x, Object y){
            this.x = x;
            this.y = y;
        }

        public double[] getX(){
            return this.x;
        }

        public Object getY(){
            return this.y;
        }
    }
}