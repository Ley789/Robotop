package com.example.alexander.robotop.visualOrientation;

import android.util.Log;

import com.example.alexander.robotop.datastruct.Beacons;
import com.example.alexander.robotop.datastruct.MassCenter;
import com.example.alexander.robotop.datastruct.Pair;
import com.example.alexander.robotop.datastruct.Point;

import java.util.List;

/**
 * Created by Alexander on 11/05/2015.
 */
public class Beacon {
    private static String TAG = "Beacon";
    private static int instance = 0;

    private int id;
    private Point worldCoordinate;
    private org.opencv.core.Point relativeCoordinate = null;
    public Pair centerIndex;


    /**
     *
     * @param world
     * @param colorIndex must be a pair of 2 int, the first color index is over the second
     */
    public Beacon(Point world, Pair colorIndex){
        id = instance++;
        this.centerIndex = colorIndex;
        worldCoordinate = world;
    }


    //do not use this function, it will be called from calculateMassPoint
    public void searchBeaconPoint(){
        List<List<org.opencv.core.Point>> massCenter = MassCenter.getInstance().getMassCenter();
        if(massCenter.size() <= this.centerIndex.second || massCenter.size() <= this.centerIndex.first){
            this.relativeCoordinate = null;
            return;
        }
        Log.d(TAG,""+massCenter.size());
        this.relativeCoordinate = filterData(massCenter.get(centerIndex.first)
        ,massCenter.get(centerIndex.second));
    }

    private org.opencv.core.Point filterData(List<org.opencv.core.Point> first, List<org.opencv.core.Point> second){
        if(first == null || second == null)
            return null;
        for(org.opencv.core.Point f : first){
            for(org.opencv.core.Point s: second){
               // Log.d(TAG, " first : "+ f.toString() + " || second: " + s.toString());
                if(f.y < s.y && (f.x - s.x < 5)){

                    return new org.opencv.core.Point(f.x, (s.y - f.y)/2 + f.y);
                }
            }
        }
        return null;
    }
    public void setWorldCoordinate(Point location){
        worldCoordinate = location;
    }
    public Point getWorldCoordinate(){
        return worldCoordinate;
    }
    public org.opencv.core.Point getRelativeCoordinate(){
        return relativeCoordinate;
    }
    public int getId(){
        return id;
    }
}
