package com.example.alexander.robotop.visualOrientation;

import com.example.alexander.robotop.datastruct.Point;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * Created by uli on 27.05.15.
 */

public class Selflocalization {
    /**
     * @param robot                    Point of the robot in worldCoordinates
     * @param leftBeaconWorlCoordinate Point of the beacon in worldCoordinates.
     * @param leftBeaconRelative      left beacon relative to the robot
     * @return angle of the robot from the x-axis in direction left
     */

    public static double getOrientation(Point robot, Point leftBeaconWorlCoordinate, Point leftBeaconRelative) {
        double result;
        /*The point pseudovector has the same x an y as the vector from robot to beacon
          Because it is only something like an translation to the center, herefrom the orientation can be calculated*/
        Point pseudoVector = new Point(leftBeaconWorlCoordinate.getX() - robot.getX(), leftBeaconWorlCoordinate.getY() - robot.getY());
        /*Angle between vector robot-beacon relative to x axis*/
        double angleRobot = getAngle(pseudoVector);
        /*Angle between direction to beacon and the direction the robot looks*/
        double innerAngle;
        if(leftBeaconRelative.getY()<0) {
            innerAngle = -(360-getAngle(leftBeaconRelative));
        }else{
            innerAngle=getAngle(leftBeaconRelative);
        }
        result = angleRobot - innerAngle;
        if (result < 0) {
            result = 360 + result;
        }
        result = result%360;
        return result;
    }



    public static float getAngle(Point target) {
        float angle = (float) Math.toDegrees(Math.atan2(target.getY(), target.getX()));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    /**
     *Calculates the position of the robot when it sees two beacons which position is known
     *
     * @param aIn left beacon  in egocentric coordinates, relative to robot
     * @param bIn right beacon egocentric coordintaes, relative to robot
     * @param beaconL left beacon (a) in world coordinates,
     * @param beaconR right beacon (b) in word coordinates
     * @return position of the robot
     */
    public static Point selfLocalisation(Point aIn, Point bIn, Point beaconL, Point beaconR){
        double[][] ret = {{-999,-999},{-999,-999}};
        Point a= new Point(-aIn.getY(), aIn.getX()); //TODO FURTHER TEST
        Point b= new Point(-bIn.getY(), bIn.getX());//TODO FURTHER TEST
        Point beacon1 = beaconL;
        Point beacon2 =beaconR;
        Point retPoint;
        int sig = 1;
        boolean swap = false;
        if(beaconL.getX() ==beaconR.getX() && beaconL.getY() < beaconR.getY()){
            beacon1 = new Point(beaconL.getY(),beaconL.getX());
            beacon2 = new Point(beaconR.getY(),beaconR.getX());
            swap=true;

        }else if(beaconL.getX() ==beaconR.getX() && beaconL.getY() > beaconR.getY()){
            beacon1 = new Point(beaconL.getY(),beaconL.getX());
            beacon2 = new Point(beaconR.getY(),beaconR.getX());
            sig =-1;
            swap=true;
        }else if(beaconL.getX() > beaconR.getX() && beaconL.getY() == beaconR.getY()) {
            sig = -1;
        }
        double ar = sqrt((pow(a.getX(),2))+(pow(a.getY(),2)));
        double br = sqrt((pow(b.getX(),2))+(pow(b.getY(), 2)));
        double dist =sqrt((pow((a.getX()-b.getX()), 2))+(pow((a.getY()-b.getY()), 2)));
        double x = (pow(dist,2)-pow(br,2)+pow(ar,2))/(2*dist);
        double y = sqrt(abs(pow(ar,2)-pow(x,2)));

        if(swap==true){

            ret[0][0]= (beaconL.getX())+sig*y;
            ret[0][1]= (beaconL.getY()+sig*x);
            retPoint =new Point((int) round(ret[0][0]),(int) round(ret[0][1]));
            return retPoint;

        }
        ret[0][0]= (beaconL.getX())+sig*x;
        ret[0][1]= (beaconL.getY()-sig*y);
        retPoint = new Point((int) round(ret[0][0]),(int) round(ret[0][1]));
        return  retPoint;
    }


    /**
     **Calculates the position of the robot when it sees two beacons which position is known
     * and the robot hast turned to the left (with angle angle) when searching the left beacon
     *
     * @param a left beacon  in egocentric coordinates, relative to robot
     * @param b right beacon egocentric coordintaes, relative to robot
     * @param beaconL left beacon (a) in world coordinates,
     * @param beaconR right beacon (b) in word coordinates
     * @param angle if the robot turns (left), the left beacon will be calulated new (uses a rotation with angle)
     * @return position of the robot
     */

    public static Point selfLocalisation(Point a, Point b, Point beaconL, Point beaconR, int angle){
        Point aNew = rotate(a,angle);
        return selfLocalisation(aNew, b, beaconL, beaconR);
    }

    public static Point rotate(Point p, int angle){
        int x = p.getX();
        int y = p.getY();
        double radian = Math.toRadians(angle);
        double [] vectorData = {x,y,1};
        double cos = Math.cos(radian);
        double sin = Math.sin(radian);
        double [][] rotData = {{cos,-sin,0},{sin,cos,0},{0,0,1}};
        RealMatrix rotation = new Array2DRowRealMatrix(rotData);
        RealMatrix vector = new Array2DRowRealMatrix(vectorData);
        RealMatrix mult = rotation.multiply(vector);
        int newX = (int) round(mult.getEntry(0, 0));
        int newY = (int) round(mult.getEntry(1, 0));
        Point ret = new Point(newX, newY);
        return ret;
    }

}

