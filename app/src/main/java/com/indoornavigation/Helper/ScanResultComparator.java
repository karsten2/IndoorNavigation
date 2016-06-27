package com.indoornavigation.Helper;

import android.net.wifi.ScanResult;

import java.util.Comparator;

/**
 * Class to compare wifi scan results.
 */
public class ScanResultComparator implements Comparator<ScanResult> {
    @Override
    public int compare(ScanResult lhs, ScanResult rhs) {
        if (lhs.level < rhs.level)
            return 1;
        else if (lhs.level > rhs.level)
            return -1;

        return 0;
    }
}
