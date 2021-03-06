package com.example.alexander.robotop.robotData;

import android.util.Log;

import com.example.alexander.robotop.datastruct.Point;

/**
 * Created by michael on 20.04.15.
 */
public class RobotOdometry {

    private final String TAG = "Odometry";
    private int x;
    private int y;
    private int angle;
    private static RobotOdometry instance;


    /**
     * robot looks along the x-axis
     */
    private RobotOdometry() {
        this.x = 0;
        this.y = 0;
        this.angle = 0;
    }

    public static RobotOdometry getInstance(){
        if(instance == null){
            instance = new RobotOdometry();
            return instance;
        }else{
            return instance;
        }
    }

    public void setCoord(int range){
        y = (int)(y + range * Math.sin(Math.toRadians(angle)));
        x = (int)(x + range * Math.cos(Math.toRadians(angle)));
        logOdometry();

    }

    public void updateAngle(int angleChange){
        this.angle += angleChange;
        this.angle = this.angle % 360;
        logOdometry();
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

    @Override
    public String toString() {
        return "RobotOdometry{" +
                "angle=" + angle +
                ", y=" + y +
                ", x=" + x +
                '}';
    }

    public Point getPoint(){
        return new Point(x, y);
    }

    public void setOdometry(int x, int y, int angle){
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
}
