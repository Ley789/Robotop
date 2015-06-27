package com.example.alexander.robotop.datastruct;

import com.example.alexander.robotop.visualOrientation.Beacon;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander on 11/05/2015.
 */
public class Beacons {

    //value as cm, starting at the corner at (125,125) and going clockwise
    private final static Point[] position = {new Point(125,125), new Point(125,0),new Point(125,-125),
                                             new Point(0,-125), new Point(-125,-125),
                                             new Point(-125,0), new Point(-125,125), new Point(0,125)};
    //mapping indexes
    //convention red = 0, yellow =1, with=2, blue=3
    private final static Pair[] colorIndex = {new Pair(0,1), new Pair(2,0), new Pair(1,0),
                                              new Pair(0,3), new Pair(1,3),
                                              new Pair(2,3), new Pair(3,1), new Pair(3,0)};

    private static Beacons beacons;

    private List<Beacon> beaconList = new ArrayList<>();

    private Beacons(){
        for(int i = 0; i < position.length;i++){
            beaconList.add(new Beacon(position[i],colorIndex[i]));
        }
    };
    public static Beacons getInstance(){
        if(beacons == null){
            beacons = new Beacons();
        }
        return beacons;
    }


    public Beacon getBeacon(int i){
        return beaconList.get(i);
    }

    public int size(){
        return beaconList.size();
    }

}
