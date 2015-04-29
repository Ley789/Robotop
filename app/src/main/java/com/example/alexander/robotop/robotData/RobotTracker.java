package com.example.alexander.robotop.robotData;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 29.04.15.
 */
public class RobotTracker implements Runnable {
    private List<MatOfPoint> track = new ArrayList<>();
    private RobotOdometry odometry = RobotOdometry.getInstance();
    boolean running = true;

    @Override
    public void run() {
        while(running) {
            int x = odometry.getX();
            int y = odometry.getY();
            MatOfPoint pt = new MatOfPoint(new Point(x, y));
            track.add(pt);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        running = false;
    }

    public List<MatOfPoint> getTrack(){
        return track;
    }

}
