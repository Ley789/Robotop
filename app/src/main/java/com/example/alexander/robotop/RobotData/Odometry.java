package com.example.alexander.robotop.RobotData;

import android.util.Log;

/**
 * Created by michael on 20.04.15.
 */
public class Odometry {

    private final String TAG = "Odometry";
    private int x;
    private int y;
    private int angle;

    /**
     * robot looks along the x-axis
     */
    public Odometry() {
        this.x = 0;
        this.y = 0;
        this.angle = 0;
    }

    public void setCoord(int range){
        y = (int)(y + range * Math.sin(angle));
        x = (int)(x + range * Math.cos(angle));
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public void logOdometry(){
        Log.d(TAG, "x: " + x + " | y: " + y + " | angle: " + angle);
    }
}
