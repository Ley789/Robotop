package com.example.alexander.robotop.datastruct;

/**
 * Created by Alexander on 20/04/2015.
 */
public class Point {

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
            return (this.distance(p) < 20);
        }
        return false;
    }
    public int degreeToPoint(Point goal){
        int triLength = this.coordinateDifferenzX(goal);
        int triHight = this.coordinateDifferenzY(goal);
        if(triLength == 0) {
            return 90;
        }
        if(triHight == 0){
            return 0;
        }
        return (int)Math.toDegrees(Math.atan(triHight/triLength));
    }

    //returns abs value
    public int distance(Point goal){
        return (int)(Math.sqrt(Math.pow(this.coordinateDifferenzX(goal) ,2) + Math.pow(this.coordinateDifferenzY(goal), 2)));
    }
}
