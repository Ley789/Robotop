package com.example.alexander.robotop.robotData;

/**
 * Created by Alexander on 22/04/2015.
 */
public class World {
    private static World instance;
    private static int worldLength = 300;
    private static int worldHight = 300;
    private static int quadratsLength = 10;



    private World(){
    }

    public static World getInstance(){
        if(instance == null){
            instance = new World();
            return instance;
        }else{
            return instance;
        }
    }
}
