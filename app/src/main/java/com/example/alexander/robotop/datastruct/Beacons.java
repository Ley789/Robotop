package com.example.alexander.robotop.datastruct;

import com.example.alexander.robotop.visualOrientation.Beacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander on 11/05/2015.
 */
public class Beacons {
    private static Beacons beacons;
    private List<Beacon> beaconList = new ArrayList<>();

    private Beacons(){};

    public void add(Beacon beacon){
        beaconList.add(beacon);
    }

    public Beacon getBeacon(int i){
        if(i >= beaconList.size())
            return null;

        return beaconList.get(i);
    }

    public void reset(){
        beaconList.clear();
    }

    public static Beacons getInstance(){
        if(beacons == null){
            beacons = new Beacons();
        }
        return beacons;
    }
}
