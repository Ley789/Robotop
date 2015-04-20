package com.example.alexander.robotop.movement;


import android.util.Log;

import com.example.alexander.robotop.communication.Data;
import com.example.alexander.robotop.modell.NoSpaceToMoveException;
import com.example.alexander.robotop.modell.OutOfRangeException;
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
    private double adjustTurn=1;
    private RobotOdometry robotOd;
    private static RobotMovement instance;

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


	public void driveFreeSpace(int distance_cm) throws NoSpaceToMoveException{
		Sensor data = Data.getSensorData();
		
		Log.d("FreeSpace",""+data.getMid() + "Expected: "+ distance_cm);
		if(!freeSpaceToMove(data,distance_cm)){
			throw new NoSpaceToMoveException("no free space");
		}
		robotDrive(distance_cm);
	}


	//approaches obstacle, but leaves 10cm security-distance. If range out of sensors, exception.
	//Returns moved distance.
	public double approach(int distance_cm) throws OutOfRangeException{
		if (distance_cm >80){
			throw new OutOfRangeException("Not in sensor-range");
		}
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
	public double robotMoveForward(int distance_cm) throws OutOfRangeException {
		
		int steps = distance_cm / 80;
		int difference = distance_cm - (steps * 80);
		try {
			do{
				if(steps == 0){
					driveFreeSpace(difference);
				}else{
					driveFreeSpace(80);
				}
				distance_cm -= difference;
			steps--;
			}while(steps > 0);
		} catch (NoSpaceToMoveException e) {
			distance_cm -= approach(difference);
		}
		return distance_cm;
	}




	//Robot drives a given distance
	public void robotDrive(int distance_cm) {
		comReadWrite(
				new byte[] { 'k', (byte)(distance_cm * adjustMovement), '\r', '\n' }
				);
		try {
			Thread.sleep(calcSleepTime(distance_cm));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		robotOd.setCoord(distance_cm);
	}
	//Robot turns a given degree
	public void robotTurn(int degree) {
		comReadWrite(
				new byte[] { 'l',(byte) (degree * adjustTurn), '\r', '\n' }
				);
				try {
					Thread.sleep(calcSleepTime( degree + 1000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		if(degree!=288){
			robotOd.setAngle(degree);
		}
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
	public boolean freeSpaceToMove(Sensor measuredValues, int length){
		if(measuredValues.getMid() - length > 15 )
			return true;
		return false;
	}


	private int calcSleepTime(int length)	{
		return Math.abs(length * 20);
	}
}


