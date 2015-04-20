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

    //returns abs value
    public static int distance(Point start, Point goal){
        return (int)(Math.sqrt(Math.pow(goal.getX()- start.getX() ,2) + Math.pow(goal.getY() - start.getY(),2)));
    }
}
