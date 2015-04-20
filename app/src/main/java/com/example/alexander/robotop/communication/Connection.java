package com.example.alexander.robotop.communication;

/**
 * Created by Alexander on 20/04/2015.
 */

import android.util.Log;

import jp.ksksue.driver.serial.FTDriver;

public class Connection {
    public static FTDriver com;

    /**
     * transfers given bytes via the serial connection.
     *
     * @param data
     */
    public static void comWrite(byte[] data) {
        if (com.isConnected()) {
            com.write(data);
        } else {
            Log.d("Connection", "not connected\n");
        }
    }
    /**
     * reads from the serial buffer. due to buffering, the read command is
     * issued 3 times at minimum and continuously as long as there are bytes to
     * read from the buffer. Note that this function does not block, it might
     * return an empty string if no bytes have been read at all.
     *
     * @return buffer content as string
     */
    public static String comRead() {
        String s = "";
        int i = 0;
        int n = 0;
        while (i < 3 || n > 0) {
            byte[] buffer = new byte[256];
            n = com.read(buffer);
            s += new String(buffer, 0, n);
            i++;
        }
        return s;
    }
    /**
     * write data to serial interface, wait 100 ms and read answer.
     *
     * @param data
     * to write
     * @return answer from serial interface
     */
    public static String comReadWrite(byte[] data) {
        com.write(data);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        return comRead();
    }
}



