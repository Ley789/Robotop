package com.example.alexander.robotop.bugAlgorithms;
import android.util.Log;

import com.example.alexander.robotop.communication.Data;
import com.example.alexander.robotop.datastruct.Point;
import com.example.alexander.robotop.movement.RobotMovement;
import com.example.alexander.robotop.movement.Sensor;
import com.example.alexander.robotop.robotData.RobotOdometry;

/**
 * Created by Alexander on 20/04/2015.
 */
public class Bug0Alg {
    private static String TAG ="BUG";


    private static int aimSens=15;
    private static int midSensorSens = 50;
    private static int length = 40;
    private static int turnSens = 90;

    private RobotOdometry odometry;
    private RobotMovement move;

    public Bug0Alg(){
        odometry = RobotOdometry.getInstance();
        move = RobotMovement.getInstance();
    }
    //Brute force tries to get to the goal and push away obstacles
    public boolean forcedBug0(Point goal){
        move.robotMoveForward(goal.getX());
        move.robotTurn(90);
        move.robotMoveForward(goal.getY());
        return true;
    }

    public boolean bug0(Point goal){
        Point currentPosition = odometry.getPoint();
        boolean done = false;
        do {
            if (move.robotMoveForward(currentPosition.coordinateDifferenzX(goal)) > aimSens) {
                if (!avoidObstacle(goal)) {
                    return false;
                }
            }
            currentPosition = odometry.getPoint();
            move.robotTurn(90 - odometry.getAngle());
            if (move.robotMoveForward(currentPosition.coordinateDifferenzY(goal)) > aimSens) {
                if (!avoidObstacle(goal)) {
                    return false;
                }
            }
            currentPosition = odometry.getPoint();
            done = currentPosition.equals(goal);
        }while(done);
        return true;
    }


    public boolean bug0d(Point goal){
        Point currentPosition = odometry.getPoint();
        boolean done = true;
        int degreeAim;
        int distance;
        do{
            degreeAim = currentPosition.degreeToPoint(goal);
            distance = currentPosition.distance(goal);
            //we need to adjust the robot to the calculated degree
            //we do that by subtracting his current angle from the degree he needs to turn
            move.robotTurn(degreeAim - odometry.getAngle());

            if (move.robotMoveForward(currentPosition.distance(goal)) > aimSens) {
                if (!avoidObstacle(goal)) {
                    return false;
                }
            }
            currentPosition = odometry.getPoint();

            Log.d(TAG, "degree: " + degreeAim + "degree turned: " + (degreeAim - odometry.getAngle()) +" distance: " + distance);
            Log.d(TAG,"position now: " + currentPosition.toString() +" goal is: " + goal.toString());
            done = currentPosition.equals(goal);
            Log.d(TAG,"done: " + done);
        }while(!done);
        return true;
    }

    //Todo add point control to check if bug fails
    public boolean avoidObstacle(Point goal){
        Sensor update = Data.getSensorData();
        boolean avoided = true;
        boolean deathEnd;
        Point nearest = odometry.getPoint();
        Point current;
        do {
            if (!turnLeft()) {
                return false;
            }
            avoided = goForward();

            current = odometry.getPoint();
            deathEnd = nearest.equals(current);
            if(deathEnd){
                return false;
            }else{
                if(nearest.distance(goal) > current.distance(goal)){
                    nearest = current;
                }
            }
            Log.d(TAG, " avoided is :" + avoided);
        }while(!avoided);
        return avoided;
    }

    /**The Robot turn 90 degree left till he can go forward
     *
     * @return true if succeeded else failed
     */
    private boolean turnLeft(){
        int counter=0;
        Sensor update = Data.getSensorData();
        while(update.getMid() < midSensorSens){
            move.robotTurn(turnSens);
            if(counter > 4){
                //we made 360 grad turns so no way out
                return false;
            }
            update = Data.getSensorData();
            counter++;
        }

        move.robotMoveForward(midSensorSens -10);
        return true;
    }

    private boolean goForward(){
        move.robotTurn(-turnSens);
        Sensor update = Data.getSensorData();
        while(update.getMid() < midSensorSens){
            move.robotTurn(turnSens);
            if(move.robotMoveForward(length) > 15){
                return false;
            }
            move.robotTurn(-turnSens);
            update = Data.getSensorData();
        }
        move.robotMoveForward(midSensorSens -10);
        return true;
    }
}
