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
    //returns abs value
    public int distance(Point goal){
        return (int)(Math.sqrt(Math.pow(goal.getX()- this.getX() ,2) + Math.pow(goal.getY() - this.getY(), 2)));
    }
}
