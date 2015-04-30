package com.example.alexander.robotop.robotData;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 29.04.15.
 */
public class RobotTracker implements Runnable {
    private List<Point> track = new ArrayList<>();
    private RobotOdometry odometry = RobotOdometry.getInstance();
    boolean running = true;

    @Override
    public void run() {
        track.add(new Point(0,0));
        int x = 0;
        int y = 0;
        while(running) {
            int cnt = 5;
            x = x + cnt;
            y = y + cnt;
            //int x = odometry.getX();
            //int y = odometry.getY();
            Point pt = new Point(x, y);
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

    public List<Point> getTrack(){
        return track;
    }

}
