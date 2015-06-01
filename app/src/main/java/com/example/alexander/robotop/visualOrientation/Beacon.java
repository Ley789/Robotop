package com.example.alexander.robotop.visualOrientation;

import android.util.Log;

import com.example.alexander.robotop.datastruct.Beacons;
import com.example.alexander.robotop.datastruct.MassCenter;
import com.example.alexander.robotop.datastruct.Pair;
import com.example.alexander.robotop.datastruct.Point;

import org.opencv.core.Rect;

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
    public void searchBeaconPoint() {
        List<List<Rect>> massCenter = MassCenter.getInstance().getMassRect();
        if (massCenter.size() <= this.centerIndex.second || massCenter.size() <= this.centerIndex.first) {
            this.relativeCoordinate = null;
            return;
        }
        Log.d(TAG, "" + massCenter.size());
        Rect r = filterData(massCenter.get(centerIndex.first)
                , massCenter.get(centerIndex.second));
        if (r != null) {
            this.relativeCoordinate = new org.opencv.core.Point(r.x + r.width / 2, r.y + r.height);

        } else {
            this.relativeCoordinate = null;
        }
    }

    private Rect filterData(List<Rect> first, List<Rect> second){
        if(first == null || second == null)
            return null;
        org.opencv.core.Point f = new org.opencv.core.Point();
        org.opencv.core.Point s = new org.opencv.core.Point();
        for(Rect fRect : first){
            f.x = (fRect.x + fRect.width/2);
            f.y = (fRect.y + fRect.height/2);
            for(Rect sRect: second){
                s.x = (sRect.x + sRect.width/2);
                s.y = (sRect.y + sRect.height/2);
               // Log.d(TAG, " first : "+ f.toString() + " || second: " + s.toString());
                if(f.y < s.y && (f.x - s.x < 10)){

                    return sRect;
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
