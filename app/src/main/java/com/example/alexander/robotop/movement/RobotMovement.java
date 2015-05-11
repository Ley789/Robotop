package com.example.alexander.robotop.movement;


import android.util.Log;

import com.example.alexander.robotop.communication.Data;
import com.example.alexander.robotop.datastruct.Point;
import com.example.alexander.robotop.modell.NoSpaceToMoveException;
import com.example.alexander.robotop.robotData.RobotOdometry;

import static com.example.alexander.robotop.communication.Connection.comReadWrite;


/**
 *
 * @author Alexander
 * This class will implement some utility methods to control the movement of the robot
 */
public class RobotMovement {

    //Static variable to set how often the sensor should be adjusted

    private double adjustMovement = 1.42;
    private double adjustTurn=1.135;
    private RobotOdometry robotOd;
    private static RobotMovement instance;
    private static int maxDrive = 80;

    private RobotMovement(){
        robotOd = RobotOdometry.getInstance();
    }

    public static RobotMovement getInstance(){
        if(instance == null){
            instance = new RobotMovement();
            return instance;
        }else{
            return instance;
        }
    }


    private void driveFreeSpace(int distance_cm) throws NoSpaceToMoveException{
        Sensor data = Data.getSensorData();

        Log.d("FreeSpace",""+data.getMid() + "Expected: "+ distance_cm);
        if(!freeSpaceToMove(data,distance_cm)){
            throw new NoSpaceToMoveException("no free space");
        }
        robotDrive(distance_cm);
    }


    //approaches obstacle, but leaves 10cm security-distance. If range out of sensors, exception.
    //Returns moved distance.
    private int approach(int distance_cm)   {

        if(distance_cm<10){
            return 0;
        }
        Sensor data = Data.getSensorData();
        distance_cm= (data.getMid())-15;
        robotDrive(distance_cm);

        return distance_cm;
    }

    //Drives distances bigger than 80cm.
    //Returns difference between moved distance and expected distance.
    //TODO check right and left sensor...if around 10, need to test and adjust stop
    public int robotMoveForward(int distance_cm_input){
        if(distance_cm_input==0){
            return 0;
        }
        int distance_cm=distance_cm_input;
        double si =Math.signum(distance_cm_input);
        if (si<0){
            robotTurn(180);
            distance_cm=distance_cm*(-1);
        }
        int restDistanceToDrive = distance_cm;
        int steps = distance_cm / maxDrive;
        int difference = distance_cm - (steps * maxDrive);
        try {
            do{
                if(steps == 0){
                    driveFreeSpace(difference);
                    restDistanceToDrive -= difference;
                }else{
                    driveFreeSpace(maxDrive);
                    restDistanceToDrive -= maxDrive;
                }
                steps--;
            }while(steps >= 0);
        } catch (NoSpaceToMoveException e) {
            restDistanceToDrive -= approach(restDistanceToDrive);
        }
        return restDistanceToDrive;
    }
    public int robotMoveBlindForward(int distance_cm_input){
        if(distance_cm_input==0){
            return 0;
        }
        int distance_cm=distance_cm_input;
        double si =Math.signum(distance_cm_input);
        if (si<0){
            robotTurn(180);
            distance_cm=distance_cm*(-1);
        }
        int restDistanceToDrive = distance_cm;
        int steps = distance_cm / maxDrive;
        int difference = distance_cm - (steps * maxDrive);
            do{
                if(steps == 0){
                    robotDrive(difference);
                    restDistanceToDrive -= difference;
                }else{
                    robotDrive(maxDrive);
                    restDistanceToDrive -= maxDrive;
                }
                steps--;
            }while(steps >= 0);
        return restDistanceToDrive;
    }



    //Robot drives a given distance
    public void robotDrive(int distance_cm) {
        comReadWrite(
                new byte[] { 'k', adjustedMove(distance_cm), '\r', '\n' }
        );
        WaitHelp.waitWhileMoving(distance_cm);
        robotOd.setCoord(distance_cm);
    }

    private byte adjustedMove(int v){
        byte r;
        /*if(false){
            r=(byte)(adjustMovement*v);
            return r;
        }*/
        if(v <25){
            r=(byte)((adjustMovement*v)+1);
        }else if(v<45){
            r=(byte)((adjustMovement*v));
        }else if(v<65){
            r=(byte)((adjustMovement*v)-2);
        }else{
            r=(byte)((adjustMovement*v)-3);
        }

        return r;
    }


    //Robot turns a given degree
    private void robotPrivateTurn(int degree) {
        byte turn = adjustTurn(degree);
        comReadWrite(
                new byte[] { 'l',turn, '\r', '\n' }
        );
        WaitHelp.waitWhileMoving(degree);
        if(degree!=288){
            robotOd.updateAngle(degree);
        }
    }

    private byte adjustTurn(int degree){
        int si = (int)Math.signum(degree);
        degree = Math.abs(degree);
        if (degree <35) {
            degree *= 1.2*1.15;
        } else if (degree <= 45) {
            degree *= 1.205;
        } else if (degree <= 55) {
            degree *= 1.150*1.11;
        } else if (degree <= 65) {
            degree *= 1.44*0.83;
        } else if (degree <= 75) {
            degree *= 1.137;
        } else if (degree <= 85) {
            degree *= 1.135;
        } else {
            degree *= 1.135;
        }
        return (byte) (si*degree);
    }


    public void robotTurn(int deg){
        int degree=deg%360;
        int si = (int) Math.signum(deg);
        degree = si * degree;
        while(degree >90){
            degree=degree-90;
            robotPrivateTurn(si*90);
        }
        if(degree <25){
            robotPrivateTurn(si*(90));
            robotPrivateTurn(si*(-(90-degree)));
            return;
        }
        robotPrivateTurn(si*degree);

    }
    /**
     * Robot drives a polygon with n-vertex and k-edges of size the same size.
     * @param vertex defines the number of vertex
     * @param length defines the length of the edges
     * to do
     * check if 360 modulo vertex not 0. Don't know the behavior
     */
    public void orderedPolygon(int vertex, int length){
        int degree = (360 / vertex);
        int distance = length;
        try {
            for(int i = 0 ; i < vertex; i++){
                driveFreeSpace(distance);
                robotTurn(degree);
            }
        }catch (NoSpaceToMoveException e1) {
            Log.d("Abgebrochen, weil: ",e1.getMessage());
        }
        //Log.d("NAtive Odometrie: ", ""+ Odometry.getNativeOdometry());
        //Log.d("Unsere Odometrie: ", Odometry.getX()+ " " + odometry.Odometry.getY());
    }







    /**
     * 	this method will calculate the distance of the to move and the read values
     * @param length
     * @return true if the robot can move
     */
    private boolean freeSpaceToMove(Sensor measuredValues, int length){
        if(measuredValues.getMid() - length > 15 )
            return true;
        return false;
    }


    public void moveBlind(Point goal){
        RobotOdometry odometry = RobotOdometry.getInstance();
        Point currentPosition = odometry.getPoint();
        robotTurn(currentPosition.degreeToPoint(goal) - odometry.getAngle());
        robotMoveBlindForward(currentPosition.distance(goal));


    }
}
