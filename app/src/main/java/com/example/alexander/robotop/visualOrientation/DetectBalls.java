package com.example.alexander.robotop.visualOrientation;

/**
 * Created by Alexander on 28/04/2015.
 */
import com.example.alexander.robotop.datastruct.ColorBound;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Callable;

public class DetectBalls implements Callable<Mat>{
    private Mat mRgba;
    private ColorBound color;
    public DetectBalls(Mat mat, ColorBound c){
        mRgba = mat;
        color =c;
    }
    /**
     * Returns a mat of circles of red balls
     */
    @Override
    public Mat call() throws Exception {
        Mat mHSV = new Mat();
        Mat res = new Mat();
        Mat circles = new Mat();
        //Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(8,8));
        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHSV, color.getmLowerBound(), color.getmUpperBound(), res);

        Imgproc.GaussianBlur(res, res, new Size(9,9),0,0);
        //Imgproc.erode(res, res, erodeElement);
        //Imgproc.erode(res, res, erodeElement);
        Imgproc.HoughCircles(res, circles, Imgproc.CV_HOUGH_GRADIENT, 2, res.height()/4,500, 50, 0, 0);
        if(circles.rows() > 0) {
            return circles;
        }
        return null;
    }

}

