package com.example.alexander.robotop.bugAlgorithms;
import com.example.alexander.robotop.communication.Data;
import com.example.alexander.robotop.datastruct.Point;
import com.example.alexander.robotop.movement.RobotMovement;
import com.example.alexander.robotop.movement.Sensor;
import com.example.alexander.robotop.robotData.RobotOdometry;

/**
 * Created by Alexander on 20/04/2015.
 */
public class Bug0Alg {
    private static int aimSens=10;
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



    //Todo add point control to check if bug fails
    private boolean avoidObstacle(Point goal){
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
        }while(avoided);
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
            if(move.robotMoveForward(length) > 10){
                return false;
            }
            move.robotTurn(-turnSens);
            update = Data.getSensorData();
        }
        move.robotMoveForward(midSensorSens -10);
        return true;
    }
}
