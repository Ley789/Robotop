package com.example.alexander.robotop.datastruct;

/**
 * Created by Alexander on 20/04/2015.
 */
public class Point {
    private static int Sensibility = 20;

    private int x;
    private int y;

    public Point(){
        x=0;
        y=0;
    }
    public Point(int setX, int setY){
        x=setX;
        y=setY;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int coordinateDifferenzX(Point goal){
        return (goal.getX() - this.getX());
    }

    public int coordinateDifferenzY(Point goal){
        return (goal.getY() - this.getY());
    }

    @Override
    public boolean equals(Object obj){
        if( obj instanceof  Point) {
            Point p = (Point) obj;
            return (this.distance(p) < Sensibility);
        }
        return false;
    }
    public int degreeToPoint(Point goal){
        double triLength = (double)this.coordinateDifferenzX(goal);
        double triHight =  (double)this.coordinateDifferenzY(goal);
        if(triLength == 0) {
            return 90;
        }
        if(triHight == 0){
            return 0;
        }
        //first quadrant
        if(triLength > 0 && triHight > 0){
            return (int)Math.toDegrees(Math.atan(triHight/triLength));
        }//second quadrant
        else if(triLength < 0 && triHight > 0 ){
            return (int)(180 - Math.toDegrees(Math.atan(triHight/ -triLength)));
        }//third quadrant
        else if(triLength < 0 && triHight < 0){
            return (int)(Math.toDegrees(Math.atan(triHight/ triLength)) +180);
        }else {
            return (int)(-Math.toDegrees(Math.atan(-triHight /triLength)));
        }
    }

    //returns abs value
    public int distance(Point goal){
        return (int)(Math.sqrt(Math.pow(this.coordinateDifferenzX(goal) ,2) + Math.pow(this.coordinateDifferenzY(goal), 2)));
    }
    @Override
    public String toString(){
        return ("Point(" + x + "," +y +")");
    }
}
