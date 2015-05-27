package com.example.alexander.robotop.modell;


import com.example.alexander.robotop.datastruct.ColorBound;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Created by Alexander on 27/05/2015.
 */
public class Detector {
    public static Mat detectBlob(Mat mHsv, ColorBound c){
        Mat threshold = new Mat();

        double lowerBound = c.getmLowerBound().val[0];
        if(lowerBound < 12 || lowerBound > 340){
           return handleRedBlobs(mHsv);
        }
        Core.inRange(mHsv, c.getmLowerBound(), c.getmUpperBound(), threshold);
        return threshold;
    }
    private static Mat handleRedBlobs(Mat mHsv){
        Mat threshold = new Mat();
        Mat threshold2 = new Mat();
        Core.inRange(mHsv, new Scalar(0, 100, 100), new Scalar(10, 255, 255), threshold);
        Core.inRange(mHsv, new Scalar(340,100,100), new Scalar(360,255,255), threshold2);
        Core.bitwise_or(threshold,threshold2,threshold);
        return threshold;
    }
}
