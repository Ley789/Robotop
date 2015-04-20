package com.example.alexander.robotop.movement;

        import com.example.alexander.robotop.communication.Data;

        import java.util.ArrayList;

public class Sensor {
    public static double adjustSensor = 0.8;
    //Static variable to index the Sensors of the robot
    private static int left_index = 2, mid_index =6, right_index =3;
    private int mid;
    private int left;
    private int right;

    public Sensor(ArrayList<Integer> arr){
        mid = arr.get(mid_index);
        left = arr.get(left_index);
        right= arr.get(right_index);
    }

    public int getMid() {
        return (int) (mid * adjustSensor);
    }

    public int getLeft() {
        return (int) (left * adjustSensor);
    }

    public int getRight() {
        return (int) (right * adjustSensor);
    }


    /**
     * To check the sensors we call the adjust method from given parameter
     * @param currentValue the distance measured from the sensors
     * @param actuallDistance actual driven distance
     * @param adjustSensor coefficient to adjust the sensors measurement
     */
    public static void adjustSensor(Sensor currentValue, int actualDistance){
        adjustSensor =  actualDistance / (currentValue.getMid() * adjustSensor);
    }

}
