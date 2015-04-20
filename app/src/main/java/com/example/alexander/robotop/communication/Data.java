package com.example.alexander.robotop.communication;


import com.example.alexander.robotop.movement.Sensor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import static com.example.alexander.robotop.communication.Connection.comReadWrite;


public class Data {
    /**
     * 	This method will parse the sensor output to integer
     * @param data
     * @return
     */
    private static ArrayList<Integer> parseSensor(String data){
        return parseString(data, "sensor");
    }

    /**
     * 	This method will parse the odometry info to integer
     * @param data
     * @return
     */
    private static ArrayList<Integer> parseOdometry(String data){
        return parseString(data, "odometry");
    }

    private static ArrayList<Integer> parseString(String data, String key){
        Scanner scanner = new Scanner(data);
        ArrayList<Integer> sensorData = new ArrayList<Integer>();
        String nextValue;
        //Drop first string
        while(scanner.hasNext()){
            nextValue = scanner.next();
            if(nextValue.contains(key)) break;
        }
        while(scanner.hasNext()){
            nextValue = scanner.next();
            //Drop the 0x Prefix
            nextValue = nextValue.substring(2);
            sensorData.add(Integer.parseInt(nextValue, 16));
        }
        return sensorData;
    }

    public static Sensor getSensorData(){
        ArrayList<Integer> arr = parseSensor(comReadWrite(new byte[] { 'q', '\r', '\n' }));
        for (Integer elem : arr){
            elem = (int) (elem * Sensor.adjustSensor);
        }
        return new Sensor(arr);
    }

    public static ArrayList<Integer> getOdometryData(){
        ArrayList<Integer> arr = parseOdometry(comReadWrite(new byte[] { 'h', '\r', '\n' }));
        ArrayList<Integer> result = new ArrayList<Integer>();
        for(int i = 0; i< arr.size(); i = i + 2){
            result.add(toInt(arr.get(i+1).byteValue(),arr.get(i).byteValue()));
        }
        return result;
    }

    private static int toInt(byte hb, byte lb){
        ByteBuffer bb = ByteBuffer.wrap(new byte[] {hb,lb});
        return bb.getShort();
    }
}
