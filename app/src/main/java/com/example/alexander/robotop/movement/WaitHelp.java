package com.example.alexander.robotop.movement;

import android.util.Log;

import com.example.alexander.robotop.communication.Data;

import java.util.ArrayList;
import java.util.Scanner;

import static com.example.alexander.robotop.communication.Connection.comReadWrite;

/**
 * Created by uli on 21.04.15.
 */
public class WaitHelp {

    public static void waitWhileMoving(int value){
        int a1=0;
        int a2=1;
        int b1=0;
        int b2=1;
        int c1=0;
        int c2=1;
        ArrayList<Integer> mov;
        while(a1!=a2 || b1!=b2 || c1!=c2 ) {
            a2 = a1;
            b2 = b1;
            c2 = c1;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //try-catch only for developement;
            try {
                mov = Data.getOdometryData();
                a1 = mov.get(0) + mov.get(1);
                b1 = mov.get(2) + mov.get(3);
                c1 = mov.get(4) + mov.get(5);
            }catch (Exception e){
                Log.d(e.toString(), "waiting");
                waitWhileMoving(value,0);

            }

            }
    }

    public static void waitWhileMoving(int value, int v){
      long sl = 20*Math.abs(value);

        try {
            Thread.sleep(sl);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void waitWhileMoving(int value, char c){
        long sl = 20 * Math.abs(value) *2;

        try {
            Thread.sleep(sl);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
