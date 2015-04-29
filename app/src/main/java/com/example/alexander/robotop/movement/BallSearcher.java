package com.example.alexander.robotop.movement;

/**
 * Created by Alexander on 29/04/2015.
 */
public class BallSearcher {
    private RobotMovement movement = RobotMovement.getInstance();

    public void run() {
        movement.robotMoveForward(20);
        movement.robotTurn(45);
    }
}

