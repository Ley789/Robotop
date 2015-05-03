package com.example.alexander.robotop.movement;

import com.example.alexander.robotop.datastruct.Point;
import com.example.alexander.robotop.robotData.RobotOdometry;

/**
 * Created by Alexander on 29/04/2015.
 */
public class BallSearcher {
    private RobotOdometry odometry = RobotOdometry.getInstance();
    private RobotMovement movement = RobotMovement.getInstance();

    public void run() {
        movement.robotMoveForward(20);
        movement.robotTurn(45);
    }

    public void avoidBall(){
        Point goal = new Point(0,0);
        Point current = odometry.getPoint();
        int angle = current.degreeToPoint(goal);
        if (Math.abs(angle - odometry.getAngle()) < 20){
            movement.robotTurn(45);
            movement.robotMoveForward(50);
        }
    }
}

