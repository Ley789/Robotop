package com.example.alexander.robotop.movement;

/**
 * Created by Alexander on 29/04/2015.
 */
public class BallSearcher implements Runnable {
    private boolean run = true;
    private RobotMovement movement = RobotMovement.getInstance();
    @Override
    public void run(){
        do{
            movement.robotMoveForward(20);
            movement.robotTurn(45);
        }while(run);
    }

    public void stopExecute(){
        run = false;
    }
}
